package com.example.vitaai.data

import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.vitaai.data.local.ExerciseSetEntity
import com.example.vitaai.data.local.RoutePointEntity
import com.example.vitaai.data.local.VitaDao
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.data.local.WorkoutTemplateEntity
import com.example.vitaai.data.local.WorkoutSessionWithSets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

const val TRACKING_STRENGTH = "strength"
const val TRACKING_BODYWEIGHT = "bodyweight"
const val TRACKING_CARDIO = "cardio"
const val TRACKING_MOBILITY = "mobility"

data class WorkoutSetDraft(
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val durationSeconds: Long,
    val timestampMillis: Long = Instant.now().toEpochMilli()
)

data class RoutePointDraft(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
)

@Singleton
class WorkoutRepository @Inject constructor(
    private val dao: VitaDao,
    private val healthConnectManager: HealthConnectManager
) {
    fun observeTemplates(): Flow<List<WorkoutTemplateEntity>> = dao.observeWorkoutTemplates()

    fun observeExerciseSets(sessionId: Long): Flow<List<com.example.vitaai.data.local.ExerciseSetEntity>> = dao.observeExerciseSets(sessionId)

    fun observeRoutePoints(sessionId: Long): Flow<List<com.example.vitaai.data.local.RoutePointEntity>> = dao.observeRoutePoints(sessionId)

    fun observeRecentSessionsWithSets(limit: Int = 8): Flow<List<WorkoutSessionWithSets>> {
        return dao.observeRecentWorkoutSessionsWithSets(limit).combine(flow {
            while (true) {
                if (healthConnectManager.hasAllPermissions()) {
                    val now = Instant.now()
                    val thirtyDaysAgo = now.minus(java.time.Duration.ofDays(30))
                    
                    val sessions = healthConnectManager.readExerciseSessions(thirtyDaysAgo, now)
                        .sortedByDescending { it.startTime }
                        .take(limit)
                        .map { record ->
                            WorkoutSessionWithSets(
                                session = WorkoutSessionEntity(
                                    id = 0L, 
                                    templateId = "",
                                    title = record.title ?: "Workout",
                                    category = "HC",
                                    startTimeMillis = record.startTime.toEpochMilli(),
                                    endTimeMillis = record.endTime.toEpochMilli(),
                                    durationSeconds = java.time.Duration.between(record.startTime, record.endTime).seconds,
                                    totalSets = 0,
                                    totalReps = 0,
                                    calories = 0.0,
                                    avgHeartRate = 0.0,
                                    distanceMeters = 0.0,
                                    notes = record.notes ?: "",
                                    completed = true
                                ),
                                sets = emptyList()
                            )
                        }
                    emit(sessions)
                } else {
                    emit(emptyList<WorkoutSessionWithSets>())
                }
                delay(20000)
            }
        }) { localSessions, hcSessions ->
            val merged = localSessions.toMutableList()
            for (hc in hcSessions) {
                val alreadyExists = localSessions.any { local ->
                    Math.abs(local.session.startTimeMillis - hc.session.startTimeMillis) < 60000
                }
                if (!alreadyExists) {
                    merged.add(hc)
                }
            }
            merged.sortedByDescending { it.session.startTimeMillis }.take(limit)
        }
    }

    fun observeRecentSessions(limit: Int = 8): Flow<List<WorkoutSessionEntity>> {
        return dao.observeRecentWorkoutSessions(limit).combine(flow {
            while (true) {
                if (healthConnectManager.hasAllPermissions()) {
                    val now = Instant.now()
                    val thirtyDaysAgo = now.minus(java.time.Duration.ofDays(30))
                    
                    val sessions = healthConnectManager.readExerciseSessions(thirtyDaysAgo, now)
                        .sortedByDescending { it.startTime }
                        .take(limit)
                        .map { record ->
                            WorkoutSessionEntity(
                                id = 0L, 
                                templateId = "",
                                title = record.title ?: "Workout",
                                category = "HC",
                                startTimeMillis = record.startTime.toEpochMilli(),
                                endTimeMillis = record.endTime.toEpochMilli(),
                                durationSeconds = java.time.Duration.between(record.startTime, record.endTime).seconds,
                                totalSets = 0,
                                totalReps = 0,
                                calories = 0.0,
                                avgHeartRate = 0.0,
                                distanceMeters = 0.0,
                                notes = record.notes ?: "",
                                completed = true
                            )
                        }
                    emit(sessions)
                } else {
                    emit(emptyList<WorkoutSessionEntity>())
                }
                delay(20000)
            }
        }) { localSessions, hcSessions ->
            val merged = localSessions.toMutableList()
            for (hc in hcSessions) {
                val alreadyExists = localSessions.any { local ->
                    Math.abs(local.startTimeMillis - hc.startTimeMillis) < 60000
                }
                if (!alreadyExists) {
                    merged.add(hc)
                }
            }
            merged.sortedByDescending { it.startTimeMillis }.take(limit)
        }
    }

    suspend fun getTemplate(id: String): WorkoutTemplateEntity? {
        seedDefaultTemplatesIfNeeded()
        return dao.getWorkoutTemplate(id)
    }

    suspend fun saveTemplate(template: WorkoutTemplateEntity) {
        dao.insertWorkoutTemplates(listOf(template))
    }

    suspend fun seedDefaultTemplatesIfNeeded() {
        val count = dao.templateCount()
        if (count == 0) {
            dao.insertWorkoutTemplates(defaultTemplates())
        } else {
            if (dao.getWorkoutTemplate("adaptive_strength") == null) {
                dao.insertWorkoutTemplates(listOf(
                    WorkoutTemplateEntity("adaptive_strength", "Adaptive Strength", "Strength", androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 90, "AI-adjusted strength protocol targeted for current fatigue profile.", "sets")
                ))
            }
            if (dao.getWorkoutTemplate("mobility_recovery") == null) {
                dao.insertWorkoutTemplates(listOf(
                    WorkoutTemplateEntity("mobility_recovery", "Recovery Mobility", "Mobility", androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_YOGA, TRACKING_MOBILITY, false, 0, "Guided mobility routine emphasizing breathing, hips, and shoulders.", "time")
                ))
            }
        }
    }

    suspend fun saveWorkoutSession(
        template: WorkoutTemplateEntity,
        startTime: Instant,
        endTime: Instant,
        totalReps: Int,
        sets: List<WorkoutSetDraft>,
        route: List<RoutePointDraft>,
        avgHeartRate: Double,
        calories: Double,
        notes: String,
        manualDistanceMeters: Double? = null
    ): Long {
        val durationSeconds = java.time.Duration.between(startTime, endTime).seconds.coerceAtLeast(1)
        val distanceMeters = manualDistanceMeters ?: routeDistanceMeters(route)
        val sessionId = dao.insertWorkoutSession(
            WorkoutSessionEntity(
                templateId = template.id,
                title = template.name,
                category = template.category,
                startTimeMillis = startTime.toEpochMilli(),
                endTimeMillis = endTime.toEpochMilli(),
                durationSeconds = durationSeconds,
                totalSets = sets.size,
                totalReps = totalReps,
                calories = calories,
                avgHeartRate = avgHeartRate,
                distanceMeters = distanceMeters,
                notes = notes,
                completed = true
            )
        )
        sets.forEach { set ->
            dao.insertExerciseSet(
                ExerciseSetEntity(
                    sessionId = sessionId,
                    exerciseName = set.exerciseName,
                    setNumber = set.setNumber,
                    reps = set.reps,
                    weightKg = set.weightKg,
                    durationSeconds = set.durationSeconds,
                    timestampMillis = set.timestampMillis
                )
            )
        }
        dao.insertRoutePoints(
            route.map {
                RoutePointEntity(
                    sessionId = sessionId,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    altitudeMeters = it.altitudeMeters,
                    accuracyMeters = it.accuracyMeters,
                    timestampMillis = it.timestampMillis
                )
            }
        )
        runCatching {
            healthConnectManager.writeWorkoutSession(
                title = template.name,
                exerciseType = template.exerciseType,
                startTime = startTime,
                endTime = endTime,
                calories = calories,
                distanceMeters = distanceMeters
            )
        }
        return sessionId
    }

    suspend fun getSessionsForRange(startMillis: Long, endMillis: Long): List<WorkoutSessionEntity> {
        return dao.getWorkoutSessions(startMillis, endMillis)
    }

    private fun defaultTemplates(): List<WorkoutTemplateEntity> {
        fun template(
            id: String,
            name: String,
            category: String,
            exerciseType: Int,
            mode: String,
            gps: Boolean,
            rest: Int,
            description: String,
            metric: String
        ) = WorkoutTemplateEntity(id, name, category, exerciseType, mode, gps, rest, description, metric)

        return listOf(
            template("push_up", "Push-up", "Bodyweight", ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS, TRACKING_BODYWEIGHT, false, 60, "Tap each rep or complete a set after counting reps.", "reps"),
            template("pull_up", "Pull-up", "Bodyweight", ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS, TRACKING_BODYWEIGHT, false, 90, "Track strict reps and longer rest periods.", "reps"),
            template("squat", "Squat", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 120, "Log reps, weight, and working sets.", "sets"),
            template("lunges", "Lunges", "Bodyweight", ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS, TRACKING_BODYWEIGHT, false, 60, "Count reps per leg and track sets.", "reps"),
            template("bench_press", "Bench Press", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 150, "Track barbell sets, reps, and load.", "sets"),
            template("deadlift", "Deadlift", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 180, "Track heavy sets with longer recovery.", "sets"),
            template("overhead_press", "Overhead Press", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 120, "Track pressing volume and set quality.", "sets"),
            template("rows", "Rows", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE, TRACKING_STRENGTH, false, 90, "Track pulling volume by set.", "sets"),
            template("plank", "Plank", "Core", ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS, TRACKING_BODYWEIGHT, false, 60, "Track holds by duration and set count.", "time"),
            template("burpees", "Burpees", "HIIT", ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING, TRACKING_BODYWEIGHT, false, 45, "Tap reps quickly during intervals.", "reps"),
            template("jumping_jacks", "Jumping Jacks", "HIIT", ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS, TRACKING_BODYWEIGHT, false, 30, "Count reps or use the timer for intervals.", "reps"),
            template("walking", "Walking", "Cardio", ExerciseSessionRecord.EXERCISE_TYPE_WALKING, TRACKING_CARDIO, true, 0, "GPS route, distance, pace, HR, and calories.", "distance"),
            template("running", "Running", "Cardio", ExerciseSessionRecord.EXERCISE_TYPE_RUNNING, TRACKING_CARDIO, true, 0, "GPS route with live distance, pace, HR, and calories.", "pace"),
            template("cycling", "Cycling", "Cardio", ExerciseSessionRecord.EXERCISE_TYPE_BIKING, TRACKING_CARDIO, true, 0, "GPS route, speed, distance, and HR.", "speed"),
            template("swimming_pool", "Swimming Pool", "Swimming", ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL, TRACKING_CARDIO, false, 0, "Pool swim timer without GPS.", "time"),
            template("swimming_open", "Open-water Swim", "Swimming", ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER, TRACKING_CARDIO, true, 0, "Open-water swim with GPS route when permission is granted.", "distance"),
            template("yoga", "Yoga", "Mobility", ExerciseSessionRecord.EXERCISE_TYPE_YOGA, TRACKING_MOBILITY, false, 0, "Timer, HR, breath pacing, and session notes.", "time"),
            template("hiit", "HIIT", "HIIT", ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING, TRACKING_BODYWEIGHT, false, 30, "Fast intervals with rep taps and rest prompts.", "rounds"),
            template("kettlebell_flow", "Kettlebell Flow", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 90, "Dynamic kettlebell circuits combining strength and coordination.", "sets"),
            template("pilates_core", "Pilates Core", "Mobility", ExerciseSessionRecord.EXERCISE_TYPE_PILATES, TRACKING_MOBILITY, false, 45, "Mat-based core stability and structural alignment drills.", "time"),
            template("swim_interval", "Swim Intervals", "Swimming", ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL, TRACKING_CARDIO, false, 60, "Pool laps with timed interval rests and target pacing.", "time"),
            template("tabata_protocol", "Tabata Protocol", "HIIT", ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING, TRACKING_BODYWEIGHT, false, 10, "Ultra-short recovery intervals: 20s effort, 10s rest.", "rounds"),
            template("adaptive_strength", "Adaptive Strength", "Strength", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, TRACKING_STRENGTH, false, 90, "AI-adjusted strength protocol targeted for current fatigue profile.", "sets"),
            template("mobility_recovery", "Recovery Mobility", "Mobility", ExerciseSessionRecord.EXERCISE_TYPE_YOGA, TRACKING_MOBILITY, false, 0, "Guided mobility routine emphasizing breathing, hips, and shoulders.", "time")
        )
    }

    private fun routeDistanceMeters(route: List<RoutePointDraft>): Double {
        if (route.size < 2) return 0.0
        return route.zipWithNext().sumOf { (a, b) ->
            val result = FloatArray(1)
            android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
            result[0].toDouble()
        }
    }

    fun todayBounds(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = Instant.now().atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()
        return start.toEpochMilli() to Instant.now().toEpochMilli()
    }

    suspend fun getUserWeight(): Double? = healthConnectManager.readLatestWeight()
}
