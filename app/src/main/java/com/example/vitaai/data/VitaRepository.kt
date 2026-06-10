package com.example.vitaai.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class HealthSnapshot(
    val steps: Long = 0,
    val avgHeartRate: Double = 0.0,
    val restingHeartRate: Double = 0.0,
    val sleepDurationHours: Double = 0.0,
    val calories: Double = 0.0,
    val basalCalories: Double = 0.0,
    val hydrationLiters: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val exerciseMinutes: Double = 0.0,
    val caloriesIntake: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val hourlySteps: Map<String, Long> = emptyMap(),
    val hourlyHeartRate: Map<String, Double> = emptyMap()
)

@Singleton
class VitaRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val dao: com.example.vitaai.data.local.VitaDao,
    private val openRouterApi: OpenRouterApi
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
        val restingHr = healthConnectManager.readRestingHeartRate(startOfDay, now)
        val hourlyHeartRate = healthConnectManager.readHourlyHeartRate(startOfDay, now)
        val hcSleepDuration = healthConnectManager.readSleepDuration(
            now.minus(24, ChronoUnit.HOURS),
            now
        )
        val startMillis = startOfDay.toEpochMilli()
        val endMillis = now.toEpochMilli()
        
        val localSleepSessions = dao.getSleepSessions(startMillis, endMillis)
        val localSleepDuration = localSleepSessions.sumOf { it.durationMinutes } / 60.0
        val sleepDuration = maxOf(hcSleepDuration, localSleepDuration)

        // Sum active calories of local workouts logged today
        val localWorkouts = dao.getWorkoutSessions(startMillis, endMillis)
        val localWorkoutsCalories = localWorkouts.sumOf { it.calories }
        val stepsCalories = CalorieCalculator.estimateActiveCaloriesFromSteps(steps, 75.0)

        val hcCalories = healthConnectManager.readDailyCalories(startOfDay, now)
        val calories = maxOf(hcCalories, localWorkoutsCalories + stepsCalories)
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
            restingHeartRate = restingHr,
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
            hourlySteps = hourlySteps.mapKeys { it.key.toString() },
            hourlyHeartRate = hourlyHeartRate.mapKeys { it.key.toString() }
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
    }.shareIn(
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

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

    suspend fun getChatbotResponse(history: List<ChatMessage>, snapshot: HealthSnapshot): String {
        val weekSteps = getMetricTrend(HealthMetricType.STEPS, 7).values.joinToString(", ")
        val weekSleep = getMetricTrend(HealthMetricType.SLEEP, 7).values.joinToString(", ") { String.format("%.1f", it) }
        val weekCalories = getMetricTrend(HealthMetricType.ACTIVE_CALORIES, 7).values.joinToString(", ") { it.toInt().toString() }

        val systemPrompt = """
            You are VitaAI, an expert health and wellness coach.
            Your goal is to provide encouraging, science-backed, and highly personalized advice based on the user's real-time health data.
            Keep your responses concise, empathetic, and directly related to the user's prompt. Do not hallucinate data. Be encouraging!

            TODAY'S DATA:
            - Steps: ${snapshot.steps} / 10000
            - Sleep: ${String.format("%.1f", snapshot.sleepDurationHours)} hrs
            - Hydration: ${String.format("%.1f", snapshot.hydrationLiters)} L
            - Avg Heart Rate: ${snapshot.avgHeartRate.toInt()} BPM
            - Active Calories Burned: ${snapshot.calories.toInt()} kcal
            - Distance: ${String.format("%.1f", snapshot.distanceMeters / 1000.0)} km
            - Nutrition Logged: ${snapshot.caloriesIntake.toInt()} kcal (Protein: ${snapshot.proteinGrams.toInt()}g, Carbs: ${snapshot.carbsGrams.toInt()}g, Fats: ${snapshot.fatGrams.toInt()}g)

            WEEKLY TRENDS (Last 7 days):
            - Steps: $weekSteps
            - Sleep (hrs): $weekSleep
            - Active Calories: $weekCalories
        """.trimIndent()

        val fullMessages = mutableListOf(ChatMessage("system", systemPrompt))
        // To save context window size if history gets too long, we could trim it, but we'll send it all for now.
        fullMessages.addAll(history)

        return try {
            val response = openRouterApi.createChatCompletion(
                request = ChatRequest(
                    model = "openai",
                    messages = fullMessages,
                    temperature = 0.7
                )
            )
            response.choices.firstOrNull()?.message?.content ?: "I'm sorry, I couldn't generate a response."
        } catch (e: Exception) {
            e.printStackTrace()
            "I'm sorry, my AI services are currently unavailable. Please check your network connection. Details: ${e.localizedMessage}"
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
                val steps = healthConnectManager.readDailySteps(start, end)
                val stepsCalories = CalorieCalculator.estimateActiveCaloriesFromSteps(steps, 75.0)
                maxOf(hcCalories, localWorkoutsCalories + stepsCalories)
            }
            HealthMetricType.BASAL_CALORIES -> healthConnectManager.readDailyBasalCalories(start, end)
            HealthMetricType.STEPS -> healthConnectManager.readDailySteps(start, end).toDouble()
            HealthMetricType.HEART_RATE -> {
                val samples = healthConnectManager.readHeartRate(start, end)
                if (samples.isEmpty()) 0.0 else samples.average()
            }
            HealthMetricType.SLEEP -> {
                val hcSessions = healthConnectManager.readSleepSessions(start, end)
                val hcDuration = hcSessions.sumOf { session -> java.time.Duration.between(session.startTime, session.endTime).toMinutes() } / 60.0
                val localSessions = dao.getSleepSessions(start.toEpochMilli(), end.toEpochMilli())
                val localDuration = localSessions.sumOf { it.durationMinutes } / 60.0
                maxOf(hcDuration, localDuration)
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
