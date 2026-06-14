package com.example.vitaai.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.BorderStroke

@Composable
fun ChatScreen(
    onOpenHistory: () -> Unit = {},
    onClose: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // ─── Material 3 Expressive Header ───
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand switcher look
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.startNewConversation() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "VitaAI Coach",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenHistory) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Chat history",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Chat",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    // ─── Main Chat Content ───
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (showWelcome) {
                            // Welcome layout + Suggestion Cards
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .shadow(8.dp, CircleShape, spotColor = MaterialTheme.colorScheme.tertiary)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "What can I help with?",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                // Suggestion Cards Grid
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        SuggestionCard(
                                            title = "Daily Summary",
                                            subtitle = "Steps & metrics",
                                            icon = "💪",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("How am I doing today? 💪") }
                                        )
                                        SuggestionCard(
                                            title = "New Workout",
                                            subtitle = "Tailor a routine",
                                            icon = "🏋️",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("Suggest a workout 🏋️") }
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        SuggestionCard(
                                            title = "Smart Meal",
                                            subtitle = "Nutrition ideas",
                                            icon = "🥗",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("What should I eat? 🥗") }
                                        )
                                        SuggestionCard(
                                            title = "Sleep Tips",
                                            subtitle = "Improve recovery",
                                            icon = "🌙",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.sendMessage("Sleep tips for tonight 🌙") }
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
                            ) {
                                items(messages) { message ->
                                    ChatBubbleItem(message = message, viewModel = viewModel)
                                }

                                if (isThinking) {
                                    item {
                                        ThinkingIndicatorItem()
                                    }
                                }
                            }
                        }
                    }

                    // ─── Expressive Floating Input Area ───
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChatInputBar(
                            text = inputText,
                            compact = isKeyboardVisible,
                            onTextChange = { inputText = it },
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )

                        AnimatedVisibility(visible = !isKeyboardVisible) {
                            Text(
                                text = "VitaAI Coach can make mistakes. Verify important health stats.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }

                // ─── Scroll-to-Bottom Floating Action Button ───
                AnimatedVisibility(
                    visible = showScrollFab,
                    enter = fadeIn(tween(200)) + scaleIn(),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 110.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                val total = listState.layoutInfo.totalItemsCount
                                if (total > 0) listState.animateScrollToItem(total - 1)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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

// ─── Suggestion Card (Material 3 Expressive) ───
@Composable
private fun SuggestionCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 28.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Robust Markdown Text Parser ───
@Composable
private fun MarkdownText(text: String, color: Color) {
    val annotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { lineIndex, line ->
            val isBullet = line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")
            if (isBullet) {
                append("  • ")
            }
            
            val content = if (isBullet) {
                val trimmed = line.trimStart()
                if (trimmed.length > 2) trimmed.substring(2) else ""
            } else line
            
            // Basic Bold parsing (**)
            val boldParts = content.split("**")
            boldParts.forEachIndexed { bIndex, bPart ->
                if (bIndex % 2 == 1 && bIndex < boldParts.size - 1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                        append(bPart)
                    }
                } else {
                    // Very basic italics (*) fallback
                    val italicParts = bPart.split("*")
                    if(italicParts.size > 2) {
                       italicParts.forEachIndexed { iIndex, iPart ->
                           if (iIndex % 2 == 1 && iIndex < italicParts.size - 1) {
                               withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) { append(iPart) }
                           } else {
                               append(iPart)
                           }
                       }
                    } else {
                       append(bPart)
                    }
                }
            }
            if (lineIndex < lines.size - 1) append("\n")
        }
    }
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = 24.sp
        ),
        color = color
    )
}

