package com.example.vitaai.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class HealthSnapshot(
    val steps: Long,
    val avgHeartRate: Double,
    val sleepDurationHours: Double,
    val calories: Double,
    val basalCalories: Double,
    val hydrationLiters: Double,
    val distanceMeters: Double,
    val exerciseMinutes: Double,
    val caloriesIntake: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val hourlySteps: Map<Instant, Long> = emptyMap(),
    val hourlyHeartRate: Map<Instant, Double> = emptyMap()
)

@Singleton
class VitaRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val dao: com.example.vitaai.data.local.VitaDao
) {
    private val systemZone = ZoneId.systemDefault()

    private fun startOfLocalDay(instant: Instant): Instant {
        val date = instant.atZone(systemZone).toLocalDate()
        return date.atStartOfDay(systemZone).toInstant()
    }

    suspend fun getDailySnapshot(): HealthSnapshot {
        val now = Instant.now()
        val startOfDay = startOfLocalDay(now)
        
        val steps = healthConnectManager.readDailySteps(startOfDay, now)
        val avgHr = healthConnectManager.readAvgHeartRate(startOfDay, now)
        val hourlyHeartRate = healthConnectManager.readHourlyHeartRate(startOfDay, now)
        val sleepDuration = healthConnectManager.readSleepDuration(
            now.minus(24, ChronoUnit.HOURS),
            now
        )
        // Sum active calories of local workouts logged today
        val startMillis = startOfDay.toEpochMilli()
        val endMillis = now.toEpochMilli()
        val localWorkouts = dao.getWorkoutSessions(startMillis, endMillis)
        val localWorkoutsCalories = localWorkouts.sumOf { it.calories }

        val calories = maxOf(healthConnectManager.readDailyCalories(startOfDay, now), localWorkoutsCalories)
        val basalCalories = healthConnectManager.readDailyBasalCalories(startOfDay, now)

        // Sum local drink entries logged today (hydrationMl / 1000 to convert to Liters)
        val localDrinks = dao.getDrinkEntries(startMillis, endMillis)
        val localHydrationLiters = localDrinks.sumOf { it.hydrationMl } / 1000.0
        val hydration = maxOf(healthConnectManager.readDailyHydration(startOfDay, now), localHydrationLiters)

        val hourlySteps = healthConnectManager.readHourlySteps(startOfDay, now)
        val distance = healthConnectManager.readDistance(startOfDay, now)
        val exerciseMinutes = healthConnectManager.readDailyExerciseMinutes(startOfDay, now)
        val nutrition = healthConnectManager.readDailyNutrition(startOfDay, now)
        
        return HealthSnapshot(
            steps = steps,
            avgHeartRate = avgHr,
            sleepDurationHours = sleepDuration,
            calories = calories,
            basalCalories = basalCalories,
            hydrationLiters = hydration,
            distanceMeters = distance,
            exerciseMinutes = exerciseMinutes,
            caloriesIntake = nutrition.calories,
            proteinGrams = nutrition.proteinGrams,
            carbsGrams = nutrition.carbsGrams,
            fatGrams = nutrition.fatGrams,
            hourlySteps = hourlySteps,
            hourlyHeartRate = hourlyHeartRate
        )
    }

    val healthSnapshotFlow: Flow<HealthSnapshot> = flow {
        while (true) {
            if (healthConnectManager.hasAllPermissions()) {
                val snapshot = getDailySnapshot()
                emit(snapshot)
            }
            delay(10000) // Refresh every 10 seconds
        }
    }

    suspend fun getHistoricalSteps(days: Int): Map<Instant, Long> {
        val now = Instant.now()
        val startTime = startOfLocalDay(now.minus(days.toLong(), ChronoUnit.DAYS))
        return healthConnectManager.readHourlySteps(startTime, now)
    }

    suspend fun logWater(liters: Double) {
        healthConnectManager.writeHydration(liters)
    }

    suspend fun getAiInsight(snapshot: HealthSnapshot): String {
        return when {
            snapshot.steps < 5000 -> "You've been a bit sedentary today. A 15-minute walk could boost your energy!"
            snapshot.sleepDurationHours < 6 -> "Your sleep was a bit short last night. Try to wind down earlier tonight."
            snapshot.avgHeartRate > 100 -> "Your resting heart rate seems elevated. Take a deep breath and relax."
            snapshot.hydrationLiters < 2.0 -> "Remember to stay hydrated! Aim for at least 2 liters today."
            else -> "You're doing great! Keep up the healthy habits."
        }
    }

    suspend fun getMetricTrend(metric: HealthMetricType, days: Int): Map<LocalDate, Double> {
        val trend = linkedMapOf<LocalDate, Double>()
        val today = Instant.now().atZone(systemZone).toLocalDate()

        for (offset in (days - 1) downTo 0) {
            val day = today.minusDays(offset.toLong())
            val start = day.atStartOfDay(systemZone).toInstant()
            val end = day.plusDays(1).atStartOfDay(systemZone).toInstant()
            trend[day] = readMetricValue(metric, start, end)
        }

        return trend
    }

    private suspend fun readMetricValue(metric: HealthMetricType, start: Instant, end: Instant): Double {
        return when (metric) {
            HealthMetricType.ACTIVE_CALORIES -> {
                val hcCalories = healthConnectManager.readDailyCalories(start, end)
                val localWorkouts = dao.getWorkoutSessions(start.toEpochMilli(), end.toEpochMilli())
                val localWorkoutsCalories = localWorkouts.sumOf { it.calories }
                maxOf(hcCalories, localWorkoutsCalories)
            }
            HealthMetricType.BASAL_CALORIES -> healthConnectManager.readDailyBasalCalories(start, end)
            HealthMetricType.STEPS -> healthConnectManager.readDailySteps(start, end).toDouble()
            HealthMetricType.HEART_RATE -> {
                val samples = healthConnectManager.readHeartRate(start, end)
                if (samples.isEmpty()) 0.0 else samples.average()
            }
            HealthMetricType.SLEEP -> {
                val sessions = healthConnectManager.readSleepSessions(start, end)
                sessions.sumOf { session -> java.time.Duration.between(session.startTime, session.endTime).toMinutes() } / 60.0
            }
            HealthMetricType.DISTANCE -> healthConnectManager.readDistance(start, end) / 1000.0
            HealthMetricType.HYDRATION -> {
                val hcHydration = healthConnectManager.readDailyHydration(start, end)
                val localDrinks = dao.getDrinkEntries(start.toEpochMilli(), end.toEpochMilli())
                val localHydrationLiters = localDrinks.sumOf { it.hydrationMl } / 1000.0
                maxOf(hcHydration, localHydrationLiters)
            }
            HealthMetricType.EXERCISE_MINUTES -> healthConnectManager.readDailyExerciseMinutes(start, end)
            HealthMetricType.CALORIES_INTAKE -> healthConnectManager.readDailyNutrition(start, end).calories
            HealthMetricType.PROTEIN -> healthConnectManager.readDailyNutrition(start, end).proteinGrams
            HealthMetricType.CARBOHYDRATE -> healthConnectManager.readDailyNutrition(start, end).carbsGrams
            HealthMetricType.FAT -> healthConnectManager.readDailyNutrition(start, end).fatGrams
            else -> 0.0
        }
    }
}
