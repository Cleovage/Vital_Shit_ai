package com.example.vitaai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val meal: String,
    val servingLabel: String,
    val servingMultiplier: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val sugarGrams: Double,
    val sodiumMg: Double,
    val caffeineMg: Double,
    val timestampMillis: Long
)

@Entity(tableName = "drink_entries")
data class DrinkEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val drinkType: String,
    val volumeMl: Double,
    val hydrationMl: Double,
    val caffeineMg: Double,
    val sugarGrams: Double,
    val sodiumMg: Double,
    val benefit: String,
    val timestampMillis: Long
)

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val exerciseType: Int,
    val trackingMode: String,
    val gpsEnabled: Boolean,
    val defaultRestSeconds: Int,
    val description: String,
    val primaryMetric: String
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: String,
    val title: String,
    val category: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationSeconds: Long,
    val totalSets: Int,
    val totalReps: Int,
    val calories: Double,
    val avgHeartRate: Double,
    val distanceMeters: Double,
    val notes: String,
    val completed: Boolean
)

@Entity(tableName = "workout_exercises")
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val templateId: String,
    val name: String,
    val orderIndex: Int
)

@Entity(tableName = "exercise_sets")
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val durationSeconds: Long,
    val timestampMillis: Long
)

@Entity(tableName = "route_points")
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
)

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Long,
    val sleepQualityScore: Int,
    val source: String,
    val notes: String? = null
)

@Entity(tableName = "ambient_light_logs")
data class AmbientLightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val luxValue: Float
)

@Entity(tableName = "screen_state_events")
data class ScreenStateEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val eventType: String // "SCREEN_ON", "SCREEN_OFF"
)

data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<ExerciseSetEntity>
)
