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
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _liveSteps = MutableStateFlow(0L)
    val liveSteps: StateFlow<Long> = _liveSteps

    private var snapshotJob: Job? = null
    private var stepBaseline: Float? = null

    init {
        observeLiveSensorSteps()
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
                    .combine(moodRepository.currentMood) { snapshot, mood ->
                        val insight = repository.getAiInsight(snapshot)
                        DashboardUiState.Success(snapshot, insight, mood) as DashboardUiState
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
        val mood: MoodEntry?
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
