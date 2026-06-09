package com.example.vitaai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted chat conversation. A conversation is a stream of user/AI
 * messages exchanged in a single sitting. Stored locally in Room and
 * mirrored to Firestore at
 * `users/{uid}/chats/{chatId}/messages/{messageId}`.
 */
@Entity(tableName = "chats")
data class ChatConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int = 0,
    val syncedToCloud: Boolean = false
)

/**
 * A single message in a chat conversation. All messages in a conversation
 * share the same [chatId]. We keep one row per message so the cloud
 * schema (`chats/{chatId}/messages/{messageId}`) is a 1:1 mirror.
 */
@Entity(
    tableName = "chat_messages",
    indices = [androidx.room.Index(value = ["chatId"], name = "index_chat_messages_chatId")]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val text: String,
    val isUser: Boolean,
    val timestampMillis: Long,
    val syncedToCloud: Boolean = false
)
