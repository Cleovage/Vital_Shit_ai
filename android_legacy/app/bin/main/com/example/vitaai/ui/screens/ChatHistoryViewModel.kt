package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.ChatRepository
import com.example.vitaai.data.local.ChatConversationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val conversations: StateFlow<List<ChatConversationEntity>> =
        chatRepository.observeConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(chatId: String) {
        viewModelScope.launch { chatRepository.deleteConversation(chatId) }
    }
}
