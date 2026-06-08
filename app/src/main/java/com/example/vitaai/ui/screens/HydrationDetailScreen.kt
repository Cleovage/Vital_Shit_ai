package com.example.vitaai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.PageHeader

@Composable
fun HydrationDetailScreen(
    navController: NavController,
    viewModel: HydrationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val progress = (state.todayMl.toFloat() / state.goalMl.toFloat()).coerceIn(0f, 1f)
    val cyan = Color(0xFF06B6D4)
    val cyanDim = cyan.copy(alpha = 0.2f)

    AuraBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            PageHeader(
                title = "Hydration",
                kicker = "Today"
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Hero progress card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White.copy(alpha = 0.78f))
                            .border(
                                width = 1.dp,
                                color = Color.Black.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${state.todayMl} / ${state.goalMl} ml",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HydrationProgressRing(progress = progress, todayMl = state.todayMl, color = cyan, dim = cyanDim)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(progress * 100).toInt()}% of daily goal",
                                color = Color(0xFF475569),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(250, 500, 750).forEach { ml ->
                                    QuickAddChip(ml = ml, color = cyan) { viewModel.addWater(ml) }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.04f))
                                        .clickable { viewModel.removeLast() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Undo,
                                        contentDescription = "Undo last entry",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "This week",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                item {
                    WeeklyBars(
                        data = state.weeklyData.map { it.totalMl },
                        labels = state.weeklyData.map { it.date.dayOfWeek.name.take(3) },
                        color = cyan,
                        goal = state.goalMl
                    )
                }

                item {
                    Text(
                        text = "Today's hourly log",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                item {
                    HourlyMask(
                        mask = state.hourlyMask,
                        color = cyan
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.78f))
                            .border(
                                width = 1.dp,
                                color = Color.Black.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = cyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hydration streak",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${state.streak} day${if (state.streak == 1) "" else "s"}",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            }
                            Text(
                                text = "consecutive",
                                color = Color(0xFF475569),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (state.log.isNotEmpty()) {
                    item {
                        Text(
                            text = "Log (${state.log.size})",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    items(state.log.reversed()) { entry ->
                        LogRow(hour = entry.hour, ml = entry.ml, color = cyan)
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun HydrationProgressRing(progress: Float, todayMl: Int, color: Color, dim: Color) {
    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14f
            val r = (this.size.minDimension - stroke) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val topLeft = Offset(center.x - r, center.y - r)
            val arcSize = Size(r * 2f, r * 2f)
            drawArc(
                color = dim,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${statePct(progress)}%",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Text(
                text = "$todayMl ml",
                color = Color(0xFF475569),
                fontSize = 12.sp
            )
        }
    }
}

private fun statePct(p: Float) = (p * 100).toInt()

@Composable
private fun QuickAddChip(ml: Int, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${ml}ml",
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun WeeklyBars(data: List<Int>, labels: List<String>, color: Color, goal: Int) {
    val maxV = (data.maxOrNull() ?: 0).coerceAtLeast(goal).coerceAtLeast(1)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.07f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val w = size.width
                val h = size.height
                val padBottom = 20f
                val padTop = 8f
                val n = data.size.coerceAtLeast(1)
                val gap = w / (n * 4f)
                val barW = w / n - gap

                // goal line
                val goalY = padTop + (1f - goal.toFloat() / maxV) * (h - padTop - padBottom)
                drawLine(
                    color = color.copy(alpha = 0.4f),
                    start = Offset(0f, goalY),
                    end = Offset(w, goalY),
                    strokeWidth = 1.5f
                )

                data.forEachIndexed { i, v ->
                    val barH = (v.toFloat() / maxV) * (h - padTop - padBottom)
                    val x = i * (barW + gap) + gap / 2f
                    val y = h - padBottom - barH
                    drawRoundRect(
                        color = if (v >= goal) color else color.copy(alpha = 0.45f),
                        topLeft = Offset(x, y),
                        size = Size(barW, barH.coerceAtLeast(2f)),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.forEach { l ->
                    Text(
                        text = l.take(3),
                        color = Color(0xFF475569),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyMask(mask: List<Boolean>, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.07f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            mask.forEachIndexed { hour, active ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(13.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (active) color else Color(0xFFE2E8F0))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hour.toString(),
                        color = Color(0xFF94A3B8),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LogRow(hour: Int, ml: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "%02d:00".format(hour),
            color = Color(0xFF475569),
            fontSize = 13.sp,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = "$ml ml",
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
