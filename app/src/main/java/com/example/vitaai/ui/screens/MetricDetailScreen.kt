package com.example.vitaai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.LuminousBarChart
import com.example.vitaai.ui.components.LuminousLineChart
import com.example.vitaai.ui.theme.Error
import com.example.vitaai.ui.theme.OnPrimary
import com.example.vitaai.ui.theme.OnSurfaceVariant
import com.example.vitaai.ui.theme.OutlineVariant
import com.example.vitaai.ui.theme.Primary
import com.example.vitaai.ui.theme.SurfaceContainerHigh
import com.example.vitaai.ui.theme.VitaTextStyles
import java.util.Locale
import kotlin.math.abs

@Composable
fun MetricDetailScreen(
    navController: NavController,
    metricRoute: String?,
    viewModel: MetricDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(metricRoute) {
        viewModel.load(metricRoute)
    }

    AuraBackground {
        when (val state = uiState) {
            is MetricDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is MetricDetailUiState.PermissionsRequired -> {
                MetricPermissionScreen(viewModel = viewModel)
            }
            is MetricDetailUiState.Success -> {
                MetricDetailContent(
                    navController = navController,
                    detail = state.data
                )
            }
            is MetricDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = Error)
                }
            }
        }
    }
}

private fun formatAxisValue(value: Float, unit: String): String {
    val absValue = abs(value)
    val formatted = when {
        absValue >= 1000f -> String.format(Locale.US, "%.0f", value)
        absValue >= 100f -> String.format(Locale.US, "%.0f", value)
        absValue >= 10f -> String.format(Locale.US, "%.1f", value)
        absValue >= 1f -> String.format(Locale.US, "%.2f", value)
        else -> String.format(Locale.US, "%.3f", value)
    }
    return if (unit.isBlank()) formatted else "$formatted $unit"
}

@Composable
private fun MetricPermissionScreen(viewModel: MetricDetailViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val healthConnectLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        val sdkStatus = androidx.health.connect.client.HealthConnectClient.getSdkStatus(context)
        if (sdkStatus == androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE) {
            healthConnectLauncher.launch(viewModel.getRequestedPermissions())
        } else {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            }
            context.startActivity(intent)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Permission Required", style = MaterialTheme.typography.headlineSmall, color = Primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Grant health permissions to view this metric with full charts and source sync.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val sdkStatus = androidx.health.connect.client.HealthConnectClient.getSdkStatus(context)
                if (sdkStatus == androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE) {
                    activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                } else {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                    }
                    context.startActivity(intent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Text("GRANT ACCESS")
        }
    }
}

@Composable
private fun MetricDetailContent(navController: NavController, detail: MetricDetailData) {
    val metricColor = getMetricColor(detail.metric.title)
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = detail.metric.title.uppercase(Locale.US),
                        style = VitaTextStyles.detailKicker,
                        color = Color.Black.copy(alpha = 0.40f)
                    )
                    Text(
                        text = "Metric Details",
                        style = VitaTextStyles.screenSubtitle,
                        color = Color(0xFF0F172A)
                    )
                }
            }
            HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = "TODAY",
                    style = VitaTextStyles.cardOverline,
                    color = Color.Black.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = detail.currentValue,
                        style = VitaTextStyles.metricLarge,
                        color = metricColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detail.unitLabel.uppercase(Locale.US),
                        style = VitaTextStyles.metricUnitLabel,
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detail.description,
                    style = VitaTextStyles.bodySecondary,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = "SYNCED SOURCES",
                    style = VitaTextStyles.cardOverline,
                    color = Color.Black.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                detail.sourceLabels.forEach { source ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.Black.copy(alpha = 0.03f), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = source.uppercase(Locale.US),
                            style = VitaTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = "TODAY'S TREND",
                    style = VitaTextStyles.cardOverline,
                    color = Color.Black.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LuminousLineChart(
                    dataPoints = detail.todayChartValues.ifEmpty { listOf(0f, 0f) },
                    lineColor = metricColor,
                    glowColor = metricColor.copy(alpha = 0.35f),
                    xAxisLabels = detail.todayLabels,
                    yAxisLabelFormatter = { value -> formatAxisValue(value, detail.unitLabel) },
                    showGrid = false,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = "7-DAY TREND",
                    style = VitaTextStyles.cardOverline,
                    color = Color.Black.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LuminousBarChart(
                    dataPoints = detail.weekChartValues.ifEmpty { listOf(0f, 0f) },
                    barColor = metricColor,
                    glowColor = metricColor.copy(alpha = 0.3f),
                    xAxisLabels = detail.weekLabels,
                    yAxisLabelFormatter = { value -> formatAxisValue(value, detail.unitLabel) },
                    showGrid = false,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = "RELATED STATS",
                    style = VitaTextStyles.cardOverline,
                    color = Color.Black.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    detail.relatedStats.forEach { stat ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = stat.first,
                                style = VitaTextStyles.bodySecondary,
                                color = Color.Black.copy(alpha = 0.55f)
                            )
                            Text(
                                text = stat.second,
                                style = VitaTextStyles.metricRowValue,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun getMetricColor(title: String): Color {
    return when (title) {
        "Heart Rate" -> Color(0xFFF43F5E) // Rose-500
        "Sleep" -> Color(0xFF3B82F6) // Blue-500
        "Steps", "Distance", "Workouts", "Exercise Minutes" -> Color(0xFF06B6D4) // Cyan-500
        "Calories Burned" -> Color(0xFFF59E0B) // Amber-500
        "Calories In" -> Color(0xFFEAB308) // Yellow-500
        "Hydration" -> Color(0xFF06B6D4) // Cyan-500
        "Protein" -> Color(0xFFEAB308) // Yellow-500
        "Macro Balance" -> Color(0xFF3B82F6) // Blue-500
        else -> Primary // Fallback to app's primary color
    }
}
