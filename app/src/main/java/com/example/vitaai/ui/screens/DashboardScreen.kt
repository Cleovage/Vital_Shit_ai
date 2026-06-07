package com.example.vitaai.ui.screens

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.NutritionSummary
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.ProgressRing
import com.example.vitaai.data.GoalProgress
import com.example.vitaai.ui.theme.*
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun StreakFireBadge(streakDays: Int, modifier: Modifier = Modifier) {
    var isTapped by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isTapped) 1.25f else 1.0f, label = "streakScale")
    
    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { isTapped = !isTapped }
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFFFF5722).copy(alpha = 0.18f))
            .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.45f), MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = "Streak",
                tint = Color(0xFFFF7043),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$streakDays DAY STREAK",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFAB91),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun HydrationCup(
    amountMl: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = Color(0xFF00B0FF).copy(alpha = 0.25f),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocalDrink,
                contentDescription = "+$amountMl ml",
                tint = Color(0xFF4FC3F7),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "+$amountMl ML",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun RecentWorkoutSessionRow(session: WorkoutSessionEntity) {
    val categoryColor = when (session.category.lowercase()) {
        "cardio" -> Color(0xFF00E676)
        "strength" -> Color(0xFFD500F9)
        "mobility" -> Color(0xFF2979FF)
        else -> Color(0xFF00B0FF)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(categoryColor.copy(alpha = 0.05f))
            .border(1.dp, categoryColor.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(categoryColor, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = session.title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatSessionTime(session.startTimeMillis).uppercase(),
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = GlebSlate400)
                )
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
                        else -> "COMPLETED"
                    }
                }
                Text(
                    text = detailText,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = GlebSlate400)
                )
            }
        }
    }
}

private fun formatSessionTime(millis: Long): String {
    return DateTimeFormatter.ofPattern("MMM dd, HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
}

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
            color = GlebSlate400
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { healthConnectLauncher.launch(viewModel.getRequestedPermissions()) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Text("Grant Access")
        }
    }
}

