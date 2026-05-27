package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlowButton
import com.example.vitaai.ui.components.StatChip
import com.example.vitaai.ui.theme.*

@Composable
fun ActivityScreen() {
    AuraBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title
                item {
                    Text(
                        text = "Activity",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Filter Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { StatChip(label = "All", color = PrimaryContainer) }
                        item { StatChip(label = "Cardio", color = OnSurfaceVariant.copy(alpha = 0.5f), textColor = OnSurface) }
                        item { StatChip(label = "Strength", color = OnSurfaceVariant.copy(alpha = 0.5f), textColor = OnSurface) }
                        item { StatChip(label = "Yoga", color = OnSurfaceVariant.copy(alpha = 0.5f), textColor = OnSurface) }
                        item { StatChip(label = "Walking", color = OnSurfaceVariant.copy(alpha = 0.5f), textColor = OnSurface) }
                    }
                }

                // Activity List
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceVariant
                        )
                        
                        ActivityCard(
                            icon = Icons.Rounded.DirectionsRun,
                            title = "Morning Run",
                            subtitle = "5.2 km • 32 min",
                            calories = "320 cal",
                            time = "8:00 AM",
                            color = PrimaryContainer
                        )
                        ActivityCard(
                            icon = Icons.Rounded.SelfImprovement,
                            title = "Yoga Session",
                            subtitle = "45 min",
                            calories = "180 cal",
                            time = "12:30 PM",
                            color = Secondary
                        )
                        ActivityCard(
                            icon = Icons.Rounded.FitnessCenter,
                            title = "Weight Training",
                            subtitle = "60 min",
                            calories = "450 cal",
                            time = "5:00 PM",
                            color = TertiaryContainer
                        )
                        ActivityCard(
                            icon = Icons.Rounded.DirectionsWalk,
                            title = "Evening Walk",
                            subtitle = "2.1 km • 25 min",
                            calories = "150 cal",
                            time = "8:15 PM",
                            color = PrimaryContainer
                        )
                    }
                }
                
                // Bottom Nav spacing
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            // Floating Action Button
            GlowButton(
                text = "+ Log Activity",
                onClick = { /* TODO */ },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .fillMaxWidth(0.6f)
            )
        }
    }
}

@Composable
private fun ActivityCard(icon: ImageVector, title: String, subtitle: String, calories: String, time: String, color: Color) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = OnSurface)
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = calories, style = MaterialTheme.typography.titleMedium, color = color)
                Text(text = time, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        }
    }
}
