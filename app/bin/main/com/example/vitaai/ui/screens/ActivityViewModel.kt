package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.data.local.WorkoutTemplateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val uiState: StateFlow<ActivityUiState> = _uiState

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _uiState.value = ActivityUiState.Loading
            runCatching { workoutRepository.seedDefaultTemplatesIfNeeded() }
                .onFailure {
                    _uiState.value = ActivityUiState.Error(it.message ?: "Unable to seed workouts")
                    return@launch
                }

            combine(
                workoutRepository.observeTemplates(),
                workoutRepository.observeRecentSessions()
            ) { templates, sessions ->
                ActivityUiState.Success(templates, sessions) as ActivityUiState
            }.collect { _uiState.value = it }
        }
    }

    fun saveManualSession(
        template: WorkoutTemplateEntity,
        durationMinutes: Int,
        calories: Double,
        sets: Int,
        reps: Int,
        distanceMeters: Double
    ) {
        viewModelScope.launch {
            val endTime = java.time.Instant.now()
            val startTime = endTime.minusSeconds(durationMinutes * 60L)
            
            val mockSets = List(sets) { i ->
                com.example.vitaai.data.WorkoutSetDraft(
                    exerciseName = template.name,
                    setNumber = i + 1,
                    reps = reps / sets.coerceAtLeast(1),
                    weightKg = 0.0,
                    durationSeconds = 60,
                    timestampMillis = startTime.toEpochMilli() + (i * 60000)
                )
            }
            
            // Mock route points for distance if needed (just start/end to get the distance logged if we handled that manually, but WorkoutRepository relies on distance being calculated from RoutePoints if we don't change it. Wait, WorkoutRepository calculates distance from route. I should change WorkoutRepository to accept distance directly or I can pass an empty route and modify WorkoutRepository slightly. Let's pass empty route and modify WorkoutRepository later if needed, or just let distance be 0 for manual right now, but manual distance is good for cardio.)
            
            workoutRepository.saveWorkoutSession(
                template = template,
                startTime = startTime,
                endTime = endTime,
                totalReps = reps,
                sets = mockSets,
                route = emptyList(), // Route will be empty, meaning 0 calculated distance. I will need to update WorkoutRepository to allow overriding distance.
                avgHeartRate = 0.0,
                calories = calories,
                notes = "Manual Entry",
                manualDistanceMeters = distanceMeters
            )
        }
    }
}

sealed class ActivityUiState {
    object Loading : ActivityUiState()
    data class Success(
        val templates: List<WorkoutTemplateEntity>,
        val sessions: List<WorkoutSessionEntity>
    ) : ActivityUiState()
    data class Error(val message: String) : ActivityUiState()
}
