package com.example.vitaai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted record of one meditation play event. Mirrors
 * `users/{uid}/meditation_log/{id}` in Firestore (see
 * scratch/firebase_profile_sync_research.md §2.3).
 */
@Entity(tableName = "meditation_log")
data class MeditationLogEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val title: String,
    val category: String,
    val plannedDurationSeconds: Long,
    val startedAtMillis: Long,
    val durationSeconds: Long,
    val completed: Boolean,
    val deviceId: String,
    val syncedToCloud: Boolean = false
)

/**
 * Persisted record of one hydration add/remove event. Mirrors
 * `users/{uid}/hydration_log/{id}` in Firestore.
 */
@Entity(tableName = "hydration_log")
data class HydrationLogEntity(
    @PrimaryKey val id: String,
    val timestampMillis: Long,
    val ml: Int,
    val hour: Int,
    val source: String,
    val deleted: Boolean = false,
    val syncedToCloud: Boolean = false
)
