package com.example.vitaai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.MoodEntry
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val liveSteps by viewModel.liveSteps.collectAsState()

    AuraBackground {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is DashboardUiState.PermissionsRequired -> {
                PermissionsScreen(viewModel)
            }
            is DashboardUiState.Success -> {
                DashboardContent(state.snapshot, state.insight, state.mood, liveSteps, viewModel)
            }
            is DashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = Error)
                }
            }
        }
    }
}

@Composable
private fun PermissionsScreen(viewModel: DashboardViewModel) {
    // Note: In a real app, I'd use the Health Connect permissions launcher here.
    // For this task, I'll provide a button to "Connect".
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Connect to Health Data", style = MaterialTheme.typography.headlineSmall, color = Primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Apex Vitality needs access to your health metrics to optimize your performance.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.loadData() }, // Mocking permission grant
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Text("GRANT ACCESS")
        }
    }
}

@Composable
private fun DashboardContent(
    snapshot: HealthSnapshot,
    insight: String,
    mood: MoodEntry?,
    liveSteps: Long,
    viewModel: DashboardViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ─── Header ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APEX VITALITY",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp, letterSpacing = (-1).sp),
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceContainerHigh).border(1.dp, OutlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Primary)
                }
            }
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
        }

        // ─── Mood Tracker Section ──────────────────────────────────────
        item {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HOW ARE YOU FEELING?", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (i in 1..5) {
                            val isSelected = mood?.score == i * 2
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Primary else SurfaceContainerHigh)
                                    .clickable { viewModel.recordMood(i * 2) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(i) {
                                        1 -> "😫"
                                        2 -> "😕"
                                        3 -> "😐"
                                        4 -> "🙂"
                                        5 -> "🤩"
                                        else -> ""
                                    },
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── Insight Context ───────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(text = "Today's Focus", style = MaterialTheme.typography.headlineLarge, color = Primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = insight, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
        }

        // ─── Hero Ring (Active Calories) ───────────────────────────────
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                ApexCard(modifier = Modifier.aspectRatio(1f).widthIn(max = 320.dp)) {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        Text(text = "ACTIVE CALORIES", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        val progress = (snapshot.calories / 1000.0).coerceIn(0.0, 1.0).toFloat()
                            Canvas(modifier = Modifier.size(200.dp)) {
                                val strokeWidth = 6.dp.toPx()
                                drawArc(color = SurfaceContainerHighest, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeWidth))
                                drawArc(color = Primary, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Square))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = snapshot.calories.toInt().toString(), style = MaterialTheme.typography.displayLarge, color = Primary, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                                Text(text = "KCAL", style = MaterialTheme.typography.labelMedium, color = Primary, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }
        }

        // ─── Bento Grid ────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Steps (Live + Health Connect)
                    val displaySteps = if (liveSteps > snapshot.steps) liveSteps else snapshot.steps
                    ApexCard(modifier = Modifier.weight(1f).aspectRatio(4f / 3f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "STEPS", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(text = displaySteps.toString(), style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(SurfaceContainerHighest)) {
                                    Box(modifier = Modifier.fillMaxWidth((displaySteps / 10000f).coerceIn(0f, 1f)).height(2.dp).background(Primary))
                                }
                            }
                        }
                    }

                    // Heart Rate
                    ApexCard(modifier = Modifier.weight(1f).aspectRatio(4f / 3f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "HEART RATE", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(text = snapshot.avgHeartRate.toInt().toString(), style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "BPM", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Sparkline(color = Primary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Sleep
                    ApexCard(modifier = Modifier.weight(1f).aspectRatio(4f / 3f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "SLEEP", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                Icon(Icons.Default.Star, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                            val hours = snapshot.sleepDurationHours.toInt()
                            val minutes = ((snapshot.sleepDurationHours - hours) * 60).toInt()
                            Text(text = "${hours}h ${minutes}m", style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Hydration (Workable)
                    ApexCard(modifier = Modifier.weight(1f).aspectRatio(4f / 3f).clickable { viewModel.logWater(8) }) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "HYDRATION", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    val oz = (snapshot.hydrationLiters * 33.814).toInt()
                                    Text(text = oz.toString(), style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "OZ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                                }
                                Text("+ LOG 8oz", style = MaterialTheme.typography.labelSmall, color = Primary.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }

        // ─── Upcoming Session ──────────────────────────────────────────
        item {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "UPCOMING SESSION", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "HIIT Intervals - 45m", style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(text = "START PROTOCOL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkline(color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.2f, h * 0.6f)
            lineTo(w * 0.4f, h * 0.3f)
            lineTo(w * 0.5f, h * 0.8f)
            lineTo(w * 0.6f, h * 0.1f)
            lineTo(w * 0.8f, h * 0.6f)
            lineTo(w, h * 0.5f)
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}
