package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.HealthConnectManager
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.FirebaseRepository
import com.example.vitaai.data.VitaRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val lastSyncStatus: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val workoutRepository: WorkoutRepository,
    private val firebaseRepository: FirebaseRepository,
    private val vitaRepository: VitaRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        _uiState.value = _uiState.value.copy(
            isLoggedIntoFirebase = auth.currentUser != null
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
            val sessions = workoutRepository.observeRecentSessions(limit = 50).first()
            val workoutCount = sessions.size
            
            // Calculate Level: Base 10 + 2 levels per completed session, capped at level 100
            val calculatedLevel = (10 + workoutCount * 2).coerceIn(10, 100)
            // Progress toward next level is fractional completion of 5 sessions per level
            val levelProgress = (workoutCount % 5) / 5f
            
            val isConnected = healthConnectManager.hasAllPermissions()

            _uiState.value = ProfileUiState(
                weightKg = weight,
                heightMeters = height,
                completedWorkoutsCount = workoutCount,
                level = calculatedLevel,
                levelProgress = levelProgress,
                permissionsGranted = isConnected,
                hasAllRequiredPermissions = isConnected
            )
        }
    }

    fun calibrate(weightLbs: Double, heightInches: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                // Convert Lbs to Kg
                val weightKg = weightLbs / 2.20462
                // Convert Inches to Meters
                val heightMeters = heightInches / 39.3701
                
                healthConnectManager.writeWeight(weightKg)
                healthConnectManager.writeHeight(heightMeters)
                
                loadProfile()
            } catch (e: Exception) {
                // Ignore or log error
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
}
