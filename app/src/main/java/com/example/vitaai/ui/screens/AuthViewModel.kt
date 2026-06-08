package com.example.vitaai.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.FirebaseRepository
import com.example.vitaai.data.GoalsRepository
import com.example.vitaai.data.DailyGoals
import com.example.vitaai.data.HealthConnectManager
import com.example.vitaai.data.local.VitaDao
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth?,
    private val firebaseRepository: FirebaseRepository,
    private val goalsRepository: GoalsRepository,
    private val healthConnectManager: HealthConnectManager,
    private val vitaDao: VitaDao
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    
    var isRegistering by mutableStateOf(false)
    
    // Calibration Wizard States
    var currentStep by mutableStateOf(0)
    
    // Somatic biometrics
    var age by mutableStateOf("25")
    var gender by mutableStateOf("Male")
    var activityLevel by mutableStateOf("Active")
    var weightLbs by mutableStateOf("160")
    var heightInches by mutableStateOf("68")
    
    // Custom goals
    var stepGoal by mutableStateOf("10000")
    var hydrationGoalLiters by mutableStateOf("2.5")
    var exerciseMinutesGoal by mutableStateOf("30")
    var caloriesBurnGoal by mutableStateOf("500")

    fun login(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            error = "Email and password cannot be empty."
            return
        }
        
        val a = auth
        if (a == null) {
            error = "Authentication service is unavailable."
            return
        }
        
        isLoading = true
        error = null
        
        viewModelScope.launch {
            try {
                a.signInWithEmailAndPassword(email.trim(), password)
                    .await()
                
                // On success, pull data from Firestore
                syncUserDataFromCloud()
                onSuccess()
            } catch (t: Throwable) {
                error = t.localizedMessage ?: "Login failed."
            } finally {
                isLoading = false
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            error = "Email and password cannot be empty."
            return
        }
        if (password != confirmPassword) {
            error = "Passwords do not match."
            return
        }
        
        val a = auth
        if (a == null) {
            error = "Authentication service is unavailable."
            return
        }
        
        isLoading = true
        error = null
        
        viewModelScope.launch {
            try {
                // Check if currently anonymous, then link or create
                val prevUser = a.currentUser
                if (prevUser != null && prevUser.isAnonymous) {
                    // Link credential
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email.trim(), password)
                    prevUser.linkWithCredential(credential).await()
                } else {
                    // Create new user
                    a.createUserWithEmailAndPassword(email.trim(), password).await()
                }

                // Save calibration settings to Firestore and locally
                saveProfileSettingsAndSync()
                onSuccess()
            } catch (t: Throwable) {
                error = t.localizedMessage ?: "Registration failed."
            } finally {
                isLoading = false
            }
        }
    }

    fun loginAnonymously(onSuccess: () -> Unit) {
        val a = auth
        if (a == null) {
            error = "Authentication service is unavailable."
            return
        }
        
        isLoading = true
        error = null
        
        viewModelScope.launch {
            try {
                if (a.currentUser == null) {
                    a.signInAnonymously().await()
                }
                onSuccess()
            } catch (t: Throwable) {
                error = t.localizedMessage ?: "Guest login failed."
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun saveProfileSettingsAndSync() {
        val ageVal = age.toIntOrNull() ?: 25
        val genderVal = gender
        val activityVal = activityLevel
        val stepsVal = stepGoal.toLongOrNull() ?: 10000L
        val waterVal = hydrationGoalLiters.toDoubleOrNull() ?: 2.5
        val exerciseVal = exerciseMinutesGoal.toDoubleOrNull() ?: 30.0
        val burnVal = caloriesBurnGoal.toDoubleOrNull() ?: 500.0

        val weightVal = weightLbs.toDoubleOrNull() ?: 160.0
        val heightVal = heightInches.toDoubleOrNull() ?: 68.0

        // Convert locally
        val weightKg = weightVal / 2.20462
        val heightMeters = heightVal / 39.3701

        runCatching {
            healthConnectManager.writeWeight(weightKg)
            healthConnectManager.writeHeight(heightMeters)
        }

        goalsRepository.updateAge(ageVal)
        goalsRepository.updateGender(genderVal)
        goalsRepository.updateActivityLevel(activityVal)
        goalsRepository.updateGoals(
            DailyGoals(
                stepGoal = stepsVal,
                hydrationGoalLiters = waterVal,
                exerciseMinutesGoal = exerciseVal,
                caloriesBurnGoal = burnVal
            )
        )

        // Upload calibration to Firestore
        firebaseRepository.saveProfileGoals(
            age = ageVal,
            gender = genderVal,
            activityLevel = activityVal,
            stepGoal = stepsVal,
            hydrationGoalLiters = waterVal,
            exerciseMinutesGoal = exerciseVal,
            caloriesBurnGoal = burnVal
        )

        // Upload any existing local Room data
        firebaseRepository.uploadLocalData(vitaDao)
    }

    private suspend fun syncUserDataFromCloud() {
        // 1. Download database tables
        firebaseRepository.downloadCloudData(vitaDao)
        
        // 2. Download profile goals
        val profileData = firebaseRepository.getProfileData() ?: return
        
        val ageVal = (profileData["age"] as? Long)?.toInt() ?: 25
        val genderVal = (profileData["gender"] as? String) ?: "Male"
        val activityVal = (profileData["activityLevel"] as? String) ?: "Active"
        val stepsVal = (profileData["stepGoal"] as? Long) ?: 10000L
        val waterVal = (profileData["hydrationGoalLiters"] as? Double) ?: 2.5
        val exerciseVal = (profileData["exerciseMinutesGoal"] as? Double) ?: 30.0
        val burnVal = (profileData["caloriesBurnGoal"] as? Double) ?: 500.0

        goalsRepository.updateAge(ageVal)
        goalsRepository.updateGender(genderVal)
        goalsRepository.updateActivityLevel(activityVal)
        goalsRepository.updateGoals(
            DailyGoals(
                stepGoal = stepsVal,
                hydrationGoalLiters = waterVal,
                exerciseMinutesGoal = exerciseVal,
                caloriesBurnGoal = burnVal
            )
        )
    }

    fun signInWithGoogleCredential(idToken: String, onSuccess: () -> Unit) {
        val a = auth
        if (a == null) {
            error = "Authentication service is unavailable."
            return
        }

        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                a.signInWithCredential(credential).await()
                
                // Pull settings/data from Firestore
                syncUserDataFromCloud()
                onSuccess()
            } catch (t: Throwable) {
                error = t.localizedMessage ?: "Google Sign-In failed."
            } finally {
                isLoading = false
            }
        }
    }

    fun signInWithApple(activity: android.app.Activity, onSuccess: () -> Unit) {
        val a = auth
        if (a == null) {
            error = "Authentication service is unavailable."
            return
        }

        isLoading = true
        error = null

        val provider = com.google.firebase.auth.OAuthProvider.newBuilder("apple.com")
        provider.scopes = listOf("email", "name")

        a.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener {
                viewModelScope.launch {
                    try {
                        syncUserDataFromCloud()
                        onSuccess()
                    } catch (t: Throwable) {
                        error = t.localizedMessage ?: "Syncing failed after Apple Sign-In."
                    } finally {
                        isLoading = false
                    }
                }
            }
            .addOnFailureListener { e ->
                error = e.localizedMessage ?: "Apple Sign-In failed."
                isLoading = false
            }
    }
}
