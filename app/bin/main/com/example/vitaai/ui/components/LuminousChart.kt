package com.example.vitaai.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.*
import kotlin.math.abs
import kotlin.math.max
import java.util.Locale

/**
 * Luminous line chart with neon glow effect and interactive touch support.
 */
@Composable
fun LuminousLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    lineColor: Color = PrimaryContainer,
    glowColor: Color = GlowPrimary,
    lineWidth: Float = 3f,
    glowWidth: Float = 10f,
    xAxisLabels: List<String> = emptyList(),
    yAxisTicks: Int = 5,
    yAxisLabelFormatter: (Float) -> String = { value -> formatAxisValue(value) },
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    axisColor: Color = OutlineVariant,
    labelColor: Color = OnSurfaceVariant,
    labelTextSize: TextUnit = 10.sp
) {
    if (dataPoints.size < 2) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { selectedIndex = null },
                    onDragCancel = { selectedIndex = null },
                    onDrag = { change, _ ->
                        val w = size.width
                        val labelTextSizePx = labelTextSize.toPx()
                        val axisTickLength = 4.dp.toPx()
                        val labelPadding = 6.dp.toPx()
                        val rightPadding = 8.dp.toPx()
                        
                        val maxVal = dataPoints.maxOrNull() ?: 0f
                        val minVal = dataPoints.minOrNull() ?: 0f
                        val axisMax = if (maxVal == minVal) maxVal + 1f else maxVal
                        val axisMin = if (maxVal == minVal) minVal - 1f else minVal
                        val range = (axisMax - axisMin).coerceAtLeast(1f)
                        val tickCount = yAxisTicks.coerceAtLeast(2)
                        val yTickValues = (0 until tickCount).map { index ->
                            axisMin + (range * (index / (tickCount - 1f)))
                        }
                        val yLabels = yTickValues.map(yAxisLabelFormatter)
                        val textPaint = Paint().apply {
                            isAntiAlias = true
                            color = labelColor.toArgb()
                            textSize = labelTextSizePx
                        }
                        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { textPaint.measureText(it) } else 0f
                        val leftPadding = if (showAxes || yLabels.isNotEmpty()) {
                            max(28.dp.toPx(), maxLabelWidth + labelPadding + axisTickLength)
                        } else {
                            8.dp.toPx()
                        }
                        
                        val chartLeft = leftPadding
                        val chartRight = w - rightPadding
                        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
                        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
                        
                        val touchX = change.position.x
                        if (touchX in chartLeft..chartRight) {
                            val rawIndex = ((touchX - chartLeft) / stepX).toInt()
                            val remainder = ((touchX - chartLeft) % stepX)
                            selectedIndex = if (remainder > stepX / 2 && rawIndex + 1 < dataPoints.size) {
                                rawIndex + 1
                            } else {
                                rawIndex
                            }.coerceIn(0, dataPoints.lastIndex)
                        } else {
                            selectedIndex = null
                        }
                    }
                )
            }
            .pointerInput(dataPoints) {
                detectTapGestures(
                    onPress = { offset ->
                        val w = size.width
                        val labelTextSizePx = labelTextSize.toPx()
                        val axisTickLength = 4.dp.toPx()
                        val labelPadding = 6.dp.toPx()
                        val rightPadding = 8.dp.toPx()
                        
                        val maxVal = dataPoints.maxOrNull() ?: 0f
                        val minVal = dataPoints.minOrNull() ?: 0f
                        val axisMax = if (maxVal == minVal) maxVal + 1f else maxVal
                        val axisMin = if (maxVal == minVal) minVal - 1f else minVal
                        val range = (axisMax - axisMin).coerceAtLeast(1f)
                        val tickCount = yAxisTicks.coerceAtLeast(2)
                        val yTickValues = (0 until tickCount).map { index ->
                            axisMin + (range * (index / (tickCount - 1f)))
                        }
                        val yLabels = yTickValues.map(yAxisLabelFormatter)
                        val textPaint = Paint().apply {
                            isAntiAlias = true
                            color = labelColor.toArgb()
                            textSize = labelTextSizePx
                        }
                        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { textPaint.measureText(it) } else 0f
                        val leftPadding = if (showAxes || yLabels.isNotEmpty()) {
                            max(28.dp.toPx(), maxLabelWidth + labelPadding + axisTickLength)
                        } else {
                            8.dp.toPx()
                        }
                        
                        val chartLeft = leftPadding
                        val chartRight = w - rightPadding
                        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
                        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
                        
                        val touchX = offset.x
                        if (touchX in chartLeft..chartRight) {
                            val rawIndex = ((touchX - chartLeft) / stepX).toInt()
                            val remainder = ((touchX - chartLeft) % stepX)
                            selectedIndex = if (remainder > stepX / 2 && rawIndex + 1 < dataPoints.size) {
                                rawIndex + 1
                            } else {
                                rawIndex
                            }.coerceIn(0, dataPoints.lastIndex)
                        }
                        
                        tryAwaitRelease()
                        selectedIndex = null
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        val labelTextSizePx = labelTextSize.toPx()
        val axisStroke = 1.dp.toPx()
        val axisTickLength = 4.dp.toPx()
        val labelPadding = 6.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 8.dp.toPx()
        val bottomPadding = if (xAxisLabels.isNotEmpty()) max(20.dp.toPx(), labelTextSizePx * 2.2f) else 8.dp.toPx()

        val maxVal = dataPoints.maxOrNull() ?: 0f
        val minVal = dataPoints.minOrNull() ?: 0f
        val axisMax = if (maxVal == minVal) maxVal + 1f else maxVal
        val axisMin = if (maxVal == minVal) minVal - 1f else minVal
        val range = (axisMax - axisMin).coerceAtLeast(1f)
        val tickCount = yAxisTicks.coerceAtLeast(2)
        val yTickValues = (0 until tickCount).map { index ->
            axisMin + (range * (index / (tickCount - 1f)))
        }
        val yLabels = yTickValues.map(yAxisLabelFormatter)
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textSize = labelTextSizePx
        }
        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { textPaint.measureText(it) } else 0f
        val leftPadding = if (showAxes || yLabels.isNotEmpty()) {
            max(28.dp.toPx(), maxLabelWidth + labelPadding + axisTickLength)
        } else {
            8.dp.toPx()
        }

        val chartLeft = leftPadding
        val chartTop = topPadding
        val chartRight = w - rightPadding
        val chartBottom = h - bottomPadding
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        val points = dataPoints.mapIndexed { i, value ->
            Offset(
                x = chartLeft + i * stepX,
                y = chartBottom - ((value - axisMin) / range) * chartHeight
            )
        }

        val gridColor = axisColor.copy(alpha = 0.2f)
        if (showGrid) {
            yTickValues.forEach { value ->
                val y = chartBottom - ((value - axisMin) / range) * chartHeight
                drawLine(
                    color = gridColor,
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = axisStroke
                )
            }

            if (xAxisLabels.size > 1) {
                val xStep = if (xAxisLabels.size == dataPoints.size) stepX else chartWidth / (xAxisLabels.size - 1)
                xAxisLabels.forEachIndexed { index, _ ->
                    val x = if (xAxisLabels.size == dataPoints.size) {
                        chartLeft + index * stepX
                    } else {
                        chartLeft + index * xStep
                    }
                    drawLine(
                        color = gridColor,
                        start = Offset(x, chartTop),
                        end = Offset(x, chartBottom),
                        strokeWidth = axisStroke
                    )
                }
            }
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
            lineTo(points.last().x, chartBottom)
            lineTo(points.first().x, chartBottom)
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

        if (showAxes) {
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartTop),
                end = Offset(chartLeft, chartBottom),
                strokeWidth = axisStroke
            )
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartBottom),
                end = Offset(chartRight, chartBottom),
                strokeWidth = axisStroke
            )
        }
        
        // Draw interactive tooltip
        selectedIndex?.let { index ->
            val point = points[index]
            val valueStr = yAxisLabelFormatter(dataPoints[index])
            val labelStr = if (index < xAxisLabels.size) xAxisLabels[index] else ""
            
            // Highlight line
            drawLine(
                color = lineColor.copy(alpha = 0.5f),
                start = Offset(point.x, chartTop),
                end = Offset(point.x, chartBottom),
                strokeWidth = 2.dp.toPx()
            )
            
            // Highlight point
            drawCircle(color = glowColor, radius = 12f, center = point)
            drawCircle(color = Color.White, radius = 6f, center = point)
            
            drawIntoCanvas { canvas ->
                val tooltipTextPaint = Paint().apply {
                    color = Color.White.toArgb()
                    textSize = labelTextSizePx * 1.2f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                
                val tooltipBgPaint = Paint().apply {
                    color = Color.DarkGray.toArgb()
                    alpha = 220
                    isAntiAlias = true
                }
                
                val textToDraw = "$valueStr ${if (labelStr.isNotEmpty()) "($labelStr)" else ""}"
                val textWidth = tooltipTextPaint.measureText(textToDraw)
                val padding = 16f
                
                var tooltipX = point.x
                if (tooltipX - textWidth / 2 - padding < chartLeft) {
                    tooltipX = chartLeft + textWidth / 2 + padding
                } else if (tooltipX + textWidth / 2 + padding > chartRight) {
                    tooltipX = chartRight - textWidth / 2 - padding
                }
                
                val tooltipY = point.y - 30f
                val bgRect = android.graphics.RectF(
                    tooltipX - textWidth / 2 - padding,
                    tooltipY - tooltipTextPaint.textSize - padding,
                    tooltipX + textWidth / 2 + padding,
                    tooltipY + padding
                )
                
                canvas.nativeCanvas.drawRoundRect(bgRect, 8f, 8f, tooltipBgPaint)
                canvas.nativeCanvas.drawText(textToDraw, tooltipX, tooltipY, tooltipTextPaint)
            }
        }

        drawIntoCanvas { canvas ->
            val tickPaint = Paint().apply {
                color = axisColor.toArgb()
                strokeWidth = axisStroke
                isAntiAlias = true
                style = Paint.Style.STROKE
            }

            if (yLabels.isNotEmpty()) {
                val yPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
                yLabels.forEachIndexed { index, label ->
                    val value = yTickValues[index]
                    val y = chartBottom - ((value - axisMin) / range) * chartHeight
                    canvas.nativeCanvas.drawText(label, chartLeft - labelPadding, y + labelTextSizePx * 0.35f, yPaint)
                    if (showAxes) {
                        canvas.nativeCanvas.drawLine(
                            chartLeft - axisTickLength,
                            y,
                            chartLeft,
                            y,
                            tickPaint
                        )
                    }
                }
            }

            if (xAxisLabels.isNotEmpty()) {
                val xPaint = Paint(textPaint).apply { textAlign = Paint.Align.CENTER }
                val xStep = if (xAxisLabels.size == dataPoints.size) stepX else chartWidth / (xAxisLabels.size - 1).coerceAtLeast(1)
                val minSpacing = labelTextSizePx * 3.2f
                var lastX = -Float.MAX_VALUE

                xAxisLabels.forEachIndexed { index, label ->
                    val x = if (xAxisLabels.size == dataPoints.size) {
                        chartLeft + index * stepX
                    } else {
                        chartLeft + index * xStep
                    }
                    val labelWidth = xPaint.measureText(label)
                    val desiredSpacing = max(minSpacing, labelWidth * 0.75f)
                    val isEdge = index == 0 || index == xAxisLabels.lastIndex
                    if (!isEdge && x - lastX < desiredSpacing) return@forEachIndexed
                    lastX = x
                    canvas.nativeCanvas.drawText(label, x, chartBottom + labelTextSizePx * 1.6f, xPaint)
                    if (showAxes) {
                        canvas.nativeCanvas.drawLine(
                            x,
                            chartBottom,
                            x,
                            chartBottom + axisTickLength,
                            tickPaint
                        )
                    }
                }
            }
        }
    }
}

