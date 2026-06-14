package com.example.vitaai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    // ---- meditation ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeditation(entry: MeditationLogEntity)

    @Query("SELECT * FROM meditation_log ORDER BY startedAtMillis DESC")
    fun observeMeditationLog(): Flow<List<MeditationLogEntity>>

    @Query("SELECT * FROM meditation_log WHERE syncedToCloud = 0")
    suspend fun unsyncedMeditation(): List<MeditationLogEntity>

    @Query("UPDATE meditation_log SET syncedToCloud = 1 WHERE id = :id")
    suspend fun markMeditationSynced(id: String)

    // ---- hydration ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydration(entry: HydrationLogEntity)

    @Query("SELECT * FROM hydration_log WHERE deleted = 0 ORDER BY timestampMillis DESC")
    fun observeHydrationLog(): Flow<List<HydrationLogEntity>>

    @Query("SELECT * FROM hydration_log WHERE deleted = 0 ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun recentHydration(limit: Int): List<HydrationLogEntity>

    @Query("SELECT * FROM hydration_log WHERE syncedToCloud = 0")
    suspend fun unsyncedHydration(): List<HydrationLogEntity>

    @Query("UPDATE hydration_log SET syncedToCloud = 1 WHERE id = :id")
    suspend fun markHydrationSynced(id: String)

    @Query("UPDATE hydration_log SET deleted = 1, syncedToCloud = 0 WHERE id = :id")
    suspend fun markHydrationDeleted(id: String)
}
