package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.MeditationRepository
import com.example.vitaai.data.MeditationSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MeditationViewModel @Inject constructor(
    private val repository: MeditationRepository
) : ViewModel() {

    val sessions: StateFlow<List<MeditationSession>> = repository.sessions
    val currentlyPlaying: StateFlow<MeditationSession?> = repository.currentlyPlaying
    val isPlaying: StateFlow<Boolean> = repository.isPlaying

    fun play(session: MeditationSession) {
        repository.play(session)
    }

    fun togglePlayback() {
        repository.togglePlayback()
    }

    fun stop() {
        repository.stop()
    }

    fun refresh() {
        viewModelScope.launch {
            // simulate small delay to feel like a refresh
            kotlinx.coroutines.delay(400)
            repository.shuffle()
        }
    }
}
