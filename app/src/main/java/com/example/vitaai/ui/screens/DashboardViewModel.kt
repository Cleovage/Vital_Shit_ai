package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: VitaRepository,
    private val sensorManager: LiveSensorManager,
    private val moodRepository: MoodRepository,
    private val healthConnectManager: HealthConnectManager,
    private val nutritionRepository: NutritionRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _liveSteps = MutableStateFlow(0L)
    val liveSteps: StateFlow<Long> = _liveSteps

    private var snapshotJob: Job? = null
    private var stepBaseline: Float? = null

    init {
        observeLiveSensorSteps()
        viewModelScope.launch { workoutRepository.seedDefaultTemplatesIfNeeded() }
        loadData()
    }

    fun loadData() {
        snapshotJob?.cancel()
        viewModelScope.launch {
            if (!healthConnectManager.hasAllPermissions()) {
                _uiState.value = DashboardUiState.PermissionsRequired
                return@launch
            }

            snapshotJob = launch {
                repository.healthSnapshotFlow
                    .combine(moodRepository.currentMood) { snapshot, mood -> snapshot to mood }
                    .combine(nutritionRepository.observeTodaySummary()) { snapshotAndMood, nutrition ->
                        Triple(snapshotAndMood.first, snapshotAndMood.second, nutrition)
                    }
                    .combine(workoutRepository.observeRecentSessions(limit = 1)) { values, workouts ->
                        val insight = repository.getAiInsight(values.first)
                        DashboardUiState.Success(
                            snapshot = values.first,
                            insight = insight,
                            mood = values.second,
                            nutrition = values.third,
                            recentWorkouts = workouts
                        ) as DashboardUiState
                    }
                    .catch { e ->
                        _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            }
        }
    }

    fun getRequiredPermissions() = healthConnectManager.requiredPermissions

    fun getRequestedPermissions() = healthConnectManager.permissions

    fun onPermissionsResult(grantedPermissions: Set<String>) {
        if (grantedPermissions.containsAll(getRequiredPermissions())) {
            loadData()
        } else {
            _uiState.value = DashboardUiState.PermissionsRequired
        }
    }

    fun logWater(oz: Int) {
        viewModelScope.launch {
            repository.logWater(oz * 0.0295735) // convert oz to liters
        }
    }

    fun recordMood(score: Int) {
        moodRepository.recordMood(score)
    }

    private fun observeLiveSensorSteps() {
        viewModelScope.launch {
            sensorManager.getStepCountFlow().collect { rawValue ->
                if (stepBaseline == null) {
                    stepBaseline = rawValue
                }
                val normalizedSteps = (rawValue - (stepBaseline ?: rawValue)).coerceAtLeast(0f).toLong()
                _liveSteps.value = normalizedSteps
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object PermissionsRequired : DashboardUiState()
    data class Success(
        val snapshot: HealthSnapshot, 
        val insight: String,
        val mood: MoodEntry?,
        val nutrition: NutritionSummary,
        val recentWorkouts: List<com.example.vitaai.data.local.WorkoutSessionEntity>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
