package com.example.vitaai.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.LocalVitaColors
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

enum class ActiveHandle { NONE, START, END }

@Composable
fun CircadianClockDial(
    modifier: Modifier = Modifier,
    sleepStartHour: Double = 23.0, // 11 PM
    sleepEndHour: Double = 7.0,    // 7 AM
    currentHourOverride: Double? = null,
    onSleepStartHourChanged: (Double) -> Unit = {},
    onSleepEndHourChanged: (Double) -> Unit = {}
) {
    val vitaColors = LocalVitaColors.current
    var activeHandle by remember { mutableStateOf(ActiveHandle.NONE) }
    
    // Get actual current time if no override
    val currentHour by produceState(initialValue = currentHourOverride ?: 12.0) {
        if (currentHourOverride != null) {
            value = currentHourOverride
            return@produceState
        }
        while (true) {
            val cal = Calendar.getInstance()
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            value = h.toDouble() + (m.toDouble() / 60.0)
            delay(30000) // update every 30s
        }
    }

    // Animation for current time node pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "DialGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Smooth entry animation for the sleep arc
    val animatedSleepPercent by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "SleepArcEntry"
    )

    val onSurface = MaterialTheme.colorScheme.onSurface
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    val sleepBackingPaint = remember(density) {
        androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = with(density) { 20.dp.toPx() }
            strokeCap = android.graphics.Paint.Cap.ROUND
            color = Color(0xFF3F51B5).copy(alpha = 0.25f).toArgb()
            maskFilter = BlurMaskFilter(with(density) { 12.dp.toPx() }, BlurMaskFilter.Blur.NORMAL)
        }
    }
    
    val sleepForegroundBrush = remember {
        Brush.sweepGradient(
            colors = listOf(Color(0xFF3F51B5), Color(0xFF673AB7), Color(0xFF3F51B5))
        )
    }

    val labelPaint = remember(onSurface, density) {
        androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = onSurface.copy(alpha = 0.85f).toArgb()
            textSize = with(density) { 13.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }
    }

    val glowPaint = remember(density) {
        androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            maskFilter = BlurMaskFilter(with(density) { 8.dp.toPx() }, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val dashPathEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .pointerInput(sleepStartHour, sleepEndHour) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val startRad = hourToAngleRad(sleepStartHour)
                        val startHandleCenter = Offset(
                            x = size.width / 2f + ((size.width / 2f) * 0.65f) * cos(startRad).toFloat(),
                            y = size.height / 2f + ((size.width / 2f) * 0.65f) * sin(startRad).toFloat()
                        )

                        val endRad = hourToAngleRad(sleepEndHour)
                        val endHandleCenter = Offset(
                            x = size.width / 2f + ((size.width / 2f) * 0.65f) * cos(endRad).toFloat(),
                            y = size.height / 2f + ((size.width / 2f) * 0.65f) * sin(endRad).toFloat()
                        )

                        val distToStart = (offset - startHandleCenter).getDistance()
                        val distToEnd = (offset - endHandleCenter).getDistance()

                        val touchThreshold = 40.dp.toPx() // generous hit target
                        activeHandle = when {
                            distToStart < distToEnd && distToStart < touchThreshold -> ActiveHandle.START
                            distToEnd < distToStart && distToEnd < touchThreshold -> ActiveHandle.END
                            else -> ActiveHandle.NONE
                        }
                    },
                    onDrag = { change, _ ->
                        if (activeHandle != ActiveHandle.NONE) {
                            change.consume()
                            val touchPoint = change.position
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val delta = touchPoint - center
                            var angleRad = Math.atan2(delta.y.toDouble(), delta.x.toDouble())
                            if (angleRad < 0) {
                                angleRad += 2 * Math.PI
                            }
                            val angleDegrees = Math.toDegrees(angleRad)
                            val hour = ((angleDegrees - 270.0 + 360.0) % 360.0) / 360.0 * 24.0
                            val roundedHour = Math.round(hour * 12.0) / 12.0
                            val finalHour = (roundedHour + 24.0) % 24.0

                            if (activeHandle == ActiveHandle.START) {
                                onSleepStartHourChanged(finalHour)
                            } else if (activeHandle == ActiveHandle.END) {
                                onSleepEndHourChanged(finalHour)
                            }
                        }
                    },
                    onDragEnd = {
                        activeHandle = ActiveHandle.NONE
                    },
                    onDragCancel = {
                        activeHandle = ActiveHandle.NONE
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        val outerRadius = (width / 2) * 0.65f
        val innerRadius = outerRadius * 0.88f
        
        // 1. Draw Dial Ring Backing
        drawCircle(
            color = vitaColors.glassBorderDark.copy(alpha = 0.15f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw tick marks (24 hours)
        for (h in 0 until 24) {
            val angleRad = hourToAngleRad(h.toDouble())
            val tickStart = Offset(
                x = center.x + innerRadius * cos(angleRad).toFloat(),
                y = center.y + innerRadius * sin(angleRad).toFloat()
            )
            val tickEnd = Offset(
                x = center.x + outerRadius * cos(angleRad).toFloat(),
                y = center.y + outerRadius * sin(angleRad).toFloat()
            )
            val isMajor = h % 6 == 0
            val tickColor = if (isMajor) vitaColors.accentAmber else onSurface.copy(alpha = 0.3f)
            val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            
            drawLine(
                color = tickColor.copy(alpha = if (isMajor) 0.8f else 0.3f),
                start = tickStart,
                end = tickEnd,
                strokeWidth = tickWidth
            )
        }

        // Helper calculations for arcs
        val startSleepAngle = hourToAngleDegrees(sleepStartHour)
        var sleepSweep = sleepEndHour - sleepStartHour
        if (sleepSweep < 0) sleepSweep += 24.0
        val sleepSweepDegrees = (sleepSweep / 24.0) * 360.0 * animatedSleepPercent

        // 2. Draw Melatonin Onset Zone (9 PM to 11 PM) - Ambient orange
        val startMelaAngle = hourToAngleDegrees(21.0)
        drawArc(
            color = Color(0xFFFFB300).copy(alpha = 0.15f),
            startAngle = startMelaAngle.toFloat(),
            sweepAngle = ((2.0 / 24.0) * 360.0).toFloat(),
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Draw Cortisol Awakening Zone (6 AM to 9 AM) - Energy cyan/blue
        val startCortAngle = hourToAngleDegrees(6.0)
        drawArc(
            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
            startAngle = startCortAngle.toFloat(),
            sweepAngle = ((3.0 / 24.0) * 360.0).toFloat(),
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Draw Sleep Zone Arc (Navy / Violet glass glow)
        // Draw backing glow via blur
        drawIntoCanvas { canvas ->
            val rect = android.graphics.RectF(
                center.x - outerRadius,
                center.y - outerRadius,
                center.x + outerRadius,
                center.y + outerRadius
            )
            canvas.nativeCanvas.drawArc(
                rect,
                startSleepAngle.toFloat(),
                sleepSweepDegrees.toFloat(),
                false,
                sleepBackingPaint
            )
        }

        // Draw foreground sharp Sleep Zone arc
        drawArc(
            brush = sleepForegroundBrush,
            startAngle = startSleepAngle.toFloat(),
            sweepAngle = sleepSweepDegrees.toFloat(),
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // 5. Draw Major Hour Labels (12 AM, 6 AM, 12 PM, 6 PM)
        drawIntoCanvas { canvas ->
            // Labels positions: Midnight (top), Noon (bottom), 6 AM (right), 6 PM (left)
            labelPaint.textAlign = android.graphics.Paint.Align.CENTER
            canvas.nativeCanvas.drawText("12 AM", center.x, center.y - outerRadius - 12.dp.toPx(), labelPaint)
            canvas.nativeCanvas.drawText("12 PM", center.x, center.y + outerRadius + 16.dp.toPx(), labelPaint)
            
            labelPaint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.nativeCanvas.drawText("6 AM", center.x + outerRadius + 8.dp.toPx(), center.y + 4.dp.toPx(), labelPaint)
            
            labelPaint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.nativeCanvas.drawText("6 PM", center.x - outerRadius - 8.dp.toPx(), center.y + 4.dp.toPx(), labelPaint)
        }

        // 6. Draw Current Time Indicator Hand
        val currentRad = hourToAngleRad(currentHour)
        val pointerEnd = Offset(
            x = center.x + outerRadius * cos(currentRad).toFloat(),
            y = center.y + outerRadius * sin(currentRad).toFloat()
        )

        // Draw indicator connector line
        drawLine(
            color = vitaColors.accentAmber.copy(alpha = 0.5f),
            start = center,
            end = pointerEnd,
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashPathEffect
        )

        // Draw center pivot node
        drawCircle(
            color = Color.White,
            radius = 10.dp.toPx(),
            center = center
        )
        drawCircle(
            color = vitaColors.accentAmber,
            radius = 4.dp.toPx(),
            center = center
        )

        // Draw indicator node with glowing blur
        drawIntoCanvas { canvas ->
            glowPaint.color = vitaColors.accentAmber.copy(alpha = pulseAlpha).toArgb()
            canvas.nativeCanvas.drawCircle(pointerEnd.x, pointerEnd.y, 16.dp.toPx(), glowPaint)
        }

        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = pointerEnd
        )
        drawCircle(
            color = vitaColors.accentAmber,
            radius = 4.dp.toPx(),
            center = pointerEnd
        )

        // 7. Draw Bedtime (Moon) and Wake-time (Sun) handles
        val startRad = hourToAngleRad(sleepStartHour)
        val startHandleCenter = Offset(
            x = center.x + outerRadius * cos(startRad).toFloat(),
            y = center.y + outerRadius * sin(startRad).toFloat()
        )
        
        // Bedtime handle (Moon)
        drawCircle(
            color = Color.White,
            radius = 16.dp.toPx(),
            center = startHandleCenter
        )
        drawCircle(
            color = Color(0xFFFFB300), // Moon color
            radius = 8.dp.toPx(),
            center = startHandleCenter
        )
        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = Offset(startHandleCenter.x - 3.dp.toPx(), startHandleCenter.y - 1.dp.toPx())
        )
        drawCircle(
            color = vitaColors.glassBorderDark.copy(alpha = 0.4f),
            radius = 16.dp.toPx(),
            center = startHandleCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )

        val endRad = hourToAngleRad(sleepEndHour)
        val endHandleCenter = Offset(
            x = center.x + outerRadius * cos(endRad).toFloat(),
            y = center.y + outerRadius * sin(endRad).toFloat()
        )
        
        // Wake-time handle (Sun)
        drawCircle(
            color = Color.White,
            radius = 16.dp.toPx(),
            center = endHandleCenter
        )
        drawCircle(
            color = Color(0xFFFF5722), // Orange/Sun color
            radius = 6.dp.toPx(),
            center = endHandleCenter
        )
        for (i in 0 until 8) {
            val rayAngle = Math.toRadians(i * 45.0)
            val rayStart = Offset(
                x = endHandleCenter.x + 7.dp.toPx() * cos(rayAngle).toFloat(),
                y = endHandleCenter.y + 7.dp.toPx() * sin(rayAngle).toFloat()
            )
            val rayEnd = Offset(
                x = endHandleCenter.x + 10.dp.toPx() * cos(rayAngle).toFloat(),
                y = endHandleCenter.y + 10.dp.toPx() * sin(rayAngle).toFloat()
            )
            drawLine(
                color = Color(0xFFFF5722),
                start = rayStart,
                end = rayEnd,
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawCircle(
            color = vitaColors.glassBorderDark.copy(alpha = 0.4f),
            radius = 16.dp.toPx(),
            center = endHandleCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

private fun OnColorFallback(vitaColors: com.example.vitaai.ui.theme.VitaColors): Color {
    // Use theme-aware border dark color for minor tick marks
    return vitaColors.glassBorderDark.copy(alpha = 1f)
}

private fun hourToAngleRad(hour: Double): Double {
    return Math.toRadians(hourToAngleDegrees(hour))
}

private fun hourToAngleDegrees(hour: Double): Double {
    return (270.0 + (hour / 24.0) * 360.0) % 360.0
}
