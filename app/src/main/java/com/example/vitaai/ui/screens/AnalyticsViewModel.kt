package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthConnectManager
import com.example.vitaai.data.VitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val repository: VitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState
    
    private val _timeframe = MutableStateFlow(7) // default 7 days
    val timeframe: StateFlow<Int> = _timeframe

    init {
        loadTrends()
    }
    
    fun setTimeframe(days: Int) {
        _timeframe.value = days
        loadTrends()
    }

    fun loadTrends() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            try {
                val hourlySteps = repository.getHistoricalSteps(_timeframe.value)
                
                // Aggregating by day for simplicity in the chart
                val dailyTrends = hourlySteps.entries
                    .groupBy { it.key.truncatedTo(ChronoUnit.DAYS) }
                    .mapValues { it.value.sumOf { entry -> entry.value } }
                    .toSortedMap()

                _uiState.value = AnalyticsUiState.Success(dailyTrends)
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val dailySteps: Map<Instant, Long>) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}
