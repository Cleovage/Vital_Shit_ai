package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.NutritionRepository
import com.example.vitaai.data.VitaRepository
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.GoalsRepository
import com.example.vitaai.data.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class AnalyticsMetricCard(
    val title: String,
    val value: String,
    val unit: String,
    val values: List<Float>,
    val detail: String,
    val stackedValues: List<Pair<Float, Float>>? = null
)

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val cards: List<AnalyticsMetricCard>, val aiInsight: String) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: VitaRepository,
    private val nutritionRepository: NutritionRepository,
    private val workoutRepository: WorkoutRepository,
    private val goalsRepository: GoalsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    private val _timeframe = MutableStateFlow(7)
    val timeframe: StateFlow<Int> = _timeframe

    init {
        viewModelScope.launch {
            repository.healthSnapshotFlow
                .catch { e ->
                    _uiState.value = AnalyticsUiState.Error(e.message ?: "Unable to load health snapshot flow")
                }
                .collect {
                    loadTrends(showLoading = false)
                }
        }
    }

    fun setTimeframe(days: Int) {
        _timeframe.value = days
        loadTrends(showLoading = true)
    }

    fun loadTrends(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AnalyticsUiState.Success) {
                _uiState.value = AnalyticsUiState.Loading
            }
            try {
                val days = _timeframe.value
                val isHourly = days == 1

                val snapshot = repository.healthSnapshotFlow.first()
                val aiInsight = repository.getAiInsight(snapshot)

                val cards = if (isHourly) {
                    val zone = ZoneId.systemDefault()
                    val now = Instant.now()
                    val localToday = now.atZone(zone).toLocalDate()
                    val startOfDay = localToday.atStartOfDay(zone).toInstant()
                    val startMillis = startOfDay.toEpochMilli()
                    val endMillis = now.toEpochMilli()

                    val stepsList = FloatArray(24)
                    val activeCalList = FloatArray(24)
                    val basalCalList = FloatArray(24)
                    val heartRateList = FloatArray(24)
                    val sleepList = FloatArray(24)
                    val distanceList = FloatArray(24)
                    val hydrationList = FloatArray(24)
                    val exerciseList = FloatArray(24)
                    val calInList = FloatArray(24)
                    val proteinList = FloatArray(24)
                    val macroBalanceList = FloatArray(24)
                    val workoutSessionsList = FloatArray(24)

                    val hourlySteps = snapshot.hourlySteps
                    val hourlyHr = snapshot.hourlyHeartRate

                    hourlySteps.forEach { (instantStr, count) ->
                        val hr = Instant.parse(instantStr).atZone(zone).hour
                        if (hr in 0..23) {
                            stepsList[hr] += count.toFloat()
                        }
                    }

                    val hrCounts = IntArray(24)
                    hourlyHr.forEach { (instantStr, rate) ->
                        val hr = Instant.parse(instantStr).atZone(zone).hour
                        if (hr in 0..23) {
                            heartRateList[hr] += rate.toFloat()
                            hrCounts[hr]++
                        }
                    }
                    val currentHour = now.atZone(zone).hour
                    for (i in 0..23) {
                        if (hrCounts[i] > 0) {
                            heartRateList[i] /= hrCounts[i]
                        } else {
                            heartRateList[i] = if (i <= currentHour) 70f else 0f
                        }
                    }

                    val age = goalsRepository.age.value
                    val gender = goalsRepository.gender.value
                    val activityLevel = goalsRepository.activityLevel.value
                    val bmr = CalorieCalculator.estimateBmr(75.0, 1.75, age, gender)
                    val multiplier = when (activityLevel.lowercase()) {
                        "sedentary" -> 1.1
                        "light" -> 1.2
                        "active" -> 1.3
                        "very active" -> 1.4
                        else -> 1.2
                    }
                    val hourlyBmr = (bmr * multiplier) / 24.0

                    for (h in 0..23) {
                        if (h <= currentHour) {
                            basalCalList[h] = hourlyBmr.toFloat()
                            activeCalList[h] = (stepsList[h] * 0.04 * (75.0 / 70.0)).toFloat()
                            distanceList[h] = (stepsList[h] * 0.00075f)
                        }
                    }

                    val todayWorkouts = workoutRepository.getSessionsForRange(startMillis, endMillis)
                    todayWorkouts.forEach { session ->
                        val hr = Instant.ofEpochMilli(session.startTimeMillis).atZone(zone).hour
                        if (hr in 0..23) {
                            activeCalList[hr] = activeCalList[hr] + session.calories.toFloat()
                            exerciseList[hr] = exerciseList[hr] + (session.durationSeconds / 60f)
                            workoutSessionsList[hr] = workoutSessionsList[hr] + 1f
                        }
                    }

                    val todayNutrition = nutritionRepository.getSummaryForRange(startMillis, endMillis)
                    todayNutrition.foods.forEach { food ->
                        val hr = Instant.ofEpochMilli(food.timestampMillis).atZone(zone).hour
                        if (hr in 0..23) {
                            calInList[hr] = calInList[hr] + food.calories.toFloat()
                            proteinList[hr] = proteinList[hr] + food.proteinGrams.toFloat()
                            macroBalanceList[hr] = macroBalanceList[hr] + (food.proteinGrams + food.carbsGrams + food.fatGrams).toFloat()
                        }
                    }
                    todayNutrition.drinks.forEach { drink ->
                        val hr = Instant.ofEpochMilli(drink.timestampMillis).atZone(zone).hour
                        if (hr in 0..23) {
                            hydrationList[hr] = hydrationList[hr] + (drink.hydrationMl / 1000f).toFloat()
                        }
                    }

                    val localHydrationSum = hydrationList.sum()
                    val hcHydrationSum = snapshot.hydrationLiters
                    if (hcHydrationSum > localHydrationSum) {
                        val diff = hcHydrationSum - localHydrationSum
                        val elapsedHoursCount = (currentHour + 1).coerceAtLeast(1)
                        val hourlyDiff = (diff / elapsedHoursCount).toFloat()
                        for (h in 0..currentHour) {
                            hydrationList[h] = hydrationList[h] + hourlyDiff
                        }
                    }

                    val localCalInSum = calInList.sum()
                    val hcCalInSum = snapshot.caloriesIntake
                    if (hcCalInSum > localCalInSum) {
                        val diff = hcCalInSum - localCalInSum
                        val elapsedHoursCount = (currentHour + 1).coerceAtLeast(1)
                        val hourlyDiff = (diff / elapsedHoursCount).toFloat()
                        for (h in 0..currentHour) {
                            calInList[h] = calInList[h] + hourlyDiff
                        }
                    }

                    val localProteinSum = proteinList.sum()
                    val hcProteinSum = snapshot.proteinGrams
                    if (hcProteinSum > localProteinSum) {
                        val diff = hcProteinSum - localProteinSum
                        val elapsedHoursCount = (currentHour + 1).coerceAtLeast(1)
                        val hourlyDiff = (diff / elapsedHoursCount).toFloat()
                        for (h in 0..currentHour) {
                            proteinList[h] = proteinList[h] + hourlyDiff
                        }
                    }

                    val localMacroSum = macroBalanceList.sum()
                    val hcMacroSum = snapshot.proteinGrams + snapshot.carbsGrams + snapshot.fatGrams
                    if (hcMacroSum > localMacroSum) {
                        val diff = hcMacroSum - localMacroSum
                        val elapsedHoursCount = (currentHour + 1).coerceAtLeast(1)
                        val hourlyDiff = (diff / elapsedHoursCount).toFloat()
                        for (h in 0..currentHour) {
                            macroBalanceList[h] = macroBalanceList[h] + hourlyDiff
                        }
                    }

                    val sleepDuration = snapshot.sleepDurationHours
                    if (sleepDuration > 0.0) {
                        val sleepHours = sleepDuration.toInt().coerceIn(1, 12)
                        for (h in 0 until sleepHours) {
                            sleepList[h] = 1f
                        }
                        val remainder = (sleepDuration - sleepHours).toFloat()
                        if (sleepHours in 0..23) {
                            sleepList[sleepHours] = remainder
                        }
                    }

                    listOf(
                        AnalyticsMetricCard("Steps", snapshot.steps.toString(), "steps", stepsList.toList(), "Movement volume hourly distribution"),
                        AnalyticsMetricCard(
                            "Calories Burned", 
                            String.format(Locale.US, "%.0f", snapshot.calories + snapshot.basalCalories), 
                            "kcal", 
                            activeCalList.toList(), 
                            "Hourly active vs idle calories",
                            stackedValues = basalCalList.zip(activeCalList) { idle, active -> idle to active }
                        ),
                        AnalyticsMetricCard("Calories In", snapshot.caloriesIntake.roundToInt().toString(), "kcal", calInList.toList(), "Food calories hourly intake"),
                        AnalyticsMetricCard("Protein", snapshot.proteinGrams.roundToInt().toString(), "g", proteinList.toList(), "Protein intake hourly distribution"),
                        AnalyticsMetricCard("Macro Balance", (snapshot.proteinGrams + snapshot.carbsGrams + snapshot.fatGrams).roundToInt().toString(), "g", macroBalanceList.toList(), "Total macro grams logged"),
                        AnalyticsMetricCard("Hydration", String.format(Locale.US, "%.1f", snapshot.hydrationLiters), "L", hydrationList.toList(), "Hourly fluid consumption"),
                        AnalyticsMetricCard("Workouts", todayWorkouts.size.toString(), "sessions", workoutSessionsList.toList(), "Completed VitaAI workouts"),
                        AnalyticsMetricCard("Distance", String.format(Locale.US, "%.2f", snapshot.distanceMeters / 1000.0), "km", distanceList.toList(), "Hourly tracked movement"),
                        AnalyticsMetricCard("Heart Rate", if (snapshot.avgHeartRate > 0) snapshot.avgHeartRate.roundToInt().toString() else "--", "bpm", heartRateList.toList(), "Average heart rate by hour"),
                        AnalyticsMetricCard("Sleep", String.format(Locale.US, "%.1f", snapshot.sleepDurationHours), "h", sleepList.toList(), "Sleep duration hourly visualizer"),
                        AnalyticsMetricCard("Exercise Minutes", snapshot.exerciseMinutes.roundToInt().toString(), "min", exerciseList.toList(), "Hourly recorded exercise minutes")
                    )
                } else {
                    // Optimized trends loading with parallel coroutine async/awaitAll
                    coroutineScope {
                        val stepsDef = async { repository.getMetricTrend(HealthMetricType.STEPS, days) }
                        val activeCaloriesDef = async { repository.getMetricTrend(HealthMetricType.ACTIVE_CALORIES, days) }
                        val basalCaloriesDef = async { repository.getMetricTrend(HealthMetricType.BASAL_CALORIES, days) }
                        val heartRateDef = async { repository.getMetricTrend(HealthMetricType.HEART_RATE, days) }
                        val sleepDef = async { repository.getMetricTrend(HealthMetricType.SLEEP, days) }
                        val distanceDef = async { repository.getMetricTrend(HealthMetricType.DISTANCE, days) }
                        val hydrationDef = async { repository.getMetricTrend(HealthMetricType.HYDRATION, days) }
                        val exerciseMinutesDef = async { repository.getMetricTrend(HealthMetricType.EXERCISE_MINUTES, days) }
                        val localNutritionDef = async { localNutritionTrend(days) }
                        val localWorkoutsDef = async { localWorkoutTrend(days) }

                        val steps = stepsDef.await()
                        val caloriesBurned = activeCaloriesDef.await()
                        val basalCalories = basalCaloriesDef.await()
                        val heartRate = heartRateDef.await()
                        val sleep = sleepDef.await()
                        val distance = distanceDef.await()
                        val hydration = hydrationDef.await()
                        val exerciseMinutes = exerciseMinutesDef.await()
                        val localNutrition = localNutritionDef.await()
                        val localWorkouts = localWorkoutsDef.await()

                        listOf(
                            card("Steps", steps, "steps", "Movement volume from Health Connect"),
                            card("Calories Burned", caloriesBurned, "kcal", "Active vs Idle calories").copy(
                                stackedValues = caloriesBurned.keys.map { date ->
                                    val idle = basalCalories[date]?.toFloat() ?: 0f
                                    val active = caloriesBurned[date]?.toFloat() ?: 0f
                                    idle to active
                                }.ifEmpty { listOf(0f to 0f, 0f to 0f) },
                                value = String.format(Locale.US, "%.0f", (caloriesBurned.values.lastOrNull() ?: 0.0) + (basalCalories.values.lastOrNull() ?: 0.0))
                            ),
                            card("Calories In", localNutrition.mapValues { it.value.calories }, "kcal", "Food logged in VitaAI"),
                            card("Protein", localNutrition.mapValues { it.value.proteinGrams }, "g", "Protein intake distribution"),
                            card("Macro Balance", localNutrition.mapValues { it.value.carbsGrams + it.value.fatGrams + it.value.proteinGrams }, "g", "Total macro grams logged"),
                            card("Hydration", mergeHydration(hydration, localNutrition), "L", "Health Connect plus logged drinks"),
                            card("Workouts", localWorkouts.mapValues { it.value.toDouble() }, "sessions", "Completed VitaAI workouts"),
                            card("Distance", distance, "km", "GPS and synced distance"),
                            card("Heart Rate", heartRate, "bpm", "Average heart-rate trend"),
                            card("Sleep", sleep, "h", "Sleep duration trend"),
                            card("Exercise Minutes", exerciseMinutes, "min", "Recorded training time")
                        )
                    }
                }

                _uiState.value = AnalyticsUiState.Success(
                    cards = cards,
                    aiInsight = aiInsight
                )
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(e.message ?: "Unable to load analytics")
            }
        }
    }

    private suspend fun localNutritionTrend(days: Int): Map<LocalDate, com.example.vitaai.data.NutritionSummary> {
        val zone = ZoneId.systemDefault()
        val today = Instant.now().atZone(zone).toLocalDate()
        return coroutineScope {
            val deferredList = ((days - 1) downTo 0).map { offset ->
                val day = today.minusDays(offset.toLong())
                val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                async {
                    day to nutritionRepository.getSummaryForRange(start, end)
                }
            }
            deferredList.awaitAll().toMap()
        }
    }

    private suspend fun localWorkoutTrend(days: Int): Map<LocalDate, Int> {
        val zone = ZoneId.systemDefault()
        val today = Instant.now().atZone(zone).toLocalDate()
        return coroutineScope {
            val deferred = ((days - 1) downTo 0).map { offset ->
                val day = today.minusDays(offset.toLong())
                val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                async {
                    day to workoutRepository.getSessionsForRange(start, end).size
                }
            }
            deferred.awaitAll().toMap()
        }
    }

    private fun mergeHydration(
        syncedHydration: Map<LocalDate, Double>,
        localNutrition: Map<LocalDate, com.example.vitaai.data.NutritionSummary>
    ): Map<LocalDate, Double> {
        return localNutrition.mapValues { (day, summary) ->
            val localLiters = summary.hydrationMl / 1000.0
            maxOf(syncedHydration[day] ?: 0.0, localLiters)
        }
    }

    private fun card(
        title: String,
        trend: Map<LocalDate, Double>,
        unit: String,
        detail: String
    ): AnalyticsMetricCard {
        val values = trend.values.map { it.toFloat() }.ifEmpty { listOf(0f, 0f) }
        val latest = trend.values.lastOrNull() ?: 0.0
        val formatted = when {
            latest >= 1000 -> String.format(Locale.US, "%.0f", latest)
            latest >= 10 -> String.format(Locale.US, "%.1f", latest)
            else -> String.format(Locale.US, "%.2f", latest)
        }
        return AnalyticsMetricCard(title, formatted, unit, values, detail)
    }
}
