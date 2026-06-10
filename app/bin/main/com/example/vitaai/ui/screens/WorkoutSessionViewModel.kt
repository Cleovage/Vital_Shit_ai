package com.example.vitaai.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.CalorieCalculator
import com.example.vitaai.data.RoutePointDraft
import com.example.vitaai.data.TRACKING_CARDIO
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.WorkoutSetDraft
import com.example.vitaai.data.local.WorkoutTemplateEntity
import com.example.vitaai.tracking.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class WorkoutSessionUiState(
    val template: WorkoutTemplateEntity? = null,
    val elapsedSeconds: Long = 0,
    val running: Boolean = false,
    val currentReps: Int = 0,
    val completedSets: Int = 0,
    val totalReps: Int = 0,
    val restRemainingSeconds: Int = 0,
    val liveHeartRate: Int = 0,
    val distanceMeters: Double = 0.0,
    val routePointCount: Int = 0,
    val calories: Double = 0.0,
    val saving: Boolean = false,
    val savedSessionId: Long? = null,
    val error: String? = null,
    val currentWeightKg: Double = 20.0,
    val currentPace: String = "--"
)

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {
    private val templateId: String = savedStateHandle["templateId"] ?: "walking"
    private val _uiState = MutableStateFlow(WorkoutSessionUiState())
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState

    private var timerJob: Job? = null
    private var heartRateJob: Job? = null
    private var locationJob: Job? = null
    private var restJob: Job? = null
    private var startTime: Instant? = null
    private val route = mutableListOf<RoutePointDraft>()
    private val sets = mutableListOf<WorkoutSetDraft>()
    private val heartRates = mutableListOf<Int>()
    private var userWeightKg: Double = 75.0

    init {
        viewModelScope.launch {
            userWeightKg = workoutRepository.getUserWeight() ?: 75.0
            val template = workoutRepository.getTemplate(templateId)
            _uiState.value = _uiState.value.copy(template = template, error = if (template == null) "Workout not found" else null)
        }
    }

    fun start() {
        val template = _uiState.value.template ?: return
        if (startTime == null) startTime = Instant.now()
        _uiState.value = _uiState.value.copy(running = true)
        startTimer()
        startHeartRate()
        if (template.gpsEnabled) startLocation()
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(running = false)
        timerJob?.cancel()
        locationJob?.cancel()
    }

    fun addRep() {
        val nextReps = _uiState.value.currentReps + 1
        val nextTotalReps = _uiState.value.totalReps + 1
        val template = _uiState.value.template
        val nextCalories = if (template != null) {
            if (template.trackingMode == TRACKING_CARDIO) {
                CalorieCalculator.estimateCardioCalories(
                    templateId = templateId,
                    durationSeconds = _uiState.value.elapsedSeconds,
                    distanceMeters = _uiState.value.distanceMeters,
                    weightKg = userWeightKg
                )
            } else {
                CalorieCalculator.estimateStrengthCalories(
                    templateId = templateId,
                    durationSeconds = _uiState.value.elapsedSeconds,
                    completedSets = _uiState.value.completedSets,
                    totalReps = nextTotalReps,
                    weightKg = userWeightKg
                )
            }
        } else 0.0

        _uiState.value = _uiState.value.copy(
            currentReps = nextReps,
            totalReps = nextTotalReps,
            calories = nextCalories
        )
    }

    fun setWeight(weight: Double) {
        _uiState.value = _uiState.value.copy(currentWeightKg = weight.coerceAtLeast(0.0))
    }
    
    fun adjustWeight(delta: Double) {
        val next = _uiState.value.currentWeightKg + delta
        _uiState.value = _uiState.value.copy(currentWeightKg = next.coerceAtLeast(0.0))
    }

    fun adjustReps(delta: Int) {
        val nextReps = (_uiState.value.currentReps + delta).coerceAtLeast(0)
        val nextTotalReps = (_uiState.value.totalReps + delta).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentReps = nextReps, totalReps = nextTotalReps)
    }

    fun completeSet() {
        val template = _uiState.value.template ?: return
        val weightKg = _uiState.value.currentWeightKg
        val reps = _uiState.value.currentReps.coerceAtLeast(if (template.trackingMode == TRACKING_CARDIO) 0 else 1)
        val setNumber = sets.size + 1
        sets += WorkoutSetDraft(
            exerciseName = template.name,
            setNumber = setNumber,
            reps = reps,
            weightKg = weightKg,
            durationSeconds = _uiState.value.elapsedSeconds
        )
        val nextCalories = if (template.trackingMode == TRACKING_CARDIO) {
            CalorieCalculator.estimateCardioCalories(
                templateId = templateId,
                durationSeconds = _uiState.value.elapsedSeconds,
                distanceMeters = _uiState.value.distanceMeters,
                weightKg = userWeightKg
            )
        } else {
            CalorieCalculator.estimateStrengthCalories(
                templateId = templateId,
                durationSeconds = _uiState.value.elapsedSeconds,
                completedSets = setNumber,
                totalReps = _uiState.value.totalReps,
                weightKg = userWeightKg
            )
        }
        _uiState.value = _uiState.value.copy(
            currentReps = 0,
            completedSets = setNumber,
            calories = nextCalories
        )
        startRest(template.defaultRestSeconds)
    }

    fun save(notes: String = "") {
        val template = _uiState.value.template ?: return
        val start = startTime ?: Instant.now().minusSeconds(_uiState.value.elapsedSeconds)
        val end = Instant.now()
        val avgHr = if (heartRates.isEmpty()) 0.0 else heartRates.average()
        val calories = estimateCalories(template.trackingMode, _uiState.value.elapsedSeconds, avgHr)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true)
            runCatching {
                workoutRepository.saveWorkoutSession(
                    template = template,
                    startTime = start,
                    endTime = end,
                    totalReps = _uiState.value.totalReps,
                    sets = sets.toList(),
                    route = route.toList(),
                    avgHeartRate = avgHr,
                    calories = calories,
                    notes = notes
                )
            }.onSuccess { sessionId ->
                stopCollectors()
                _uiState.value = _uiState.value.copy(saving = false, running = false, savedSessionId = sessionId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(saving = false, error = error.message ?: "Unable to save workout")
            }
        }
    }

    private fun calculatePace(seconds: Long, distanceMeters: Double): String {
        if (distanceMeters <= 0.0 || seconds <= 0) return "--"
        val distKm = distanceMeters / 1000.0
        val paceSecs = (seconds / distKm).roundToInt()
        val mins = paceSecs / 60
        val secs = paceSecs % 60
        return String.format(Locale.US, "%02d:%02d/KM", mins, secs)
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.running) {
                    val nextElapsed = _uiState.value.elapsedSeconds + 1
                    val template = _uiState.value.template
                    val avgHr = if (heartRates.isEmpty()) 0.0 else heartRates.average()
                    val nextCalories = if (template != null) {
                        estimateCalories(template.trackingMode, nextElapsed, avgHr)
                    } else 0.0
                    val pace = calculatePace(nextElapsed, _uiState.value.distanceMeters)
                    
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = nextElapsed,
                        calories = nextCalories,
                        currentPace = pace
                    )
                }
            }
        }
    }

    private fun startHeartRate() {
        // No local heart rate measurement
    }

    private fun startLocation() {
        if (locationJob?.isActive == true) return
        locationJob = viewModelScope.launch {
            locationTracker.locationFlow().collect { location ->
                if (!_uiState.value.running) return@collect
                route += RoutePointDraft(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitudeMeters = location.altitude,
                    accuracyMeters = location.accuracy,
                    timestampMillis = location.time
                )
                val dist = distanceMeters(route)
                val pace = calculatePace(_uiState.value.elapsedSeconds, dist)
                _uiState.value = _uiState.value.copy(
                    routePointCount = route.size,
                    distanceMeters = dist,
                    currentPace = pace
                )
            }
        }
    }

    private fun startRest(seconds: Int) {
        restJob?.cancel()
        if (seconds <= 0) return
        restJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _uiState.value = _uiState.value.copy(restRemainingSeconds = remaining)
                delay(1000)
            }
        }
    }

    private fun stopCollectors() {
        timerJob?.cancel()
        heartRateJob?.cancel()
        locationJob?.cancel()
        restJob?.cancel()
    }

    private fun estimateCalories(mode: String, seconds: Long, avgHr: Double): Double {
        return if (mode == TRACKING_CARDIO) {
            CalorieCalculator.estimateCardioCalories(
                templateId = templateId,
                durationSeconds = seconds,
                distanceMeters = _uiState.value.distanceMeters,
                weightKg = userWeightKg
            )
        } else {
            CalorieCalculator.estimateStrengthCalories(
                templateId = templateId,
                durationSeconds = seconds,
                completedSets = _uiState.value.completedSets,
                totalReps = _uiState.value.totalReps,
                weightKg = userWeightKg
            )
        }
    }

    private fun distanceMeters(points: List<RoutePointDraft>): Double {
        if (points.size < 2) return 0.0
        return points.zipWithNext().sumOf { (a, b) ->
            val result = FloatArray(1)
            android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
            result[0].toDouble()
        }
    }

    override fun onCleared() {
        stopCollectors()
    }
}
