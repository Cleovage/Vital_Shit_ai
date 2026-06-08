package com.example.vitaai.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.local.SleepSessionEntity
import com.example.vitaai.ui.components.*
import com.example.vitaai.ui.theme.LocalVitaColors
import com.example.vitaai.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircadianScreen(
    navController: NavController,
    viewModel: CircadianViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val vitaColors = LocalVitaColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        AuraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp) // Leave room for floating bottom bar
            ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Circadian Alignment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Circadian Clock Dial Card
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Biological Phase Clock"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircadianClockDial(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(vertical = 12.dp),
                        sleepStartHour = uiState.targetBedtimeHour.toDouble() + (uiState.targetBedtimeMinute.toDouble() / 60.0),
                        sleepEndHour = (uiState.targetBedtimeHour.toDouble() + 8.0) % 24.0 // Nominally 8 hours later
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Sleep tracking controls
                    GlowButton(
                        text = if (uiState.isTrackingSleep) "Stop Sleep Tracker" else "Start Sleep Tracker",
                        onClick = { viewModel.toggleSleepTracking(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bento Grid: Sleep Regularity, Sleep Debt, Social Jetlag, Circadian Disruption
            Text(
                text = "Circadian Vitals",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sleep Regularity Index Card
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    BentoStatCard(
                        title = "Sleep Regularity",
                        value = "${uiState.sleepRegularityIndex}%",
                        statusText = getSRIStatus(uiState.sleepRegularityIndex),
                        icon = Icons.Default.Bedtime,
                        color = Color(0xFF3F51B5),
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                
                // Sleep Debt Card
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val debtText = if (uiState.sleepDebtHours <= 0) {
                        "${String.format("%.1f", Math.abs(uiState.sleepDebtHours))}h surplus"
                    } else {
                        "${String.format("%.1f", uiState.sleepDebtHours)}h debt"
                    }
                    BentoStatCard(
                        title = "Sleep Debt",
                        value = debtText,
                        statusText = if (uiState.sleepDebtHours > 2.0) "High Deficit" else "Optimal",
                        icon = Icons.Default.Alarm,
                        color = if (uiState.sleepDebtHours > 2.0) Color(0xFFD32F2F) else Color(0xFF4CAF50),
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Social Jetlag Card
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    BentoStatCard(
                        title = "Social Jetlag",
                        value = "${String.format("%.1f", uiState.socialJetlagHours)}h",
                        statusText = if (uiState.socialJetlagHours > 1.0) "Irregular" else "Optimal",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                
                // Circadian Disruption Score
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    BentoStatCard(
                        title = "Circadian Disruption",
                        value = "${uiState.circadianDisruptionScore}/100",
                        statusText = getCDSStatus(uiState.circadianDisruptionScore),
                        icon = Icons.Default.LightMode,
                        color = if (uiState.circadianDisruptionScore > 30) Color(0xFFFF9800) else Color(0xFF00BCD4),
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Light Exposure Timeline Section
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Light Exposure Timeline"
            ) {
                LuminousLuxTimeline(
                    modifier = Modifier.fillMaxWidth(),
                    lightLogs = uiState.todayLightLogs
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Bedtime Scheduler Card
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Bedtime Reminder Schedule"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Target Bedtime",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format("%02d:%02d", uiState.targetBedtimeHour, uiState.targetBedtimeMinute),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = vitaColors.accentAmber
                            )
                        )
                    }

                    // Simple controls to adjust bedtime
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                var h = uiState.targetBedtimeHour - 1
                                if (h < 0) h = 23
                                viewModel.updateBedtimeSchedule(context, h, uiState.targetBedtimeMinute)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.03f))
                                .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                        ) {
                            Text("-1h", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                var h = uiState.targetBedtimeHour + 1
                                if (h > 23) h = 0
                                viewModel.updateBedtimeSchedule(context, h, uiState.targetBedtimeMinute)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.03f))
                                .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                        ) {
                            Text("+1h", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ChronotypeInsightCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                bedtimeHour = uiState.targetBedtimeHour
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Sleep Sessions List
            Text(
                text = "Recent Sleep History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (uiState.recentSleepSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(vitaColors.glassFill.copy(alpha = 0.02f))
                        .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sleep sessions recorded yet.\nTurn on the Sleep Tracker tonight to record logs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                uiState.recentSleepSessions.forEach { session ->
                    SleepSessionRow(session = session)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            }
        }
    }
}

@Composable
private fun BentoStatCard(
    title: String,
    value: String,
    statusText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.07f),
                shape = shape
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.Black.copy(alpha = 0.5f)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun SleepSessionRow(session: SleepSessionEntity) {
    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(16.dp)
    val sdf = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val dateText = remember(session.startTimeMillis) { sdf.format(Date(session.startTimeMillis)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.03f))
            .border(1.dp, Color.Black.copy(alpha = 0.07f), shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            Text(
                text = "${session.durationMinutes / 60}h ${session.durationMinutes % 60}m duration • ${session.source}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black.copy(alpha = 0.5f)
            )
            session.notes?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.Black.copy(alpha = 0.45f)
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (session.sleepQualityScore >= 80) Color(0xFF4CAF50).copy(alpha = 0.15f)
                    else if (session.sleepQualityScore >= 60) Color(0xFFFF9800).copy(alpha = 0.15f)
                    else Color(0xFFD32F2F).copy(alpha = 0.15f)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${session.sleepQualityScore} Quality",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (session.sleepQualityScore >= 80) Color(0xFF4CAF50)
                else if (session.sleepQualityScore >= 60) Color(0xFFFF9800)
                else Color(0xFFD32F2F)
            )
        }
    }
}

private fun getSRIStatus(sri: Int): String {
    return when {
        sri >= 90 -> "Excellent Consistency"
        sri >= 80 -> "Good Regularity"
        sri >= 70 -> "Moderate Consistency"
        else -> "Irregular Sleep"
    }
}

private fun getCDSStatus(cds: Int): String {
    return when {
        cds <= 15 -> "Optimal (Low Night Light)"
        cds <= 30 -> "Mild Exposure"
        cds <= 50 -> "Moderate Disruption"
        else -> "High Disruption"
    }
}
