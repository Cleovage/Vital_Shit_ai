package com.example.vitaai.ui.screens

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that carries the id of the chat currently being
 * read/written in [ChatScreen]. The history screen reads it to
 * highlight the active row. The chat screen updates it whenever the
 * user starts or opens a new conversation.
 */
val LocalActiveChatId = compositionLocalOf<String?> { null }
