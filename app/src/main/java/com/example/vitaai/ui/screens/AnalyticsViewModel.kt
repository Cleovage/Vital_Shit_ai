package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.NutritionRepository
import com.example.vitaai.data.VitaRepository
import com.example.vitaai.data.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

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
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    private val _timeframe = MutableStateFlow(7)
    val timeframe: StateFlow<Int> = _timeframe

    init {
        viewModelScope.launch {
            repository.healthSnapshotFlow.collect {
                loadTrends()
            }
        }
    }

    fun setTimeframe(days: Int) {
        _timeframe.value = days
        loadTrends()
    }

    fun loadTrends() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            try {
                val days = _timeframe.value
                val steps = repository.getMetricTrend(HealthMetricType.STEPS, days)
                val caloriesBurned = repository.getMetricTrend(HealthMetricType.ACTIVE_CALORIES, days)
                val basalCalories = repository.getMetricTrend(HealthMetricType.BASAL_CALORIES, days)
                val heartRate = repository.getMetricTrend(HealthMetricType.HEART_RATE, days)
                val sleep = repository.getMetricTrend(HealthMetricType.SLEEP, days)
                val distance = repository.getMetricTrend(HealthMetricType.DISTANCE, days)
                val hydration = repository.getMetricTrend(HealthMetricType.HYDRATION, days)
                val exerciseMinutes = repository.getMetricTrend(HealthMetricType.EXERCISE_MINUTES, days)
                val localNutrition = localNutritionTrend(days)
                val localWorkouts = localWorkoutTrend(days)

                val snapshot = repository.healthSnapshotFlow.first()
                val aiInsight = repository.getAiInsight(snapshot)
                
                _uiState.value = AnalyticsUiState.Success(
                    cards = listOf(
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
                    ),
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
        return ((days - 1) downTo 0).associate { offset ->
            val day = today.minusDays(offset.toLong())
            val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            day to nutritionRepository.getSummaryForRange(start, end)
        }
    }

    private suspend fun localWorkoutTrend(days: Int): Map<LocalDate, Int> {
        val zone = ZoneId.systemDefault()
        val today = Instant.now().atZone(zone).toLocalDate()
        return ((days - 1) downTo 0).associate { offset ->
            val day = today.minusDays(offset.toLong())
            val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            day to workoutRepository.getSessionsForRange(start, end).size
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
