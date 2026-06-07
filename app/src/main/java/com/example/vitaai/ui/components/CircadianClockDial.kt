package com.example.vitaai.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.LocalVitaColors
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircadianClockDial(
    modifier: Modifier = Modifier,
    sleepStartHour: Double = 23.0, // 11 PM
    sleepEndHour: Double = 7.0,    // 7 AM
    currentHourOverride: Double? = null
) {
    val vitaColors = LocalVitaColors.current
    
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

    Canvas(modifier = modifier.aspectRatio(1f).fillMaxWidth()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        val outerRadius = (width / 2) * 0.85f
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
            val color = if (isMajor) vitaColors.accentAmber else OnColorFallback(vitaColors)
            val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            
            drawLine(
                color = color.copy(alpha = if (isMajor) 0.8f else 0.3f),
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
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 20.dp.toPx()
                strokeCap = android.graphics.Paint.Cap.ROUND
                color = Color(0xFF3F51B5).copy(alpha = 0.25f).toArgb()
                maskFilter = BlurMaskFilter(12.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
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
                paint
            )
        }

        // Draw foreground sharp Sleep Zone arc
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color(0xFF3F51B5), Color(0xFF673AB7), Color(0xFF3F51B5)),
                center = center
            ),
            startAngle = startSleepAngle.toFloat(),
            sweepAngle = sleepSweepDegrees.toFloat(),
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // 5. Draw Major Hour Labels (12 AM, 6 AM, 12 PM, 6 PM)
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = Color(0xFF0F172A).toArgb() // Slate-900 label for high contrast
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            // Labels positions: Midnight (top), Noon (bottom), 6 AM (right), 6 PM (left)
            canvas.nativeCanvas.drawText("12 AM", center.x, center.y - outerRadius - 12.dp.toPx(), paint)
            canvas.nativeCanvas.drawText("12 PM", center.x, center.y + outerRadius + 20.dp.toPx(), paint)
            
            paint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.nativeCanvas.drawText("6 AM", center.x + outerRadius + 8.dp.toPx(), center.y + 4.dp.toPx(), paint)
            
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.nativeCanvas.drawText("6 PM", center.x - outerRadius - 8.dp.toPx(), center.y + 4.dp.toPx(), paint)
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
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
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
            val glowPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = vitaColors.accentAmber.copy(alpha = pulseAlpha).toArgb()
                maskFilter = BlurMaskFilter(8.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
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
    }
}

private fun OnColorFallback(vitaColors: com.example.vitaai.ui.theme.VitaColors): Color {
    return Color(0xFF4A493B)
}

/**
 * Maps hour (0.0 to 24.0) to Radian.
 * Midnight (0:00) is at -90 degrees (270 degrees) = -PI/2.
 */
private fun hourToAngleRad(hour: Double): Double {
    return Math.toRadians(hourToAngleDegrees(hour))
}

/**
 * Maps hour (0.0 to 24.0) to degrees.
 * Midnight (0:00) is at 270 degrees (Top).
 * Midday (12:00) is at 90 degrees (Bottom).
 */
private fun hourToAngleDegrees(hour: Double): Double {
    return (270.0 + (hour / 24.0) * 360.0) % 360.0
}
