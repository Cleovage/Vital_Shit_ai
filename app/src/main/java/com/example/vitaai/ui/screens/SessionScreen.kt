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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.ProgressRing
import com.example.vitaai.ui.theme.*
import java.util.Locale

@Composable
fun SessionScreen(
    navController: NavController,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showFinishDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedSessionId) {
        state.savedSessionId?.let {
            navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }

    AuraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- TOP STATUS BAR ---
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.template?.name?.uppercase() ?: "ACTIVE PROTOCOL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "STATUS: ${if (state.running) "ENGAGED" else "PAUSED"}",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = null,
                    tint = if (state.running) Primary else Color(0xFFFF3B30),
                    modifier = Modifier.size(16.dp)
                )
            }

            // --- PRIMARY TIMER HUD RING ---
            val restFraction = if (state.restRemainingSeconds > 0 && (state.template?.defaultRestSeconds ?: 0) > 0) {
                state.restRemainingSeconds.toFloat() / (state.template?.defaultRestSeconds ?: 30)
            } else {
                (state.elapsedSeconds % 60) / 60f
            }
            
            val ringColors = if (state.restRemainingSeconds > 0) {
                listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
            } else {
                listOf(PrimaryContainer, Primary)
            }
            
            val ringGlowColor = if (state.restRemainingSeconds > 0) {
                Color(0xFF00E5FF).copy(alpha = 0.3f)
            } else {
                GlowPrimary
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ProgressRing(
                        progress = restFraction,
                        size = 200.dp,
                        strokeWidth = 10.dp,
                        glowWidth = 16.dp,
                        colors = ringColors,
                        glowColor = ringGlowColor
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTime(state.elapsedSeconds),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = if (state.restRemainingSeconds > 0) Color(0xFF00E5FF) else Primary,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.restRemainingSeconds > 0) "REST ACTIVE" else "ELAPSED TIME",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        if (state.restRemainingSeconds > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.restRemainingSeconds}S REMAINING",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }

            // --- HUD STAT TILES GRID ---
            val isStrength = state.template?.trackingMode == com.example.vitaai.data.TRACKING_STRENGTH || state.template?.trackingMode == "bodyweight"
            val isCardio = state.template?.trackingMode == com.example.vitaai.data.TRACKING_CARDIO
            val hasGps = state.template?.gpsEnabled == true

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HUDStatTile(
                        label = "HEART RATE",
                        value = if (state.liveHeartRate > 0) state.liveHeartRate.toString() else "--",
                        unit = "BPM",
                        glowColor = Color(0xFFFF3B30),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Favorite
                    )
                    if (isStrength) {
                        HUDStatTile(
                            label = "REPS",
                            value = state.currentReps.toString(),
                            unit = "CUR",
                            glowColor = Color(0xFFD0BCFF),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FitnessCenter
                        )
                    } else {
                        HUDStatTile(
                            label = "INTENSITY",
                            value = "ZONE 3",
                            unit = "LVL",
                            glowColor = Primary,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Whatshot
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (isCardio && hasGps) {
                        HUDStatTile(
                            label = "DISTANCE",
                            value = String.format(Locale.US, "%.2f", state.distanceMeters / 1000.0),
                            unit = "KM",
                            glowColor = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Map
                        )
                    } else if (isStrength) {
                        HUDStatTile(
                            label = "SETS",
                            value = state.completedSets.toString(),
                            unit = "DONE",
                            glowColor = Color(0xFFD0BCFF),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FormatListNumbered
                        )
                    } else {
                        HUDStatTile(
                            label = "GPS",
                            value = "OFF",
                            unit = "GPS",
                            glowColor = OnSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.GpsOff
                        )
                    }
                    HUDStatTile(
                        label = "CALORIES",
                        value = state.calories.toInt().toString(),
                        unit = "KCAL",
                        glowColor = Color(0xFFFF9500),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bolt
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // --- STRENGTH ACTIONS CENTER ---
            if (isStrength && state.running) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val shapes = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(shapes)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.3f), shapes)
                            .clickable { viewModel.addRep() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1 REP", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(shapes)
                            .background(Primary.copy(alpha = 0.1f))
                            .border(1.dp, Primary.copy(alpha = 0.4f), shapes)
                            .clickable { viewModel.completeSet() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("FINISH SET", color = Primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // --- HUD TACTILE CONTROL BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause/Resume Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (state.running) Color.Transparent else Primary)
                        .border(1.5.dp, Primary, CircleShape)
                        .clickable { if (state.running) viewModel.pause() else viewModel.start() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (state.running) Primary else OnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Main Action Button (Stop/Finish)
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3B30),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "TERMINATE SESSION",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("TERMINATE SESSION?", fontWeight = FontWeight.Bold) },
            text = { Text("ALL ANALYTICS WILL BE SYNCED TO THE COMMAND CENTER.") },
            confirmButton = {
                TextButton(onClick = { viewModel.save("COMPLETED SESSION") }) {
                    Text("SYNC & FINISH", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("CANCEL", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerHighest,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun HUDStatTile(
    label: String,
    value: String,
    unit: String,
    glowColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    GlassCardGlow(
        modifier = modifier,
        glowColor = glowColor,
        cornerRadius = 16.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = glowColor.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp), color = OnBackground, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold))
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
