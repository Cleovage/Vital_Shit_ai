package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthConnectManager
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.FirebaseRepository
import com.example.vitaai.data.ProfileRepository
import com.example.vitaai.data.VitaRepository
import com.example.vitaai.data.GoalsRepository
import com.example.vitaai.data.DailyGoals
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ProfileUiState(
    val weightKg: Double? = null,
    val heightMeters: Double? = null,
    val completedWorkoutsCount: Int = 0,
    val level: Int = 10,
    val levelProgress: Float = 0f,
    val permissionsGranted: Boolean = false,
    val isSaving: Boolean = false,
    val hasAllRequiredPermissions: Boolean = false,
    val isLoggedIntoFirebase: Boolean = false,
    val lastSyncStatus: String? = null,
    val displayName: String = "",

    // Upgraded somatic biometrics
    val age: Int = 25,
    val gender: String = "male",
    val activityLevel: String = "light",
    val stepGoal: Long = 10000,
    val hydrationGoalLiters: Double = 2.5,
    val exerciseMinutesGoal: Double = 30.0,
    val caloriesBurnGoal: Double = 500.0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val workoutRepository: WorkoutRepository,
    private val firebaseRepository: FirebaseRepository,
    private val profileRepository: ProfileRepository,
    private val vitaRepository: VitaRepository,
    private val goalsRepository: GoalsRepository,
    private val auth: FirebaseAuth?,
    private val vitaDatabase: com.example.vitaai.data.local.VitaDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        checkLoginStatus()
        // Pull the display name from auth + cloud
        viewModelScope.launch {
            val authName = auth?.currentUser?.displayName.orEmpty()
            val cloud = profileRepository.loadFromCloud()
            val resolved = authName.ifBlank { cloud?.displayName.orEmpty() }
            _uiState.value = _uiState.value.copy(displayName = resolved)
        }
    }

    fun setDisplayName(newName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                profileRepository.updateDisplayName(newName)
                val resolved = auth?.currentUser?.displayName
                    .orEmpty()
                    .ifBlank { _uiState.value.displayName }
                _uiState.value = _uiState.value.copy(
                    displayName = resolved,
                    isSaving = false,
                    lastSyncStatus = "Name updated"
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    lastSyncStatus = "Name update failed: ${t.localizedMessage}"
                )
            }
        }
    }

    private fun checkLoginStatus() {
        _uiState.value = _uiState.value.copy(
            isLoggedIntoFirebase = auth?.currentUser != null
        )
    }

    fun syncToCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, lastSyncStatus = "Syncing...")
            try {
                val snapshot = vitaRepository.getDailySnapshot()
                firebaseRepository.saveHealthSnapshot(snapshot)
                _uiState.value = _uiState.value.copy(lastSyncStatus = "Successfully Synced")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(lastSyncStatus = "Error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            val weight = healthConnectManager.readLatestWeight()
            val height = healthConnectManager.readLatestHeight()

            // Get workout sessions from Room + Health Connect merged stream
            val sessions = workoutRepository.observeRecentSessions(limit = 1000).first()
            val workoutCount = sessions.size
            
            // Calculate Level: Base 10 + 2 levels per completed session, capped at level 100
            val calculatedLevel = (10 + workoutCount * 2).coerceIn(10, 100)
            // Progress toward next level is fractional completion of 5 sessions per level
            val levelProgress = (workoutCount % 5) / 5f
            
            val isConnected = healthConnectManager.hasAllPermissions()
            val currentGoals = goalsRepository.goals.value

            _uiState.value = ProfileUiState(
                weightKg = weight,
                heightMeters = height,
                completedWorkoutsCount = workoutCount,
                level = calculatedLevel,
                levelProgress = levelProgress,
                permissionsGranted = isConnected,
                hasAllRequiredPermissions = isConnected,
                isLoggedIntoFirebase = auth?.currentUser != null,
                displayName = auth?.currentUser?.displayName
                    .orEmpty()
                    .ifBlank { _uiState.value.displayName },
                age = goalsRepository.age.value,
                gender = goalsRepository.gender.value,
                activityLevel = goalsRepository.activityLevel.value,
                stepGoal = currentGoals.stepGoal,
                hydrationGoalLiters = currentGoals.hydrationGoalLiters,
                exerciseMinutesGoal = currentGoals.exerciseMinutesGoal,
                caloriesBurnGoal = currentGoals.caloriesBurnGoal
            )
        }
    }

    fun calibrate(
        weightLbs: Double,
        heightInches: Double,
        age: Int,
        gender: String,
        activityLevel: String,
        stepGoal: Long,
        hydrationGoalLiters: Double,
        exerciseMinutesGoal: Double,
        caloriesBurnGoal: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                // Convert Lbs to Kg
                val weightKg = weightLbs / 2.20462
                // Convert Inches to Meters
                val heightMeters = heightInches / 39.3701
                
                healthConnectManager.writeWeight(weightKg)
                healthConnectManager.writeHeight(heightMeters)
                
                goalsRepository.updateAge(age)
                goalsRepository.updateGender(gender)
                goalsRepository.updateActivityLevel(activityLevel)
                goalsRepository.updateGoals(
                    DailyGoals(
                        stepGoal = stepGoal,
                        hydrationGoalLiters = hydrationGoalLiters,
                        exerciseMinutesGoal = exerciseMinutesGoal,
                        caloriesBurnGoal = caloriesBurnGoal
                    )
                )
                
                loadProfile()
            } catch (e: Exception) {
                // Ignore or log error
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                // Wipe local Room tables
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    vitaDatabase.clearAllTables()
                }
                // Sign out of Firebase
                auth?.signOut()
                // Reset Goals
                goalsRepository.updateAge(25)
                goalsRepository.updateGender("Male")
                goalsRepository.updateActivityLevel("Active")
                goalsRepository.updateGoals(
                    DailyGoals(
                        stepGoal = 10000L,
                        hydrationGoalLiters = 2.5,
                        exerciseMinutesGoal = 30.0,
                        caloriesBurnGoal = 500.0
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(lastSyncStatus = "Logout error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun linkAccount(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth?.currentUser ?: return
        if (!user.isAnonymous) {
            onError("Current user is not a guest.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email.trim(), password)
                user.linkWithCredential(credential).await()
                
                // On success, upload local Room data to the newly linked cloud account
                firebaseRepository.uploadLocalData(vitaDatabase.vitaDao())
                
                _uiState.value = _uiState.value.copy(isLoggedIntoFirebase = true)
                onSuccess()
            } catch (t: Throwable) {
                onError(t.localizedMessage ?: "Linking failed.")
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun getRequestedPermissions() = healthConnectManager.permissions
}
