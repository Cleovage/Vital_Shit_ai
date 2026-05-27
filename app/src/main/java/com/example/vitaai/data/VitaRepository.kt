package com.example.vitaai.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class HealthSnapshot(
    val steps: Long,
    val avgHeartRate: Double,
    val sleepDurationHours: Double,
    val calories: Double,
    val hydrationLiters: Double,
    val distanceMeters: Double,
    val hourlySteps: Map<Instant, Long> = emptyMap(),
    val hourlyHeartRate: List<Long> = emptyList() // Adding for sparkline
)

@Singleton
class VitaRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val liveSensorManager: LiveSensorManager
) {
    suspend fun getDailySnapshot(): HealthSnapshot {
        val now = Instant.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
        
        val steps = healthConnectManager.readDailySteps(startOfDay, now)
        val hrSamples = healthConnectManager.readHeartRate(startOfDay, now)
        val sleepSessions = healthConnectManager.readSleepSessions(
            now.minus(24, ChronoUnit.HOURS),
            now
        )
        var calories = healthConnectManager.readDailyCalories(startOfDay, now)
        val hydration = healthConnectManager.readDailyHydration(startOfDay, now)
        val hourlySteps = healthConnectManager.readHourlySteps(startOfDay, now)
        val distance = healthConnectManager.readDistance(startOfDay, now)
        
        val avgHr = if (hrSamples.isNotEmpty()) hrSamples.average() else 0.0
        val sleepDuration = sleepSessions.sumOf { 
            java.time.Duration.between(it.startTime, it.endTime).toMinutes() 
        } / 60.0

        if (calories == 0.0 && steps > 0) {
            // Fallback calorie calculation: roughly ~0.04 kcal per step
            calories = steps * 0.04
        }

        return HealthSnapshot(steps, avgHr, sleepDuration, calories, hydration, distance, hourlySteps, hrSamples)
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
        val startTime = now.minus(days.toLong(), ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
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
}
