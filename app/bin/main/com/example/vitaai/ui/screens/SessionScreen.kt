package com.example.vitaai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
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

    AuraBackground(showGrid = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- TOP STATUS BAR ---
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                        style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = OnSurfaceVariant)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = null,
                    tint = if (state.running) Primary else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }

            // --- PRIMARY TIMER HUD ---
            ApexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                containerColor = Color.Black.copy(alpha = 0.4f)
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatTime(state.elapsedSeconds),
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 84.sp,
                            fontWeight = FontWeight.Black,
                            color = Primary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "ELAPSED TRAINING TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    if (state.restRemainingSeconds > 0) {
                        Text(
                            text = "REST: ${formatTime(state.restRemainingSeconds.toLong())}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Secondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // --- SECONDARY METRICS GRID ---
            val isStrength = state.template?.trackingMode == com.example.vitaai.data.TRACKING_STRENGTH || state.template?.trackingMode == "bodyweight"
            val isCardio = state.template?.trackingMode == com.example.vitaai.data.TRACKING_CARDIO
            val hasGps = state.template?.gpsEnabled == true

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HUDStatTile(
                    label = "HEART RATE",
                    value = if (state.liveHeartRate > 0) state.liveHeartRate.toString() else "--",
                    unit = "BPM",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Favorite
                )
                if (isStrength) {
                    HUDStatTile(
                        label = "REPS",
                        value = state.currentReps.toString(),
                        unit = "CUR",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FitnessCenter
                    )
                } else {
                    HUDStatTile(
                        label = "INTENSITY",
                        value = "ZONE 3", // Mocked logic
                        unit = "LVL",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Whatshot
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isCardio && hasGps) {
                    HUDStatTile(
                        label = "DISTANCE",
                        value = String.format(Locale.US, "%.2f", state.distanceMeters / 1000.0),
                        unit = "KM",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Map
                    )
                } else if (isStrength) {
                    HUDStatTile(
                        label = "SETS",
                        value = state.completedSets.toString(),
                        unit = "DONE",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FormatListNumbered
                    )
                } else {
                    Spacer(Modifier.weight(1f)) // Placeholder
                }
                HUDStatTile(
                    label = "CALORIES",
                    value = "242", // Mocked logic
                    unit = "KCAL",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Bolt
                )
            }

            Spacer(Modifier.weight(1f))

            // --- STRENGTH ACTIONS ---
            if (isStrength && state.running) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.addRep() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                    ) {
                        Text("+1 REP", color = OnPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.completeSet() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("FINISH SET", color = OnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- HUD ACTIONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause/Resume Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (state.running) Color.Transparent else Primary)
                        .border(2.dp, Primary, CircleShape)
                        .clickable { if (state.running) viewModel.pause() else viewModel.start() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (state.running) Primary else OnPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Main Action Button (Stop/Finish)
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        "TERMINATE SESSION",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("TERMINATE SESSION?") },
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
            shape = MaterialTheme.shapes.extraSmall
        )
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
    ApexCard(modifier) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineLarge, color = OnBackground, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = OnSurfaceVariant))
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
