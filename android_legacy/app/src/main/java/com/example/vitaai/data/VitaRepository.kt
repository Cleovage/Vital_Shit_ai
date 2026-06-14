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
    private val openRouterApi: OpenRouterApi,
    private val profileRepository: ProfileRepository
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
        // Fetch weekly trends for primary metrics
        val weekSteps = getMetricTrend(HealthMetricType.STEPS, 7).values.joinToString(", ")
        val weekSleep = getMetricTrend(HealthMetricType.SLEEP, 7).values.joinToString(", ") { String.format("%.1f", it) }
        val weekCalories = getMetricTrend(HealthMetricType.ACTIVE_CALORIES, 7).values.joinToString(", ") { it.toInt().toString() }

        val systemPrompt = """
            You are VitaAI, the world's most advanced health and wellness coach. 
            Your goal is to provide elite, science-backed, and highly personalized coaching.
            
            FORMATTING RULES:
            1. Use **Markdown** for emphasis (bold, lists).
            2. Use <viz> tags to visualize metrics when discussing them.
               Format: <viz>{"type": "progress", "label": "Steps", "current": 8500, "goal": 10000, "unit": "steps"}</viz>
               Format: <viz>{"type": "macros", "protein": 120, "carbs": 200, "fat": 60}</viz>
            3. Use <action> tags to propose logging data when the user mentions what they ate, drank, or their exercise.
               Format: <action>{"type": "nutrition", "name": "Chicken Salad", "meal": "lunch", "calories": 450, "protein": 35, "carbs": 12, "fat": 18}</action>
               Format: <action>{"type": "hydration", "volume_ml": 500}</action>
               Format: <action>{"type": "workout", "name": "Bench Press", "category": "Strength", "duration_min": 45, "calories": 250}</action>
            
            NEVER auto-log. ALWAYS use <action> tags to show a proposal card that the user can confirm.
            Keep responses professional yet warm. Keep text concise.

            TODAY'S METRICS:
            - Activity: ${snapshot.steps} steps (${String.format("%.1f", snapshot.distanceMeters / 1000.0)} km), ${snapshot.exerciseMinutes.toInt()} min exercise, ${snapshot.calories.toInt()} active kcal burned.
            - Recovery: ${String.format("%.1f", snapshot.sleepDurationHours)} hrs sleep, ${snapshot.avgHeartRate.toInt()} BPM avg HR, ${snapshot.restingHeartRate.toInt()} BPM resting HR.
            - Nutrition: ${snapshot.caloriesIntake.toInt()} kcal intake. Macros: Protein ${snapshot.proteinGrams.toInt()}g, Carbs ${snapshot.carbsGrams.toInt()}g, Fats ${snapshot.fatGrams.toInt()}g.
            - Hydration: ${String.format("%.1f", snapshot.hydrationLiters)} Liters consumed.
            - Energy: Basal metabolic rate is ${snapshot.basalCalories.toInt()} kcal.

            WEEKLY TRENDS (Last 7 days):
            - Steps Trend: $weekSteps
            - Sleep (hrs): $weekSleep
            - Active Burn (kcal): $weekCalories
        """.trimIndent()

        val fullMessages = mutableListOf(ChatMessage("system", systemPrompt))
        fullMessages.addAll(history)

        return try {
            val response = openRouterApi.createChatCompletion(
                request = ChatRequest(
                    model = "openai",
                    messages = fullMessages,
                    temperature = 0.7
                )
            )
            response.choices.firstOrNull()?.message?.content ?: generateFallbackResponse(history.lastOrNull()?.content ?: "", snapshot)
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 429) {
                generateFallbackResponse(history.lastOrNull()?.content ?: "", snapshot)
            } else {
                "I'm sorry, I encountered an error (HTTP ${e.code()}). Please try again later."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackResponse(history.lastOrNull()?.content ?: "", snapshot)
        }
    }

    private fun generateFallbackResponse(prompt: String, snapshot: HealthSnapshot): String {
        val query = prompt.lowercase(java.util.Locale.ROOT)
        return when {
            query.contains("step") || query.contains("walk") || query.contains("run") || query.contains("distance") -> {
                val stepGoal = 10000
                val diff = stepGoal - snapshot.steps
                if (snapshot.steps >= stepGoal) {
                    "Amazing job! You've crushed your step goal today with **${snapshot.steps} steps** (${String.format("%.1f", snapshot.distanceMeters / 1000.0)} km).\n<viz>{\"type\": \"progress\", \"label\": \"Steps\", \"current\": ${snapshot.steps}, \"goal\": 10000, \"unit\": \"steps\"}</viz>"
                } else {
                    "You are currently at **${snapshot.steps} steps** today. You need about $diff more steps to hit your 10,000 daily goal. Try taking a brisk 15-minute walk!\n<viz>{\"type\": \"progress\", \"label\": \"Steps\", \"current\": ${snapshot.steps}, \"goal\": 10000, \"unit\": \"steps\"}</viz>"
                }
            }
            query.contains("sleep") || query.contains("tired") || query.contains("rest") || query.contains("bed") -> {
                if (snapshot.sleepDurationHours >= 7.0) {
                    "You logged **${String.format("%.1f", snapshot.sleepDurationHours)} hours** of sleep. That is within the healthy range. Keep maintaining this healthy sleep hygiene!"
                } else {
                    "You only got **${String.format("%.1f", snapshot.sleepDurationHours)} hours** of sleep. Sleep deprivation increases cortisol. Try to wind down earlier tonight."
                }
            }
            query.contains("water") || query.contains("hydration") || query.contains("drink") -> {
                if (query.contains("drank") || query.contains("had")) {
                     "I can help you log that. Please confirm to add it to your daily hydration tracking.\n<action>{\"type\": \"hydration\", \"volume_ml\": 250}</action>"
                } else {
                    "You have drank **${String.format("%.1f", snapshot.hydrationLiters)} liters** today.\n<viz>{\"type\": \"progress\", \"label\": \"Hydration\", \"current\": ${snapshot.hydrationLiters * 1000}, \"goal\": 2500, \"unit\": \"ml\"}</viz>"
                }
            }
            query.contains("heart") || query.contains("pulse") || query.contains("bpm") -> {
                 "Your average heart rate today is **${snapshot.avgHeartRate.toInt()} BPM**, and your resting HR is **${snapshot.restingHeartRate.toInt()} BPM**."
            }
            query.contains("calorie") || query.contains("burn") -> {
                "Today you have burned **${snapshot.calories.toInt()} active calories**. (Total: ${(snapshot.calories + snapshot.basalCalories).toInt()} kcal including basal rate)."
            }
            query.contains("nutrition") || query.contains("eat") || query.contains("food") || query.contains("protein") || query.contains("carb") || query.contains("fat") -> {
                "Today's Intake: **${snapshot.caloriesIntake.toInt()} kcal**.\n<viz>{\"type\": \"macros\", \"protein\": ${snapshot.proteinGrams}, \"carbs\": ${snapshot.carbsGrams}, \"fat\": ${snapshot.fatGrams}}</viz>"
            }
            else -> {
                "(Offline Mode) Looking at your health snapshot today:\n" +
                "- Steps: **${snapshot.steps}**\n" +
                "- Sleep: **${String.format("%.1f", snapshot.sleepDurationHours)} hrs**\n" +
                "- Hydration: **${String.format("%.1f", snapshot.hydrationLiters)} L**\n" +
                "How can I help you optimize your wellness today?"
            }
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
