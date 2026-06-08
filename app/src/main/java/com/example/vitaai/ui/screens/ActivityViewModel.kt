package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.CalorieCalculator
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.data.local.WorkoutTemplateEntity
import com.example.vitaai.data.local.WorkoutSessionWithSets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.health.connect.client.records.ExerciseSessionRecord

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
                workoutRepository.observeRecentSessionsWithSets()
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
            
            val userWeight = workoutRepository.getUserWeight() ?: 75.0
            val computedCalories = if (calories > 0.0) calories else {
                if (template.trackingMode == com.example.vitaai.data.TRACKING_CARDIO) {
                    CalorieCalculator.estimateCardioCalories(
                        templateId = template.id,
                        durationSeconds = durationMinutes * 60L,
                        distanceMeters = distanceMeters,
                        weightKg = userWeight
                    )
                } else {
                    CalorieCalculator.estimateStrengthCalories(
                        templateId = template.id,
                        durationSeconds = durationMinutes * 60L,
                        completedSets = sets,
                        totalReps = reps,
                        weightKg = userWeight
                    )
                }
            }
            
            workoutRepository.saveWorkoutSession(
                template = template,
                startTime = startTime,
                endTime = endTime,
                totalReps = reps,
                sets = mockSets,
                route = emptyList(),
                avgHeartRate = 0.0,
                calories = computedCalories,
                notes = "Manual Entry",
                manualDistanceMeters = distanceMeters
            )
        }
    }

    fun createTemplate(
        name: String,
        description: String,
        trackingMode: String,
        defaultRestSeconds: Int,
        gpsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val category = when (trackingMode) {
                com.example.vitaai.data.TRACKING_CARDIO -> "Cardio"
                com.example.vitaai.data.TRACKING_STRENGTH -> "Strength"
                com.example.vitaai.data.TRACKING_MOBILITY -> "Mobility"
                else -> "Bodyweight"
            }
            val exerciseType = when (trackingMode) {
                com.example.vitaai.data.TRACKING_CARDIO -> ExerciseSessionRecord.EXERCISE_TYPE_RUNNING
                com.example.vitaai.data.TRACKING_STRENGTH -> ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
                com.example.vitaai.data.TRACKING_MOBILITY -> ExerciseSessionRecord.EXERCISE_TYPE_YOGA
                else -> ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS
            }
            val primaryMetric = when (trackingMode) {
                com.example.vitaai.data.TRACKING_CARDIO -> "pace"
                com.example.vitaai.data.TRACKING_STRENGTH -> "sets"
                com.example.vitaai.data.TRACKING_MOBILITY -> "time"
                else -> "reps"
            }
            
            val newTemplate = WorkoutTemplateEntity(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                category = category,
                exerciseType = exerciseType,
                trackingMode = trackingMode,
                gpsEnabled = gpsEnabled,
                defaultRestSeconds = defaultRestSeconds,
                description = description,
                primaryMetric = primaryMetric
            )
            workoutRepository.saveTemplate(newTemplate)
        }
    }

    fun getExerciseSets(sessionId: Long): kotlinx.coroutines.flow.Flow<List<com.example.vitaai.data.local.ExerciseSetEntity>> {
        return workoutRepository.observeExerciseSets(sessionId)
    }
}

sealed class ActivityUiState {
    object Loading : ActivityUiState()
    data class Success(
        val templates: List<WorkoutTemplateEntity>,
        val sessions: List<WorkoutSessionWithSets>
    ) : ActivityUiState()
    data class Error(val message: String) : ActivityUiState()
}
