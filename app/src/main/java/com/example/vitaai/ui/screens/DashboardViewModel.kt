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
    private val moodRepository: MoodRepository,
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
                repository.healthSnapshotFlow
                    .combine(moodRepository.currentMood) { snapshot, mood -> snapshot to mood }
                    .combine(nutritionRepository.observeTodaySummary()) { snapshotAndMood, nutrition ->
                        val (snapshot, mood) = snapshotAndMood
                        Triple(snapshot, mood, nutrition)
                    }
                    .combine(workoutRepository.observeRecentSessions(limit = 1)) { triple, workouts ->
                        val (snapshot, mood, nutrition) = triple
                        snapshot to Triple(mood, nutrition, workouts)
                    }
                    .combine(goalsRepository.observeGoalProgress(repository.healthSnapshotFlow)) { snapshotAndOthers, goalsProgress ->
                        val (snapshot, others) = snapshotAndOthers
                        val (mood, nutrition, workouts) = others
                        
                        val insight = repository.getAiInsight(snapshot)
                        DashboardUiState.Success(
                            snapshot = snapshot,
                            insight = insight,
                            mood = mood,
                            nutrition = nutrition,
                            recentWorkouts = workouts,
                            goalProgress = goalsProgress,
                            streakDays = goalsRepository.streakDays.value
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
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object PermissionsRequired : DashboardUiState()
    data class Success(
        val snapshot: HealthSnapshot, 
        val insight: String,
        val mood: MoodEntry?,
        val nutrition: NutritionSummary,
        val recentWorkouts: List<com.example.vitaai.data.local.WorkoutSessionEntity>,
        val goalProgress: GoalProgress,
        val streakDays: Int
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
