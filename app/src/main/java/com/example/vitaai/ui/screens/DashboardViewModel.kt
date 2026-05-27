package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        loadData()
        observeLiveSensors()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                if (healthConnectManager.hasAllPermissions()) {
                    val snapshot = repository.getDailySnapshot()
                    val insight = repository.getAiInsight(snapshot)
                    val mood = moodRepository.currentMood.value
                    _uiState.value = DashboardUiState.Success(snapshot, insight, mood)
                } else {
                    _uiState.value = DashboardUiState.PermissionsRequired
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeLiveSensors() {
        sensorManager.getStepCountFlow()
            .onEach { steps -> _liveSteps.value = steps.toLong() }
            .launchIn(viewModelScope)
    }

    fun logWater(oz: Int) {
        viewModelScope.launch {
            repository.logWater(oz * 0.0295735) // convert oz to liters
            loadData()
        }
    }

    fun recordMood(score: Int) {
        moodRepository.recordMood(score)
        loadData()
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
