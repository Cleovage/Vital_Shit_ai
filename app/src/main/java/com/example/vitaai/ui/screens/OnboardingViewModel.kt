package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val displayName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val canSkip: Boolean = true
)

/**
 * Drives the first-run name-picker. Defaults the field to whatever the
 * FirebaseAuth account already has (e.g. the email local-part for a
 * password signup) and writes the chosen name through [ProfileRepository]
 * which hits both FirebaseAuth `updateProfile` and Firestore.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val auth: FirebaseAuth?,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        val current = auth?.currentUser
        val default = when {
            !current?.displayName.isNullOrBlank() -> current!!.displayName!!
            !current?.email.isNullOrBlank() -> current!!.email!!.substringBefore("@")
            else -> ""
        }
        _uiState.value = _uiState.value.copy(displayName = default)
    }

    fun setName(newName: String) {
        _uiState.value = _uiState.value.copy(displayName = newName.take(40), error = null)
    }

    fun confirm(onDone: () -> Unit) {
        val name = _uiState.value.displayName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Pick a name to continue")
            return
        }
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                profileRepository.updateDisplayName(name)
                _uiState.value = _uiState.value.copy(isSaving = false)
                onDone()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = t.localizedMessage ?: "Could not save your name"
                )
            }
        }
    }

    fun skip(onDone: () -> Unit) {
        // Anonymous users should not be allowed to skip — they need a name
        // to make the rest of the app feel personal. For others, allow skip.
        if (!_uiState.value.canSkip) return
        onDone()
    }
}
