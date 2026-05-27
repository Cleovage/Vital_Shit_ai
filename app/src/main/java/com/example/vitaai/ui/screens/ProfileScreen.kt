package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.ProgressRing
import com.example.vitaai.ui.theme.*

@Composable
fun ProfileScreen() {
    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(listOf(Primary, Secondary, Primary)),
                                shape = CircleShape
                            )
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VU",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vita User",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Member since 2024",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Bio Stats
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatTile(label = "Age", value = "25", unit = "yrs", modifier = Modifier.weight(1f))
                        StatTile(label = "Weight", value = "70", unit = "kg", modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatTile(label = "Height", value = "175", unit = "cm", modifier = Modifier.weight(1f))
                        StatTile(label = "BMI", value = "22.9", unit = "Healthy", modifier = Modifier.weight(1f))
                    }
                }
            }

            // Health Goals
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Health Goals",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    GoalRow(
                        icon = Icons.Rounded.DirectionsRun,
                        label = "Steps",
                        value = "8,234 / 10k",
                        progress = 0.82f,
                        color = PrimaryContainer
                    )
                    GoalRow(
                        icon = Icons.Rounded.Bedtime,
                        label = "Sleep",
                        value = "7.5 / 8.0 hrs",
                        progress = 0.94f,
                        color = Secondary
                    )
                    GoalRow(
                        icon = Icons.Rounded.Favorite,
                        label = "Heart Rate",
                        value = "72 bpm avg",
                        progress = 1.0f, // target hit
                        color = TertiaryContainer
                    )
                }
            }

            // Settings
            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    SettingsItem(icon = Icons.Rounded.Notifications, title = "Notifications")
                    SettingsItem(icon = Icons.Rounded.Storage, title = "Data Sources")
                    SettingsItem(icon = Icons.Rounded.Shield, title = "Privacy")
                    SettingsItem(icon = Icons.Rounded.Info, title = "About")
                }
            }

            // Spacing for BottomNav
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = PrimaryContainer)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = unit, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun GoalRow(icon: ImageVector, label: String, value: String, progress: Float, color: Color) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = label, style = MaterialTheme.typography.titleMedium, color = OnSurface)
                    Text(text = value, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = progress,
                    size = 48.dp,
                    strokeWidth = 4.dp,
                    glowWidth = 8.dp,
                    colors = listOf(color, color),
                    glowColor = color.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = OnSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = OnSurface)
    }
}
