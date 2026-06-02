package com.example.vitaai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.LuminousBarChart
import com.example.vitaai.ui.components.LuminousDonutChart
import com.example.vitaai.ui.components.LuminousLineChart
import com.example.vitaai.ui.components.LuminousStackedBarChart
import com.example.vitaai.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val timeframe by viewModel.timeframe.collectAsState()

    AuraBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Analytics", style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                Text("Synced trends across training, nutrition, hydration, sleep, and heart data", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(top = 14.dp), color = OutlineVariant.copy(alpha = 0.5f))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(42.dp).background(SurfaceContainerHigh, MaterialTheme.shapes.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeSelectorItem("Day", timeframe == 1, Modifier.weight(1f).clickable { viewModel.setTimeframe(1) })
                    TimeSelectorItem("Week", timeframe == 7, Modifier.weight(1f).clickable { viewModel.setTimeframe(7) })
                    TimeSelectorItem("Month", timeframe == 30, Modifier.weight(1f).clickable { viewModel.setTimeframe(30) })
                    TimeSelectorItem("Year", timeframe == 365, Modifier.weight(1f).clickable { viewModel.setTimeframe(365) })
                }
            }

            when (val state = uiState) {
                is AnalyticsUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is AnalyticsUiState.Error -> item { Text("Error: ${state.message}", color = Error) }
                is AnalyticsUiState.Success -> {
                    val activityCards = state.cards.filter { it.title in listOf("Steps", "Distance", "Calories Burned", "Workouts", "Exercise Minutes") }
                    val vitalsCards = state.cards.filter { it.title in listOf("Heart Rate", "Sleep") }
                    val nutritionCards = state.cards.filter { it.title in listOf("Calories In", "Hydration", "Macro Balance", "Protein") }

                    item {
                        ApexCard(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
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
                                    text = state.aiInsight.uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnBackground,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (activityCards.isNotEmpty()) {
                        item {
                            Text("Activity & Training", style = MaterialTheme.typography.titleLarge, color = Primary, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(activityCards) { card -> AnalyticsCard(card, timeframe) }
                    }

                    if (vitalsCards.isNotEmpty()) {
                        item {
                            Text("Vitals & Recovery", style = MaterialTheme.typography.titleLarge, color = Primary, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(vitalsCards) { card -> AnalyticsCard(card, timeframe) }
                    }

                    if (nutritionCards.isNotEmpty()) {
                        item {
                            Text("Nutrition & Fuel", style = MaterialTheme.typography.titleLarge, color = Primary, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(nutritionCards) { card -> AnalyticsCard(card, timeframe) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCard(card: AnalyticsMetricCard, timeframe: Int) {
    val axisLabels = timeframeAxisLabels(timeframe, card.values.size)
    val metricColor = getMetricColor(card.title)

    ApexCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(card.title, style = MaterialTheme.typography.titleMedium, color = metricColor, fontWeight = FontWeight.Bold)
                    Text(card.detail, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(card.value, style = MaterialTheme.typography.titleLarge, color = metricColor, fontWeight = FontWeight.Bold)
                    Text(card.unit, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
            
            when {
                card.title == "Macro Balance" -> {
                    val latestValue = card.values.lastOrNull() ?: 0f
                    if (latestValue > 0) {
                        // In a real app we'd pass exact macros. Here we just use a fake split based on the total for demonstration
                        val protein = latestValue * 0.3f
                        val carbs = latestValue * 0.4f
                        val fat = latestValue * 0.3f
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            LuminousDonutChart(
                                values = listOf(protein, carbs, fat),
                                colors = listOf(Primary, Secondary, Tertiary),
                                modifier = Modifier.size(120.dp)
                            )
                            Column(modifier = Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                MacroLegendItem(Primary, "Protein")
                                MacroLegendItem(Secondary, "Carbs")
                                MacroLegendItem(Tertiary, "Fats")
                            }
                        }
                    } else {
                        Text("No macro data available", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                card.title in listOf("Heart Rate", "Distance") || card.values.size > 8 -> {
                    LuminousLineChart(
                        dataPoints = chartValues(card.values),
                        lineColor = metricColor,
                        glowColor = metricColor.copy(alpha = 0.35f),
                        xAxisLabels = axisLabels,
                        yAxisLabelFormatter = { value -> formatAxisValue(value, card.unit) },
                        showGrid = false,
                        modifier = Modifier.fillMaxWidth().height(130.dp)
                    )
                }
                card.stackedValues != null -> {
                    LuminousStackedBarChart(
                        dataPoints = card.stackedValues,
                        xAxisLabels = axisLabels,
                        yAxisLabelFormatter = { value -> formatAxisValue(value, card.unit) },
                        showGrid = false,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
                else -> {
                    LuminousBarChart(
                        dataPoints = chartValues(card.values),
                        barColor = metricColor,
                        glowColor = metricColor.copy(alpha = 0.3f),
                        xAxisLabels = axisLabels,
                        yAxisLabelFormatter = { value -> formatAxisValue(value, card.unit) },
                        showGrid = false,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }
        }
    }
}

private fun getMetricColor(title: String): Color {
    return when (title) {
        "Heart Rate" -> Color(0xFFFF5252) // Vibrant Red
        "Sleep" -> Color(0xFF448AFF) // Bright Blue
        "Steps", "Distance", "Workouts", "Exercise Minutes" -> Color(0xFF69F0AE) // Neon Green
        "Calories Burned" -> Color(0xFFFFAB40) // Orange
        "Calories In", "Hydration", "Protein", "Macro Balance" -> Color(0xFFFFD740) // Yellow
        else -> Primary // Fallback to app's primary color
    }
}

@Composable
private fun MacroLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, MaterialTheme.shapes.small))
        Text(label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
private fun TimeSelectorItem(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) SurfaceContainerHighest else Color.Transparent,
        label = "timeSelectorBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Primary else OnSurfaceVariant,
        label = "timeSelectorText"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.98f,
        label = "timeSelectorScale"
    )
    val shape = MaterialTheme.shapes.small

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(backgroundColor, shape)
            .border(
                width = 1.dp,
                color = if (selected) OutlineVariant.copy(alpha = 0.6f) else Color.Transparent,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

private fun chartValues(values: List<Float>): List<Float> {
    return when {
        values.isEmpty() -> listOf(0f, 0f)
        values.size == 1 -> listOf(0f, values.first())
        else -> values
    }
}

private fun timeframeAxisLabels(timeframe: Int, valuesCount: Int): List<String> {
    if (valuesCount == 0) return emptyList()
    if (timeframe == 1) {
        return listOf("0", "4", "8", "12", "16", "20", "24")
    }

    val today = LocalDate.now()
    val formatter = when {
        valuesCount <= 14 -> DateTimeFormatter.ofPattern("EEE", Locale.US)
        valuesCount <= 60 -> DateTimeFormatter.ofPattern("d", Locale.US)
        else -> DateTimeFormatter.ofPattern("MMM", Locale.US)
    }

    return (valuesCount - 1 downTo 0).map { offset ->
        today.minusDays(offset.toLong()).format(formatter)
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
