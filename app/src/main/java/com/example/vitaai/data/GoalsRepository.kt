package com.example.vitaai.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
class GoalsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("vita_goals_prefs", Context.MODE_PRIVATE)

    // Biometric metadata details
    private val _age = MutableStateFlow(prefs.getInt("user_age", 25))
    val age: StateFlow<Int> = _age

    private val _gender = MutableStateFlow(prefs.getString("user_gender", "male") ?: "male")
    val gender: StateFlow<String> = _gender

    private val _activityLevel = MutableStateFlow(prefs.getString("user_activity_level", "light") ?: "light")
    val activityLevel: StateFlow<String> = _activityLevel

    // Daily Goals
    private val _goals = MutableStateFlow(loadGoals())
    val goals: StateFlow<DailyGoals> = _goals
    
    // Streaks
    private val _streakDays = MutableStateFlow(prefs.getInt("user_streak", 3)) 
    val streakDays: StateFlow<Int> = _streakDays

    private fun loadGoals(): DailyGoals {
        val steps = prefs.getLong("goal_steps", 10000L)
        val hydration = prefs.getFloat("goal_hydration", 2.5f).toDouble()
        val exercise = prefs.getFloat("goal_exercise", 30f).toDouble()
        val burn = prefs.getFloat("goal_calories_burn", 500f).toDouble()
        return DailyGoals(steps, hydration, exercise, burn)
    }

    fun updateGoals(newGoals: DailyGoals) {
        _goals.value = newGoals
        prefs.edit()
            .putLong("goal_steps", newGoals.stepGoal)
            .putFloat("goal_hydration", newGoals.hydrationGoalLiters.toFloat())
            .putFloat("goal_exercise", newGoals.exerciseMinutesGoal.toFloat())
            .putFloat("goal_calories_burn", newGoals.caloriesBurnGoal.toFloat())
            .apply()
    }

    fun updateAge(newAge: Int) {
        _age.value = newAge
        prefs.edit().putInt("user_age", newAge).apply()
    }

    fun updateGender(newGender: String) {
        _gender.value = newGender
        prefs.edit().putString("user_gender", newGender).apply()
    }

    fun updateActivityLevel(newLevel: String) {
        _activityLevel.value = newLevel
        prefs.edit().putString("user_activity_level", newLevel).apply()
    }
    
    fun incrementStreak() {
        val next = _streakDays.value + 1
        _streakDays.value = next
        prefs.edit().putInt("user_streak", next).apply()
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
