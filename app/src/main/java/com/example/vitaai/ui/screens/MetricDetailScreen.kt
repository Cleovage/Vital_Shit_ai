package com.example.vitaai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
            onClick = { activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Text("GRANT ACCESS")
        }
    }
}

@Composable
private fun MetricDetailContent(navController: NavController, detail: MetricDetailData) {
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = detail.metric.title.uppercase(Locale.US),
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Metric Details", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("TODAY", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = detail.currentValue,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp),
                        color = Primary,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = detail.unitLabel.uppercase(), style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(detail.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant.copy(alpha = 0.9f))
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("SYNCED SOURCES", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(10.dp))
                detail.sourceLabels.forEach { source ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.White.copy(alpha = 0.03f), shape = MaterialTheme.shapes.small)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(text = source.uppercase(), style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("TODAY'S TREND", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LuminousLineChart(
                    dataPoints = detail.todayChartValues.ifEmpty { listOf(0f, 0f) },
                    lineColor = Primary,
                    glowColor = Primary.copy(alpha = 0.35f),
                    xAxisLabels = detail.todayLabels,
                    yAxisLabelFormatter = { value -> formatAxisValue(value, detail.unitLabel) },
                    showGrid = false,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("7-DAY TREND", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LuminousBarChart(
                    dataPoints = detail.weekChartValues.ifEmpty { listOf(0f, 0f) },
                    barColor = Primary,
                    glowColor = Primary.copy(alpha = 0.3f),
                    xAxisLabels = detail.weekLabels,
                    yAxisLabelFormatter = { value -> formatAxisValue(value, detail.unitLabel) },
                    showGrid = false,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("RELATED STATS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    detail.relatedStats.forEach { stat ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.first, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant.copy(alpha = 0.8f))
                            Text(stat.second, style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.Bold)
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
