package com.example.vitaai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.components.*
import com.example.vitaai.ui.theme.*

@Composable
fun AnalyticsScreen() {
    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            // Performance Score
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ProgressRing(
                            progress = 0.78f,
                            size = 160.dp,
                            strokeWidth = 12.dp,
                            colors = listOf(PrimaryContainer, Primary),
                            glowColor = GlowPrimary
                        )
                        Text(
                            text = "78",
                            style = MaterialTheme.typography.displayLarge,
                            color = PrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Performance Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Weekly Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(label = "Avg Steps", value = "7,842", modifier = Modifier.weight(1f))
                    MiniStatCard(label = "Avg Sleep", value = "7.2h", modifier = Modifier.weight(1f))
                    MiniStatCard(label = "Avg HR", value = "71 bpm", modifier = Modifier.weight(1f))
                }
            }

            // Weekly Trend Chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Weekly Trend",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LuminousLineChart(
                        dataPoints = listOf(6500f, 8200f, 7100f, 9500f, 8800f, 7600f, 8234f),
                        lineColor = PrimaryContainer,
                        glowColor = GlowPrimary
                    )
                }
            }

            // Activity Distribution
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Activity Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LuminousBarChart(
                        dataPoints = listOf(45f, 60f, 30f, 75f, 55f, 40f, 65f),
                        barColor = Secondary,
                        glowColor = GlowSecondary
                    )
                }
            }

            // Comparison
            item {
                GlassCardGlow(modifier = Modifier.fillMaxWidth(), glowColor = Secondary) {
                    Text(
                        text = "This Week vs Last",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ComparisonItem(label = "Steps", value = "12%", isUp = true)
                        ComparisonItem(label = "Sleep", value = "5%", isUp = true)
                        ComparisonItem(label = "Heart Rate", value = "3%", isUp = false)
                    }
                }
            }

            // Bottom Spacing
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, cornerRadius = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = PrimaryContainer)
        }
    }
}

@Composable
private fun ComparisonItem(label: String, value: String, isUp: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null,
                tint = if (isUp) Color(0xFF22C55E) else Color(0xFFFF3366),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = if (isUp) Color(0xFF22C55E) else Color(0xFFFF3366)
            )
        }
    }
}
