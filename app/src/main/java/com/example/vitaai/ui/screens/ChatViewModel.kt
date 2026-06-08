package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.VitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Message(val text: String, val isUser: Boolean)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: VitaRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(listOf(
        Message("Hello! I'm VitaAI, your health coach. How can I help you today?", false)
    ))
    val messages: StateFlow<List<Message>> = _messages

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _messages.value += Message(text, true)
            _isThinking.value = true

            try {
                // Fetch health context for the AI
                val aiResponse = runCatching {
                    val snapshot = repository.getDailySnapshot()
                    repository.getChatbotResponse(text, snapshot)
                }.getOrElse { e ->
                    "I'm sorry, I encountered an issue retrieving your health snapshot: ${e.localizedMessage ?: "Unknown error"}"
                }

                _messages.value += Message(aiResponse, false)
            } finally {
                _isThinking.value = false
            }
        }
    }
}