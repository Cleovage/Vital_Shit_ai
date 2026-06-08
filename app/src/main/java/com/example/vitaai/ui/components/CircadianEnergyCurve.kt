package com.example.vitaai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.LocalVitaColors
import java.util.Calendar
import kotlin.math.sin

@Composable
fun CircadianEnergyCurve(
    modifier: Modifier = Modifier,
    wakeHour: Double = 6.5,
    sleepHour: Double = 22.5
) {
    val vitaColors = LocalVitaColors.current
    var selectedHour by remember { mutableStateOf<Double?>(null) }
    
    val currentHour = remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.HOUR_OF_DAY).toDouble() + cal.get(Calendar.MINUTE).toDouble() / 60.0
    }
    
    val activeDisplayHour = selectedHour ?: currentHour

    // Calculate energy and pressure at activeDisplayHour
    val energyVal = getCircadianEnergy(activeDisplayHour)
    val pressureVal = getSleepPressure(activeDisplayHour, wakeHour, sleepHour)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (selectedHour == null) "Current Circadian State" else "Scrubbed State",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = String.format("%02d:%02d", activeDisplayHour.toInt(), ((activeDisplayHour % 1.0) * 60).toInt()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text = "Energy Drive",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "${(energyVal * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00E5FF)
                    )
                }
                Column {
                    Text(
                        text = "Sleep Pressure",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "${(pressureVal * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(wakeHour, sleepHour) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                selectedHour = (offset.x / size.width.toFloat()).coerceIn(0f, 1f).toDouble() * 24.0
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                selectedHour = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f).toDouble() * 24.0
                            },
                            onDragEnd = {
                                selectedHour = null
                            },
                            onDragCancel = {
                                selectedHour = null
                            }
                        )
                    }
                    .pointerInput(wakeHour, sleepHour) {
                        detectTapGestures(
                            onTap = { offset ->
                                selectedHour = (offset.x / size.width.toFloat()).coerceIn(0f, 1f).toDouble() * 24.0
                            }
                        )
                    }
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 24.dp.toPx()
                val graphHeight = height - paddingBottom
                
                // Draw grid lines (every 6 hours: 0, 6, 12, 18, 24)
                for (h in 0..24 step 6) {
                    val x = (h.toFloat() / 24f) * width
                    drawLine(
                        color = vitaColors.glassBorderDark.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, graphHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw horizontal zero energy baseline
                val yBaseline = graphHeight * 0.5f
                drawLine(
                    color = vitaColors.glassBorderDark.copy(alpha = 0.05f),
                    start = Offset(0f, yBaseline),
                    end = Offset(width, yBaseline),
                    strokeWidth = 1.dp.toPx()
                )

                // Build paths for Circadian Drive and Sleep Pressure
                val energyPath = Path()
                val pressurePath = Path()
                
                for (i in 0..100) {
                    val pct = i / 100f
                    val hour = pct * 24.0
                    val x = pct * width
                    
                    val energy = getCircadianEnergy(hour)
                    // Map energy from [-1, 1] to [graphHeight, 0]
                    val yEnergy = yBaseline - (energy * (graphHeight * 0.4f)).toFloat()
                    
                    val pressure = getSleepPressure(hour, wakeHour, sleepHour)
                    // Map pressure from [0, 1] to [graphHeight, 0]
                    val yPressure = graphHeight - (pressure * graphHeight * 0.8f).toFloat()

                    if (i == 0) {
                        energyPath.moveTo(x, yEnergy)
                        pressurePath.moveTo(x, yPressure)
                    } else {
                        energyPath.lineTo(x, yEnergy)
                        pressurePath.lineTo(x, yPressure)
                    }
                }

                // Draw energy fill gradient below the path (optional, nice visual polish)
                val fillPath = Path().apply {
                    addPath(energyPath)
                    lineTo(width, graphHeight)
                    lineTo(0f, graphHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), Color.Transparent),
                        startY = 0f,
                        endY = graphHeight
                    )
                )

                // Draw curves
                drawPath(
                    path = energyPath,
                    color = Color(0xFF00E5FF),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                drawPath(
                    path = pressurePath,
                    color = Color(0xFFFF9800),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw horizontal time labels
                val fontScale = 10.sp.toPx()
                val textPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = Color.Black.copy(alpha = 0.4f).toArgb()
                    textSize = fontScale
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                drawContext.canvas.nativeCanvas.apply {
                    drawText("12 AM", 0.08f * width, height - 4.dp.toPx(), textPaint)
                    drawText("6 AM", 0.25f * width, height - 4.dp.toPx(), textPaint)
                    drawText("12 PM", 0.50f * width, height - 4.dp.toPx(), textPaint)
                    drawText("6 PM", 0.75f * width, height - 4.dp.toPx(), textPaint)
                    drawText("12 AM", 0.92f * width, height - 4.dp.toPx(), textPaint)
                }

                // Draw indicator line at activeDisplayHour
                val indicatorX = (activeDisplayHour.toFloat() / 24f) * width
                drawLine(
                    color = vitaColors.accentAmber,
                    start = Offset(indicatorX, 0f),
                    end = Offset(indicatorX, graphHeight),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Draw intersecting nodes
                val activeEnergyY = yBaseline - (energyVal * (graphHeight * 0.4f)).toFloat()
                val activePressureY = graphHeight - (pressureVal * graphHeight * 0.8f).toFloat()

                // Energy dot
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(indicatorX, activeEnergyY)
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 3.dp.toPx(),
                    center = Offset(indicatorX, activeEnergyY)
                )

                // Pressure dot
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(indicatorX, activePressureY)
                )
                drawCircle(
                    color = Color(0xFFFF9800),
                    radius = 3.dp.toPx(),
                    center = Offset(indicatorX, activePressureY)
                )
            }
        }
    }
}

/**
 * Returns circadian alertness/energy level between -1.0 and 1.0.
 */
private fun getCircadianEnergy(hour: Double): Double {
    val firstTerm = sin((hour - 7.0) / 24.0 * 2.0 * Math.PI)
    val secondTerm = 0.35 * sin((hour - 14.0) / 12.0 * 2.0 * Math.PI)
    return (firstTerm + secondTerm).coerceIn(-1.0, 1.0)
}

/**
 * Returns sleep pressure/adenosine level between 0.0 and 1.0.
 */
private fun getSleepPressure(hour: Double, wakeHour: Double, sleepHour: Double): Double {
    val durationAwake = if (sleepHour > wakeHour) sleepHour - wakeHour else (24.0 - wakeHour) + sleepHour
    val sleepDuration = 24.0 - durationAwake

    return when {
        isHourBetween(hour, wakeHour, sleepHour) -> {
            val hoursAwake = getElapsedHours(hour, wakeHour)
            (hoursAwake / durationAwake).coerceIn(0.0, 1.0)
        }
        else -> {
            val hoursAsleep = getElapsedHours(hour, sleepHour)
            (1.0 - (hoursAsleep / sleepDuration)).coerceIn(0.0, 1.0)
        }
    }
}

private fun isHourBetween(hour: Double, start: Double, end: Double): Boolean {
    return if (start < end) {
        hour in start..end
    } else {
        hour >= start || hour <= end
    }
}

private fun getElapsedHours(current: Double, base: Double): Double {
    return if (current >= base) {
        current - base
    } else {
        (24.0 - base) + current
    }
}
