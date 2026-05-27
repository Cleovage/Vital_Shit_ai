package com.example.vitaai.ui.screens

import android.Manifest
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.HealthDataSource
import com.example.vitaai.data.HealthMetricType
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.data.MoodEntry
import com.example.vitaai.data.PrimaryDashboardMetrics
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.Error
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
    val liveSteps by viewModel.liveSteps.collectAsState()

    AuraBackground {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is DashboardUiState.PermissionsRequired -> {
                PermissionsScreen(viewModel = viewModel)
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    snapshot = state.snapshot,
                    insight = state.insight,
                    mood = state.mood,
                    liveSteps = liveSteps,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            is DashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = Error)
                }
            }
        }
    }
}

@Composable
private fun PermissionsScreen(viewModel: DashboardViewModel) {
    val healthConnectLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        healthConnectLauncher.launch(viewModel.getRequestedPermissions())
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Connect to Health Data", style = MaterialTheme.typography.headlineSmall, color = Primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "VitaAI needs health permissions to sync your Samsung Health and Google/Fitbit connected data.",
            textAlign = TextAlign.Center,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Text("GRANT ACCESS")
        }
    }
}

@Composable
private fun DashboardContent(
    snapshot: HealthSnapshot,
    insight: String,
    mood: MoodEntry?,
    liveSteps: Long,
    viewModel: DashboardViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APEX VITALITY",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp, letterSpacing = (-1).sp),
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier.size(32.dp).clip(MaterialTheme.shapes.small).background(SurfaceContainerHigh).border(1.dp, OutlineVariant, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Primary)
                }
            }
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
        }

        item {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HOW ARE YOU FEELING?", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val labels = listOf("1", "2", "3", "4", "5")
                        labels.forEachIndexed { index, label ->
                            val score = (index + 1) * 2
                            val selected = mood?.score == score
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(if (selected) Primary else SurfaceContainerHigh)
                                    .clickable { viewModel.recordMood(score) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) OnPrimary else Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TODAY'S FOCUS", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = insight, style = MaterialTheme.typography.bodyMedium, color = Primary)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(
                    text = "SYNCED METRICS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any tile to open complete charts, source coverage, and metric details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }

        PrimaryDashboardMetrics.chunked(2).forEach { rowMetrics ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMetrics.forEach { metric ->
                        MetricTile(
                            metric = metric,
                            snapshot = snapshot,
                            liveSteps = liveSteps,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("metric/${metric.route}") }
                        )
                    }
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FULL TRACKABLE CATALOG", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Samsung Health + Google Health/Fitbit + VitaAI coverage",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enable sync for Samsung Health and Fitbit inside Health Connect app permissions.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthMetricType.entries.forEach { metric ->
                        CatalogRow(
                            metric = metric,
                            onClick = { navController.navigate("metric/${metric.route}") }
                        )
                    }
                }
            }
        }

        item {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "QUICK PROTOCOL", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Start tracking live metrics", style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("session") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(text = "START PROTOCOL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricTile(
    metric: HealthMetricType,
    snapshot: HealthSnapshot,
    liveSteps: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val value = metricPrimaryValue(metric, snapshot, liveSteps)
    val subValue = metricSecondaryValue(metric)
    val icon = metricIcon(metric)

    ApexCard(
        modifier = modifier
            .aspectRatio(1.15f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = metric.shortLabel.uppercase(Locale.US),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Icon(icon, contentDescription = metric.title, tint = Primary, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                if (subValue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = subValue, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CatalogRow(metric: HealthMetricType, onClick: () -> Unit) {
    val sourceLabel = metric.sources.joinToString(" | ") { source ->
        when (source) {
            HealthDataSource.SAMSUNG_HEALTH -> "Samsung"
            HealthDataSource.GOOGLE_HEALTH_FITBIT -> "Google/Fitbit"
            HealthDataSource.VITA_AI -> "VitaAI"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(text = metric.title, style = MaterialTheme.typography.bodyMedium, color = Primary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = sourceLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

private fun metricPrimaryValue(metric: HealthMetricType, snapshot: HealthSnapshot, liveSteps: Long): String {
    return when (metric) {
        HealthMetricType.ACTIVE_CALORIES -> snapshot.calories.roundToInt().toString()
        HealthMetricType.STEPS -> maxOf(snapshot.steps, liveSteps).toString()
        HealthMetricType.HEART_RATE -> if (snapshot.avgHeartRate > 0) snapshot.avgHeartRate.roundToInt().toString() else "--"
        HealthMetricType.SLEEP -> String.format(Locale.US, "%.1f", snapshot.sleepDurationHours)
        HealthMetricType.DISTANCE -> String.format(Locale.US, "%.2f", snapshot.distanceMeters / 1000.0)
        HealthMetricType.HYDRATION -> String.format(Locale.US, "%.2f", snapshot.hydrationLiters)
        HealthMetricType.EXERCISE_MINUTES -> snapshot.exerciseMinutes.roundToInt().toString()
        HealthMetricType.CALORIES_INTAKE -> snapshot.caloriesIntake.roundToInt().toString()
        HealthMetricType.PROTEIN -> snapshot.proteinGrams.roundToInt().toString()
        HealthMetricType.CARBOHYDRATE -> snapshot.carbsGrams.roundToInt().toString()
        HealthMetricType.FAT -> snapshot.fatGrams.roundToInt().toString()
        else -> "0"
    }
}

private fun metricSecondaryValue(metric: HealthMetricType): String {
    return when (metric) {
        HealthMetricType.SLEEP -> "hours"
        HealthMetricType.DISTANCE -> "km today"
        HealthMetricType.HYDRATION -> "liters today"
        HealthMetricType.HEART_RATE -> "avg bpm"
        HealthMetricType.EXERCISE_MINUTES -> "minutes today"
        HealthMetricType.CALORIES_INTAKE -> "kcal consumed"
        HealthMetricType.PROTEIN, HealthMetricType.CARBOHYDRATE, HealthMetricType.FAT -> "grams today"
        else -> "${metric.unit} today"
    }
}

private fun metricIcon(metric: HealthMetricType): ImageVector {
    return when (metric) {
        HealthMetricType.ACTIVE_CALORIES -> Icons.Default.Bolt
        HealthMetricType.STEPS -> Icons.Default.DirectionsRun
        HealthMetricType.HEART_RATE -> Icons.Default.Favorite
        HealthMetricType.SLEEP -> Icons.Default.Timeline
        HealthMetricType.DISTANCE -> Icons.Default.LocationOn
        HealthMetricType.HYDRATION -> Icons.Default.LocalDrink
        HealthMetricType.EXERCISE_MINUTES -> Icons.Default.Timeline
        HealthMetricType.CALORIES_INTAKE -> Icons.Default.Restaurant
        HealthMetricType.PROTEIN -> Icons.Default.ShowChart
        HealthMetricType.CARBOHYDRATE -> Icons.Default.PieChart
        HealthMetricType.FAT -> Icons.Default.WaterDrop
        HealthMetricType.RESTING_HEART_RATE -> Icons.Default.Favorite
        HealthMetricType.HEART_RATE_VARIABILITY -> Icons.Default.ShowChart
        HealthMetricType.BLOOD_OXYGEN -> Icons.Default.Favorite
        HealthMetricType.RESPIRATORY_RATE -> Icons.Default.Timeline
        HealthMetricType.SKIN_TEMPERATURE -> Icons.Default.Timeline
        HealthMetricType.BODY_WEIGHT -> Icons.Default.Straighten
        HealthMetricType.BODY_FAT -> Icons.Default.PieChart
        HealthMetricType.BLOOD_GLUCOSE -> Icons.Default.ShowChart
    }
}
