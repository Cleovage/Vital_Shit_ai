package com.example.vitaai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

private val Context.goalsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vita_goals"
)

private object Keys {
    val AGE = intPreferencesKey("user_age")
    val GENDER = stringPreferencesKey("user_gender")
    val ACTIVITY = stringPreferencesKey("user_activity_level")
    val STREAK = intPreferencesKey("user_streak")
    val STEP_GOAL = longPreferencesKey("goal_steps")
    val HYDRATION_GOAL = doublePreferencesKey("goal_hydration")
    val EXERCISE_GOAL = doublePreferencesKey("goal_exercise")
    val BURN_GOAL = doublePreferencesKey("goal_calories_burn")
}

/**
 * Goals + biometrics + streaks, persisted via Jetpack DataStore
 * (preferences flavor) for Compose-native reactive Flows. One-shot
 * reads are still synchronous via `.first()` for the few callers
 * (e.g. `ProfileViewModel.loadProfile`) that need a snapshot during init.
 *
 * Backward-compat: if the legacy `vita_goals_prefs` SharedPreferences
 * exists, the values are copied into DataStore on the first call and
 * the legacy file is cleared so we don't keep two sources of truth.
 */
@Singleton
class GoalsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store: DataStore<Preferences> = context.goalsDataStore
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Biometric metadata
    val age: StateFlow<Int> = store.data
        .map { it[Keys.AGE] ?: 25 }
        .stateIn(ioScope, SharingStarted.Eagerly, 25)

    val gender: StateFlow<String> = store.data
        .map { it[Keys.GENDER] ?: "male" }
        .stateIn(ioScope, SharingStarted.Eagerly, "male")

    val activityLevel: StateFlow<String> = store.data
        .map { it[Keys.ACTIVITY] ?: "light" }
        .stateIn(ioScope, SharingStarted.Eagerly, "light")

    // Daily goals
    val goals: StateFlow<DailyGoals> = store.data
        .map { p ->
            DailyGoals(
                stepGoal = p[Keys.STEP_GOAL] ?: 10000L,
                hydrationGoalLiters = p[Keys.HYDRATION_GOAL] ?: 2.5,
                exerciseMinutesGoal = p[Keys.EXERCISE_GOAL] ?: 30.0,
                caloriesBurnGoal = p[Keys.BURN_GOAL] ?: 500.0
            )
        }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            // Seed with defaults so the first composition never blocks.
            DailyGoals()
        )

    // Streaks
    val streakDays: StateFlow<Int> = store.data
        .map { it[Keys.STREAK] ?: 3 }
        .stateIn(ioScope, SharingStarted.Eagerly, 3)

    init {
        // Best-effort: copy any legacy SharedPreferences values into DataStore
        // and clear the legacy file. Idempotent — running on a clean install
        // is a no-op.
        ioScope.launch { migrateFromLegacySharedPrefs() }
    }

    private suspend fun migrateFromLegacySharedPrefs() {
        val legacy = context.getSharedPreferences("vita_goals_prefs", Context.MODE_PRIVATE)
        if (legacy.all.isEmpty()) return
        store.edit { p ->
            if (p[Keys.AGE] == null) p[Keys.AGE] = legacy.getInt("user_age", 25)
            if (p[Keys.GENDER] == null) p[Keys.GENDER] = legacy.getString("user_gender", "male") ?: "male"
            if (p[Keys.ACTIVITY] == null) p[Keys.ACTIVITY] = legacy.getString("user_activity_level", "light") ?: "light"
            if (p[Keys.STREAK] == null) p[Keys.STREAK] = legacy.getInt("user_streak", 3)
            if (p[Keys.STEP_GOAL] == null) p[Keys.STEP_GOAL] = legacy.getLong("goal_steps", 10000L)
            if (p[Keys.HYDRATION_GOAL] == null) p[Keys.HYDRATION_GOAL] = legacy.getFloat("goal_hydration", 2.5f).toDouble()
            if (p[Keys.EXERCISE_GOAL] == null) p[Keys.EXERCISE_GOAL] = legacy.getFloat("goal_exercise", 30f).toDouble()
            if (p[Keys.BURN_GOAL] == null) p[Keys.BURN_GOAL] = legacy.getFloat("goal_calories_burn", 500f).toDouble()
        }
        legacy.edit().clear().apply()
    }

    /** Synchronous snapshot for callers that need it during init. */
    suspend fun currentGoals(): DailyGoals = goals.first()

    fun updateGoals(newGoals: DailyGoals) {
        ioScope.launch {
            store.edit { p ->
                p[Keys.STEP_GOAL] = newGoals.stepGoal
                p[Keys.HYDRATION_GOAL] = newGoals.hydrationGoalLiters
                p[Keys.EXERCISE_GOAL] = newGoals.exerciseMinutesGoal
                p[Keys.BURN_GOAL] = newGoals.caloriesBurnGoal
            }
        }
    }

    fun updateAge(newAge: Int) {
        ioScope.launch { store.edit { it[Keys.AGE] = newAge } }
    }

    fun updateGender(newGender: String) {
        ioScope.launch { store.edit { it[Keys.GENDER] = newGender } }
    }

    fun updateActivityLevel(newLevel: String) {
        ioScope.launch { store.edit { it[Keys.ACTIVITY] = newLevel } }
    }

    fun incrementStreak() {
        ioScope.launch { store.edit { it[Keys.STREAK] = (streakDays.value + 1).coerceAtLeast(0) } }
    }

    fun observeGoalProgress(healthSnapshotFlow: Flow<HealthSnapshot>): Flow<GoalProgress> {
        return goals.combine(healthSnapshotFlow) { g, snapshot ->
            val stepsProgress = (snapshot.steps.toFloat() / g.stepGoal).coerceIn(0f, 1f)
            val hydrationProgress = (snapshot.hydrationLiters.toFloat() / g.hydrationGoalLiters.toFloat()).coerceIn(0f, 1f)
            val exerciseProgress = (snapshot.exerciseMinutes.toFloat() / g.exerciseMinutesGoal.toFloat()).coerceIn(0f, 1f)
            val caloriesProgress = (snapshot.calories.toFloat() / g.caloriesBurnGoal.toFloat()).coerceIn(0f, 1f)
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
