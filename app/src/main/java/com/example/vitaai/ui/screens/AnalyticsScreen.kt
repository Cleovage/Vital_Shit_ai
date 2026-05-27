package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*
import java.time.Instant

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Text(
                    text = "BIOMETRIC INTELLIGENCE",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = Primary
                )
                Text(
                    text = "Detailed breakdown of biometrics and load.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = OutlineVariant.copy(alpha = 0.5f))
            }

            when (val state = uiState) {
                is AnalyticsUiState.Loading -> {
                    item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
                }
                is AnalyticsUiState.Success -> {
                    // Time Selector
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(40.dp).background(SurfaceContainerHigh),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeSelectorItem("WEEK", true, Modifier.weight(1f))
                            TimeSelectorItem("MONTH", false, Modifier.weight(1f))
                            TimeSelectorItem("YEAR", false, Modifier.weight(1f))
                        }
                    }

                    // Step Trends Card
                    item {
                        ApexCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("ACTIVITY VOLUME", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        Text("Step Trends", style = MaterialTheme.typography.titleMedium, color = Primary)
                                    }
                                    val avgSteps = if (state.dailySteps.isNotEmpty()) state.dailySteps.values.average().toLong() else 0L
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("DAILY AVG", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        Text(avgSteps.toString(), style = MaterialTheme.typography.titleMedium, color = Primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Real data chart
                                Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                                    val maxSteps = (state.dailySteps.values.maxOrNull() ?: 1L).toFloat()
                                    state.dailySteps.forEach { (_, steps) ->
                                        BarItem(steps / maxSteps, Modifier.weight(1f), steps == (state.dailySteps.values.maxOrNull()))
                                    }
                                }
                            }
                        }
                    }
                }
                is AnalyticsUiState.Error -> {
                    item { Text("Error: ${state.message}", color = Error) }
                }
            }

            // HRV vs Training Load (Simplified static view as HRV data is often sparse)
            item {
                ApexCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RECOVERY & STRESS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text("HRV vs Training Load", style = MaterialTheme.typography.titleMedium, color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Primary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HRV (MS)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(modifier = Modifier.size(8.dp).background(Secondary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LOAD", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                        Text("Trend Analysis Optimal", color = OnSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            // Muscle Recovery
            item {
                ApexCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MUSCLE RECOVERY", style = MaterialTheme.typography.labelMedium, color = Primary)
                            }
                            Text("92% Fresh", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RecoveryBar("QUADS", 0.8f)
                            RecoveryBar("HAMSTRINGS", 0.7f)
                            RecoveryBar("BACK", 0.4f, Error)
                            RecoveryBar("CORE", 0.9f)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSelectorItem(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxHeight().background(if (selected) SurfaceContainerHighest else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) Primary else OnSurfaceVariant)
    }
}

@Composable
private fun BarItem(height: Float, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Box(
        modifier = modifier.fillMaxHeight(height.coerceIn(0.1f, 1.0f)).background(if (highlighted) Primary else SurfaceContainerHighest)
    )
}

@Composable
private fun RecoveryBar(label: String, progress: Float, color: Color = Primary) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(SurfaceContainerHigh)) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(4.dp).background(color))
        }
    }
}
