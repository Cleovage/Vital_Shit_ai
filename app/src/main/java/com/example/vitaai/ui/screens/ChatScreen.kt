package com.example.vitaai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.PageHeader
import com.example.vitaai.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.launch

// ─── Blob colours ──────────────────────────────────────────────────────────────
private val BlobCyan    = Color(0xFF06B6D4)
private val BlobBlue    = Color(0xFF3B82F6)
private val BlobEmerald = Color(0xFF10B981)

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val messages    by viewModel.messages.collectAsState()
    val isThinking  by viewModel.isThinking.collectAsState()
    var inputText   by remember { mutableStateOf("") }
    val listState   = rememberLazyListState()
    val scope       = rememberCoroutineScope()

    // Derive scroll-to-bottom FAB visibility
    val showScrollFab by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalCount = listState.layoutInfo.totalItemsCount
            totalCount > 0 && lastVisibleIndex < totalCount - 1
        }
    }

    val quickSuggestions = remember {
        listOf(
            "How am I doing today? 💪",
            "Suggest a workout 🏋️",
            "What should I eat? 🥗",
            "Sleep tips for tonight 🌙"
        )
    }
    // Show suggestions only when conversation is new or after the last AI message
    val showSuggestions by remember {
        derivedStateOf {
            messages.isEmpty() || (!messages.last().isUser && !isThinking)
        }
    }

    // Auto-scroll to latest message (or thinking indicator)
    val itemCount = messages.size + if (isThinking) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    AuraBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ─── Header ────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    PageHeader(title = "VitaAI", kicker = "Coach chat")
                }

                HorizontalDivider(
                    color = Color.Black.copy(alpha = 0.06f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp)
                )

                // ─── Messages ──────────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            Column {
                                // Sender label above each bubble
                                Text(
                                    text = if (message.isUser) "YOU" else "VITAAI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = Color.Black.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = if (message.isUser) 0.dp else 4.dp,
                                            end = if (message.isUser) 4.dp else 0.dp,
                                            bottom = 6.dp
                                        )
                                        .wrapContentWidth(
                                            if (message.isUser) Alignment.End else Alignment.Start
                                        )
                                )
                                ChatBubble(message)
                            }
                        }
                    }

                    // Thinking indicator as a special last item
                    item {
                        AnimatedVisibility(
                            visible = isThinking,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it / 3 }
                            ),
                            exit = fadeOut(tween(200)) + slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { it / 3 }
                            )
                        ) {
                            AnimatedBlobThinkingIndicator()
                        }
                    }
                }

                // ─── Quick Suggestions ──────────────────────────────────
                AnimatedVisibility(
                    visible = showSuggestions,
                    enter = fadeIn(tween(250)),
                    exit = fadeOut(tween(200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickSuggestions.forEach { chip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.75f))
                                    .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel.sendMessage(chip)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = Color.Black.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }

                // ─── Input Area ────────────────────────────────────────────
                ChatInput(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    }
                )
            }

            // ─── Scroll to Bottom FAB ───────────────────────────────────
            AnimatedVisibility(
                visible = showScrollFab,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            val total = listState.layoutInfo.totalItemsCount
                            if (total > 0) listState.animateScrollToItem(total - 1)
                        }
                    },
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.85f),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}


// ─── Animated Blob Thinking Indicator ─────────────────────────────────────────

@Composable
fun AnimatedBlobThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "blobThink")

    // ── Blob 1: Cyan ──
    val blob1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "b1s"
    )
    val blob1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "b1a"
    )

    // ── Blob 2: Blue ──
    val blob2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing, delayMillis = 500),
            repeatMode = RepeatMode.Reverse
        ), label = "b2s"
    )
    val blob2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing, delayMillis = 500),
            repeatMode = RepeatMode.Reverse
        ), label = "b2a"
    )
    val blob2OffsetX by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing, delayMillis = 500),
            repeatMode = RepeatMode.Reverse
        ), label = "b2x"
    )

    // ── Blob 3: Emerald ──
    val blob3Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "b3s"
    )
    val blob3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "b3a"
    )
    val blob3OffsetX by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "b3x"
    )

    // ── Center dot ──
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot"
    )

    // ── Text alpha ──
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "txt"
    )

    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.07f),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Blob container ──
        Box(
            modifier = Modifier.size(width = 56.dp, height = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            // Blob 3 – Emerald (back)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .offset(x = blob3OffsetX.dp, y = 4.dp)
                    .graphicsLayer {
                        scaleX = blob3Scale
                        scaleY = blob3Scale
                    }
                    .blur(6.dp)
                    .clip(CircleShape)
                    .background(BlobEmerald.copy(alpha = blob3Alpha))
            )
            // Blob 2 – Blue (mid)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = blob2OffsetX.dp, y = (-3).dp)
                    .graphicsLayer {
                        scaleX = blob2Scale
                        scaleY = blob2Scale
                    }
                    .blur(4.dp)
                    .clip(CircleShape)
                    .background(BlobBlue.copy(alpha = blob2Alpha))
            )
            // Blob 1 – Cyan (front)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = blob1Scale
                        scaleY = blob1Scale
                    }
                    .blur(6.dp)
                    .clip(CircleShape)
                    .background(BlobCyan.copy(alpha = blob1Alpha))
            )
            // Center bright dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = dotAlpha))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Tuning in...",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF0F172A).copy(alpha = textAlpha)
        )
    }
}

// ─── Chat Bubble ───────────────────────────────────────────────────────────────

private fun formatMessageTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
private fun ChatBubble(message: Message) {
    val isUser    = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        if (isUser) {
            // User bubble: Solid slate-900 with white text, shadow
            Column(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.18f),
                        spotColor = Color.Black.copy(alpha = 0.18f)
                    )
                    .clip(shape)
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "✓✓",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF34D399)
                    )
                }
            }
        } else {
            // AI bubble: Translucent Glass Card
            Column(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )
                    .clip(shape)
                    .background(Color.White.copy(alpha = 0.78f))
                    .border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.07f),
                        shape = shape
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ─── Chat Input ────────────────────────────────────────────────────────────────

@Composable
private fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val containerShape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Standard Web-like input container (light white bg, black border, shadow)
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = containerShape,
                    ambientColor = Color.Black.copy(alpha = 0.05f),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(containerShape)
                .background(Color.White.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                    shape = containerShape
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) {
                        onSend()
                    }
                }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                ),
                singleLine = false,
                maxLines = 4,
                cursorBrush = SolidColor(Primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Ask about workouts, meals, recovery...",
                                color = Color.Black.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // Custom send button inside the input container row
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .clickable(
                        enabled = text.isNotBlank(),
                        onClick = onSend
                    )
                    .graphicsLayer {
                        alpha = if (text.isNotBlank()) 1f else 0.4f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
