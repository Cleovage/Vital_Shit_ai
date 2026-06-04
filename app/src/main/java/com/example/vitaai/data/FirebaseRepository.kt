package com.example.vitaai.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    suspend fun saveHealthSnapshot(snapshot: HealthSnapshot) {
        val uid = userId ?: throw IllegalStateException("User must be logged in to save data")
        firestore.collection("users")
            .document(uid)
            .collection("snapshots")
            .add(snapshot)
            .await()
    }

    suspend fun getLatestSnapshot(): HealthSnapshot? {
        val uid = userId ?: return null
        return try {
            val snapshots = firestore.collection("users")
                .document(uid)
                .collection("snapshots")
            
            val query = snapshots.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
            
            val result = query.get().await()
            result.toObjects(HealthSnapshot::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
