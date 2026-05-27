package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthConnectManager
import androidx.health.connect.client.records.ExerciseSessionRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val uiState: StateFlow<ActivityUiState> = _uiState

    init {
        loadActivities()
    }

    fun loadActivities() {
        viewModelScope.launch {
            _uiState.value = ActivityUiState.Loading
            try {
                val now = Instant.now()
                val startTime = now.minus(30, ChronoUnit.DAYS)
                val sessions = healthConnectManager.readExerciseSessions(startTime, now)
                _uiState.value = ActivityUiState.Success(sessions)
            } catch (e: Exception) {
                _uiState.value = ActivityUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ActivityUiState {
    object Loading : ActivityUiState()
    data class Success(val sessions: List<ExerciseSessionRecord>) : ActivityUiState()
    data class Error(val message: String) : ActivityUiState()
}
