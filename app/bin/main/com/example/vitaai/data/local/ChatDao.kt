package com.example.vitaai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // ─── Conversations ────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(chat: ChatConversationEntity)

    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    suspend fun getConversation(id: String): ChatConversationEntity?

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("SELECT * FROM chats WHERE syncedToCloud = 0")
    suspend fun unsyncedConversations(): List<ChatConversationEntity>

    @Query("UPDATE chats SET syncedToCloud = 1 WHERE id = :id")
    suspend fun markConversationSynced(id: String)

    // ─── Messages ─────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestampMillis ASC")
    fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestampMillis ASC")
    suspend fun messagesFor(chatId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE syncedToCloud = 0")
    suspend fun unsyncedMessages(): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET syncedToCloud = 1 WHERE id IN (:ids)")
    suspend fun markMessagesSynced(ids: List<String>)

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForConversation(chatId: String)

    // ─── Atomic helpers ───────────────────────────────────────────────

    @Transaction
    suspend fun upsertConversationAndMessage(chat: ChatConversationEntity, message: ChatMessageEntity) {
        upsertConversation(chat)
        upsertMessage(message)
    }
}
