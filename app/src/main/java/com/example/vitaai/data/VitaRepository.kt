package com.example.vitaai.data

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
    val hourlySteps: Map<Instant, Long> = emptyMap()
)

@Singleton
class VitaRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager
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
        val calories = healthConnectManager.readDailyCalories(startOfDay, now)
        val hydration = healthConnectManager.readDailyHydration(startOfDay, now)
        val hourlySteps = healthConnectManager.readHourlySteps(startOfDay, now)

        val avgHr = if (hrSamples.isNotEmpty()) hrSamples.average() else 0.0
        val sleepDuration = sleepSessions.sumOf { 
            java.time.Duration.between(it.startTime, it.endTime).toMinutes() 
        } / 60.0

        return HealthSnapshot(steps, avgHr, sleepDuration, calories, hydration, hourlySteps)
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