/**
 * Luminous bar chart with vertical linear gradients and interactive touch support.
 */
@Composable
fun LuminousBarChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    barColor: Color = PrimaryContainer,
    glowColor: Color = GlowPrimary,
    barSpacing: Float = 0.3f, // fraction of bar width used as gap
    xAxisLabels: List<String> = emptyList(),
    yAxisTicks: Int = 5,
    yAxisLabelFormatter: (Float) -> String = { value -> formatAxisValue(value) },
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    axisColor: Color = OutlineVariant,
    labelColor: Color = OnSurfaceVariant,
    labelTextSize: TextUnit = 10.sp
) {
    if (dataPoints.isEmpty()) return
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { selectedIndex = null },
                    onDragCancel = { selectedIndex = null },
                    onDrag = { change, _ ->
                        val w = size.width
                        val labelTextSizePx = labelTextSize.toPx()
                        val axisTickLength = 4.dp.toPx()
                        val labelPadding = 6.dp.toPx()
                        val rightPadding = 8.dp.toPx()
                        
                        val maxVal = dataPoints.maxOrNull() ?: 0f
                        val axisMax = if (maxVal <= 0f) 1f else maxVal
                        val axisMin = 0f
                        val range = (axisMax - axisMin).coerceAtLeast(1f)
                        val tickCount = yAxisTicks.coerceAtLeast(2)
                        val yTickValues = (0 until tickCount).map { index ->
                            axisMin + (range * (index / (tickCount - 1f)))
                        }
                        val yLabels = yTickValues.map(yAxisLabelFormatter)
                        val textPaint = Paint().apply {
                            isAntiAlias = true
                            color = labelColor.toArgb()
                            textSize = labelTextSizePx
                        }
                        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { textPaint.measureText(it) } else 0f
                        val leftPadding = if (showAxes || yLabels.isNotEmpty()) {
                            max(28.dp.toPx(), maxLabelWidth + labelPadding + axisTickLength)
                        } else {
                            8.dp.toPx()
                        }
                        
                        val chartLeft = leftPadding
                        val chartRight = w - rightPadding
                        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
                        val totalBarWidth = chartWidth / dataPoints.size
                        
                        val touchX = change.position.x
                        if (touchX in chartLeft..chartRight) {
                            selectedIndex = ((touchX - chartLeft) / totalBarWidth).toInt().coerceIn(0, dataPoints.lastIndex)
                        } else {
                            selectedIndex = null
                        }
                    }
                )
            }
            .pointerInput(dataPoints) {
                detectTapGestures(
                    onPress = { offset ->
                        val w = size.width
                        val labelTextSizePx = labelTextSize.toPx()
                        val axisTickLength = 4.dp.toPx()
                        val labelPadding = 6.dp.toPx()
                        val rightPadding = 8.dp.toPx()
                        
                        val maxVal = dataPoints.maxOrNull() ?: 0f
                        val axisMax = if (maxVal <= 0f) 1f else maxVal
                        val axisMin = 0f
                        val range = (axisMax - axisMin).coerceAtLeast(1f)
                        val tickCount = yAxisTicks.coerceAtLeast(2)
                        val yTickValues = (0 until tickCount).map { index ->
                            axisMin + (range * (index / (tickCount - 1f)))
                        }
                        val yLabels = yTickValues.map(yAxisLabelFormatter)
                        val textPaint = Paint().apply {
                            isAntiAlias = true
                            color = labelColor.toArgb()
                            textSize = labelTextSizePx
                        }
                        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { textPaint.measureText(it) } else 0f
                        val leftPadding = if (showAxes || yLabels.isNotEmpty()) {
                            max(28.dp.toPx(), maxLabelWidth + labelPadding + axisTickLength)
                        } else {
                            8.dp.toPx()
                        }
                        
                        val chartLeft = leftPadding
                        val chartRight = w - rightPadding
                        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
                        val totalBarWidth = chartWidth / dataPoints.size
                        
                        val touchX = offset.x
                        if (touchX in chartLeft..chartRight) {
                            selectedIndex = ((touchX - chartLeft) / totalBarWidth).toInt().coerceIn(0, dataPoints.lastIndex)
                        }
                        
                        tryAwaitRelease()
                        selectedIndex = null
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        val labelTextSizePx = labelTextSize.toPx()
        val axisStroke = 1.dp.toPx()
        val axisTickLength = 4.dp.toPx()
        val labelPadding = 6.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 8.dp.toPx()
        val bottomPadding = if (xAxisLabels.isNotEmpty()) max(20.dp.toPx(), labelTextSizePx * 2.2f) else 8.dp.toPx()

        val maxVal = dataPoints.maxOrNull() ?: 0f
        val axisMax = if (maxVal <= 0f) 1f else maxVal
        val axisMin = 0f
        val range = (axisMax - axisMin).coerceAtLeast(1f)
        val tickCount = yAxisTicks.coerceAtLeast(2)
        val yTickValues = (0 until tickCount).map { index ->
            axisMin + (range * (index / (tickCount - 1f)))
        }
        val yLabels = yTickValues.map(yAxisLabelFormatter)
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textSize = labelTextSizePx
        }
        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { textPaint.measureText(it) } else 0f
        val leftPadding = if (showAxes || yLabels.isNotEmpty()) {
            max(28.dp.toPx(), maxLabelWidth + labelPadding + axisTickLength)
        } else {
            8.dp.toPx()
        }

        val chartLeft = leftPadding
        val chartTop = topPadding
        val chartRight = w - rightPadding
        val chartBottom = h - bottomPadding
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

        val totalBarWidth = chartWidth / dataPoints.size
        val gap = totalBarWidth * barSpacing
        val barWidth = totalBarWidth - gap

        val gridColor = axisColor.copy(alpha = 0.2f)
        if (showGrid) {
            yTickValues.forEach { value ->
                val y = chartBottom - ((value - axisMin) / range) * chartHeight
                drawLine(
                    color = gridColor,
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = axisStroke
                )
            }

            if (xAxisLabels.size > 1) {
                val xStep = if (xAxisLabels.size == dataPoints.size) totalBarWidth else chartWidth / (xAxisLabels.size - 1)
                xAxisLabels.forEachIndexed { index, _ ->
                    val x = if (xAxisLabels.size == dataPoints.size) {
                        chartLeft + index * totalBarWidth + gap / 2 + barWidth / 2
                    } else {
                        chartLeft + index * xStep
                    }
                    drawLine(
                        color = gridColor,
                        start = Offset(x, chartTop),
                        end = Offset(x, chartBottom),
                        strokeWidth = axisStroke
                    )
                }
            }
        }

        dataPoints.forEachIndexed { i, value ->
            val barHeight = (value / axisMax) * chartHeight
            val x = chartLeft + i * totalBarWidth + gap / 2
            val y = chartBottom - barHeight
            val cornerRadius = barWidth / 3
            
            val isSelected = selectedIndex == i
            val currentBarColor = if (isSelected) glowColor else barColor
            val currentAlpha = if (isSelected) 0.8f else 0.4f

            // Bar with vertical gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(currentBarColor, currentBarColor.copy(alpha = currentAlpha)),
                    startY = y,
                    endY = chartBottom
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
        }

        if (showAxes) {
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartTop),
                end = Offset(chartLeft, chartBottom),
                strokeWidth = axisStroke
            )
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartBottom),
                end = Offset(chartRight, chartBottom),
                strokeWidth = axisStroke
            )
        }
        
        // Draw interactive tooltip for bars
        selectedIndex?.let { index ->
            val barHeight = (dataPoints[index] / axisMax) * chartHeight
            val x = chartLeft + index * totalBarWidth + gap / 2 + barWidth / 2
            val y = chartBottom - barHeight
            val valueStr = yAxisLabelFormatter(dataPoints[index])
            val labelStr = if (index < xAxisLabels.size) xAxisLabels[index] else ""
            
            drawIntoCanvas { canvas ->
                val tooltipTextPaint = Paint().apply {
                    color = Color.White.toArgb()
                    textSize = labelTextSizePx * 1.2f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                
                val tooltipBgPaint = Paint().apply {
                    color = Color.DarkGray.toArgb()
                    alpha = 220
                    isAntiAlias = true
                }
                
                val textToDraw = "$valueStr ${if (labelStr.isNotEmpty()) "($labelStr)" else ""}"
                val textWidth = tooltipTextPaint.measureText(textToDraw)
                val padding = 16f
                
                var tooltipX = x
                if (tooltipX - textWidth / 2 - padding < chartLeft) {
                    tooltipX = chartLeft + textWidth / 2 + padding
                } else if (tooltipX + textWidth / 2 + padding > chartRight) {
                    tooltipX = chartRight - textWidth / 2 - padding
                }
                
                val tooltipY = y - 30f
                val bgRect = android.graphics.RectF(
                    tooltipX - textWidth / 2 - padding,
                    tooltipY - tooltipTextPaint.textSize - padding,
                    tooltipX + textWidth / 2 + padding,
                    tooltipY + padding
                )
                
                canvas.nativeCanvas.drawRoundRect(bgRect, 8f, 8f, tooltipBgPaint)
                canvas.nativeCanvas.drawText(textToDraw, tooltipX, tooltipY, tooltipTextPaint)
            }
        }

        drawIntoCanvas { canvas ->
            val tickPaint = Paint().apply {
                color = axisColor.toArgb()
                strokeWidth = axisStroke
                isAntiAlias = true
                style = Paint.Style.STROKE
            }

            if (yLabels.isNotEmpty()) {
                val yPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
                yLabels.forEachIndexed { index, label ->
                    val value = yTickValues[index]
                    val y = chartBottom - ((value - axisMin) / range) * chartHeight
                    canvas.nativeCanvas.drawText(label, chartLeft - labelPadding, y + labelTextSizePx * 0.35f, yPaint)
                    if (showAxes) {
                        canvas.nativeCanvas.drawLine(
                            chartLeft - axisTickLength,
                            y,
                            chartLeft,
                            y,
                            tickPaint
                        )
                    }
                }
            }

            if (xAxisLabels.isNotEmpty()) {
                val xPaint = Paint(textPaint).apply { textAlign = Paint.Align.CENTER }
                val xStep = if (xAxisLabels.size == dataPoints.size) totalBarWidth else chartWidth / (xAxisLabels.size - 1).coerceAtLeast(1)
                val minSpacing = labelTextSizePx * 3.2f
                var lastX = -Float.MAX_VALUE

                xAxisLabels.forEachIndexed { index, label ->
                    val x = if (xAxisLabels.size == dataPoints.size) {
                        chartLeft + index * totalBarWidth + gap / 2 + barWidth / 2
                    } else {
                        chartLeft + index * xStep
                    }
                    val labelWidth = xPaint.measureText(label)
                    val desiredSpacing = max(minSpacing, labelWidth * 0.75f)
                    val isEdge = index == 0 || index == xAxisLabels.lastIndex
                    if (!isEdge && x - lastX < desiredSpacing) return@forEachIndexed
                    lastX = x
                    canvas.nativeCanvas.drawText(label, x, chartBottom + labelTextSizePx * 1.6f, xPaint)
                    if (showAxes) {
                        canvas.nativeCanvas.drawLine(
                            x,
                            chartBottom,
                            x,
                            chartBottom + axisTickLength,
                            tickPaint
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Donut Chart specifically for macro balance (Protein/Carbs/Fats).
 */
@Composable
fun LuminousDonutChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    thickness: Dp = 24.dp
) {
    if (values.isEmpty() || colors.isEmpty()) return
    
    val total = values.sum()
    if (total <= 0f) return
    
    val proportions = values.map { it / total }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(values) {
                detectTapGestures(
                    onPress = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val radius = kotlin.math.sqrt(dx * dx + dy * dy)
                        
                        val innerRadius = (minOf(size.width, size.height) / 2f) - thickness.toPx()
                        val outerRadius = minOf(size.width, size.height) / 2f
                        
                        if (radius in innerRadius..outerRadius) {
                            var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            
                            var currentAngle = 0f
                            selectedIndex = proportions.indexOfFirst { proportion ->
                                val sweep = proportion * 360f
                                val match = angle in currentAngle..(currentAngle + sweep)
                                currentAngle += sweep
                                match
                            }
                            if (selectedIndex == -1) selectedIndex = null
                        } else {
                            selectedIndex = null
                        }
                        
                        tryAwaitRelease()
                        selectedIndex = null
                    }
                )
            }
    ) {
        var startAngle = 0f
        val strokeWidth = thickness.toPx()
        val rectSize = minOf(size.width, size.height)
        val topLeft = Offset(
            (size.width - rectSize) / 2,
            (size.height - rectSize) / 2
        )

        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = proportion * 360f
            val isSelected = selectedIndex == index
            val currentThickness = if (isSelected) strokeWidth * 1.2f else strokeWidth
            
            if (isSelected) {
                drawArc(
                    color = colors[index].copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(rectSize, rectSize),
                    style = Stroke(width = currentThickness * 1.5f, cap = StrokeCap.Butt)
                )
            }

            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(rectSize, rectSize),
                style = Stroke(width = currentThickness, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

private fun formatAxisValue(value: Float): String {
    val absValue = abs(value)
    return when {
        absValue >= 1000f -> String.format(Locale.US, "%.0f", value)
        absValue >= 100f -> String.format(Locale.US, "%.0f", value)
        absValue >= 10f -> String.format(Locale.US, "%.1f", value)
        absValue >= 1f -> String.format(Locale.US, "%.2f", value)
        else -> String.format(Locale.US, "%.3f", value)
    }
}
