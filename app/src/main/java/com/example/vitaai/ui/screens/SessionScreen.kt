package com.example.vitaai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.*
import com.example.vitaai.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SessionScreen(
    navController: NavController,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showExitConfirm by remember { mutableStateOf(false) }

    AuraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            // --- PREMIUM TOP HUD ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showExitConfirm = true },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.03f), CircleShape)
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color(0xFF0F172A))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.template?.name?.uppercase(Locale.US) ?: "WORKOUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.running) "PROTOCOL ACTIVE" else "PAUSED",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.running) Color(0xFF00E676) else Color(0xFFFF3D00),
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f))
                        .border(1.dp, Primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- CENTRAL PROGRESS & TIMER OR REST COUNTDOWN ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (state.restRemainingSeconds > 0) {
                    val totalRestSeconds = state.template?.defaultRestSeconds?.coerceAtLeast(1) ?: 60
                    val progress = state.restRemainingSeconds.toFloat() / totalRestSeconds
                    
                    ProgressRing(
                        progress = progress,
                        size = 280.dp,
                        strokeWidth = 8.dp,
                        glowWidth = 12.dp,
                        colors = listOf(Color(0xFFFFB300), Color(0xFFFF5722)),
                        glowColor = Color(0xFFFFB300).copy(alpha = 0.3f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.restRemainingSeconds.toString(),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF9800),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "REST REMAINING",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    }

                    // Haptic Countdown Ticks
                    val context = LocalContext.current
                    LaunchedEffect(state.restRemainingSeconds) {
                        val remaining = state.restRemainingSeconds
                        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (remaining in 1..3) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    vibrator.vibrate(50)
                                }
                            } else if (remaining == 0) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1))
                                } else {
                                    vibrator.vibrate(longArrayOf(0, 150, 100, 150), -1)
                                }
                            }
                        }
                    }
                } else {
                    ProgressRing(
                        progress = (state.elapsedSeconds % 60 / 60f),
                        size = 280.dp,
                        strokeWidth = 8.dp,
                        glowWidth = 12.dp,
                        colors = listOf(Primary, Secondary),
                        glowColor = Primary.copy(alpha = 0.3f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTime(state.elapsedSeconds),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "ELAPSED TRAINING TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- SECONDARY METRICS GRID ---
            val isStrength = state.template?.category == "strength" || state.template?.category == "bodyweight"
            
            if (isStrength) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HUDStatTile(
                        label = "HEART RATE",
                        value = if (state.liveHeartRate > 0) state.liveHeartRate.toString() else "--",
                        unit = "BPM",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Favorite
                    )
                    HUDStatTile(
                        label = "COMPLETED SETS",
                        value = state.completedSets.toString(),
                        unit = "SETS",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FitnessCenter
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HUDStatTile(
                            label = "HEART RATE",
                            value = if (state.liveHeartRate > 0) state.liveHeartRate.toString() else "--",
                            unit = "BPM",
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Favorite
                        )
                        HUDStatTile(
                            label = "CALORIES",
                            value = state.calories.roundToInt().toString(),
                            unit = "KCAL",
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Whatshot
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HUDStatTile(
                            label = "DISTANCE",
                            value = String.format(Locale.US, "%.2f", state.distanceMeters / 1000.0),
                            unit = "KM",
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.DirectionsRun
                        )
                        HUDStatTile(
                            label = "CURRENT PACE",
                            value = state.currentPace,
                            unit = "",
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Timer
                        )
                    }
                }
            }

            if (isStrength) {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SET EDITING PANEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.5f))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.adjustWeight(-2.5) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Black.copy(alpha = 0.03f), CircleShape)
                                            .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                    ) {
                                        Text("-2.5", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                    }
                                    Text(
                                        text = "${state.currentWeightKg} kg",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF0F172A)
                                    )
                                    IconButton(
                                        onClick = { viewModel.adjustWeight(2.5) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Black.copy(alpha = 0.03f), CircleShape)
                                            .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                    ) {
                                        Text("+2.5", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("REPS", style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.5f))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.adjustReps(-1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Black.copy(alpha = 0.03f), CircleShape)
                                            .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "${state.currentReps}",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF0F172A)
                                    )
                                    IconButton(
                                        onClick = { viewModel.adjustReps(1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Black.copy(alpha = 0.03f), CircleShape)
                                            .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GlowButton(
                            text = "COMPLETE SET #${state.completedSets + 1}",
                            onClick = { viewModel.completeSet() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // --- HUD CONTROLS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowButton(
                    text = if (state.running) "PAUSE" else "RESUME",
                    onClick = { if (state.running) viewModel.pause() else viewModel.start() },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { viewModel.save("Manual training protocol completed via VitaAI HUD.") },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.03f), CircleShape)
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), CircleShape)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF0F172A))
                }
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("ABANDON PROTOCOL?") },
            text = { Text("Unsaved progress will be lost. Terminate session?") },
            confirmButton = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("EXIT", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("CONTINUE")
                }
            },
            containerColor = Color.White,
            titleContentColor = Color(0xFF0F172A),
            textContentColor = Color.Black.copy(alpha = 0.65f)
        )
    }

    LaunchedEffect(state.savedSessionId) {
        if (state.savedSessionId != null) {
            navController.navigate("dashboard") {
                popUpTo("session") { inclusive = true }
            }
        }
    }
}

@Composable
private fun HUDStatTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    GlassCard(modifier = modifier, contentPadding = 12.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.titleLarge, color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.5f))
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
