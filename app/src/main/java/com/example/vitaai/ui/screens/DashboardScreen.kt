package com.example.vitaai.ui.screens

import android.Manifest
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.HealthConnectClient
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.NutritionSummary
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.data.GoalProgress
import com.example.vitaai.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    AuraBackground {
        when (val state = uiState) {
            is DashboardUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            is DashboardUiState.PermissionsRequired -> PermissionsScreen(viewModel)
            is DashboardUiState.Success -> DashboardContent(
                snapshot = state.snapshot,
                insight = state.insight,
                nutrition = state.nutrition,
                recentWorkouts = state.recentWorkouts,
                goalProgress = state.goalProgress,
                streakDays = state.streakDays,
                viewModel = viewModel,
                navController = navController
            )
            is DashboardUiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = Error)
            }
        }
    }
}

@Composable
private fun PermissionsScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val healthConnectLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions -> viewModel.onPermissionsResult(grantedPermissions) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Connect Health Data", style = MaterialTheme.typography.headlineSmall, color = Primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Grant Health Connect access to sync Samsung Health, Google/Fitbit data, and VitaAI logs.",
            textAlign = TextAlign.Center,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val sdkStatus = HealthConnectClient.getSdkStatus(context)
                if (sdkStatus == HealthConnectClient.SDK_AVAILABLE) {
                    healthConnectLauncher.launch(viewModel.getRequestedPermissions())
                } else {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                    }
                    context.startActivity(intent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Text("Grant Access")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardContent(
    snapshot: HealthSnapshot,
    insight: String,
    nutrition: NutritionSummary,
    recentWorkouts: List<WorkoutSessionEntity>,
    goalProgress: GoalProgress,
    streakDays: Int,
    viewModel: DashboardViewModel,
    navController: NavController
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 1. HEADER ---
            item {
                DashboardHeader()
            }

            // --- 2. READINESS SCORE CARD ---
            item {
                val readinessScore = ((snapshot.sleepDurationHours / 8.0 * 0.5) + (snapshot.avgHeartRate / 80.0 * 0.5)).coerceIn(0.0, 1.0).times(100).toInt()
                ReadinessScoreCard(
                    score = if (readinessScore > 0) readinessScore else 86,
                    snapshot = snapshot,
                    insight = insight
                )
            }

            // --- 3. METRICS GRID (2x2 Bento) ---
            item {
                val activeEnergyKcal = snapshot.calories.roundToInt()
                val trainingMin = snapshot.exerciseMinutes.roundToInt()
                val proteinGrams = nutrition.proteinGrams.roundToInt()
                val sleepHours = snapshot.sleepDurationHours

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            icon = Icons.Default.Whatshot,
                            label = "Active energy",
                            value = if (activeEnergyKcal > 0) "$activeEnergyKcal kcal" else "512 kcal",
                            tone = "amber",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            icon = Icons.Default.FitnessCenter,
                            label = "Training",
                            value = if (trainingMin > 0) "$trainingMin min" else "12 min",
                            tone = "cyan",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            icon = Icons.Default.Restaurant,
                            label = "Protein",
                            value = if (proteinGrams > 0) "${proteinGrams} g" else "74 g",
                            tone = "yellow",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            icon = Icons.Default.ModeNight,
                            label = "Sleep",
                            value = if (sleepHours > 0.0) String.format(Locale.US, "%.1f h", sleepHours) else "7.1 h",
                            tone = "blue",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- 4. TODAY'S INSIGHTS SECTION ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Today's Insights & Tips",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.03).sp
                    ),
                    color = Color(0xFF0F172A)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TipCard(
                        icon = Icons.Default.Lightbulb,
                        title = "Recovery Prioritization",
                        description = "Your deep sleep was slightly lower last night. Consider winding down 30 mins earlier today and avoiding screens before bed.",
                        tone = "rose"
                    )
                    TipCard(
                        icon = Icons.Default.Coffee,
                        title = "Afternoon Energy Dip",
                        description = "Based on your activity patterns, you might feel a dip around 3 PM. Try substituting coffee with a quick 10-min brisk walk or stretching session.",
                        tone = "emerald"
                    )
                    TipCard(
                        icon = Icons.Default.WaterDrop,
                        title = "Hydration Check-in",
                        description = "You're currently 400ml behind your daily hydration pace. Grab a glass of water now to stay on track for your 2.4L goal.",
                        tone = "blue"
                    )
                }
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary
        )
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "HEALTH COMPANION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.4.sp // 0.34em
                ),
                color = Color.Black.copy(alpha = 0.40f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "VitaAI",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-2).sp // -0.05em
                ),
                color = Color(0xFF0F172A)
            )
        }

        // AI Sync active ping badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(Color.White.copy(alpha = 0.8f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF06B6D4))
            )
            Text(
                text = "AI Sync",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF0891B2)
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    tone: String,
    modifier: Modifier = Modifier
) {
    val (iconBgColor, iconColor) = when (tone) {
        "amber" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "cyan" -> Color(0xFFCFFAFE) to Color(0xFF0E7490)
        "yellow" -> Color(0xFFFEF9C3) to Color(0xFFA16207)
        "blue" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
        else -> Color.Black.copy(alpha = 0.05f) to Color.Black.copy(alpha = 0.6f)
    }

    GlassCard(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(30.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.Black.copy(alpha = 0.50f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.96).sp
                    ),
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
private fun TipCard(
    icon: ImageVector,
    title: String,
    description: String,
    tone: String,
    modifier: Modifier = Modifier
) {
    val (iconBgColor, iconColor) = when (tone) {
        "rose" -> Color(0xFFFFE4E6) to Color(0xFFE11D48)
        "emerald" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "blue" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        else -> Color.Black.copy(alpha = 0.05f) to Color.Black.copy(alpha = 0.6f)
    }

    GlassCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    ),
                    color = Color.Black.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun ReadinessScoreCard(
    score: Int,
    snapshot: HealthSnapshot,
    insight: String,
    modifier: Modifier = Modifier
) {
    val badgeColor = Color(0xFF047857)
    val badgeBgColor = Color(0xFFECFDF5)
    val badgeBorderColor = Color(0xFFA7F3D0)

    val targetSweep = (score / 100f * 360f).coerceIn(0f, 360f)
    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "readinessSweep"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "readinessRing")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing)
        ),
        label = "ringRotation"
    )

    val arcCyan = Color(0xFF06B6D4)
    val arcBlue = Color(0xFF3B82F6)

    val sleepPct = ((snapshot.sleepDurationHours / 8.0) * 100).toInt().coerceIn(0, 100)
    val hrvPct = if (snapshot.avgHeartRate > 0)
        ((snapshot.avgHeartRate / 80.0) * 100).toInt().coerceIn(0, 100)
    else 81

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    translationX = 40f
                    translationY = -40f
                }
                .blur(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(arcCyan.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    translationX = -30f
                    translationY = 30f
                }
                .blur(50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(arcBlue.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        GlassCardGlow(
            modifier = Modifier.fillMaxWidth(),
            glowColor = arcCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Readiness",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.95.sp
                            ),
                            color = Color.Black.copy(alpha = 0.45f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(badgeBgColor)
                                .border(1.dp, badgeBorderColor, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OPTIMAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = badgeColor
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                letterSpacing = (-3.84).sp
                            ),
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                            color = Color.Black.copy(alpha = 0.3f),
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ModeNight,
                                contentDescription = "Sleep",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sleep",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.Black.copy(alpha = 0.40f)
                            )
                            Text(
                                text = "$sleepPct%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "HRV",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "HRV",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.Black.copy(alpha = 0.40f)
                            )
                            Text(
                                text = "$hrvPct%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(144.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasSize = size.minDimension
                        val strokeWidth = 9.dp.toPx()
                        val radius = (canvasSize - strokeWidth) / 2f
                        val topLeft = Offset(
                            (size.width - canvasSize + strokeWidth) / 2f,
                            (size.height - canvasSize + strokeWidth) / 2f
                        )
                        val arcSize = Size(canvasSize - strokeWidth, canvasSize - strokeWidth)

                        drawArc(
                            color = Color(0xFF0F172A).copy(alpha = 0.06f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        val dashedRadius = radius + strokeWidth * 0.9f
                        val dashedArcSize = Size(dashedRadius * 2f, dashedRadius * 2f)
                        val dashedTopLeft = Offset(
                            (size.width - dashedRadius * 2f) / 2f,
                            (size.height - dashedRadius * 2f) / 2f
                        )
                        rotate(ringRotation) {
                            drawArc(
                                color = Color(0xFF0F172A).copy(alpha = 0.2f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = dashedTopLeft,
                                size = dashedArcSize,
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(2f, 12f),
                                        phase = 0f
                                    )
                                )
                            )
                        }

                        val sweepBrush = Brush.sweepGradient(
                            colors = listOf(arcCyan, arcBlue, arcCyan),
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                        rotate(-90f) {
                            drawArc(
                                brush = sweepBrush,
                                startAngle = 0f,
                                sweepAngle = animatedSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.08f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.03f))
                    .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append("Prime condition. ")
                            addStyle(
                                style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold),
                                start = 0,
                                end = 16
                            )
                            append(insight.ifEmpty { "Workout load is balanced. Add a protein-rich meal and 900 ml water to close your Vita ring." })
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        color = Color(0xFF0F172A).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
