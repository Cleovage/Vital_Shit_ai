package com.example.vitaai.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.data.local.AmbientLightLogEntity
import com.example.vitaai.ui.theme.LocalVitaColors
import java.util.*

@Composable
fun LuminousLuxTimeline(
    modifier: Modifier = Modifier,
    lightLogs: List<AmbientLightLogEntity> = emptyList()
) {
    val vitaColors = LocalVitaColors.current
    
    // Process logs: group into 24 hourly averages for the current day
    val hourlyData = remember(lightLogs) {
        val data = FloatArray(24) { 0f }
        val counts = IntArray(24) { 0 }
        val calendar = Calendar.getInstance()
        
        for (log in lightLogs) {
            calendar.timeInMillis = log.timestampMillis
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            data[hour] += log.luxValue
            counts[hour]++
        }
        
        for (h in 0 until 24) {
            if (counts[h] > 0) {
                data[h] /= counts[h]
            } else {
                // If no data, supply a baseline (e.g. 0) or simulate nominal day/night curve
                data[h] = if (h in 7..18) 120f else 2f
            }
        }
        data
    }

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(hourlyData) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val linePath = remember { Path() }
    val fillPath = remember { Path() }
    val guideLabelPaint = remember { android.graphics.Paint().apply { textAlign = android.graphics.Paint.Align.RIGHT } }
    val glowPaint = remember { android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    } }
    val bottomLabelPaint = remember { android.graphics.Paint().apply { textAlign = android.graphics.Paint.Align.CENTER } }

    if (lightLogs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No light logs recorded today. Syncing data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingLeft = 32.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            
            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            // Scale lux values logarithmically to make low-lux (0-20) details visible alongside high-lux (1000+) daylight
            // scaledVal = ln(1 + lux)
            val maxLogLux = Math.log(1.0 + 1000.0) // Cap normal scale at 1000 lux
            val points = hourlyData.mapIndexed { hour, lux ->
                val logLux = Math.log(1.0 + lux.toDouble()).coerceAtMost(maxLogLux)
                val x = paddingLeft + (hour / 23f) * chartWidth
                val y = height - paddingBottom - ((logLux / maxLogLux).toFloat() * chartHeight * animationProgress.value)
                Offset(x, y)
            }

            // 1. Draw horizontal guide lines & night zone
            // Night zone (10 PM to 6 AM) shading
            val nightStartIdx = 22
            val nightEndIdx = 6
            val nightStartX = paddingLeft + (nightStartIdx / 23f) * chartWidth
            val nightEndX = paddingLeft + (nightEndIdx / 23f) * chartWidth

            // Draw night zone shadow (overlay on left and right)
            drawRect(
                color = Color(0xFF0F172A).copy(alpha = 0.05f),
                topLeft = Offset(paddingLeft, paddingTop),
                size = Size(nightEndX - paddingLeft, chartHeight)
            )
            drawRect(
                color = Color(0xFF0F172A).copy(alpha = 0.05f),
                topLeft = Offset(nightStartX, paddingTop),
                size = Size(width - paddingRight - nightStartX, chartHeight)
            )

            // Horizontal guides (e.g. 5 lux, 100 lux, 1000 lux)
            val guideLevels = listOf(5.0, 100.0, 1000.0)
            val guideLabels = listOf("5 lx", "100 lx", "1000 lx")
            
            val guideTextSize = 8.sp.toPx()
            guideLabelPaint.apply {
                color = Color(0xFF0F172A).copy(alpha = 0.4f).toArgb()
                textSize = guideTextSize
            }

            guideLevels.forEachIndexed { idx, level ->
                val logL = Math.log(1.0 + level).coerceAtMost(maxLogLux)
                val y = height - paddingBottom - ((logL / maxLogLux).toFloat() * chartHeight)
                
                drawLine(
                    color = Color(0xFF0F172A).copy(alpha = 0.08f),
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )

                // Labels
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(guideLabels[idx], paddingLeft - 4.dp.toPx(), y + 3.dp.toPx(), guideLabelPaint)
                }
            }

            // 2. Build Bezier Path
            linePath.reset()
            if (points.isNotEmpty()) {
                linePath.moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    linePath.quadraticBezierTo(controlX, p0.y, controlX, p1.y)
                    linePath.lineTo(p1.x, p1.y)
                }
            }

            // 3. Draw Gradient Fill Area
            if (points.isNotEmpty()) {
                fillPath.reset()
                fillPath.addPath(linePath)
                fillPath.lineTo(width - paddingRight, height - paddingBottom)
                fillPath.lineTo(paddingLeft, height - paddingBottom)
                fillPath.close()
                
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            vitaColors.accentAmber.copy(alpha = 0.25f),
                            vitaColors.accentAmber.copy(alpha = 0.0f)
                        ),
                        startY = paddingTop,
                        endY = height - paddingBottom
                    )
                )
            }

            // 4. Draw Glowing Chart Line
            drawIntoCanvas { canvas ->
                glowPaint.apply {
                    strokeWidth = 4.dp.toPx()
                    color = vitaColors.accentAmber.copy(alpha = 0.4f).toArgb()
                    maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawPath(linePath.asAndroidPath(), glowPaint)
            }

            drawPath(
                path = linePath,
                color = vitaColors.accentAmber,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 5. Draw bottom hour ticks labels (12 AM, 6 AM, 12 PM, 6 PM, 12 AM)
            val labelHours = listOf(0, 6, 12, 18, 23)
            val labels = listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM")
            
            val bottomTextSize = 9.sp.toPx()
            bottomLabelPaint.apply {
                color = Color(0xFF0F172A).copy(alpha = 0.4f).toArgb()
                textSize = bottomTextSize
            }
            
            labelHours.forEachIndexed { idx, hour ->
                val x = paddingLeft + (hour / 23f) * chartWidth
                val y = height - paddingBottom + 16.dp.toPx()
                
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(labels[idx], x, y, bottomLabelPaint)
                }
            }
        }
    }
}
