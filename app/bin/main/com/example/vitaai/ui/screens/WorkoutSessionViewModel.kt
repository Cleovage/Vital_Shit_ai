package com.example.vitaai.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.LiveSensorManager
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
    val saving: Boolean = false,
    val savedSessionId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val sensorManager: LiveSensorManager,
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

    init {
        viewModelScope.launch {
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
    }

    fun addRep() {
        val nextReps = _uiState.value.currentReps + 1
        _uiState.value = _uiState.value.copy(
            currentReps = nextReps,
            totalReps = _uiState.value.totalReps + 1
        )
    }

    fun completeSet(weightKg: Double = 0.0) {
        val template = _uiState.value.template ?: return
        val reps = _uiState.value.currentReps.coerceAtLeast(if (template.trackingMode == TRACKING_CARDIO) 0 else 1)
        val setNumber = sets.size + 1
        sets += WorkoutSetDraft(
            exerciseName = template.name,
            setNumber = setNumber,
            reps = reps,
            weightKg = weightKg,
            durationSeconds = _uiState.value.elapsedSeconds
        )
        _uiState.value = _uiState.value.copy(
            currentReps = 0,
            completedSets = setNumber
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

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.running) {
                    _uiState.value = _uiState.value.copy(elapsedSeconds = _uiState.value.elapsedSeconds + 1)
                }
            }
        }
    }

    private fun startHeartRate() {
        if (heartRateJob?.isActive == true) return
        heartRateJob = viewModelScope.launch {
            sensorManager.getHeartRateFlow().collect { hr ->
                val value = hr.roundToInt()
                heartRates += value
                _uiState.value = _uiState.value.copy(liveHeartRate = value)
            }
        }
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
                _uiState.value = _uiState.value.copy(
                    routePointCount = route.size,
                    distanceMeters = distanceMeters(route)
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
        val minutes = seconds / 60.0
        val base = when (mode) {
            TRACKING_CARDIO -> 8.0
            else -> 5.5
        }
        val heartBoost = if (avgHr > 110) 1.15 else 1.0
        return minutes * base * heartBoost
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
