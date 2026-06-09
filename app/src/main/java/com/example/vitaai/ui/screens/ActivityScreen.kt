package com.example.vitaai.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.HealthMetrics
import com.example.vitaai.data.*
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.data.local.WorkoutSessionWithSets
import com.example.vitaai.data.local.WorkoutTemplateEntity
import com.example.vitaai.ui.components.ActionRow
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.PageHeader
import com.example.vitaai.ui.components.ProgressRing
import com.example.vitaai.ui.components.LuminousLineChart
import com.example.vitaai.ui.components.LuminousBarChart
import com.example.vitaai.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ActivityScreen(
    navController: NavController,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val healthData by viewModel.healthData.collectAsState()
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
                onQueryChange = { query = it },
                healthData = healthData,
                onCreateTemplate = { name, desc, mode, rest, gps ->
                    viewModel.createTemplate(name, desc, mode, rest, gps)
                }
            )
        }
    }
}

@Composable
private fun WorkoutHome(
    navController: NavController,
    templates: List<WorkoutTemplateEntity>,
    sessions: List<WorkoutSessionWithSets>,
    query: String,
    onQueryChange: (String) -> Unit,
    healthData: ActivityHealthData,
    onCreateTemplate: (String, String, String, Int, Boolean) -> Unit
) {
    val snapshot = healthData.snapshot
    val nutrition = healthData.nutrition
    val stepGoal = healthData.stepGoal.coerceAtLeast(1L)

    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zoneId) }
    val weekLabels = remember(today) {
        val formatter = DateTimeFormatter.ofPattern("EEE", Locale.US)
        (6 downTo 0).map { offset ->
            today.minusDays(offset.toLong()).format(formatter)
        }
    }

    var selectedCategory by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredTemplates = templates.filter {
        val matchesCategory = if (selectedCategory == "ALL") true else {
            it.trackingMode.uppercase(Locale.US) == selectedCategory
        }
        val matchesQuery = query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.trackingMode.contains(query, ignoreCase = true)

        matchesCategory && matchesQuery
    }

    if (showCreateDialog) {
        CustomProtocolDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, mode, rest, gps ->
                onCreateTemplate(name, desc, mode, rest, gps)
                showCreateDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            PageHeader(title = "Health", kicker = "Movement & fuel")
        }

        // --- 2. WEB UI: STEPS & HEART RATE SIDE-BY-SIDE CARD ROWS ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Steps Card (Left Column)
                val stepsGlow = Color(0xFF06B6D4)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Ambient glow blob
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.TopEnd)
                            .graphicsLayer {
                                translationX = 10f
                                translationY = -10f
                            }
                            .blur(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(stepsGlow.copy(alpha = 0.12f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        onClick = { navController.navigate("metric/steps") }
                    ) {
                        val stepsPercent = (snapshot.steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsRun,
                                        contentDescription = null,
                                        tint = stepsGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Steps",
                                        style = VitaTextStyles.metricLabel,
                                        color = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = String.format(Locale.US, "%,d", snapshot.steps),
                                    style = VitaTextStyles.metricMedium,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "GOAL: ${String.format(Locale.US, "%,d", stepGoal)}",
                                    style = VitaTextStyles.caption,
                                    color = Color.Black.copy(alpha = 0.4f)
                                )
                            }

                            // Steps Mini Progress Ring
                            val infiniteTransition = rememberInfiniteTransition(label = "stepsAnim")
                            val yOffset by infiniteTransition.animateFloat(
                                initialValue = -2f,
                                targetValue = 2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1250, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "iconBounce"
                            )

                            Box(
                                modifier = Modifier.requiredSize(52.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProgressRing(
                                    progress = stepsPercent,
                                    size = 52.dp,
                                    strokeWidth = 4.dp,
                                    glowWidth = 8.dp,
                                    colors = listOf(stepsGlow, stepsGlow.copy(alpha = 0.5f)),
                                    glowColor = stepsGlow,
                                    startAngle = -90f,
                                    sweepAngle = 360f
                                )
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = Color(0xFF0891B2),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer {
                                            translationY = yOffset.dp.toPx()
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 8-segment progress bar indicators
                        val segmentCount = 8
                        val segmentsActive = (stepsPercent * segmentCount).roundToInt().coerceIn(0, segmentCount)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until segmentCount) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(if (i < segmentsActive) stepsGlow else stepsGlow.copy(alpha = 0.12f))
                                )
                            }
                        }
                    }
                }

                // Heart Rate Card (Right Column)
                val hrGlow = Color(0xFFF43F5E)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Ambient glow blob
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomStart)
                            .graphicsLayer {
                                translationX = -10f
                                translationY = 10f
                            }
                            .blur(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(hrGlow.copy(alpha = 0.12f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        onClick = { navController.navigate("metric/heart_rate") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = hrGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Avg heart",
                                        style = VitaTextStyles.metricLabel,
                                        color = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = HealthMetrics.formatHeartRateBpm(snapshot.avgHeartRate),
                                        style = VitaTextStyles.metricMedium,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = " bpm",
                                        style = VitaTextStyles.metricUnitLabel.copy(fontWeight = FontWeight.Medium),
                                        color = Color.Black.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = HealthMetrics.formatRestingHeartRate(
                                        resting = snapshot.restingHeartRate,
                                        avg = snapshot.avgHeartRate
                                    ),
                                    style = VitaTextStyles.caption,
                                    color = hrGlow.copy(alpha = 0.8f)
                                )
                            }

                            // Heart Rate Pulse Box
                            val infiniteTransition = rememberInfiniteTransition(label = "pulseAnim")
                            val heartScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.25f,
                                animationSpec = infiniteRepeatable(
                                    animation = keyframes {
                                        durationMillis = 1500
                                        1f at 0
                                        1.25f at 150
                                        1f at 300
                                        1.25f at 450
                                        1f at 600
                                        1f at 1500
                                    },
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "heartScale"
                            )

                            Box(
                                modifier = Modifier
                                    .requiredSize(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFE4E6))
                                    .border(1.dp, Color(0xFFFECDD3), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = heartScale
                                            scaleY = heartScale
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 11-bar vertical ECG sparkline graph
                        val ecgHeights = listOf(14f, 18f, 10f, 28f, 16f, 8f, 22f, 16f, 10f, 18f, 14f)
                        val ecgTransition = rememberInfiniteTransition(label = "ecgAnim")
                        val ecgBounceMultiplier by ecgTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "ecgBounce"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            ecgHeights.forEach { height ->
                                val finalHeight = (height * 0.8f) * ecgBounceMultiplier
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(finalHeight.dp)
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(hrGlow)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. WEB UI: CHARTS COLLECTION (Recorded Training, Food, Protein) ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recorded Training Time Chart Card
                ChartCard(
                    title = "Recorded training time",
                    value = if (snapshot.exerciseMinutes > 0.0) {
                        String.format(Locale.US, "%.1f", snapshot.exerciseMinutes)
                    } else {
                        "--"
                    },
                    unit = "MIN",
                    color = Color(0xFF06B6D4),
                    dataPoints = healthData.exerciseTrend.ifEmpty { listOf(0f, snapshot.exerciseMinutes.toFloat()) },
                    xAxisLabels = weekLabels,
                    chartType = "bar"
                )

                // Food Logged Chart Card
                ChartCard(
                    title = "Food logged in VitaAI",
                    value = if (nutrition.calories > 0.0) nutrition.calories.roundToInt().toString() else "--",
                    unit = "KCAL",
                    color = Color(0xFFEAB308),
                    dataPoints = healthData.caloriesTrend.ifEmpty { listOf(0f, nutrition.calories.toFloat()) },
                    xAxisLabels = weekLabels,
                    chartType = "bar"
                )

                // Protein intake Chart Card
                ChartCard(
                    title = "Protein intake distribution",
                    value = if (nutrition.proteinGrams > 0.0) nutrition.proteinGrams.roundToInt().toString() else "--",
                    unit = "G",
                    color = Color(0xFFF59E0B),
                    dataPoints = healthData.proteinTrend.ifEmpty { listOf(0f, nutrition.proteinGrams.toFloat()) },
                    xAxisLabels = weekLabels,
                    chartType = "line"
                )
            }
        }

        // --- 4. WEB UI: ACTIONS STACK ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionRow(
                    icon = Icons.Default.Add,
                    title = "Start adaptive session",
                    subtitle = "20 min full-body strength"
                ) {
                    navController.navigate("workout/session/adaptive_strength")
                }

                ActionRow(
                    icon = Icons.Default.Bolt,
                    title = "Recovery mobility",
                    subtitle = "Breathing, hips, shoulders"
                ) {
                    navController.navigate("workout/session/mobility_recovery")
                }

                ActionRow(
                    icon = Icons.Default.Restaurant,
                    title = "Log smart meal",
                    subtitle = "Snap a plate or type ingredients"
                ) {
                    navController.navigate("chat")
                }

                ActionRow(
                    icon = Icons.Default.Eco,
                    title = "Macro coach",
                    subtitle = "Balanced targets for training days"
                ) {
                    navController.navigate("nutrition")
                }
            }
        }

        // ────────── MOBILE ADDITION: CUSTOM PROTOCOLS & HISTORY SECTIONS ──────────
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "CUSTOM PROTOCOLS & HISTORY",
                style = VitaTextStyles.cardOverline,
                color = Color.Black.copy(alpha = 0.45f)
            )
        }

        // Search Protocols Input Bar
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                label = { Text("SEARCH PROTOCOLS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = Color.Black.copy(alpha = 0.4f),
                    cursorColor = Primary
                )
            )
        }

        // Category Segmented Chips
        item {
            val categories = listOf("ALL", "STRENGTH", "BODYWEIGHT", "CARDIO", "MOBILITY")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    val activeColor = when (category) {
                        "CARDIO" -> Primary
                        "MOBILITY" -> Color(0xFF06B6D4)
                        "STRENGTH" -> Color(0xFF3B82F6)
                        "BODYWEIGHT" -> Color(0xFF10B981)
                        else -> Primary
                    }
                    val textColor = if (isSelected) Color.White else Color.Black.copy(alpha = 0.5f)
                    val containerColor = if (isSelected) activeColor else Color.White.copy(alpha = 0.78f)
                    val borderColor = if (isSelected) activeColor else Color.Black.copy(alpha = 0.07f)

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                shadowElevation = 6f
                                shape = RoundedCornerShape(12.dp)
                                ambientShadowColor = Color.Black.copy(alpha = 0.06f)
                                spotShadowColor = Color.Black.copy(alpha = 0.08f)
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }

        // Custom Protocols Cards List
        item {
            Text(
                "ACTIVE PROTOCOLS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.5f),
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    GlassCardGlow(
                        modifier = Modifier.size(width = 180.dp, height = 150.dp),
                        onClick = { showCreateDialog = true },
                        glowColor = Primary,
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "NEW PROTOCOL",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "DESIGN CUSTOM VECTOR",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, color = Color.Black.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                items(filteredTemplates) { template ->
                    WorkoutTemplateCard(template = template, navController = navController)
                }
            }
        }

        // History list title
        item {
            Text(
                "PERFORMANCE HISTORY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.5f),
                letterSpacing = 1.2.sp
            )
        }

        if (sessions.isEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "NO SESSION DATA DETECTED. INITIATE TRAINING TO GENERATE LOGS.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color.Black.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            items(sessions) { session ->
                WorkoutSessionRow(session.session)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ChartCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    dataPoints: List<Float>,
    xAxisLabels: List<String>,
    modifier: Modifier = Modifier,
    chartType: String = "line"
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background blur orbs
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationX = 30f
                        translationY = -30f
                    }
                    .blur(45.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.12f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        style = VitaTextStyles.cardTitle.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = value,
                            style = VitaTextStyles.metricProminent,
                            color = color
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = unit,
                            style = VitaTextStyles.statLabel.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black.copy(alpha = 0.4f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                ) {
                    if (chartType == "bar") {
                        LuminousBarChart(
                            dataPoints = dataPoints,
                            barColor = color,
                            glowColor = color,
                            xAxisLabels = xAxisLabels,
                            height = 195.dp,
                            showGrid = true
                        )
                    } else {
                        LuminousLineChart(
                            dataPoints = dataPoints,
                            lineColor = color,
                            glowColor = color.copy(alpha = 0.12f),
                            xAxisLabels = xAxisLabels,
                            height = 195.dp,
                            showGrid = true
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun WorkoutTemplateCard(template: WorkoutTemplateEntity, navController: NavController) {
    val glowColor = when {
        template.trackingMode == TRACKING_CARDIO -> Primary
        template.trackingMode == TRACKING_MOBILITY -> Color(0xFF06B6D4)
        template.trackingMode == TRACKING_STRENGTH || template.trackingMode == "bodyweight" -> Color(0xFF3B82F6)
        else -> Color(0xFF64748B)
    }

    GlassCardGlow(
        modifier = Modifier.size(width = 180.dp, height = 150.dp),
        onClick = { navController.navigate("workout/session/${template.id}") },
        glowColor = glowColor,
        cornerRadius = 16.dp
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(iconForTemplate(template), contentDescription = null, tint = glowColor, modifier = Modifier.size(20.dp))
                Box(
                    Modifier
                        .background(glowColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = template.trackingMode.uppercase(Locale.US),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = glowColor)
                    )
                }
            }
            Column {
                Text(
                    text = template.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = template.description,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Black.copy(alpha = 0.5f)),
                    maxLines = 2
                )
            }
            Text(
                text = if (template.gpsEnabled) "GPS ENGINE ACTIVE" else "MANUAL METRICS",
                style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = glowColor, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun WorkoutSessionRow(session: WorkoutSessionEntity) {
    val categoryColor = when {
        session.category == TRACKING_CARDIO -> Primary
        session.category == TRACKING_MOBILITY -> Color(0xFF06B6D4)
        session.category == TRACKING_STRENGTH || session.category == "bodyweight" -> Color(0xFF3B82F6)
        else -> Color(0xFF64748B)
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(categoryColor, CircleShape)
                )
                Column {
                    Text(
                        text = session.title.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatSessionTime(session.startTimeMillis).uppercase(),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Black.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.durationSeconds / 60} MIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor,
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
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Black.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
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

@Composable
fun CustomProtocolDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, mode: String, rest: Int, gps: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(TRACKING_STRENGTH) }
    var restSeconds by remember { mutableStateOf(60f) }
    var gpsEnabled by remember { mutableStateOf(false) }

    val modes = listOf(
        TRACKING_STRENGTH to "STRENGTH",
        TRACKING_BODYWEIGHT to "BODYWEIGHT",
        TRACKING_CARDIO to "CARDIO",
        TRACKING_MOBILITY to "MOBILITY"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(28.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DESIGN PROTOCOL VECTOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Custom Template Creator",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("PROTOCOL NAME", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.4f),
                        cursorColor = Primary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.4f),
                        cursorColor = Primary
                    )
                )

                Column {
                    Text(
                        text = "TRACKING MODE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        modes.forEach { (mode, label) ->
                            val isSelected = selectedMode == mode
                            val color = when (mode) {
                                TRACKING_CARDIO -> Primary
                                TRACKING_MOBILITY -> Color(0xFF06B6D4)
                                TRACKING_STRENGTH -> Color(0xFF3B82F6)
                                else -> Color(0xFF10B981)
                            }
                            val textCol = if (isSelected) Color.White else Color.Black.copy(alpha = 0.5f)
                            val bgCol = if (isSelected) color else Color.White.copy(alpha = 0.78f)
                            val borderCol = if (isSelected) color else Color.Black.copy(alpha = 0.07f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgCol)
                                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMode = mode
                                        if (mode == TRACKING_CARDIO) {
                                            gpsEnabled = true
                                        } else {
                                            gpsEnabled = false
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                  Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = textCol
                                )
                            }
                        }
                    }
                }

                if (selectedMode != TRACKING_CARDIO) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DEFAULT REST PERIOD",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color.Black.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${restSeconds.toInt()}s",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Slider(
                            value = restSeconds,
                            onValueChange = { restSeconds = it },
                            valueRange = 0f..300f,
                            steps = 29,
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary,
                                inactiveTrackColor = Color.Black.copy(alpha = 0.08f)
                            )
                        )
                    }
                }

                if (selectedMode == TRACKING_CARDIO) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shadowElevation = 8f
                                shape = RoundedCornerShape(12.dp)
                                ambientShadowColor = Color.Black.copy(alpha = 0.08f)
                                spotShadowColor = Color.Black.copy(alpha = 0.10f)
                            }
                            .background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "GPS ENGINE ROUTE TRACKING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                "Enables absolute velocity & space recording",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, color = Color.Black.copy(alpha = 0.4f))
                            )
                        }
                        Switch(
                            checked = gpsEnabled,
                            onCheckedChange = { gpsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = Primary.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Black.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.78f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description, selectedMode, restSeconds.toInt(), gpsEnabled)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("INITIALIZE VECTOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ABORT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Error)
            }
        }
    )
}
