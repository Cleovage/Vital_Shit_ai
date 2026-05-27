package com.example.vitaai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.*

/**
 * Luminous line chart with neon glow effect.
 *
 * - Primary line with gradient stroke
 * - Secondary blurred "glow" stroke behind the main line
 * - Gradient fill under the line (fading to transparent)
 */
@Composable
fun LuminousLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    lineColor: Color = PrimaryContainer,
    glowColor: Color = GlowPrimary,
    lineWidth: Float = 3f,
    glowWidth: Float = 10f
) {
    if (dataPoints.size < 2) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val w = size.width
        val h = size.height
        val maxVal = dataPoints.max().coerceAtLeast(1f)
        val minVal = dataPoints.min()
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)

        val points = dataPoints.mapIndexed { i, value ->
            Offset(
                x = i * stepX,
                y = h - ((value - minVal) / range) * h * 0.85f - h * 0.05f
            )
        }

        // Build the line path
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cx1 = (prev.x + curr.x) / 2
                cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
            }
        }

        // Build the fill path (line + close to bottom)
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, h)
            lineTo(points.first().x, h)
            close()
        }

        // Gradient fill under the line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
        )

        // Glow stroke (wider, semi-transparent)
        drawPath(
            path = linePath,
            color = glowColor,
            style = Stroke(
                width = glowWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Main line stroke
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = lineWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Dot on last point
        drawCircle(
            color = lineColor,
            radius = 5f,
            center = points.last()
        )
        drawCircle(
            color = glowColor.copy(alpha = 0.5f),
            radius = 10f,
            center = points.last()
        )
    }
}

/**
 * Luminous bar chart with vertical linear gradients.
 */
@Composable
fun LuminousBarChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    barColor: Color = PrimaryContainer,
    glowColor: Color = GlowPrimary,
    barSpacing: Float = 0.3f // fraction of bar width used as gap
) {
    if (dataPoints.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val w = size.width
        val h = size.height
        val maxVal = dataPoints.max().coerceAtLeast(1f)
        val totalBarWidth = w / dataPoints.size
        val gap = totalBarWidth * barSpacing
        val barWidth = totalBarWidth - gap

        dataPoints.forEachIndexed { i, value ->
            val barHeight = (value / maxVal) * h * 0.85f
            val x = i * totalBarWidth + gap / 2
            val y = h - barHeight
            val cornerRadius = barWidth / 3

            // Bar with vertical gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barColor, barColor.copy(alpha = 0.4f)),
                    startY = y,
                    endY = h
                ),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}
