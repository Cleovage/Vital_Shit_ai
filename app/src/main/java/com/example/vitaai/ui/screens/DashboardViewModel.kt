package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.VitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: VitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val snapshot = repository.getDailySnapshot()
                val insight = repository.getAiInsight(snapshot)
                _uiState.value = DashboardUiState.Success(snapshot, insight)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val snapshot: HealthSnapshot, val insight: String) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}