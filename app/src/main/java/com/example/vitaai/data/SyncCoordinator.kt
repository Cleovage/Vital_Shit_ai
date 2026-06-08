package com.example.vitaai.data

import android.util.Log
import com.example.vitaai.data.local.HydrationLogEntity
import com.example.vitaai.data.local.LogDao
import com.example.vitaai.data.local.MeditationLogEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Per-user sync state — exposed as a [StateFlow] for the UI to render a
 * small badge / banner ("Syncing…" / "Synced 2 min ago" / "Offline — will retry").
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val pushed: Int, val pulled: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Orchestrates push-on-write + pull-on-launch for the per-user log
 * sub-collections. See scratch/firebase_profile_sync_research.md §3 for
 * the sync strategy.
 *
 * All Firestore calls short-circuit if there is no signed-in user or
 * Firestore is unavailable. Errors on individual rows are logged and
 * skipped so a single bad row does not abort the whole sync.
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val logDao: LogDao,
    private val auth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?
) {
    private val tag = "SyncCoordinator"

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val uid: String?
        get() = auth?.currentUser?.uid

    /**
     * Pushes every row that has `syncedToCloud = 0` in either log table.
     * Best-effort: per-row errors are logged, the rest continue.
     */
    suspend fun pushPendingLogs(): Int {
        val userId = uid ?: return 0
        val db = firestore ?: return 0
        _state.value = SyncState.Syncing

        var pushed = 0

        val meditation = logDao.unsyncedMeditation()
        for (row in meditation) {
            try {
                val data = mapOf(
                    "sessionId" to row.sessionId,
                    "title" to row.title,
                    "category" to row.category,
                    "plannedDurationSeconds" to row.plannedDurationSeconds,
                    "startedAtMillis" to row.startedAtMillis,
                    "durationSeconds" to row.durationSeconds,
                    "completed" to row.completed,
                    "deviceId" to row.deviceId,
                    "syncedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users").document(userId)
                    .collection("meditation_log").document(row.id)
                    .set(data, SetOptions.merge())
                    .await()
                logDao.markMeditationSynced(row.id)
                pushed++
            } catch (t: Throwable) {
                Log.w(tag, "push meditation ${row.id} failed", t)
            }
        }

        val hydration = logDao.unsyncedHydration()
        for (row in hydration) {
            try {
                val data = mapOf(
                    "timestampMillis" to row.timestampMillis,
                    "ml" to row.ml,
                    "hour" to row.hour,
                    "source" to row.source,
                    "deleted" to row.deleted,
                    "syncedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users").document(userId)
                    .collection("hydration_log").document(row.id)
                    .set(data, SetOptions.merge())
                    .await()
                logDao.markHydrationSynced(row.id)
                pushed++
            } catch (t: Throwable) {
                Log.w(tag, "push hydration ${row.id} failed", t)
            }
        }

        _state.value = SyncState.Success(pushed = pushed, pulled = 0)
        return pushed
    }

    /**
     * Pulls rows from Firestore that don't exist locally (by id) so
     * the user can see logs created on a different device.
     */
    suspend fun pullCloudLogs(): Int {
        val userId = uid ?: return 0
        val db = firestore ?: return 0
        _state.value = SyncState.Syncing

        var pulled = 0
        try {
            val medSnap = db.collection("users").document(userId)
                .collection("meditation_log").get().await()
            for (doc in medSnap.documents) {
                val id = doc.id
                val row = MeditationLogEntity(
                    id = id,
                    sessionId = doc.getString("sessionId") ?: "",
                    title = doc.getString("title") ?: "",
                    category = doc.getString("category") ?: "",
                    plannedDurationSeconds = doc.getLong("plannedDurationSeconds") ?: 0L,
                    startedAtMillis = doc.getLong("startedAtMillis") ?: 0L,
                    durationSeconds = doc.getLong("durationSeconds") ?: 0L,
                    completed = doc.getBoolean("completed") ?: false,
                    deviceId = doc.getString("deviceId") ?: "",
                    syncedToCloud = true
                )
                logDao.insertMeditation(row)
                pulled++
            }
        } catch (t: Throwable) {
            Log.w(tag, "pull meditation failed", t)
        }

        try {
            val hydSnap = db.collection("users").document(userId)
                .collection("hydration_log").get().await()
            for (doc in hydSnap.documents) {
                val id = doc.id
                val row = HydrationLogEntity(
                    id = id,
                    timestampMillis = doc.getLong("timestampMillis") ?: 0L,
                    ml = (doc.getLong("ml") ?: 0L).toInt(),
                    hour = (doc.getLong("hour") ?: 0L).toInt(),
                    source = doc.getString("source") ?: "cloud",
                    deleted = doc.getBoolean("deleted") ?: false,
                    syncedToCloud = true
                )
                logDao.insertHydration(row)
                pulled++
            }
        } catch (t: Throwable) {
            Log.w(tag, "pull hydration failed", t)
        }

        _state.value = SyncState.Success(pushed = 0, pulled = pulled)
        return pulled
    }

    /**
     * Convenience: run push then pull. Returns the total number of
     * round-tripped rows. Surfaces errors into [state].
     */
    suspend fun fullSync(): Int {
        return try {
            val pushed = pushPendingLogs()
            val pulled = pullCloudLogs()
            _state.value = SyncState.Success(pushed = pushed, pulled = pulled)
            pushed + pulled
        } catch (t: Throwable) {
            Log.e(tag, "fullSync failed", t)
            _state.value = SyncState.Error(t.localizedMessage ?: "Sync failed")
            0
        }
    }
}