// ─── Action Proposal Card ───
@Composable
private fun ActionProposalCard(
    message: Message,
    onConfirm: () -> Unit,
    onRefuse: () -> Unit
) {
    val proposal = message.actionProposal ?: return
    val status = message.actionStatus

    var showCard by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showCard = true }

    AnimatedVisibility(
        visible = showCard,
        enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp), // M3 expressive roundness
            colors = CardDefaults.cardColors(
                containerColor = when(status) {
                    ActionStatus.CONFIRMED -> Color(0xFFECFDF5) // Soft green
                    ActionStatus.REFUSED -> Color(0xFFFEF2F2) // Soft red
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).animateContentSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when(proposal) {
                            is ActionProposal.LogNutrition -> "🥗"
                            is ActionProposal.LogHydration -> "💧"
                            is ActionProposal.LogWorkout -> "🏋️"
                        }
                        Text(icon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = when(proposal) {
                            is ActionProposal.LogNutrition -> "Log Nutrition"
                            is ActionProposal.LogHydration -> "Log Hydration"
                            is ActionProposal.LogWorkout -> "Log Workout"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when(proposal) {
                    is ActionProposal.LogNutrition -> {
                        Text(
                            "${proposal.foodName} (${proposal.meal.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }})",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${proposal.calories.toInt()} kcal • P:${proposal.protein.toInt()}g C:${proposal.carbs.toInt()}g F:${proposal.fat.toInt()}g",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is ActionProposal.LogHydration -> {
                        Text("${proposal.volumeMl.toInt()} ml of water", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Adds to today's hydration tracking.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is ActionProposal.LogWorkout -> {
                        Text(proposal.workoutName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${proposal.durationMinutes} min • ~${proposal.caloriesBurned.toInt()} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (status == ActionStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onRefuse,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Text("Refuse", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (status == ActionStatus.CONFIRMED) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (status == ActionStatus.CONFIRMED) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (status == ActionStatus.CONFIRMED) "Data Logged Successfully" else "Action Cancelled",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (status == ActionStatus.CONFIRMED) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

// ─── Visualization Card ───
@Composable
private fun VisualizationCard(viz: Visualization) {
    var showViz by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showViz = true }

    AnimatedVisibility(
        visible = showViz,
        enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                when(viz) {
                    is Visualization.ProgressRing -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { (viz.current / viz.goal).toFloat().coerceIn(0f, 1f) },
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                                    strokeWidth = 8.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Text("${(viz.current / viz.goal * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(viz.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${viz.current.toInt()} / ${viz.goal.toInt()} ${viz.unit}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    is Visualization.MacroPie -> {
                        Text("Daily Macronutrients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroIndicator("Protein", viz.protein, Color(0xFFEF4444))
                            MacroIndicator("Carbs", viz.carbs, Color(0xFF3B82F6))
                            MacroIndicator("Fat", viz.fat, Color(0xFFF59E0B))
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun MacroIndicator(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${value.toInt()}g", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

// ─── Chat Bubble Item Selector ───
@Composable
private fun ChatBubbleItem(message: Message, viewModel: ChatViewModel) {
    if (message.isUser) {
        // User message: Material 3 Primary colored bubble
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            val shape = RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    } else {
        // Assistant message: Left aligned with Tertiary avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "VitaAI Coach",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                MarkdownText(text = message.text, color = MaterialTheme.colorScheme.onSurface)

                if (message.visualization != null) {
                    VisualizationCard(viz = message.visualization)
                }

                if (message.actionProposal != null) {
                    ActionProposalCard(
                        message = message,
                        onConfirm = { viewModel.handleAction(message, true) },
                        onRefuse = { viewModel.handleAction(message, false) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val clipboardManager = LocalClipboardManager.current
                val context = LocalContext.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.sendMessage("Regenerate response for last query")
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Thinking Indicator ───
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
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "VitaAI Coach",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val transition = rememberInfiniteTransition(label = "thinkingDots")
                val dot1Alpha by transition.animateFloat(
                    initialValue = 0.3f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "tDot1"
                )
                val dot2Alpha by transition.animateFloat(
                    initialValue = 0.3f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing, delayMillis = 200),
                        repeatMode = RepeatMode.Reverse
                    ), label = "tDot2"
                )
                val dot3Alpha by transition.animateFloat(
                    initialValue = 0.3f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing, delayMillis = 400),
                        repeatMode = RepeatMode.Reverse
                    ), label = "tDot3"
                )

                val dotColor = MaterialTheme.colorScheme.onSurface
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor.copy(alpha = dot1Alpha)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor.copy(alpha = dot2Alpha)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor.copy(alpha = dot3Alpha)))
            }
        }
    }
}

// ─── Expressive Floating Input Bar ───
@Composable
private fun ChatInputBar(
    text: String,
    compact: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val containerShape = RoundedCornerShape(32.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (compact) 6.dp else 12.dp)
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = EaseInOutQuart)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = containerShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                .clip(containerShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = containerShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach file",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) onSend()
                }),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = false,
                maxLines = if (compact) 3 else 5,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Message VitaAI...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            val hasText = text.isNotBlank()
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                    tint = if (hasText) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}