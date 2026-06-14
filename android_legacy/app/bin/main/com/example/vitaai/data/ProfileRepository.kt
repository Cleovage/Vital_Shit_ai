package com.example.vitaai.data

import android.util.Log
import com.example.vitaai.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Owns the per-user profile document (`users/{uid}`) and keeps a
 * reactive [StateFlow] that the UI can collect.
 *
 * Writes use [SetOptions.merge] so we never clobber fields we don't know
 * about (in particular, the original `saveProfileGoals` bug where
 * `displayName` would be erased on every goal write).
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val auth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?
) {
    private val tag = "ProfileRepository"

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val uid: String?
        get() = auth?.currentUser?.uid

    /**
     * Pulls `users/{uid}` from Firestore and updates the [profile] flow.
     * No-op if the user is not signed in or Firestore is unavailable.
     */
    suspend fun loadFromCloud(): UserProfile? {
        val userId = uid ?: return null
        val db = firestore ?: return null
        return try {
            val snap = db.collection("users").document(userId).get().await()
            val map = snap.data
            if (map.isNullOrEmpty()) {
                _profile.value = null
                null
            } else {
                val p = UserProfile.fromMap(map)
                _profile.value = p
                p
            }
        } catch (t: Throwable) {
            Log.e(tag, "loadFromCloud failed", t)
            _profile.value
        }
    }

    /**
     * Persists the given [profile] to `users/{uid}` with `SetOptions.merge()`
     * so unrelated fields are preserved. Also stamps `updatedAt` server-side.
     */
    suspend fun saveToCloud(profile: UserProfile) {
        val userId = uid ?: return
        val db = firestore ?: return
        val data = mapOf(
            "displayName" to profile.displayName,
            "email" to profile.email,
            "age" to profile.age,
            "gender" to profile.gender,
            "activityLevel" to profile.activityLevel,
            "stepGoal" to profile.stepGoal,
            "hydrationGoalLiters" to profile.hydrationGoalLiters,
            "exerciseMinutesGoal" to profile.exerciseMinutesGoal,
            "caloriesBurnGoal" to profile.caloriesBurnGoal,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId)
            .set(data, SetOptions.merge())
            .await()
        // Update local cache optimistically
        _profile.value = profile
    }

    /**
     * Updates only the display name. Calls the Firebase Auth
     * `UserProfileChangeRequest` builder so `FirebaseUser.displayName` is
     * non-null afterwards, and writes to Firestore with merge semantics.
     */
    suspend fun updateDisplayName(newName: String) {
        val trimmed = newName.trim().take(40)
        val current = auth?.currentUser
        if (current != null) {
            try {
                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmed)
                    .build()
                current.updateProfile(req).await()
            } catch (t: Throwable) {
                Log.w(tag, "Failed to update FirebaseAuth displayName", t)
            }
        }
        val userId = uid ?: return
        val db = firestore ?: return
        try {
            db.collection("users").document(userId)
                .set(
                    mapOf(
                        "displayName" to trimmed,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
        } catch (t: Throwable) {
            Log.e(tag, "updateDisplayName firestore write failed", t)
        }
        // Refresh local flow
        loadFromCloud()
    }
}
