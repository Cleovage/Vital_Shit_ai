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
import com.example.vitaai.data.DailyGoals
import com.example.vitaai.data.HealthMetrics
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.NutritionSummary
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.PageHeader
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
                goals = state.goals,
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
    goals: DailyGoals,
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
                PageHeader(title = "VitaAI", kicker = "Health companion")
            }

            // --- 2. READINESS SCORE CARD ---
            item {
                val readinessScore = HealthMetrics.computeReadinessScore(snapshot)
                ReadinessScoreCard(
                    score = readinessScore,
                    snapshot = snapshot,
                    insight = insight,
                    onClick = { navController.navigate("circadian") }
                )
            }

            // --- 3. METRICS GRID (2x2 Bento) ---
            item {
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
                            value = HealthMetrics.formatActiveEnergyKcal(snapshot.calories),
                            tone = "amber",
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("metric/active_calories") }
                        )
                        MetricCard(
                            icon = Icons.Default.FitnessCenter,
                            label = "Training",
                            value = HealthMetrics.formatTrainingMinutes(snapshot.exerciseMinutes),
                            tone = "cyan",
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("activity") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            icon = Icons.Default.Restaurant,
                            label = "Protein",
                            value = HealthMetrics.formatProteinGrams(nutrition.proteinGrams),
                            tone = "yellow",
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("nutrition") }
                        )
                        MetricCard(
                            icon = Icons.Default.ModeNight,
                            label = "Sleep",
                            value = HealthMetrics.formatSleepHours(snapshot.sleepDurationHours),
                            tone = "blue",
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("circadian") }
                        )
                    }
                }
            }

            // --- 4. TODAY'S INSIGHTS SECTION ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Today's Insights & Tips",
                    style = VitaTextStyles.cardSectionTitle,
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
                        description = HealthMetrics.recoveryTip(snapshot.sleepDurationHours),
                        tone = "rose",
                        onClick = { navController.navigate("circadian") }
                    )
                    TipCard(
                        icon = Icons.Default.Coffee,
                        title = "Afternoon Energy Dip",
                        description = insight.ifBlank {
                            "Based on your synced activity, a short walk or mobility break can help sustain afternoon energy."
                        },
                        tone = "emerald",
                        onClick = { navController.navigate("activity") }
                    )
                    TipCard(
                        icon = Icons.Default.WaterDrop,
                        title = "Hydration Check-in",
                        description = HealthMetrics.hydrationPaceMessage(
                            hydrationLiters = maxOf(snapshot.hydrationLiters, nutrition.hydrationMl / 1000.0),
                            goalLiters = goals.hydrationGoalLiters
                        ),
                        tone = "blue",
                        onClick = { navController.navigate("nutrition") }
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
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    tone: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (iconBgColor, iconColor) = when (tone) {
        "amber" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "cyan" -> Color(0xFFCFFAFE) to Color(0xFF0E7490)
        "yellow" -> Color(0xFFFEF9C3) to Color(0xFFA16207)
        "blue" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
        else -> Color.Black.copy(alpha = 0.05f) to Color.Black.copy(alpha = 0.6f)
    }

    GlassCard(
        modifier = modifier,
        onClick = onClick
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
                    style = VitaTextStyles.metricLabel,
                    color = Color.Black.copy(alpha = 0.50f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = VitaTextStyles.metricCompact,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (iconBgColor, iconColor) = when (tone) {
        "rose" -> Color(0xFFFFE4E6) to Color(0xFFE11D48)
        "emerald" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "blue" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        else -> Color.Black.copy(alpha = 0.05f) to Color.Black.copy(alpha = 0.6f)
    }

    GlassCard(
        modifier = modifier,
        onClick = onClick
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
                    style = VitaTextStyles.cardTitle,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = VitaTextStyles.bodyPrimary,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
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

    val sleepPct = HealthMetrics.sleepProgressPercent(snapshot.sleepDurationHours)
    val heartPct = HealthMetrics.heartRecoveryPercent(snapshot)
    val readinessBadge = HealthMetrics.readinessLabel(score)

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
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                            style = VitaTextStyles.cardOverline,
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
                                text = readinessBadge,
                                style = VitaTextStyles.badge,
                                color = badgeColor
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = score.toString(),
                            style = VitaTextStyles.metricHero,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "%",
                            style = VitaTextStyles.metricUnit,
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
                                style = VitaTextStyles.metricRowLabel,
                                color = Color.Black.copy(alpha = 0.40f)
                            )
                            Text(
                                text = "$sleepPct%",
                                style = VitaTextStyles.metricRowValue,
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
                                text = "Heart",
                                style = VitaTextStyles.metricRowLabel,
                                color = Color.Black.copy(alpha = 0.40f)
                            )
                            Text(
                                text = "$heartPct%",
                                style = VitaTextStyles.metricRowValue,
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
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.08f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
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
                    .graphicsLayer {
                        shadowElevation = 8f
                        shape = RoundedCornerShape(20.dp)
                        ambientShadowColor = Color.Black.copy(alpha = 0.08f)
                        spotShadowColor = Color.Black.copy(alpha = 0.10f)
                    }
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
                        style = VitaTextStyles.bodyPrimary,
                        color = Color(0xFF0F172A).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
