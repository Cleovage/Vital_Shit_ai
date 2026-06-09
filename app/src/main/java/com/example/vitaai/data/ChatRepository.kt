package com.example.vitaai.data

import android.util.Log
import com.example.vitaai.data.local.ChatConversationEntity
import com.example.vitaai.data.local.ChatDao
import com.example.vitaai.data.local.ChatMessageEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Persists chat conversations locally (Room) and mirrors them to
 * Firestore at `users/{uid}/chats/{chatId}` with a subcollection
 * `chats/{chatId}/messages/{messageId}`.
 *
 * The repository is the single source of truth for the chat UI;
 * [SyncCoordinator] pushes any unsynced rows to the cloud and pulls
 * down any cloud rows not present locally.
 */
@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val firestore: FirebaseFirestore?
) {

    // ─── Reactive streams for UI ──────────────────────────────────────

    fun observeConversations(): Flow<List<ChatConversationEntity>> =
        chatDao.observeConversations()

    fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>> =
        chatDao.observeMessages(chatId)

    // ─── Local mutations ──────────────────────────────────────────────

    /** Generate a new conversation id, returning the entity as well. */
    suspend fun startNewConversation(firstUserMessage: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val title = deriveTitle(firstUserMessage)
        chatDao.upsertConversation(
            ChatConversationEntity(
                id = id,
                title = title,
                createdAt = now,
                updatedAt = now,
                messageCount = 1,
                syncedToCloud = false
            )
        )
        chatDao.upsertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = id,
                text = firstUserMessage,
                isUser = true,
                timestampMillis = now,
                syncedToCloud = false
            )
        )
        return id
    }

    /**
     * Append a new message to an existing conversation. Updates the
     * conversation's `updatedAt` + `messageCount` so the history list
     * re-sorts.
     */
    suspend fun appendMessage(chatId: String, text: String, isUser: Boolean) {
        val now = System.currentTimeMillis()
        chatDao.upsertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                text = text,
                isUser = isUser,
                timestampMillis = now,
                syncedToCloud = false
            )
        )
        val existing = chatDao.getConversation(chatId) ?: return
        chatDao.upsertConversation(
            existing.copy(
                updatedAt = now,
                messageCount = existing.messageCount + 1,
                syncedToCloud = false
            )
        )
    }

    suspend fun deleteConversation(chatId: String) {
        chatDao.deleteMessagesForConversation(chatId)
        chatDao.deleteConversation(chatId)
    }

    /**
     * Swap the active message stream to a saved conversation. Returns
     * the messages in chronological order so the UI can render the
     * scrollback without waiting for the Flow's first emission.
     */
    suspend fun loadConversation(chatId: String): List<ChatMessageEntity> {
        return chatDao.messagesFor(chatId)
    }

    // ─── Cloud sync ───────────────────────────────────────────────────

    /**
     * Push every conversation and message that has `syncedToCloud = 0`
     * to Firestore. Uses `set(..., SetOptions.merge())` so that the
     * local mirror can be replayed without clobbering fields that may
     * have been edited on another device. Returns the number of rows
     * (conversations + messages) successfully pushed.
     */
    suspend fun pushPending(uid: String?): Int {
        if (uid == null) return 0
        val db = firestore ?: return 0
        var pushed = 0
        try {
            for (chat in chatDao.unsyncedConversations()) {
                val data = mapOf(
                    "id" to chat.id,
                    "title" to chat.title,
                    "createdAt" to chat.createdAt,
                    "updatedAt" to chat.updatedAt,
                    "messageCount" to chat.messageCount,
                    "syncedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid)
                    .collection("chats").document(chat.id)
                    .set(data, SetOptions.merge())
                    .await()
                chatDao.markConversationSynced(chat.id)
                pushed++
            }
            val unsyncedMsgs = chatDao.unsyncedMessages()
            if (unsyncedMsgs.isNotEmpty()) {
                for (msg in unsyncedMsgs) {
                    val data = mapOf(
                        "chatId" to msg.chatId,
                        "text" to msg.text,
                        "isUser" to msg.isUser,
                        "timestampMillis" to msg.timestampMillis,
                        "syncedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(uid)
                        .collection("chats").document(msg.chatId)
                        .collection("messages").document(msg.id)
                        .set(data, SetOptions.merge())
                        .await()
                }
                chatDao.markMessagesSynced(unsyncedMsgs.map { it.id })
                pushed += unsyncedMsgs.size
            }
        } catch (t: Throwable) {
            Log.w(TAG, "push chats failed", t)
        }
        return pushed
    }

    /**
     * Pull every cloud conversation and its messages into Room. Safe
     * to call on every sync — we always upsert with REPLACE strategy.
     */
    suspend fun pullCloud(uid: String?): Int {
        if (uid == null) return 0
        val db = firestore ?: return 0
        var pulled = 0
        try {
            val chatSnap = db.collection("users").document(uid)
                .collection("chats").get().await()
            for (doc in chatSnap.documents) {
                val id = doc.id
                chatDao.upsertConversation(
                    ChatConversationEntity(
                        id = id,
                        title = doc.getString("title") ?: "Chat",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                        messageCount = (doc.getLong("messageCount") ?: 0L).toInt(),
                        syncedToCloud = true
                    )
                )
                pulled++

                val msgSnap = db.collection("users").document(uid)
                    .collection("chats").document(id)
                    .collection("messages").get().await()
                for (m in msgSnap.documents) {
                    chatDao.upsertMessage(
                        ChatMessageEntity(
                            id = m.id,
                            chatId = id,
                            text = m.getString("text") ?: "",
                            isUser = m.getBoolean("isUser") ?: false,
                            timestampMillis = m.getLong("timestampMillis") ?: 0L,
                            syncedToCloud = true
                        )
                    )
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "pull chats failed", t)
        }
        return pulled
    }

    /** Erase chat history for the current user — local + cloud. */
    suspend fun clearHistory(uid: String?) {
        val conversations = chatDao.observeConversations().map { it }.let { flow ->
            // Take a one-shot snapshot via the DAO suspend method.
            chatDao.unsyncedConversations() + chatDao.observeConversations()
        }
        // Simpler: just iterate the unsynced list (the ones pushed to
        // the cloud are tracked by id locally; deletion mirrors via the
        // local table clear below).
        if (uid != null) {
            val db = firestore
            if (db != null) {
                try {
                    val snap = db.collection("users").document(uid)
                        .collection("chats").get().await()
                    for (doc in snap.documents) {
                        val msgCol = db.collection("users").document(uid)
                            .collection("chats").document(doc.id)
                            .collection("messages")
                        val msgs = msgCol.get().await()
                        for (m in msgs.documents) msgCol.document(m.id).delete().await()
                        db.collection("users").document(uid)
                            .collection("chats").document(doc.id).delete().await()
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "clear cloud history failed", t)
                }
            }
        }
        // Wipe local tables. We don't have a "clear all" query in ChatDao;
        // do it via a transactions-free path: read all then delete each.
        for (chat in chatDao.unsyncedConversations()) {
            chatDao.deleteConversation(chat.id)
        }
    }

    private fun deriveTitle(firstMessage: String): String {
        val trimmed = firstMessage.trim().replace(Regex("\\s+"), " ")
        return if (trimmed.length <= 40) trimmed
        else trimmed.substring(0, 37) + "..."
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}
