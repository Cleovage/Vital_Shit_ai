package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthDataSource
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.HealthConnectManager
import com.example.vitaai.data.VitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class MetricDetailData(
    val metric: HealthMetricType,
    val currentValue: String,
    val unitLabel: String,
    val description: String,
    val sourceLabels: List<String>,
    val todayChartValues: List<Float>,
    val todayLabels: List<String>,
    val weekChartValues: List<Float>,
    val weekLabels: List<String>,
    val relatedStats: List<Pair<String, String>>
)

sealed class MetricDetailUiState {
    object Loading : MetricDetailUiState()
    object PermissionsRequired : MetricDetailUiState()
    data class Success(val data: MetricDetailData) : MetricDetailUiState()
    data class Error(val message: String) : MetricDetailUiState()
}

private data class ChartSeries(
    val values: List<Float>,
    val labels: List<String>
)

@HiltViewModel
class MetricDetailViewModel @Inject constructor(
    private val repository: VitaRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MetricDetailUiState>(MetricDetailUiState.Loading)
    val uiState: StateFlow<MetricDetailUiState> = _uiState

    private var currentMetric: HealthMetricType? = null
    private val systemZone = ZoneId.systemDefault()

    fun load(metricRoute: String?) {
        val metric = HealthMetricType.fromRoute(metricRoute)
            ?: run {
                _uiState.value = MetricDetailUiState.Error("Unknown metric")
                return
            }

        if (currentMetric == metric && _uiState.value is MetricDetailUiState.Success) {
            return
        }

        currentMetric = metric
        loadMetric(metric)
    }

    fun getRequestedPermissions() = healthConnectManager.permissions

    fun onPermissionsResult(grantedPermissions: Set<String>) {
        if (grantedPermissions.containsAll(healthConnectManager.requiredPermissions)) {
            currentMetric?.let { loadMetric(it) }
        } else {
            _uiState.value = MetricDetailUiState.PermissionsRequired
        }
    }

    private fun loadMetric(metric: HealthMetricType) {
        viewModelScope.launch {
            _uiState.value = MetricDetailUiState.Loading

            if (!healthConnectManager.hasAllPermissions()) {
                _uiState.value = MetricDetailUiState.PermissionsRequired
                return@launch
            }

            try {
                val snapshot = repository.getDailySnapshot()
                val trend = repository.getMetricTrend(metric, days = 7)
                _uiState.value = MetricDetailUiState.Success(
                    buildDetail(metric = metric, snapshot = snapshot, trend = trend)
                )
            } catch (e: Exception) {
                _uiState.value = MetricDetailUiState.Error(e.message ?: "Unable to load metric")
            }
        }
    }

    private fun buildDetail(
        metric: HealthMetricType,
        snapshot: HealthSnapshot,
        trend: Map<LocalDate, Double>
    ): MetricDetailData {
        val currentValue = currentValue(metric, snapshot)
        val todaySeries = todaySeries(metric, snapshot)
        val weekChartValues = trend.values.map { it.toFloat() }.ifEmpty { listOf(0f, 0f) }
        val weekLabels = trend.keys.map { it.format(DateTimeFormatter.ofPattern("EEE", Locale.US)) }
        val sourceLabels = metric.sources.map(::toSourceLabel)
        val relatedStats = relatedStats(metric, snapshot, trend)

        return MetricDetailData(
            metric = metric,
            currentValue = currentValue,
            unitLabel = metric.unit,
            description = metric.description,
            sourceLabels = sourceLabels,
            todayChartValues = todaySeries.values,
            todayLabels = todaySeries.labels,
            weekChartValues = weekChartValues,
            weekLabels = weekLabels,
            relatedStats = relatedStats
        )
    }

    private fun currentValue(metric: HealthMetricType, snapshot: HealthSnapshot): String {
        return when (metric) {
            HealthMetricType.ACTIVE_CALORIES -> snapshot.calories.roundToInt().toString()
            HealthMetricType.STEPS -> snapshot.steps.toString()
            HealthMetricType.HEART_RATE -> if (snapshot.avgHeartRate > 0) snapshot.avgHeartRate.roundToInt().toString() else "0"
            HealthMetricType.SLEEP -> String.format(Locale.US, "%.1f", snapshot.sleepDurationHours)
            HealthMetricType.DISTANCE -> String.format(Locale.US, "%.2f", snapshot.distanceMeters / 1000.0)
            HealthMetricType.HYDRATION -> String.format(Locale.US, "%.2f", snapshot.hydrationLiters)
            HealthMetricType.EXERCISE_MINUTES -> snapshot.exerciseMinutes.roundToInt().toString()
            HealthMetricType.CALORIES_INTAKE -> snapshot.caloriesIntake.roundToInt().toString()
            HealthMetricType.PROTEIN -> snapshot.proteinGrams.roundToInt().toString()
            HealthMetricType.CARBOHYDRATE -> snapshot.carbsGrams.roundToInt().toString()
            HealthMetricType.FAT -> snapshot.fatGrams.roundToInt().toString()
            else -> "0"
        }
    }

    private fun todaySeries(metric: HealthMetricType, snapshot: HealthSnapshot): ChartSeries {
        return when (metric) {
            HealthMetricType.STEPS -> hourlySeries(snapshot.hourlySteps.mapKeys { Instant.parse(it.key) }, snapshot.steps.toFloat())
            HealthMetricType.HEART_RATE -> hourlySeries(snapshot.hourlyHeartRate.mapKeys { Instant.parse(it.key) }, snapshot.avgHeartRate.toFloat())
            HealthMetricType.ACTIVE_CALORIES -> simpleSeries(snapshot.calories.toFloat())
            HealthMetricType.SLEEP -> simpleSeries(snapshot.sleepDurationHours.toFloat())
            HealthMetricType.DISTANCE -> simpleSeries((snapshot.distanceMeters / 1000.0).toFloat())
            HealthMetricType.HYDRATION -> simpleSeries(snapshot.hydrationLiters.toFloat())
            HealthMetricType.EXERCISE_MINUTES -> simpleSeries(snapshot.exerciseMinutes.toFloat())
            HealthMetricType.CALORIES_INTAKE -> simpleSeries(snapshot.caloriesIntake.toFloat())
            HealthMetricType.PROTEIN -> simpleSeries(snapshot.proteinGrams.toFloat())
            HealthMetricType.CARBOHYDRATE -> simpleSeries(snapshot.carbsGrams.toFloat())
            HealthMetricType.FAT -> simpleSeries(snapshot.fatGrams.toFloat())
            else -> simpleSeries(0f)
        }
    }

    private fun simpleSeries(value: Float): ChartSeries {
        return ChartSeries(values = listOf(0f, value), labels = listOf("0", "24"))
    }

    private fun <T : Number> hourlySeries(values: Map<Instant, T>, fallbackValue: Float): ChartSeries {
        if (values.isEmpty()) {
            return simpleSeries(fallbackValue)
        }

        val sorted = values.toSortedMap()
        return ChartSeries(
            values = sorted.values.map { it.toFloat() },
            labels = sorted.keys.map { instant ->
                val hour = instant.atZone(systemZone).hour
                String.format(Locale.US, "%02d", hour)
            }
        )
    }

    private fun relatedStats(
        metric: HealthMetricType,
        snapshot: HealthSnapshot,
        trend: Map<LocalDate, Double>
    ): List<Pair<String, String>> {
        val weeklyAverage = if (trend.isNotEmpty()) trend.values.average() else 0.0
        val weeklyHigh = trend.values.maxOrNull() ?: 0.0

        val base = mutableListOf(
            "Weekly Avg" to String.format(Locale.US, "%.1f", weeklyAverage),
            "Weekly High" to String.format(Locale.US, "%.1f", weeklyHigh),
            "Sources" to metric.sources.size.toString()
        )

        when (metric) {
            HealthMetricType.HYDRATION -> base.add("Goal Progress" to String.format(Locale.US, "%.0f%%", (snapshot.hydrationLiters / 2.5) * 100))
            HealthMetricType.STEPS -> base.add("Goal Progress" to String.format(Locale.US, "%.0f%%", (snapshot.steps / 10000.0) * 100))
            HealthMetricType.SLEEP -> base.add("Recovery Target" to String.format(Locale.US, "%.0f%%", (snapshot.sleepDurationHours / 8.0) * 100))
            HealthMetricType.PROTEIN -> base.add("Nutrition Balance" to if (snapshot.proteinGrams >= 90) "On Track" else "Below Target")
            else -> Unit
        }

        return base
    }

    private fun toSourceLabel(source: HealthDataSource): String {
        return when (source) {
            HealthDataSource.SAMSUNG_HEALTH -> "Samsung Health"
            HealthDataSource.GOOGLE_HEALTH_FITBIT -> "Google Health/Fitbit"
            HealthDataSource.VITA_AI -> "VitaAI"
        }
    }
}
