package com.example.vitaai.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class DailyGoals(
    val stepGoal: Long = 10000,
    val hydrationGoalLiters: Double = 2.5,
    val exerciseMinutesGoal: Double = 30.0,
    val caloriesBurnGoal: Double = 500.0
)

data class GoalProgress(
    val stepsProgress: Float,
    val hydrationProgress: Float,
    val exerciseProgress: Float,
    val caloriesBurnProgress: Float,
    val allGoalsMet: Boolean
)

@Singleton
class GoalsRepository @Inject constructor() {
    private val _goals = MutableStateFlow(DailyGoals())
    val goals: StateFlow<DailyGoals> = _goals
    
    // In a real app this would read from SharedPreferences or Room DB
    private val _streakDays = MutableStateFlow(3) 
    val streakDays: StateFlow<Int> = _streakDays

    fun updateGoals(newGoals: DailyGoals) {
        _goals.value = newGoals
    }
    
    fun incrementStreak() {
        _streakDays.value += 1
    }

    fun observeGoalProgress(healthSnapshotFlow: Flow<HealthSnapshot>): Flow<GoalProgress> {
        return goals.combine(healthSnapshotFlow) { goals, snapshot ->
            val stepsProgress = (snapshot.steps.toFloat() / goals.stepGoal).coerceIn(0f, 1f)
            val hydrationProgress = (snapshot.hydrationLiters.toFloat() / goals.hydrationGoalLiters.toFloat()).coerceIn(0f, 1f)
            val exerciseProgress = (snapshot.exerciseMinutes.toFloat() / goals.exerciseMinutesGoal.toFloat()).coerceIn(0f, 1f)
            val caloriesProgress = (snapshot.calories.toFloat() / goals.caloriesBurnGoal.toFloat()).coerceIn(0f, 1f)
            
            val allMet = stepsProgress >= 1f && hydrationProgress >= 1f && exerciseProgress >= 1f
            GoalProgress(
                stepsProgress = stepsProgress,
                hydrationProgress = hydrationProgress,
                exerciseProgress = exerciseProgress,
                caloriesBurnProgress = caloriesProgress,
                allGoalsMet = allMet
            )
        }
    }
}
