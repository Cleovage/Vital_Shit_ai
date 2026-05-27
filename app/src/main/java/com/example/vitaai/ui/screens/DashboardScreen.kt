package com.example.vitaai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.ui.components.*
import com.example.vitaai.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            DashboardUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryContainer
                )
            }
            is DashboardUiState.Error -> {
                Text(
                    "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.Center)
                )
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    snapshot = state.snapshot,
                    insight = state.insight,
                    onRefresh = { viewModel.loadData() }
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    snapshot: HealthSnapshot,
    insight: String,
    onRefresh: () -> Unit
) {
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ─── Header ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = today,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = Primary
                    )
                }
            }
        }

        // ─── Hero Step Ring ────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TODAY'S STEPS",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(contentAlignment = Alignment.Center) {
                            ProgressRing(
                                progress = snapshot.steps.toFloat() / 10000f,
                                size = 200.dp,
                                strokeWidth = 16.dp,
                                colors = listOf(PrimaryContainer, Primary),
                                glowColor = GlowPrimary
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = snapshot.steps.toString(),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = PrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "/ 10,000",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // ─── Insight Card ──────────────────────────────────────────────
        item {
            GlassCardGlow(
                modifier = Modifier.fillMaxWidth(),
                glowColor = PrimaryContainer
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatChip(label = "AI Insight", color = PrimaryContainer)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurface
                    )
                }
            }
        }

        // ─── Stat Cards Row ────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sleep Card
                GlassCard(modifier = Modifier.weight(1f)) {
                    StatChip(label = "SLEEP", color = Secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "%.1f".format(snapshot.sleepDurationHours),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "hours",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }

                // Heart Rate Card
                GlassCard(modifier = Modifier.weight(1f)) {
                    StatChip(label = "HEART RATE", color = TertiaryContainer)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "%.0f".format(snapshot.avgHeartRate),
                        style = MaterialTheme.typography.headlineLarge,
                        color = TertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "bpm",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        // ─── Activity Trend ────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Activity Trend",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val chartData = if (snapshot.hourlySteps.isEmpty()) {
                    listOf(0f, 0f, 0f, 0f, 0f, 0f)
                } else {
                    snapshot.hourlySteps.entries
                        .sortedBy { it.key }
                        .takeLast(6)
                        .map { it.value.toFloat() }
                }

                LuminousLineChart(
                    dataPoints = chartData,
                    lineColor = PrimaryContainer,
                    glowColor = GlowPrimary
                )
            }
        }

        // Bottom spacing for navigation bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
