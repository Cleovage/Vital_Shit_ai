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

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            _messages.value += Message(text, true)
            
            // Fetch health context for the AI
            val snapshot = repository.getDailySnapshot()
            val aiResponse = repository.getAiInsight(snapshot) 
            // In a real app, this would be a full LLM call with the text and snapshot
            
            _messages.value += Message("Based on your data: $aiResponse", false)
        }
    }
}