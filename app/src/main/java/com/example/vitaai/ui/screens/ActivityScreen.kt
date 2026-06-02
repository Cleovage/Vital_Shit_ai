package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.*
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.data.local.WorkoutTemplateEntity
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ActivityScreen(navController: NavController, viewModel: ActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    AuraBackground {
        when (val state = uiState) {
            is ActivityUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            is ActivityUiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = Error)
            }
            is ActivityUiState.Success -> WorkoutHome(
                navController = navController,
                templates = state.templates,
                sessions = state.sessions,
                query = query,
                onQueryChange = { query = it }
            )
        }
    }
}

@Composable
private fun WorkoutHome(
    navController: NavController,
    templates: List<WorkoutTemplateEntity>,
    sessions: List<WorkoutSessionEntity>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    val filteredTemplates = templates.filter {
        query.isBlank() ||
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.trackingMode.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "TRAINING HUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "INTEL & LOGS",
                    style = MaterialTheme.typography.displaySmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Primary.copy(alpha = 0.2f), thickness = 2.dp)
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                label = { Text("SEARCH PROTOCOLS", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                shape = MaterialTheme.shapes.extraSmall,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Text(
                "ACTIVE PROTOCOLS",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredTemplates) { template ->
                    WorkoutTemplateCard(template = template, navController = navController)
                }
            }
        }

        item {
            Text(
                "PERFORMANCE HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                letterSpacing = 1.2.sp
            )
        }

        if (sessions.isEmpty()) {
            item {
                ApexCard(Modifier.fillMaxWidth()) {
                    Text(
                        "NO SESSION DATA DETECTED. INITIATE TRAINING TO GENERATE LOGS.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            items(sessions) { session ->
                WorkoutSessionRow(session)
            }
        }
        
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun WorkoutTemplateCard(template: WorkoutTemplateEntity, navController: NavController) {
    ApexCard(
        modifier = Modifier
            .size(width = 180.dp, height = 150.dp)
            .clickable { navController.navigate("workout/session/${template.id}") }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(iconForTemplate(template), contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Box(
                    Modifier
                        .background(Primary.copy(alpha = 0.1f), MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = template.trackingMode.uppercase(Locale.US),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Primary)
                    )
                }
            }
            Column {
                Text(
                    text = template.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = template.description,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = OnSurfaceVariant),
                    maxLines = 2
                )
            }
            Text(
                text = if (template.gpsEnabled) "GPS ENABLED" else "MANUAL ENTRY",
                style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, color = Primary, fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun WorkoutSessionRow(session: WorkoutSessionEntity) {
    ApexCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = session.title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatSessionTime(session.startTimeMillis).uppercase(),
                    style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = OnSurfaceVariant)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.durationSeconds / 60} MIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                val detailText = if (session.totalSets > 0 || session.totalReps > 0) {
                    "${session.totalSets} SETS | ${session.totalReps} REPS"
                } else {
                    val distanceKm = session.distanceMeters / 1000.0
                    val caloriesKcal = session.calories
                    when {
                        distanceKm > 0.0 && caloriesKcal > 0.0 -> {
                            String.format(Locale.US, "%.1f KM | %.0f KCAL", distanceKm, caloriesKcal)
                        }
                        distanceKm > 0.0 -> {
                            String.format(Locale.US, "%.1f KM", distanceKm)
                        }
                        caloriesKcal > 0.0 -> {
                            String.format(Locale.US, "%.0f KCAL", caloriesKcal)
                        }
                        else -> "COMPLETED SESSION"
                    }
                }
                Text(
                    text = detailText,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = OnSurfaceVariant)
                )
            }
        }
    }
}

private fun iconForTemplate(template: WorkoutTemplateEntity): ImageVector {
    return when {
        template.trackingMode == TRACKING_CARDIO && template.name.contains("Cycl", ignoreCase = true) -> Icons.Default.DirectionsBike
        template.trackingMode == TRACKING_CARDIO -> Icons.Default.DirectionsRun
        template.trackingMode == TRACKING_MOBILITY -> Icons.Default.SelfImprovement
        template.trackingMode == TRACKING_BODYWEIGHT -> Icons.Default.Timer
        else -> Icons.Default.FitnessCenter
    }
}

private fun formatSessionTime(millis: Long): String {
    return DateTimeFormatter.ofPattern("MMM dd, HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
}
