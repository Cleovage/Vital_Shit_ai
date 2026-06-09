package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.data.local.ChatConversationEntity
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * History of every past conversation with the coach. Tapping a row
 * invokes [onOpenConversation] (which routes back to ChatScreen with
 * the messages loaded) and the "+" button starts a fresh chat.
 */
@Composable
fun ChatHistoryScreen(
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onNewConversation: () -> Unit,
    viewModel: ChatHistoryViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val activeChatId = LocalActiveChatId.current
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    AuraBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Chat history",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onNewConversation) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "New conversation",
                        tint = Primary
                    )
                }
            }

            if (conversations.isEmpty()) {
                EmptyHistoryState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations, key = { it.id }) { chat ->
                        ChatRow(
                            chat = chat,
                            isActive = chat.id == activeChatId,
                            onClick = { onOpenConversation(chat.id) },
                            onDelete = { confirmDeleteId = chat.id }
                        )
                    }
                }
            }
        }
    }

    if (confirmDeleteId != null) {
        val id = confirmDeleteId!!
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Delete conversation?") },
            text = { Text("This will remove the chat from this device and from your account.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(id)
                    confirmDeleteId = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChatRow(
    chat: ChatConversationEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = Brush.linearGradient(
        colors = listOf(
            Color(0xFF06B6D4).copy(alpha = 0.85f),
            Color(0xFF3B82F6).copy(alpha = 0.85f)
        )
    )
    val containerColor = if (isActive)
        Color(0xFFE0F2FE)
    else
        Color.White.copy(alpha = 0.85f)
    val borderColor = if (isActive) Primary else Color.Black.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar / first letter
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            val letter = chat.title.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = letter,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${chat.messageCount} messages • ${formatTimestamp(chat.updatedAt)}",
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete conversation",
                tint = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.25f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No conversations yet",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start chatting with the coach and your past sessions will appear here.",
                color = Color.Black.copy(alpha = 0.55f),
                fontSize = 13.sp
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return "—"
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
