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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(vitaColors.glassFill.copy(alpha = 0.6f))
                        .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Circadian Alignment",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sleep · Light · Rhythm",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Circadian Clock Dial Card
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Biological Phase Clock"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircadianClockDial(
                        modifier = Modifier
                            .size(260.dp)
                            .padding(vertical = 8.dp),
                        sleepStartHour = uiState.targetBedtimeHour.toDouble() + (uiState.targetBedtimeMinute.toDouble() / 60.0),
                        sleepEndHour = uiState.targetWakeHour.toDouble() + (uiState.targetWakeMinute.toDouble() / 60.0),
                        onSleepStartHourChanged = { hour ->
                            viewModel.updateBedtimeSchedule(context, hour.toInt(), ((hour % 1.0) * 60.0).toInt())
                        },
                        onSleepEndHourChanged = { hour ->
                            viewModel.updateWakeSchedule(hour.toInt(), ((hour % 1.0) * 60.0).toInt())
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sleep Tracking toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(vitaColors.glassFill.copy(alpha = 0.5f))
                            .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Sleep Tracking",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.isTrackingSleep) "Active — tracking in progress" else "Tap to start tonight\'s session",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = uiState.isTrackingSleep,
                            onCheckedChange = { viewModel.toggleSleepTracking(context) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color(0xFF06B6D4),
                                checkedThumbColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Premium sleep tracking CTA
                    GlowPrimaryButton(
                        text = if (uiState.isTrackingSleep) "Stop Sleep Tracker" else "Start Sleep Tracker",
                        onClick = { viewModel.toggleSleepTracking(context) },
                        glowColor = if (uiState.isTrackingSleep) com.example.vitaai.ui.theme.Tertiary else Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
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
                        color = if (uiState.sleepDebtHours > 2.0) com.example.vitaai.ui.theme.Tertiary else com.example.vitaai.ui.theme.AccentGreen,
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

            // Circadian Energy Dynamics Section
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Circadian Energy Dynamics"
            ) {
                CircadianEnergyCurve(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    wakeHour = uiState.targetWakeHour.toDouble() + (uiState.targetWakeMinute.toDouble() / 60.0),
                    sleepHour = uiState.targetBedtimeHour.toDouble() + (uiState.targetBedtimeMinute.toDouble() / 60.0)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Light Exposure Timeline Section
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Light Exposure Timeline"
            ) {
                LuminousLuxTimeline(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    lightLogs = uiState.todayLightLogs
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Bedtime Scheduler Card
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Bedtime Reminder"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Target Bedtime",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        Text(
                            text = String.format("%02d:%02d", uiState.targetBedtimeHour, uiState.targetBedtimeMinute),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = vitaColors.accentAmber,
                                letterSpacing = (-1).sp
                            )
                        )
                    }

                    // Bedtime adjustment controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(vitaColors.glassFill.copy(alpha = 0.6f))
                                .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .clickable {
                                    var h = uiState.targetBedtimeHour - 1
                                    if (h < 0) h = 23
                                    viewModel.updateBedtimeSchedule(context, h, uiState.targetBedtimeMinute)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "-1h",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(vitaColors.accentAmber.copy(alpha = 0.12f))
                                .border(1.dp, vitaColors.accentAmber.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable {
                                    var h = uiState.targetBedtimeHour + 1
                                    if (h > 23) h = 0
                                    viewModel.updateBedtimeSchedule(context, h, uiState.targetBedtimeMinute)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+1h",
                                color = vitaColors.accentAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Wake-up Scheduler Card
            ApexCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Wake-up Schedule"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Target Wake-up",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        Text(
                            text = String.format("%02d:%02d", uiState.targetWakeHour, uiState.targetWakeMinute),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = (-1).sp
                            )
                        )
                    }

                    // Wake-up adjustment controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(vitaColors.glassFill.copy(alpha = 0.6f))
                                .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .clickable {
                                    var h = uiState.targetWakeHour - 1
                                    if (h < 0) h = 23
                                    viewModel.updateWakeSchedule(h, uiState.targetWakeMinute)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "-1h",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Primary.copy(alpha = 0.12f))
                                .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .clickable {
                                    var h = uiState.targetWakeHour + 1
                                    if (h > 23) h = 0
                                    viewModel.updateWakeSchedule(h, uiState.targetWakeMinute)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+1h",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
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
                        .graphicsLayer {
                            shadowElevation = 10f
                            shape = RoundedCornerShape(24.dp)
                            ambientShadowColor = Color.Black.copy(alpha = 0.08f)
                            spotShadowColor = Color.Black.copy(alpha = 0.10f)
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(vitaColors.glassFill.copy(alpha = 0.45f))
                        .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
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
    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(24.dp)
    val statShadowColor = Color.Black.copy(alpha = 0.10f).toArgb()
    val elevPx = 10f

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Paint-based Gaussian shadow for real depth outside clip
            .drawWithCache {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        setColor(android.graphics.Color.TRANSPARENT)
                        setShadowLayer(elevPx * 2.2f, 0f, elevPx * 0.8f, statShadowColor)
                    }
                }
                onDrawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = 24.dp.toPx(),
                            radiusY = 24.dp.toPx(),
                            paint = paint
                        )
                    }
                }
            }
            // Hardware-accelerated shadow layer
            .graphicsLayer(
                shadowElevation = 10f,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.08f),
                spotShadowColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(vitaColors.glassFill.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = vitaColors.glassBorderDark.copy(alpha = 0.09f),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                color = color
            )
        }
    }
}

@Composable
private fun SleepSessionRow(session: SleepSessionEntity) {
    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(20.dp)
    val sdf = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val dateText = remember(session.startTimeMillis) { sdf.format(Date(session.startTimeMillis)) }
    val qualityColor = when {
        session.sleepQualityScore >= 80 -> com.example.vitaai.ui.theme.AccentGreen
        session.sleepQualityScore >= 60 -> com.example.vitaai.ui.theme.AccentAmber
        else -> com.example.vitaai.ui.theme.Tertiary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(vitaColors.glassFill.copy(alpha = 0.65f))
            .border(1.dp, vitaColors.glassBorderDark.copy(alpha = 0.09f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${session.durationMinutes / 60}h ${session.durationMinutes % 60}m  ·  ${session.source}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            session.notes?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${session.sleepQualityScore}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = qualityColor
                )
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(qualityColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = when {
                        session.sleepQualityScore >= 80 -> "Great"
                        session.sleepQualityScore >= 60 -> "Good"
                        else -> "Poor"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    ),
                    color = qualityColor
                )
            }
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
