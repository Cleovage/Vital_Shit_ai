package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.LocalVitaColors

data class ChronotypeDefinition(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val glowColor: Color,
    val sleepWindow: String,
    val peakFocus: String,
    val exerciseWindow: String,
    val coachingAdvice: String
)

@Composable
fun ChronotypeInsightCard(
    modifier: Modifier = Modifier,
    bedtimeHour: Int
) {
    val vitaColors = LocalVitaColors.current
    
    // Determine Chronotype based on Bedtime Hour
    val chronotype = when {
        bedtimeHour < 22 -> ChronotypeDefinition(
            name = "LION CHRONOTYPE",
            description = "Early Riser",
            icon = Icons.Default.WbSunny,
            glowColor = Color(0xFFF59E0B), // Warm amber
            sleepWindow = "9:30 PM - 5:30 AM",
            peakFocus = "8:00 AM - 12:00 PM",
            exerciseWindow = "6:30 AM - 7:30 AM (Morning)",
            coachingAdvice = "Maximize your early morning peak focus window for analytical tasks. Wind down early to preserve deep sleep cycles."
        )
        bedtimeHour in 22..23 -> ChronotypeDefinition(
            name = "BEAR CHRONOTYPE",
            description = "Solar-Aligned",
            icon = Icons.Default.LightMode,
            glowColor = Color(0xFF10B981), // Emerald
            sleepWindow = "10:30 PM - 6:30 AM",
            peakFocus = "10:00 AM - 2:00 PM",
            exerciseWindow = "7:00 AM or 5:30 PM",
            coachingAdvice = "Your biological clock tracks the sun closely. Take an active midday recovery break to avoid the afternoon energy dip."
        )
        bedtimeHour in 0..1 -> ChronotypeDefinition(
            name = "WOLF CHRONOTYPE",
            description = "Night Owl",
            icon = Icons.Default.ModeNight,
            glowColor = Color(0xFF6366F1), // Indigo/purple
            sleepWindow = "12:00 AM - 8:00 AM",
            peakFocus = "4:00 PM - 8:00 PM",
            exerciseWindow = "6:00 PM - 7:30 PM (Evening)",
            coachingAdvice = "Embrace your evening creative sprint. Use low-intensity warm light (lux < 50) past 10 PM to prevent circadian phase delay."
        )
        else -> ChronotypeDefinition(
            name = "DOLPHIN CHRONOTYPE",
            description = "Light Sleeper",
            icon = Icons.Default.Waves,
            glowColor = Color(0xFF06B6D4), // Cyan
            sleepWindow = "11:30 PM - 6:30 AM",
            peakFocus = "1:00 PM - 4:00 PM",
            exerciseWindow = "7:30 AM (Calming mobility)",
            coachingAdvice = "As a light sleeper, prioritize cooling environments and breathing protocols to naturally lower core temperature before bed."
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ChronotypeBlob")
    val blobOpacity by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobOpacity"
    )
    val blobScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobScale"
    )

    GlassCardGlow(
        modifier = modifier.fillMaxWidth(),
        glowColor = chronotype.glowColor,
        cornerRadius = 24.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Animated ambient glow blob in top-right corner
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .graphicsLayer {
                        scaleX = blobScale
                        scaleY = blobScale
                        alpha = blobOpacity
                    }
                    .blur(35.dp)
                    .background(chronotype.glowColor, CircleShape)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = chronotype.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = chronotype.glowColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = chronotype.description,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(chronotype.glowColor.copy(alpha = 0.12f))
                            .border(1.dp, chronotype.glowColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = chronotype.icon,
                            contentDescription = null,
                            tint = chronotype.glowColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule metrics list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScheduleRow(label = "Ideal Sleep Window", value = chronotype.sleepWindow, icon = Icons.Default.Bedtime, color = chronotype.glowColor)
                    ScheduleRow(label = "Cognitive Focus Peak", value = chronotype.peakFocus, icon = Icons.Default.Lightbulb, color = chronotype.glowColor)
                    ScheduleRow(label = "Exercise Peak Time", value = chronotype.exerciseWindow, icon = Icons.Default.FitnessCenter, color = chronotype.glowColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coaching advice text box
                Text(
                    text = "VITA COACH RECOMMENDATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.Black.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chronotype.coachingAdvice,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = Color.Black.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black.copy(alpha = 0.5f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF0F172A)
        )
    }
}
