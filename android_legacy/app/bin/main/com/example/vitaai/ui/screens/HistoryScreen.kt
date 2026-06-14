package com.example.vitaai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.data.local.WorkoutSessionWithSets
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.Error
import com.example.vitaai.ui.theme.OnSurfaceVariant
import com.example.vitaai.ui.theme.Primary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: ActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    AuraBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Workout History", style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                            Text("Performance logs", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            when (val state = uiState) {
                is ActivityUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is ActivityUiState.Success -> {
                    if (state.sessions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Text("No workout history found.", color = OnSurfaceVariant)
                        }
                    } else {
                        val activeStreak = remember(state.sessions) { getWeeklyCompletion(state.sessions) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "WEEKLY CONSISTENCY PIPELINE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurfaceVariant,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                StreakPipelineTimeline(activeStreakDays = activeStreak)
                            }

                            item {
                                Text(
                                    text = "HISTORY LOGS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurfaceVariant,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                             items(state.sessions) { sessionWithSets ->
                                 WorkoutHistoryCard(sessionWithSets = sessionWithSets, viewModel = viewModel)
                             }
                        }
                    }
                }
                is ActivityUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Error)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryCard(
    sessionWithSets: WorkoutSessionWithSets,
    viewModel: ActivityViewModel
) {
    val session = sessionWithSets.session
    val sets = sessionWithSets.sets
    var expanded by remember { mutableStateOf(false) }

    ApexCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(session.title, color = Primary, fontWeight = FontWeight.Bold)
                    Text(
                        DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochMilli(session.startTimeMillis)),
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = OnSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${session.durationSeconds / 60} min | ${session.totalSets} sets | ${session.totalReps} reps | ${session.calories.toInt()} kcal",
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    Spacer(Modifier.height(8.dp))
                    
                    if (session.avgHeartRate > 0) {
                        Text(
                            "Average Heart Rate: ${session.avgHeartRate.roundToInt()} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                    if (session.distanceMeters > 0) {
                        Text(
                            "Distance: ${String.format("%.2f", session.distanceMeters / 1000.0)} KM",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                    if (session.notes.isNotBlank() && session.notes != "Manual Entry") {
                        Text(
                            "Notes: ${session.notes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                    
                    if (sets.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sets Details:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Primary
                        )
                        sets.forEach { set ->
                            Text(
                                "  Set ${set.setNumber}: ${set.reps} reps ${if (set.weightKg > 0) "@ ${set.weightKg} kg" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreakPipelineTimeline(
    activeStreakDays: List<Boolean>
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val shape = RoundedCornerShape(20.dp)
    val vitaColors = com.example.vitaai.ui.theme.LocalVitaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                shadowElevation = 8f,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.08f),
                spotShadowColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(vitaColors.glassFill.copy(alpha = 0.72f))
            .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.09f), shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in days.indices) {
            val completed = activeStreakDays.getOrElse(i) { false }
            val nextCompleted = activeStreakDays.getOrElse(i + 1) { false }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    .border(1.dp, if (completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = days[i],
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (completed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            if (i < days.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (completed && nextCompleted) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                )
            }
        }
    }
}

private fun getWeeklyCompletion(sessions: List<WorkoutSessionWithSets>): List<Boolean> {
    val completed = MutableList(7) { false }
    val now = java.time.LocalDate.now()
    val zone = java.time.ZoneId.systemDefault()
    
    val monday = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    
    sessions.forEach { sessionWithSets ->
        val session = sessionWithSets.session
        val date = Instant.ofEpochMilli(session.startTimeMillis).atZone(zone).toLocalDate()
        if (!date.isBefore(monday) && !date.isAfter(now)) {
            val dayIndex = date.dayOfWeek.value - 1
            if (dayIndex in 0..6) {
                completed[dayIndex] = true
            }
        }
    }
    return completed
}
