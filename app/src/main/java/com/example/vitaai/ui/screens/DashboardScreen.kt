package com.example.vitaai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.data.HealthSnapshot
import com.example.vitaai.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Vitality",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                DashboardUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DashboardUiState.Error -> Text(
                    "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                is DashboardUiState.Success -> {
                    DashboardContent(state.snapshot, state.insight)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(snapshot: HealthSnapshot, insight: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                HeroStepCircle(snapshot.steps)
            }
        }

        item { InsightCardExpressive(insight) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCardExpressive(
                    label = "Sleep",
                    value = "%.1f".format(snapshot.sleepDurationHours),
                    unit = "hours",
                    color = Secondary,
                    modifier = Modifier.weight(1f)
                )
                StatCardExpressive(
                    label = "Heart Rate",
                    value = "%.0f".format(snapshot.avgHeartRate),
                    unit = "bpm",
                    color = Tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Activity Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TrendChart(snapshot.hourlySteps)
        }
    }
}

@Composable
fun HeroStepCircle(steps: Long) {
    val targetSteps = 10000f
    val progress = (steps.toFloat() / targetSteps).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            drawArc(
                color = Gray.copy(alpha = 0.2f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Primary, SereneBlue, Primary)
                ),
                startAngle = 135f,
                sweepAngle = progress * 270f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = steps.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = Primary
            )
            Text(
                text = "STEPS",
                style = MaterialTheme.typography.labelMedium,
                color = Gray
            )
        }
    }
}

@Composable
fun InsightCardExpressive(insight: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Today's Insight",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    insight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = White
                )
            }
        }
    }
}

@Composable
fun StatCardExpressive(label: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineLarge,
                    color = color,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TrendChart(hourlySteps: Map<java.time.Instant, Long>) {
    val entries = if (hourlySteps.isEmpty()) {
        entryModelOf(0, 0, 0, 0, 0, 0)
    } else {
        val sortedSteps = hourlySteps.entries.sortedBy { it.key }.takeLast(6)
        entryModelOf(*sortedSteps.map { it.value.toFloat() }.toTypedArray())
    }

    Chart(
        chart = lineChart(),
        model = entries,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
fun HeroStepCirclePreview() {
    VitaAITheme {
        HeroStepCircle(steps = 7500)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
fun DashboardContentPreview() {
    VitaAITheme {
        DashboardContent(
            snapshot = HealthSnapshot(
                steps = 8234,
                avgHeartRate = 72.0,
                sleepDurationHours = 7.5,
                hourlySteps = mapOf(
                    java.time.Instant.now().minus(5, java.time.temporal.ChronoUnit.HOURS) to 1200L,
                    java.time.Instant.now().minus(4, java.time.temporal.ChronoUnit.HOURS) to 800L,
                    java.time.Instant.now().minus(3, java.time.temporal.ChronoUnit.HOURS) to 1500L,
                    java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.HOURS) to 2000L,
                    java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS) to 500L,
                    java.time.Instant.now() to 234L
                )
            ),
            insight = "You're doing great! Your activity level is higher than usual today."
        )
    }
}
