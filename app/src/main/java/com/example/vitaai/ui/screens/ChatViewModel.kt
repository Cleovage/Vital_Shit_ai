package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.ChatRepository
import com.example.vitaai.data.VitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

/**
 * Drives the coach chat. Each call to [sendMessage] either starts a new
 * conversation (if [startNewConversation] hasn't been called) or appends
 * to the active one. Every persisted row has `syncedToCloud = 0` until
 * the next [SyncCoordinator.fullSync] tick.
 *
 * UI can swap to a saved conversation via [openConversation]; that
 * replaces the active message stream and updates the header.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: VitaRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    init {
        // Seed with the welcome bubble only if there is no active chat.
        _messages.value = listOf(
            Message("Hello! I'm VitaAI, your health coach. How can I help you today?", false)
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // First user message in this session -> create a new
            // conversation row in Room and persist the message.
            val chatId = _activeChatId.value ?: run {
                val newId = chatRepository.startNewConversation(text)
                _activeChatId.value = newId
                newId
            }

            _messages.value = _messages.value + Message(text, true)
            chatRepository.appendMessage(chatId, text, isUser = true)

            _isThinking.value = true
            try {
                val aiResponse = runCatching {
                    val snapshot = repository.getDailySnapshot()
                    repository.getChatbotResponse(text, snapshot)
                }.getOrElse { e ->
                    "I'm sorry, I encountered an issue retrieving your health snapshot: ${e.localizedMessage ?: "Unknown error"}"
                }
                _messages.value = _messages.value + Message(aiResponse, false)
                chatRepository.appendMessage(chatId, aiResponse, isUser = false)
            } finally {
                _isThinking.value = false
            }
        }
    }

    /**
     * Switch the active conversation to a saved one. The messages list
     * is replaced with the loaded scrollback so the UI updates
     * immediately. Used by [ChatHistoryScreen] when a row is tapped.
     */
    fun openConversation(chatId: String) {
        viewModelScope.launch {
            val rows = chatRepository.loadConversation(chatId)
            _activeChatId.value = chatId
            _messages.value = rows.map { Message(it.text, it.isUser, it.timestampMillis) }
        }
    }

    /** Reset to the welcome-only state for a fresh "New chat" action. */
    fun startNewConversation() {
        _activeChatId.value = null
        _messages.value = listOf(
            Message("Hello! I'm VitaAI, your health coach. How can I help you today?", false)
        )
    }

    fun deleteConversation(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(chatId)
            if (_activeChatId.value == chatId) {
                startNewConversation()
            }
        }
    }
}
