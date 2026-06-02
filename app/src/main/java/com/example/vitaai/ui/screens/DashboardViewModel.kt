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
    private val healthConnectManager: HealthConnectManager,
    private val nutritionRepository: NutritionRepository,
    private val workoutRepository: WorkoutRepository,
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private var snapshotJob: Job? = null

    init {
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
                combine(
                    repository.healthSnapshotFlow,
                    nutritionRepository.observeTodaySummary(),
                    workoutRepository.observeRecentSessions(limit = 3),
                    goalsRepository.observeGoalProgress(repository.healthSnapshotFlow),
                    goalsRepository.streakDays
                ) { snapshot, nutrition, workouts, goalsProgress, streakDays ->
                    val insight = repository.getAiInsight(snapshot)
                    DashboardUiState.Success(
                        snapshot = snapshot,
                        insight = insight,
                        nutrition = nutrition,
                        recentWorkouts = workouts,
                        goalProgress = goalsProgress,
                        streakDays = streakDays
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
            val ml = oz * 29.5735
            val waterItem = nutritionRepository.drinkCatalog.firstOrNull { it.name == "Water" }
                ?: DrinkCatalogItem("Water", "Water", 250.0, 1.0, 0.0, 0.0, 0.0, "Direct hydration with no calories.")
            nutritionRepository.addDrink(waterItem, ml)
        }
    }


}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object PermissionsRequired : DashboardUiState()
    data class Success(
        val snapshot: HealthSnapshot, 
        val insight: String,
        val nutrition: NutritionSummary,
        val recentWorkouts: List<com.example.vitaai.data.local.WorkoutSessionEntity>,
        val goalProgress: GoalProgress,
        val streakDays: Int
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
