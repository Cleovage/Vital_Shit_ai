package com.example.vitaai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VitaDao {
    @Query("SELECT * FROM food_entries WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    fun observeFoodEntries(startMillis: Long, endMillis: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM drink_entries WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    fun observeDrinkEntries(startMillis: Long, endMillis: Long): Flow<List<DrinkEntryEntity>>

    @Query("SELECT * FROM workout_templates ORDER BY category, name")
    fun observeWorkoutTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMillis DESC LIMIT :limit")
    fun observeRecentWorkoutSessions(limit: Int): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE startTimeMillis BETWEEN :startMillis AND :endMillis ORDER BY startTimeMillis DESC")
    fun observeWorkoutSessions(startMillis: Long, endMillis: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY setNumber")
    fun observeExerciseSets(sessionId: Long): Flow<List<ExerciseSetEntity>>

    @Query("SELECT * FROM route_points WHERE sessionId = :sessionId ORDER BY timestampMillis")
    fun observeRoutePoints(sessionId: Long): Flow<List<RoutePointEntity>>

    @Query("SELECT COUNT(*) FROM workout_templates")
    suspend fun templateCount(): Int

    @Query("SELECT * FROM workout_templates WHERE id = :id LIMIT 1")
    suspend fun getWorkoutTemplate(id: String): WorkoutTemplateEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getWorkoutSession(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM food_entries WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    suspend fun getFoodEntries(startMillis: Long, endMillis: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM drink_entries WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    suspend fun getDrinkEntries(startMillis: Long, endMillis: Long): List<DrinkEntryEntity>

    @Query("SELECT * FROM workout_sessions WHERE startTimeMillis BETWEEN :startMillis AND :endMillis ORDER BY startTimeMillis DESC")
    suspend fun getWorkoutSessions(startMillis: Long, endMillis: Long): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutTemplates(templates: List<WorkoutTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(entry: FoodEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinkEntry(entry: DrinkEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSet(set: ExerciseSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoints(points: List<RoutePointEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmbientLightLog(log: AmbientLightLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenStateEvent(event: ScreenStateEventEntity): Long

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeMillis DESC LIMIT :limit")
    fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE startTimeMillis BETWEEN :startMillis AND :endMillis ORDER BY startTimeMillis DESC")
    fun observeSleepSessions(startMillis: Long, endMillis: Long): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE startTimeMillis BETWEEN :startMillis AND :endMillis ORDER BY startTimeMillis DESC")
    suspend fun getSleepSessions(startMillis: Long, endMillis: Long): List<SleepSessionEntity>

    @Query("SELECT * FROM ambient_light_logs WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis ASC")
    suspend fun getAmbientLightLogs(startMillis: Long, endMillis: Long): List<AmbientLightLogEntity>

    @Query("SELECT * FROM screen_state_events WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis ASC")
    suspend fun getScreenStateEvents(startMillis: Long, endMillis: Long): List<ScreenStateEventEntity>
}
