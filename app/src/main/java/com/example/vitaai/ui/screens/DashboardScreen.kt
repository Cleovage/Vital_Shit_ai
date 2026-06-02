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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.NutritionSummary
import com.example.vitaai.data.local.WorkoutSessionEntity
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.data.GoalProgress
import com.example.vitaai.ui.theme.Error
import com.example.vitaai.ui.theme.OnBackground
import com.example.vitaai.ui.theme.OnPrimary
import com.example.vitaai.ui.theme.OnSurfaceVariant
import com.example.vitaai.ui.theme.OutlineVariant
import com.example.vitaai.ui.theme.Primary
import com.example.vitaai.ui.theme.SurfaceContainerHigh
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
            color = OnSurfaceVariant
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. APEX HEADER (Command Center) ---
        item {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
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
                            color = OnBackground,
                            fontWeight = FontWeight.Bold
                        )
                        if (streakDays > 0) {
                            Text(
                                text = "$streakDays DAY STREAK \uD83D\uDD25",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = { navController.navigate("activity") },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("INITIATE TRAINING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Primary.copy(alpha = 0.2f), thickness = 2.dp)
            }
        }

        // --- 2. VITALS HUB ---
        item {
            ApexCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Critical Vitals"
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    VitalItem(
                        label = "HEART",
                        value = if (snapshot.avgHeartRate > 0) snapshot.avgHeartRate.roundToInt().toString() else "--",
                        unit = "BPM",
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider(color = OutlineVariant.copy(alpha = 0.2f), modifier = Modifier.height(48.dp))
                    VitalItem(
                        label = "SLEEP",
                        value = String.format(Locale.US, "%.1f", snapshot.sleepDurationHours),
                        unit = "HRS",
                        icon = Icons.Default.Favorite, // Replace with Bed icon if available
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider(color = OutlineVariant.copy(alpha = 0.2f), modifier = Modifier.height(48.dp))
                    VitalItem(
                        label = "IDLE",
                        value = snapshot.basalCalories.roundToInt().toString(),
                        unit = "KCAL",
                        icon = Icons.Default.Whatshot,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2.5. DAILY DIRECTIVES (Gamification Goals) ---
        item {
            ApexCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Daily Directives"
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GoalProgressBar(label = "STEPS", progress = goalProgress.stepsProgress, color = Primary)
                    GoalProgressBar(label = "HYDRATION", progress = goalProgress.hydrationProgress, color = Color(0xFF00B0FF))
                    GoalProgressBar(label = "EXERCISE", progress = goalProgress.exerciseProgress, color = Color(0xFF4CAF50))
                    GoalProgressBar(label = "BURN", progress = goalProgress.caloriesBurnProgress, color = Color(0xFFFF5722))
                    
                    if (goalProgress.allGoalsMet) {
                        Text(
                            text = "ALL DIRECTIVES COMPLETED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // --- 3. DAILY PERFORMANCE GRID ---
        item {
            Text(
                "DAILY PERFORMANCE",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                letterSpacing = 1.2.sp
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
                    color = Primary
                ) {
                    navController.navigate("metric/${HealthMetricType.STEPS.route}")
                }
                DashboardMetricTile(
                    label = "TOTAL BURN",
                    value = (snapshot.calories + snapshot.basalCalories).roundToInt().toString(),
                    unit = "KCAL",
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF5722) // High contrast orange
                ) {
                    navController.navigate("metric/${HealthMetricType.ACTIVE_CALORIES.route}")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricTile(
                    label = "HYDRATION",
                    value = String.format(Locale.US, "%.1f", maxOf(snapshot.hydrationLiters, nutrition.hydrationMl / 1000.0)),
                    unit = "LITERS",
                    icon = Icons.Default.LocalDrink,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF00B0FF) // Technical Blue
                ) {
                    navController.navigate("nutrition")
                }
                DashboardMetricTile(
                    label = "PROTEIN",
                    value = nutrition.proteinGrams.roundToInt().toString(),
                    unit = "GRAMS",
                    icon = Icons.Default.Restaurant,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE91E63) // Performance Pink
                ) {
                    navController.navigate("nutrition")
                }
            }
        }

        // --- 4. FUELING & OPTIMIZATION ---
        item {
            ApexCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Fueling Status"
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroBar(nutrition)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${nutrition.calories.roundToInt()} KCAL INTAKE",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "OPTIMAL RANGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                    }
                }
            }
        }

        // --- 5. TACTICAL INSIGHTS ---
        item {
            ApexCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Tactical Analysis",
                containerColor = Primary.copy(alpha = 0.05f)
            ) {
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
                        color = OnBackground,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }


        
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun VitalItem(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = OnBackground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
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
    onClick: () -> Unit
) {
    ApexCard(modifier = modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = label, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(unit, style = androidx.compose.ui.text.TextStyle(
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            ))
            
            // Technical "Progress" Bar (Decorative)
            Box(Modifier.fillMaxWidth().height(2.dp).background(OutlineVariant.copy(alpha = 0.2f))) {
                Box(Modifier.fillMaxWidth(0.6f).fillMaxHeight().background(color))
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).background(OutlineVariant.copy(alpha = 0.2f), MaterialTheme.shapes.small)) {
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(color, MaterialTheme.shapes.small))
        }
    }
}