@Composable
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. APEX HEADER (Command Center) ---
        item {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMAND CENTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OPERATIONAL",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Button(
                        onClick = { navController.navigate("activity") },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("INITIATE TRAINING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                if (streakDays > 0) {
                    Spacer(Modifier.height(8.dp))
                    StreakFireBadge(streakDays = streakDays)
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Primary.copy(alpha = 0.2f), thickness = 2.dp)
            }
        }

        // --- 2. VITALS HUB (Circular Progress Rings HUD) ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "CRITICAL VITALS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val heartProgress = if (snapshot.avgHeartRate > 0) (snapshot.avgHeartRate.toFloat() / 150f).coerceIn(0f, 1f) else 0f
                    VitalProgressRingItem(
                        label = "HEART",
                        value = if (snapshot.avgHeartRate > 0) snapshot.avgHeartRate.roundToInt().toString() else "--",
                        unit = "BPM",
                        icon = Icons.Default.Favorite,
                        progress = heartProgress,
                        ringColors = listOf(Color(0xFFF48FB1), Color(0xFFFF2D55)),
                        ringGlowColor = Color(0xFFF48FB1),
                        modifier = Modifier.weight(1f)
                    )
                    
                    VerticalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.height(48.dp))
                    
                    val sleepProgress = (snapshot.sleepDurationHours.toFloat() / 8f).coerceIn(0f, 1f)
                    VitalProgressRingItem(
                        label = "SLEEP",
                        value = String.format(Locale.US, "%.1f", snapshot.sleepDurationHours),
                        unit = "HRS",
                        icon = Icons.Default.DirectionsRun,
                        progress = sleepProgress,
                        ringColors = listOf(Color(0xFF90CAF9), Color(0xFF2979FF)),
                        ringGlowColor = Color(0xFF90CAF9),
                        modifier = Modifier.weight(1f)
                    )
                    
                    VerticalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.height(48.dp))
                    
                    val idleProgress = (snapshot.basalCalories.toFloat() / 2000f).coerceIn(0f, 1f)
                    VitalProgressRingItem(
                        label = "IDLE",
                        value = snapshot.basalCalories.roundToInt().toString(),
                        unit = "KCAL",
                        icon = Icons.Default.Whatshot,
                        progress = idleProgress,
                        ringColors = listOf(Color(0xFFFFCC80), Color(0xFFFF9100)),
                        ringGlowColor = Color(0xFFFFCC80),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2.5. DAILY DIRECTIVES (Gamification Goals) ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "DAILY DIRECTIVES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GoalProgressBar(label = "STEPS", progress = goalProgress.stepsProgress, color = Primary)
                    GoalProgressBar(label = "HYDRATION", progress = goalProgress.hydrationProgress, color = Color(0xFF00B0FF))
                    GoalProgressBar(label = "EXERCISE", progress = goalProgress.exerciseProgress, color = Color(0xFF00E676))
                    GoalProgressBar(label = "BURN", progress = goalProgress.caloriesBurnProgress, color = Color(0xFFFF5722))
                    
                    if (goalProgress.allGoalsMet) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF00E676).copy(alpha = 0.1f), MaterialTheme.shapes.small)
                                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), MaterialTheme.shapes.small)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ALL DIRECTIVES COMPLETED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB9F6CA),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 3. DAILY PERFORMANCE GRID ---
        item {
            Text(
                "DAILY PERFORMANCE",
                style = MaterialTheme.typography.labelSmall,
                color = GlebSlate400.copy(alpha = 0.8f),
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricTile(
                    label = "STEPS",
                    value = snapshot.steps.toString(),
                    unit = "TOTAL",
                    icon = Icons.Default.DirectionsRun,
                    modifier = Modifier.weight(1f),
                    color = Primary,
                    progress = goalProgress.stepsProgress
                ) {
                    navController.navigate("metric/${HealthMetricType.STEPS.route}")
                }
                DashboardMetricTile(
                    label = "ACTIVE BURN",
                    value = snapshot.calories.roundToInt().toString(),
                    unit = "KCAL",
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF1744),
                    progress = goalProgress.caloriesBurnProgress
                ) {
                    navController.navigate("metric/${HealthMetricType.ACTIVE_CALORIES.route}")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricTile(
                    label = "TOTAL BURN",
                    value = (snapshot.calories + snapshot.basalCalories).roundToInt().toString(),
                    unit = "KCAL",
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF9100),
                    progress = ((snapshot.calories + snapshot.basalCalories) / 2500.0).toFloat().coerceIn(0f, 1f)
                ) {
                    navController.navigate("metric/${HealthMetricType.ACTIVE_CALORIES.route}")
                }
                DashboardMetricTile(
                    label = "HYDRATION",
                    value = String.format(Locale.US, "%.1f", maxOf(snapshot.hydrationLiters, nutrition.hydrationMl / 1000.0)),
                    unit = "LITERS",
                    icon = Icons.Default.LocalDrink,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF00B0FF),
                    progress = goalProgress.hydrationProgress
                ) {
                    navController.navigate("nutrition")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricTile(
                    label = "PROTEIN",
                    value = nutrition.proteinGrams.roundToInt().toString(),
                    unit = "GRAMS",
                    icon = Icons.Default.Restaurant,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE91E63),
                    progress = (nutrition.proteinGrams / 150.0).toFloat().coerceIn(0f, 1f)
                ) {
                    navController.navigate("nutrition")
                }
                Box(Modifier.weight(1f))
            }
        }

        // --- 4. QUICK HYDRATION LOG ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "QUICK HYDRATION LOG",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Tap to record hydration instant-sync:",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlebSlate400.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HydrationCup(
                            amountMl = 250,
                            onClick = { viewModel.logWater(8) }, // 8 oz = ~250ml
                            modifier = Modifier.weight(1f)
                        )
                        HydrationCup(
                            amountMl = 500,
                            onClick = { viewModel.logWater(17) }, // 17 oz = ~500ml
                            modifier = Modifier.weight(1f)
                        )
                        HydrationCup(
                            amountMl = 750,
                            onClick = { viewModel.logWater(25) }, // 25 oz = ~750ml
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 4.5. FUELING & OPTIMIZATION ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "FUELING STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroBar(nutrition)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${nutrition.calories.roundToInt()} KCAL INTAKE",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlebSlate400.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "OPTIMAL FUEL RANGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 5. TACTICAL INSIGHTS ---
        item {
            GlassCardGlow(
                modifier = Modifier.fillMaxWidth(),
                glowColor = Primary
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "TACTICAL ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(4.dp, 40.dp)
                            .background(Primary, MaterialTheme.shapes.extraSmall)
                    )
                    Text(
                        text = insight.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // --- 6. RECENT WORKOUT PROTOCOLS ---
        if (recentWorkouts.isNotEmpty()) {
            item {
                Text(
                    "RECENT COMPLETED PROTOCOLS",
                    style = MaterialTheme.typography.labelSmall,
                    color = GlebSlate400.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(recentWorkouts) { session ->
                RecentWorkoutSessionRow(session)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun VitalProgressRingItem(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    progress: Float,
    ringColors: List<Color>,
    ringGlowColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                ProgressRing(
                    progress = progress,
                    size = 52.dp,
                    strokeWidth = 5.dp,
                    glowWidth = 8.dp,
                    colors = ringColors,
                    glowColor = ringGlowColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ringGlowColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = GlebSlate400.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = unit,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlebSlate400.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricTile(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = Primary,
    progress: Float = 0.6f,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = GlebSlate400.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Icon(icon, contentDescription = label, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlebSlate400.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                ), modifier = Modifier.padding(bottom = 4.dp))
            }
            
            Box(Modifier.fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.small)) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(color, MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
private fun MacroBar(nutrition: NutritionSummary) {
    val total = (nutrition.proteinGrams + nutrition.carbsGrams + nutrition.fatGrams).coerceAtLeast(1.0)
    Row(Modifier.fillMaxWidth().height(8.dp).background(SurfaceContainerHigh, MaterialTheme.shapes.small)) {
        Box(Modifier.weight((nutrition.proteinGrams / total).toFloat().coerceAtLeast(0.05f)).fillMaxSize().background(Primary, MaterialTheme.shapes.small))
        Box(Modifier.weight((nutrition.carbsGrams / total).toFloat().coerceAtLeast(0.05f)).fillMaxSize().background(Primary.copy(alpha = 0.55f)))
        Box(Modifier.weight((nutrition.fatGrams / total).toFloat().coerceAtLeast(0.05f)).fillMaxSize().background(Primary.copy(alpha = 0.28f)))
    }
}

@Composable
private fun GoalProgressBar(label: String, progress: Float, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GlebSlate400.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.small)) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(color, MaterialTheme.shapes.small))
        }
    }
}
