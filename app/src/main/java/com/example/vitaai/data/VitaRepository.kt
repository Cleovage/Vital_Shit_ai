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

    suspend fun getChatbotResponse(prompt: String, snapshot: HealthSnapshot): String {
        val query = prompt.lowercase(java.util.Locale.ROOT)
        return when {
            query.contains("step") || query.contains("walk") || query.contains("run") || query.contains("distance") -> {
                val stepGoal = 10000
                val diff = stepGoal - snapshot.steps
                if (snapshot.steps >= stepGoal) {
                    "Amazing job! You've crushed your step goal today with ${snapshot.steps} steps (${String.format("%.1f", snapshot.distanceMeters / 1000.0)} km). Keep up this fantastic momentum!"
                } else {
                    "You are currently at ${snapshot.steps} steps today. You need about $diff more steps to hit your 10,000 daily step goal. Try taking a brisk 15-minute walk after your next meal!"
                }
            }
            query.contains("sleep") || query.contains("tired") || query.contains("rest") || query.contains("bed") -> {
                if (snapshot.sleepDurationHours >= 7.0) {
                    "You logged ${String.format("%.1f", snapshot.sleepDurationHours)} hours of sleep. That is within the healthy range (7-9 hours). Keep maintaining this healthy sleep hygiene!"
                } else {
                    "You only got ${String.format("%.1f", snapshot.sleepDurationHours)} hours of sleep. Sleep deprivation increases cortisol and slows recovery. Try to avoid blue light screens 1 hour before bed tonight."
                }
            }
            query.contains("water") || query.contains("hydration") || query.contains("drink") || query.contains("dehydrated") -> {
                val target = 2.5
                val diff = target - snapshot.hydrationLiters
                if (snapshot.hydrationLiters >= target) {
                    "Excellent hydration! You've consumed ${String.format("%.1f", snapshot.hydrationLiters)} liters of water today, meeting your target. Your kidneys and muscles thank you!"
                } else {
                    "You have drank ${String.format("%.1f", snapshot.hydrationLiters)} liters today. You need another ${String.format("%.1f", diff)} L to hit your 2.5L target. Grab a glass of water right now!"
                }
            }
            query.contains("heart") || query.contains("pulse") || query.contains("bpm") || query.contains("cardio") -> {
                if (snapshot.avgHeartRate in 60.0..85.0) {
                    "Your average heart rate today is ${snapshot.avgHeartRate.toInt()} BPM, which is in a very healthy resting range. This shows good cardiovascular efficiency!"
                } else if (snapshot.avgHeartRate > 85.0) {
                    "Your average heart rate is slightly elevated at ${snapshot.avgHeartRate.toInt()} BPM. This could be due to stress, caffeine, or active digestion. Try a 4-7-8 breathing exercise to calm your nervous system."
                } else {
                    "Your average heart rate today is ${snapshot.avgHeartRate.toInt()} BPM. Let me know if you want to log any specific exercise session."
                }
            }
            query.contains("calorie") || query.contains("burn") || query.contains("metabolism") || query.contains("weight") -> {
                "Today you have burned ${snapshot.calories.toInt()} active calories (total of ${(snapshot.calories + snapshot.basalCalories).toInt()} including basal metabolic rate). To support your goals, ensure you balance this with your nutrition intake."
            }
            query.contains("nutrition") || query.contains("eat") || query.contains("food") || query.contains("protein") || query.contains("carb") || query.contains("fat") || query.contains("diet") -> {
                val totalMacros = snapshot.proteinGrams + snapshot.carbsGrams + snapshot.fatGrams
                if (totalMacros > 0 && snapshot.caloriesIntake > 0) {
                    val pPct = (snapshot.proteinGrams * 4 / snapshot.caloriesIntake * 100).toInt().coerceIn(0, 100)
                    val cPct = (snapshot.carbsGrams * 4 / snapshot.caloriesIntake * 100).toInt().coerceIn(0, 100)
                    val fPct = (snapshot.fatGrams * 9 / snapshot.caloriesIntake * 100).toInt().coerceIn(0, 100)
                    "Today's Intake: ${snapshot.caloriesIntake.toInt()} kcal. Macros: Protein ${snapshot.proteinGrams.toInt()}g ($pPct%), Carbs ${snapshot.carbsGrams.toInt()}g ($cPct%), Fats ${snapshot.fatGrams.toInt()}g ($fPct%). Make sure you align this with your target goals!"
                } else {
                    "You haven't logged any food entries yet today. Logging your meals helps keep track of macronutrients. Let me know what you've eaten and I can estimate its profile!"
                }
            }
            else -> {
                "Hi! I'm your VitaAI Coach. Looking at your health snapshot today:\n" +
                "- Steps: ${snapshot.steps} / 10000\n" +
                "- Sleep: ${String.format("%.1f", snapshot.sleepDurationHours)} hrs\n" +
                "- Hydration: ${String.format("%.1f", snapshot.hydrationLiters)} L\n" +
                "- Heart Rate: ${snapshot.avgHeartRate.toInt()} BPM\n" +
                "How can I help you optimize your wellness, nutrition, or workout schedule today?"
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
                maxOf(hcCalories, localWorkoutsCalories)
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
