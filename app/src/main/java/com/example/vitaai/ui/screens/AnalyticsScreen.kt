package com.example.vitaai.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.LuminousBarChart
import com.example.vitaai.ui.components.LuminousDonutChart
import com.example.vitaai.ui.components.LuminousLineChart
import com.example.vitaai.ui.components.LuminousStackedBarChart
import com.example.vitaai.ui.components.PageHeader
import com.example.vitaai.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeframe by viewModel.timeframe.collectAsState()

    // Drill-down state: which card index is selected
    var selectedCardIndex by remember { mutableStateOf<Int?>(null) }

    AuraBackground {
        AnimatedContent(
            targetState = selectedCardIndex,
            transitionSpec = {
                if (targetState != null) {
                    // Entering detail view: slide in from right
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
                        initialContentExit = slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200))
                    )
                } else {
                    // Going back to list: slide in from left
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)),
                        initialContentExit = slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(200))
                    )
                }
            },
            label = "analyticsNavigation"
        ) { cardIdx ->
            if (cardIdx != null && uiState is AnalyticsUiState.Success) {
                val allCards = (uiState as AnalyticsUiState.Success).cards
                val card = allCards.getOrNull(cardIdx)
                if (card != null) {
                    AnalyticDetailView(
                        card = card,
                        timeframe = timeframe,
                        onBack = { selectedCardIndex = null }
                    )
                }
            } else {
                // Full analytics list view
                AnalyticsListView(
                    navController = navController,
                    uiState = uiState,
                    timeframe = timeframe,
                    viewModel = viewModel,
                    onCardClick = { index -> selectedCardIndex = index }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// List view
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsListView(
    navController: NavController,
    uiState: AnalyticsUiState,
    timeframe: Int,
    viewModel: AnalyticsViewModel,
    onCardClick: (Int) -> Unit
) {
    val selectedRangeString = when (timeframe) {
        1 -> "day"
        7 -> "week"
        30 -> "month"
        365 -> "year"
        else -> "week"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            PageHeader(title = "Analytics", kicker = "Vitals & recovery")
        }

        item {
            RangeSelectorPill(
                selected = selectedRangeString,
                onSelect = { range ->
                    val days = when (range) {
                        "day" -> 1
                        "week" -> 7
                        "month" -> 30
                        "year" -> 365
                        else -> 7
                    }
                    viewModel.setTimeframe(days)
                }
            )
        }

        when (val state = uiState) {
            is AnalyticsUiState.Loading -> item {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is AnalyticsUiState.Error -> item { Text("Error: ${state.message}", color = Error) }
            is AnalyticsUiState.Success -> {
                val allCards = state.cards
                val activityCards = state.cards.filter { it.title in listOf("Steps", "Distance", "Calories Burned", "Workouts", "Exercise Minutes") }
                val vitalsCards = state.cards.filter { it.title in listOf("Heart Rate", "Sleep") }
                val nutritionCards = state.cards.filter { it.title in listOf("Calories In", "Hydration", "Macro Balance", "Protein") }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "TACTICAL INSIGHTS ANALYSIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.95.sp
                            ),
                            color = Color.Black.copy(alpha = 0.45f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(4.dp, 40.dp)
                                    .background(Primary, MaterialTheme.shapes.extraSmall)
                            )
                            Text(
                                text = state.aiInsight,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp
                                ),
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (activityCards.isNotEmpty()) {
                    item {
                        Text(
                            text = "Activity & Training",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    items(activityCards) { card ->
                        val globalIndex = allCards.indexOf(card)
                        AnalyticsCard(card, timeframe, onClick = { onCardClick(globalIndex) })
                    }
                }

                if (vitalsCards.isNotEmpty()) {
                    item {
                        Text(
                            text = "Vitals & Recovery",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    item {
                        CircadianNavigationCard(onClick = { navController.navigate("circadian") })
                    }
                    items(vitalsCards) { card ->
                        val globalIndex = allCards.indexOf(card)
                        AnalyticsCard(card, timeframe, onClick = { onCardClick(globalIndex) })
                    }
                }

                if (nutritionCards.isNotEmpty()) {
                    item {
                        Text(
                            text = "Nutrition & Fuel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    items(nutritionCards) { card ->
                        val globalIndex = allCards.indexOf(card)
                        AnalyticsCard(card, timeframe, onClick = { onCardClick(globalIndex) })
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail view
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnalyticDetailView(
    card: AnalyticsMetricCard,
    timeframe: Int,
    onBack: () -> Unit
) {
    var selectedRange by remember { mutableStateOf("week") }

    // Map local range state to days so the chart uses the same axis logic
    val effectiveTimeframe = when (selectedRange) {
        "day"   -> 1
        "week"  -> 7
        "month" -> 30
        "year"  -> 365
        else    -> timeframe
    }

    val axisLabels = timeframeAxisLabels(effectiveTimeframe, card.values.size)
    val metricColor = getMetricColor(card.title)

    // Animated blobs for detail view card background
    val infiniteTransition = rememberInfiniteTransition(label = "detailBlob")
    val blobOpacity by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobOpacity"
    )
    val blobScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Back row ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ALL ANALYTICS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 3.sp
                    ),
                    color = Color.Black.copy(alpha = 0.45f)
                )
            }
        }

        // ── Large chart card ──────────────────────────────────────────────────
        item {
            GlassCardGlow(
                modifier = Modifier.fillMaxWidth(),
                glowColor = metricColor
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Top-right ambient blob
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 64.dp, y = (-64).dp)
                            .graphicsLayer {
                                scaleX = blobScale
                                scaleY = blobScale
                                alpha = blobOpacity
                            }
                            .blur(48.dp)
                            .background(metricColor, CircleShape)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = card.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.Black.copy(alpha = 0.60f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.detail,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                    color = Color.Black.copy(alpha = 0.55f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = card.value,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = (-1.12).sp
                                    ),
                                    color = metricColor
                                )
                                Text(
                                    text = card.unit.uppercase(Locale.US),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        // Large chart (240 dp height)
                        when {
                            card.title == "Macro Balance" -> {
                                val latestValue = card.values.lastOrNull() ?: 0f
                                if (latestValue > 0) {
                                    val protein = latestValue * 0.3f
                                    val carbs   = latestValue * 0.4f
                                    val fat     = latestValue * 0.3f
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LuminousDonutChart(
                                            values = listOf(protein, carbs, fat),
                                            colors = listOf(Primary, Secondary, Tertiary),
                                            modifier = Modifier.size(160.dp)
                                        )
                                        Column(
                                            modifier = Modifier.padding(start = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MacroLegendItem(Primary, "Protein")
                                            MacroLegendItem(Secondary, "Carbs")
                                            MacroLegendItem(Tertiary, "Fats")
                                        }
                                    }
                                } else {
                                    Text("No macro data available", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            card.stackedValues != null -> {
                                LuminousStackedBarChart(
                                    dataPoints = card.stackedValues,
                                    xAxisLabels = axisLabels,
                                    yAxisLabelFormatter = { value -> formatAxisValue(value, card.unit) },
                                    showGrid = false,
                                    modifier = Modifier.fillMaxWidth().height(240.dp)
                                )
                            }
                            card.title in listOf("Heart Rate", "Distance") || card.values.size > 8 -> {
                                LuminousLineChart(
                                    dataPoints = chartValues(card.values),
                                    lineColor = metricColor,
                                    glowColor = metricColor.copy(alpha = 0.35f),
                                    xAxisLabels = axisLabels,
                                    yAxisLabelFormatter = { value -> formatAxisValue(value, card.unit) },
                                    showGrid = false,
                                    modifier = Modifier.fillMaxWidth().height(240.dp)
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
                                    modifier = Modifier.fillMaxWidth().height(240.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Range selector pill ───────────────────────────────────────────────
        item {
            RangeSelectorPill(
                selected = selectedRange,
                onSelect = { selectedRange = it }
            )
        }

        // ── AI Insight card ───────────────────────────────────────────────────
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(metricColor.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                .border(1.dp, metricColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = metricColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "VitaAI Insight",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.2.sp
                        )
                    }
                    Text(
                        text = card.detail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        color = Color.Black.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Range selector pill
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RangeSelectorPill(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("day" to "Day", "week" to "Week", "month" to "Month", "year" to "Year")
    val pillShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.Black.copy(alpha = 0.03f), pillShape)
            .border(1.dp, Color.Black.copy(alpha = 0.06f), pillShape)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (key, label) ->
            val isSelected = key == selected
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF0F172A) else Color.Transparent,
                animationSpec = tween(250),
                label = "rangeBackground_$key"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Black.copy(alpha = 0.5f),
                animationSpec = tween(250),
                label = "rangeText_$key"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 6.dp,
                                shape = pillShape,
                                ambientColor = Color(0xFF0F172A).copy(alpha = 0.18f),
                                spotColor = Color(0xFF0F172A).copy(alpha = 0.18f)
                            )
                        } else Modifier
                    )
                    .clip(pillShape)
                    .clickable { onSelect(key) }
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Existing composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsCard(
    card: AnalyticsMetricCard,
    timeframe: Int,
    onClick: () -> Unit = {}
) {
    val axisLabels = timeframeAxisLabels(timeframe, card.values.size)
    val metricColor = getMetricColor(card.title)

    // Animated blobs for card background
    val infiniteTransition = rememberInfiniteTransition(label = "cardBlob")
    val blobOpacity by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobOpacity"
    )
    val blobScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobScale"
    )

    GlassCardGlow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        glowColor = metricColor
    ) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
            // Absolute blob in top-right
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 48.dp, y = (-48).dp)
                    .graphicsLayer {
                        scaleX = blobScale
                        scaleY = blobScale
                        alpha = blobOpacity
                    }
                    .blur(40.dp)
                    .background(metricColor, CircleShape)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Black.copy(alpha = 0.60f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.55f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = card.value,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-1.12).sp
                            ),
                            color = metricColor
                        )
                        Text(
                            text = card.unit.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                when {
                    card.title == "Macro Balance" -> {
                        val latestValue = card.values.lastOrNull() ?: 0f
                        if (latestValue > 0) {
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
                    card.stackedValues != null -> {
                        LuminousStackedBarChart(
                            dataPoints = card.stackedValues,
                            xAxisLabels = axisLabels,
                            yAxisLabelFormatter = { value -> formatAxisValue(value, card.unit) },
                            showGrid = false,
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )
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

@Composable
private fun MacroLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, MaterialTheme.shapes.small))
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black.copy(alpha = 0.5f)
        )
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
        absValue >= 100f  -> String.format(Locale.US, "%.0f", value)
        absValue >= 10f   -> String.format(Locale.US, "%.1f", value)
        absValue >= 1f    -> String.format(Locale.US, "%.2f", value)
        else              -> String.format(Locale.US, "%.3f", value)
    }
    return if (unit.isBlank()) formatted else "$formatted $unit"
}

@Composable
private fun CircadianNavigationCard(onClick: () -> Unit) {
    val vitaColors = LocalVitaColors.current
    val accentAmberColor = Color(0xFFFFB300)

    val infiniteTransition = rememberInfiniteTransition(label = "circBlob")
    val blobOpacity by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobOpacity"
    )
    val blobScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobScale"
    )

    GlassCardGlow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        glowColor = accentAmberColor
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Absolute blob in top-right
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 48.dp, y = (-48).dp)
                    .graphicsLayer {
                        scaleX = blobScale
                        scaleY = blobScale
                        alpha = blobOpacity
                    }
                    .blur(40.dp)
                    .background(accentAmberColor, CircleShape)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Circadian Alignment",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analyze circadian stability, social jetlag, and nocturnal light disruption",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.55f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = accentAmberColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
