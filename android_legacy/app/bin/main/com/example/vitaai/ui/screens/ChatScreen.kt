package com.example.vitaai.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onOpenHistory: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Show scroll-to-bottom FAB when scrolled up
    val showScrollFab by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalCount = listState.layoutInfo.totalItemsCount
            totalCount > 0 && lastVisibleIndex < totalCount - 1
        }
    }

    // Welcome state: show suggestion cards if history is empty or only has the welcome prompt
    val showWelcome by remember {
        derivedStateOf {
            messages.size <= 1
        }
    }

    // Auto-scroll to latest message
    val itemCount = messages.size + if (isThinking) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    AuraBackground {
        CompositionLocalProvider(LocalActiveChatId provides activeChatId) {
            // Detect nav bar visibility and get its height
            val navBarHeight = with(LocalDensity.current) {
                WindowInsets.navigationBars.getBottom(this).toDp()
            }
            val hasNavBar = navBarHeight > 0.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    // ─── ChatGPT-Style Minimalist Top Header ───
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side brand switcher look
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.startNewConversation() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VitaAI Coach",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = Color(0xFF202124)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF5f6368),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Right side action buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.startNewConversation() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "New conversation",
                                    tint = Color(0xFF5f6368)
                                )
                            }
                            IconButton(onClick = onOpenHistory) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Chat history",
                                    tint = Color(0xFF5f6368)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color.Black.copy(alpha = 0.06f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    // ─── Main Chat Content ───
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (showWelcome) {
                            // ─── Centered welcome layout + 2x2 Suggestion Cards ───
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .shadow(4.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F0FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF1F2937),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "What can I help with?",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = Color(0xFF202124)
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Suggestion Cards Grid (2x2 Column containing 2 Rows)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SuggestionCard(
                                            title = "How am I doing today?",
                                            subtitle = "Get a summary of steps & active metrics",
                                            icon = "💪",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("How am I doing today? 💪") }
                                        )
                                        SuggestionCard(
                                            title = "Suggest a workout",
                                            subtitle = "Tailor a routine for your goals",
                                            icon = "🏋️",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("Suggest a workout 🏋️") }
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SuggestionCard(
                                            title = "What should I eat?",
                                            subtitle = "Get smart meal or nutrition ideas",
                                            icon = "🥗",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("What should I eat? 🥗") }
                                        )
                                        SuggestionCard(
                                            title = "Sleep tips for tonight",
                                            subtitle = "Improve recovery based on trends",
                                            icon = "🌙",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("Sleep tips for tonight 🌙") }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Scrollable list of conversation messages
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                            ) {
                                items(messages) { message ->
                                    ChatBubbleItem(message = message, viewModel = viewModel)
                                }

                                // Subtle staggered typing dots
                                if (isThinking) {
                                    item {
                                        ThinkingIndicatorItem()
                                    }
                                }
                            }
                        }
                    }

                    // ─── Centered Input pill area ───
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChatInputBar(
                            text = inputText,
                            onTextChange = { inputText = it },
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )

                        Text(
                            text = "VitaAI Coach can make mistakes. Verify important health stats.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF9aa0a6),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // ─── Scroll-to-Bottom Floating Action Button ───
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
                        containerColor = Color(0xFF202124).copy(alpha = 0.85f),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Suggestion Card Composable (Gemini-style) ───
@Composable
private fun SuggestionCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.02f),
                spotColor = Color.Black.copy(alpha = 0.02f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 28.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                color = Color(0xFF202124)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = Color(0xFF5f6368)
            )
        }
    }
}

// ─── Chat Bubble Item Selector ───
@Composable
private fun ChatBubbleItem(message: Message, viewModel: ChatViewModel) {
    if (message.isUser) {
        // User message: Clean right-aligned bubble without ticks
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            val shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .shadow(
                        elevation = 1.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )
                    .clip(shape)
                    .background(Color(0xFF202124))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    ),
                    color = Color.White
                )
            }
        }
    } else {
        // Assistant message: Clean, left-aligned document style with avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // AI Avatar (simplified)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF1F2937),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body text column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "VitaAI Coach",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Copy to Clipboard and Regenerate actions row
                val clipboardManager = LocalClipboardManager.current
                val context = LocalContext.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = Color(0xFF5f6368),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.sendMessage("Regenerate response for last query")
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = Color(0xFF5f6368),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Thinking Indicator Item (Staggered Dots) ───
@Composable
private fun ThinkingIndicatorItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F0FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF1F2937),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "VitaAI Coach",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF202124)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val transition = rememberInfiniteTransition(label = "thinkingDots")
                val dot1Alpha by transition.animateFloat(
                    initialValue = 0.4f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "tDot1"
                )
                val dot2Alpha by transition.animateFloat(
                    initialValue = 0.4f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing, delayMillis = 200),
                        repeatMode = RepeatMode.Reverse
                    ), label = "tDot2"
                )
                val dot3Alpha by transition.animateFloat(
                    initialValue = 0.4f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing, delayMillis = 400),
                        repeatMode = RepeatMode.Reverse
                    ), label = "tDot3"
                )

                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF202124).copy(alpha = dot1Alpha)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF202124).copy(alpha = dot2Alpha)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF202124).copy(alpha = dot3Alpha)))
            }
        }
    }
}

// ─── Pill-shaped Input Bar Composable ───
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val containerShape = RoundedCornerShape(28.dp)
    
    // Detect nav bar visibility and get its height
    val navBarHeight = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    val hasNavBar = navBarHeight > 0.dp

    // Animate the padding based on nav bar presence
    val navBarPaddingAnimated by animateDpAsState(
        targetValue = if (hasNavBar) navBarHeight else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = EaseInOutQuart),
        label = "navBarPadding"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = EaseInOutQuart)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = containerShape,
                        ambientColor = Color.Black.copy(alpha = 0.03f),
                        spotColor = Color.Black.copy(alpha = 0.03f)
                    )
                    .clip(containerShape)
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.06f),
                        shape = containerShape
                    )
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Visual Clip Attachment button
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach file",
                        tint = Color(0xFF5f6368),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (text.isNotBlank()) {
                            onSend()
                        }
                    }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = Color(0xFF202124)
                    ),
                    singleLine = false,
                    maxLines = 5,
                    cursorBrush = SolidColor(Primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Message VitaAI...",
                                    color = Color(0xFF5f6368),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Round up-arrow Send Button
                val hasText = text.isNotBlank()
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasText) Color(0xFF202124) else Color.Black.copy(alpha = 0.04f)
                        )
                        .clickable(
                            enabled = hasText,
                            onClick = onSend
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = if (hasText) Color.White else Color(0xFF9aa0a6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Spacer for nav bar with smooth animation
        if (hasNavBar) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarPaddingAnimated)
            )
        }
    }
}

// ─── Format Time Helper ───
private fun formatMessageTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
