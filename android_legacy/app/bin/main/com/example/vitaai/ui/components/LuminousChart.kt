package com.example.vitaai.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
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
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
    labelTextSize: TextUnit = 10.sp,
    minY: Float? = null,
    maxY: Float? = null
) {
    if (dataPoints.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                color = labelColor,
                fontSize = 14.sp
            )
        }
        return
    }


    // ── Spring animation: each point rises 0→1 with per-point stagger ──
    val yAnimatables = remember(dataPoints.size) {
        List(dataPoints.size) { Animatable(0f) }
    }
    LaunchedEffect(dataPoints) {
        yAnimatables.forEachIndexed { index, anim ->
            launch {
                delay(index * 30L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val linePath      = remember { Path() }
    val fillPath      = remember { Path() }
    val yLabelPaint   = remember { Paint().apply { textAlign = Paint.Align.RIGHT } }
    val xLabelPaint   = remember { Paint() }
    val tipValuePaint = remember { Paint() }
    val tipBgPaint    = remember { Paint().apply { style = Paint.Style.FILL } }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val labelTextSizePx = with(density) { labelTextSize.toPx() }
    val axisTickLengthPx = with(density) { 4.dp.toPx() }
    val labelPaddingPx = with(density) { 6.dp.toPx() }
    val rightPaddingPx = with(density) { 8.dp.toPx() }
    val topPaddingPx = with(density) { 8.dp.toPx() }
    val bottomPaddingPx = if (xAxisLabels.isNotEmpty()) {
        with(density) { maxOf(32.dp.toPx(), labelTextSizePx * 3.5f) }
    } else {
        with(density) { 12.dp.toPx() }
    }
    val dp8Px = with(density) { 8.dp.toPx() }
    val dp28Px = with(density) { 28.dp.toPx() }
    val dp40Px = with(density) { 40.dp.toPx() }

    val maxVal = maxY ?: (dataPoints.maxOrNull() ?: 0f)
    val minVal = minY ?: (dataPoints.minOrNull() ?: 0f)
    val rawMax  = if (maxVal == minVal) maxVal + 1f else maxVal
    val axisMax = if (maxY != null) maxY else niceAxisMax(rawMax, yAxisTicks)
    val axisMin = if (minY != null) minY else (if (maxVal == minVal) minVal - 1f else minVal)
    val range   = (axisMax - axisMin).coerceAtLeast(1f)
    val tickCount = yAxisTicks.coerceAtLeast(2)
    val yTickValues = remember(dataPoints, yAxisTicks, minY, maxY) {
        (0 until tickCount).map { index ->
            axisMin + (range * (index / (tickCount - 1f)))
        }
    }
    val yLabels = remember(yTickValues, yAxisLabelFormatter) {
        yTickValues.map(yAxisLabelFormatter)
    }

    val leftPaddingPx = remember(yLabels, showAxes, labelTextSizePx, axisTickLengthPx, labelPaddingPx, dp8Px, dp28Px, dp40Px) {
        val paint = Paint().apply {
            textSize = labelTextSizePx
        }
        val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { paint.measureText(it) } else 0f
        if (showAxes || yLabels.isNotEmpty()) {
            maxOf(dp40Px, maxLabelWidth + labelPaddingPx * 1.5f + axisTickLengthPx)
        } else {
            dp8Px
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { /* Keep tooltip visible */ },
                    onDragCancel = { /* Keep tooltip visible */ },
                    onDrag = { change, _ ->
                        val w = size.width
                        val chartLeft = leftPaddingPx
                        val chartRight = w - rightPaddingPx
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
                        }
                    }
                )
            }
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    val chartLeft  = leftPaddingPx
                    val chartRight = size.width - rightPaddingPx
                    val cw    = (chartRight - chartLeft).coerceAtLeast(1f)
                    val stepX = cw / (dataPoints.size - 1).coerceAtLeast(1)
                    if (offset.x in chartLeft..chartRight) {
                        val rawIdx = ((offset.x - chartLeft) / stepX).toInt()
                        val rem    = (offset.x - chartLeft) % stepX
                        val idx    = if (rem > stepX / 2 && rawIdx + 1 < dataPoints.size) rawIdx + 1 else rawIdx
                        val clamped = idx.coerceIn(0, dataPoints.lastIndex)
                        selectedIndex = if (selectedIndex == clamped) null else clamped
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val axisStroke = 1.dp.toPx()
        val rightPadding = rightPaddingPx
        val topPadding = topPaddingPx
        val bottomPadding = bottomPaddingPx

        val chartLeft = leftPaddingPx
        val chartTop = topPadding
        val chartRight = w - rightPadding
        val chartBottom = h - bottomPadding
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        // Animated: each Y springs from chartBottom → target (wave-rise effect)
        val points = dataPoints.mapIndexed { i, value ->
            val targetY = chartBottom - ((value - axisMin) / range) * chartHeight
            val animY   = chartBottom - (chartBottom - targetY) * yAnimatables[i].value
            Offset(x = chartLeft + i * stepX, y = animY)
        }

        if (showGrid) {
            // Alternating major/minor grid lines for visual hierarchy
            yTickValues.forEachIndexed { idx, value ->
                val y = chartBottom - ((value - axisMin) / range) * chartHeight
                val isMajor = idx % 2 == 0
                drawLine(
                    color = axisColor.copy(alpha = if (isMajor) 0.10f else 0.05f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = if (isMajor) 1.5f else 0.8f,
                    pathEffect = if (isMajor) PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                 else PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
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
                        color = axisColor.copy(alpha = 0.06f),
                        start = Offset(x, chartTop),
                        end = Offset(x, chartBottom),
                        strokeWidth = 0.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }
            }
        }

        // Build the line path
        linePath.reset()
        linePath.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val cx1 = (prev.x + curr.x) / 2
            linePath.cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
        }

        // Build the fill path (line + close to bottom)
        fillPath.reset()
        fillPath.addPath(linePath)
        fillPath.lineTo(points.last().x, chartBottom)
        fillPath.lineTo(points.first().x, chartBottom)
        fillPath.close()

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

        // Pulsing end-dot (M3 Expressive)
        drawCircle(color = glowColor.copy(alpha = 0.20f), radius = 14f, center = points.last())
        drawCircle(color = lineColor.copy(alpha = 0.55f), radius = 8f,  center = points.last())
        drawCircle(color = Color.White,  radius = 5f,  center = points.last())
        drawCircle(color = lineColor,    radius = 3f,  center = points.last())

        if (showAxes) {
            drawLine(
                color = axisColor.copy(alpha = 0.3f),
                start = Offset(chartLeft, chartBottom),
                end = Offset(chartRight, chartBottom),
                strokeWidth = 1.5f
            )
        }
        
        // ── Active selection: vertical cursor + M3 concentric rings ───────────
        selectedIndex?.let { idx ->
            val point = points[idx]
            drawLine(
                color = lineColor.copy(alpha = 0.30f),
                start = Offset(point.x, chartTop), end = Offset(point.x, chartBottom),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
            drawCircle(color = glowColor.copy(alpha = 0.20f), radius = 18f, center = point)
            drawCircle(color = lineColor.copy(alpha = 0.55f), radius = 11f, center = point)
            drawCircle(color = Color.White,  radius = 7f,  center = point)
            drawCircle(color = lineColor,    radius = 4f,  center = point)
        }

        // ── M3 chip tooltip ──────────────────────────────────────────
        selectedIndex?.let { idx ->
            val point    = points[idx]
            val valueStr = yAxisLabelFormatter(dataPoints[idx])
            val labelStr = if (idx < xAxisLabels.size) xAxisLabels[idx] else ""
            val displayText = if (labelStr.isNotEmpty()) "$labelStr  ·  $valueStr" else valueStr

            drawIntoCanvas { canvas ->
                tipValuePaint.apply {
                    isAntiAlias = true
                    textSize    = labelTextSizePx * 1.15f
                    textAlign   = Paint.Align.CENTER
                    typeface    = android.graphics.Typeface.create(
                        android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD
                    )
                    color = lineColor.toArgb()
                }
                val textW = tipValuePaint.measureText(displayText)
                val padX  = 12.dp.toPx(); val padY = 7.dp.toPx()
                val chipW = textW + padX * 2f
                val chipH = tipValuePaint.textSize + padY * 2f
                val chipR = chipH / 2f

                var chipCx = point.x
                chipCx = chipCx.coerceIn(chartLeft + chipW / 2f, chartRight - chipW / 2f)
                var chipY = point.y - chipH / 2f - 14.dp.toPx()
                if (chipY < chartTop) chipY = point.y + chipH + 8.dp.toPx()

                val bgRect = android.graphics.RectF(
                    chipCx - chipW / 2f, chipY - chipH / 2f,
                    chipCx + chipW / 2f, chipY + chipH / 2f
                )
                tipBgPaint.apply {
                    isAntiAlias = true; color = Color(0xFFF3EFF4).toArgb()
                    setShadowLayer(8.dp.toPx(), 0f, 2.dp.toPx(), Color(0x33000000).toArgb())
                }
                canvas.nativeCanvas.drawRoundRect(bgRect, chipR, chipR, tipBgPaint)
                val stripeRect = android.graphics.RectF(bgRect.left, bgRect.top, bgRect.left + 6.dp.toPx(), bgRect.bottom)
                val stripePaint = Paint().apply { isAntiAlias = true; color = lineColor.toArgb(); style = Paint.Style.FILL }
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.clipRect(stripeRect)
                canvas.nativeCanvas.drawRoundRect(bgRect, chipR, chipR, stripePaint)
                canvas.nativeCanvas.restore()
                canvas.nativeCanvas.drawText(displayText, chipCx, chipY + tipValuePaint.textSize * 0.36f, tipValuePaint)
            }
        }

        drawIntoCanvas { canvas ->
            if (yLabels.isNotEmpty()) {
                yLabelPaint.apply {
                    isAntiAlias = true
                    color = labelColor.toArgb()
                    textSize = labelTextSizePx
                }
                yLabels.forEachIndexed { index, label ->
                    val value = yTickValues[index]
                    val y = chartBottom - ((value - axisMin) / range) * chartHeight
                    canvas.nativeCanvas.drawText(label, chartLeft - labelPaddingPx, y + labelTextSizePx * 0.35f, yLabelPaint)
                }
            }

            if (xAxisLabels.isNotEmpty()) {
                xLabelPaint.apply {
                    isAntiAlias = true
                    color = labelColor.toArgb()
                    textSize = labelTextSizePx
                }
                val xStep = if (xAxisLabels.size == dataPoints.size) stepX else chartWidth / (xAxisLabels.size - 1).coerceAtLeast(1)
                // Smart label skipping to avoid crowding
                val estimatedLabelWidth = labelTextSizePx * 4f
                val maxLabels = (chartWidth / estimatedLabelWidth).toInt().coerceAtLeast(2)
                val skipStep = (xAxisLabels.size / maxLabels).coerceAtLeast(1)

                xAxisLabels.forEachIndexed { index, label ->
                    if (index % skipStep != 0 && index != xAxisLabels.lastIndex) return@forEachIndexed
                    val x = if (xAxisLabels.size == dataPoints.size) {
                        chartLeft + index * stepX
                    } else {
                        chartLeft + index * xStep
                    }
                    xLabelPaint.textAlign = when (index) {
                        0 -> Paint.Align.LEFT
                        xAxisLabels.lastIndex -> Paint.Align.RIGHT
                        else -> Paint.Align.CENTER
                    }
                    xLabelPaint.typeface = if (selectedIndex == index)
                        android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    else android.graphics.Typeface.SANS_SERIF
                    xLabelPaint.color = if (selectedIndex == index) lineColor.toArgb() else labelColor.toArgb()
                    canvas.nativeCanvas.drawText(label, x, chartBottom + labelTextSizePx * 1.8f, xLabelPaint)
                }
            }
        }
    }
}

/**
 * Material 3 Expressive bar chart.
 *
 * Design principles:
 * - Pill-shaped (fully-rounded) bars
 * - Spring-physics staggered entry animation per bar
 * - Vibrant selected state with luminous glow halo
 * - Floating M3 chip-style tooltip
 * - Tonal inactive bars fade into background; active bar pops
 */
@Composable
fun LuminousBarChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    barColor: Color = PrimaryContainer,
    glowColor: Color = GlowPrimary,
    barSpacing: Float = 0.28f,
    xAxisLabels: List<String> = emptyList(),
    yAxisTicks: Int = 5,
    yAxisLabelFormatter: (Float) -> String = { value -> formatAxisValue(value) },
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    axisColor: Color = OutlineVariant,
    labelColor: Color = OnSurfaceVariant,
    labelTextSize: TextUnit = 10.sp,
    minY: Float? = null,
    maxY: Float? = null
) {
    // ── Empty state ────────────────────────────────────────────────────────────
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(height),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No data available", color = labelColor, fontSize = 14.sp)
        }
        return
    }

    // ── Spring-entry animation per bar ────────────────────────────────────────
    // Each animatable goes 0 → 1 with a medium-bouncy spring, staggered by 45 ms.
    val animatables = remember(dataPoints.size) {
        List(dataPoints.size) { Animatable(0f) }
    }
    LaunchedEffect(dataPoints) {
        animatables.forEachIndexed { index, anim ->
            launch {
                delay(index * 45L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // ── Paint objects (remembered to avoid GC churn) ──────────────────────────
    val yLabelPaint  = remember { Paint().apply { textAlign = Paint.Align.RIGHT } }
    val xLabelPaint  = remember { Paint() }
    val tipValuePaint  = remember { Paint() }
    val tipLabelPaint  = remember { Paint() }
    val tipBgPaint     = remember { Paint().apply { style = Paint.Style.FILL } }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val labelTextSizePx = with(density) { labelTextSize.toPx() }
    val labelPaddingPx  = with(density) { 6.dp.toPx() }
    val rightPaddingPx  = with(density) { 8.dp.toPx() }
    val topPaddingPx    = with(density) { 16.dp.toPx() }  // extra room for tooltip
    val bottomPaddingPx = if (xAxisLabels.isNotEmpty()) {
        with(density) { maxOf(34.dp.toPx(), labelTextSizePx * 3.5f) }
    } else {
        with(density) { 12.dp.toPx() }
    }
    val dp8Px  = with(density) { 8.dp.toPx() }
    val dp40Px = with(density) { 40.dp.toPx() }

    val maxVal  = maxY ?: (dataPoints.maxOrNull() ?: 0f)
    val axisMax = if (maxY != null) maxY else (if (maxVal <= 0f) 1f else niceAxisMax(maxVal, yAxisTicks))
    val axisMin = minY ?: 0f
    val range   = (axisMax - axisMin).coerceAtLeast(1f)
    val tickCount = yAxisTicks.coerceAtLeast(2)

    val yTickValues = remember(dataPoints, yAxisTicks, minY, maxY) {
        (0 until tickCount).map { i -> axisMin + range * (i / (tickCount - 1f)) }
    }
    val yLabels = remember(yTickValues, yAxisLabelFormatter) {
        yTickValues.map(yAxisLabelFormatter)
    }
    val leftPaddingPx = remember(yLabels, showAxes, labelTextSizePx, labelPaddingPx, dp8Px, dp40Px) {
        val p = Paint().apply { textSize = labelTextSizePx }
        val maxW = if (yLabels.isNotEmpty()) yLabels.maxOf { p.measureText(it) } else 0f
        if (showAxes || yLabels.isNotEmpty()) maxOf(dp40Px, maxW + labelPaddingPx * 1.5f) else dp8Px
    }

    // ── Canvas ────────────────────────────────────────────────────────────────
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    val chartLeft  = leftPaddingPx
                    val chartRight = size.width - rightPaddingPx
                    val cw = (chartRight - chartLeft).coerceAtLeast(1f)
                    val tbw = cw / dataPoints.size
                    if (offset.x in chartLeft..chartRight) {
                        val idx = ((offset.x - chartLeft) / tbw).toInt().coerceIn(0, dataPoints.lastIndex)
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                }
            }
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd   = {},
                    onDragCancel = {},
                    onDrag = { change, _ ->
                        val chartLeft  = leftPaddingPx
                        val chartRight = size.width - rightPaddingPx
                        val cw = (chartRight - chartLeft).coerceAtLeast(1f)
                        val tbw = cw / dataPoints.size
                        if (change.position.x in chartLeft..chartRight) {
                            selectedIndex = ((change.position.x - chartLeft) / tbw).toInt().coerceIn(0, dataPoints.lastIndex)
                        }
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height

        val chartLeft   = leftPaddingPx
        val chartTop    = topPaddingPx
        val chartRight  = w - rightPaddingPx
        val chartBottom = h - bottomPaddingPx
        val chartWidth  = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

        val totalBarWidth = chartWidth / dataPoints.size
        val gap      = totalBarWidth * barSpacing
        val barWidth = totalBarWidth - gap
        // Pill-shaped: corner radius = half the bar width
        val pillRadius = barWidth / 2f

        // ── Grid ─────────────────────────────────────────────────────────────
        if (showGrid) {
            yTickValues.forEachIndexed { idx, value ->
                val y = chartBottom - ((value - axisMin) / range) * chartHeight
                drawLine(
                    color = axisColor.copy(alpha = if (idx % 2 == 0) 0.09f else 0.04f),
                    start = Offset(chartLeft, y),
                    end   = Offset(chartRight, y),
                    strokeWidth = if (idx % 2 == 0) 1.2f else 0.7f,
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                )
            }
        }

        // ── Baseline ─────────────────────────────────────────────────────────
        if (showAxes) {
            drawLine(
                color = axisColor.copy(alpha = 0.25f),
                start = Offset(chartLeft, chartBottom),
                end   = Offset(chartRight, chartBottom),
                strokeWidth = 1.5f
            )
        }

        // ── Bars: rounded top, flat bottom (M3 Expressive) ──────────────────
        dataPoints.forEachIndexed { i, value ->
            val progress   = animatables[i].value
            val fullHeight = (value / axisMax) * chartHeight
            val barHeight  = (fullHeight * progress).coerceAtLeast(2f)
            val barLeft    = chartLeft + i * totalBarWidth + gap / 2f
            val barTop     = chartBottom - barHeight
            val isSelected = selectedIndex == i

            val fillTop = if (isSelected) glowColor else barColor.copy(alpha = 0.75f)
            val fillBot = if (isSelected) glowColor.copy(alpha = 0.45f) else barColor.copy(alpha = 0.30f)

            // Glow halo
            if (isSelected) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor.copy(alpha = 0.22f), Color.Transparent),
                        startY = barTop - 8f, endY = chartBottom
                    ),
                    topLeft = Offset(barLeft - 4f, barTop - 8f),
                    size    = Size(barWidth + 8f, barHeight + 8f),
                    cornerRadius = CornerRadius(pillRadius + 4f, pillRadius + 4f)
                )
            }

            // Main bar – rounded top, square bottom
            drawPath(
                path = Path().apply {
                    addRoundRect(RoundRect(
                        left = barLeft, top = barTop,
                        right = barLeft + barWidth, bottom = chartBottom,
                        topLeftCornerRadius     = CornerRadius(pillRadius),
                        topRightCornerRadius    = CornerRadius(pillRadius),
                        bottomRightCornerRadius = CornerRadius(0f),
                        bottomLeftCornerRadius  = CornerRadius(0f)
                    ))
                },
                brush = Brush.verticalGradient(
                    colors = listOf(fillTop, fillBot),
                    startY = barTop, endY = chartBottom
                )
            )

            // Top shimmer cap
            val capHeight = minOf(pillRadius * 2f, barHeight)
            drawPath(
                path = Path().apply {
                    addRoundRect(RoundRect(
                        left = barLeft, top = barTop,
                        right = barLeft + barWidth, bottom = barTop + capHeight,
                        topLeftCornerRadius     = CornerRadius(pillRadius),
                        topRightCornerRadius    = CornerRadius(pillRadius),
                        bottomRightCornerRadius = CornerRadius(0f),
                        bottomLeftCornerRadius  = CornerRadius(0f)
                    ))
                },
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isSelected) 0.30f else 0.12f),
                        Color.Transparent
                    ),
                    startY = barTop, endY = barTop + capHeight
                )
            )
        }

        // ── Floating chip tooltip for selected bar ────────────────────────────
        selectedIndex?.let { idx ->
            val value      = dataPoints[idx]
            val barHeight  = (value / axisMax) * chartHeight
            val barLeft    = chartLeft + idx * totalBarWidth + gap / 2f
            val barCenterX = barLeft + barWidth / 2f
            val barTop     = chartBottom - barHeight

            val valueStr = yAxisLabelFormatter(value)
            val labelStr = if (idx < xAxisLabels.size) xAxisLabels[idx] else ""
            val displayText = if (labelStr.isNotEmpty()) "$labelStr  ·  $valueStr" else valueStr

            drawIntoCanvas { canvas ->
                // Value text – bold
                tipValuePaint.apply {
                    isAntiAlias = true
                    textSize    = labelTextSizePx * 1.15f
                    textAlign   = Paint.Align.CENTER
                    typeface    = android.graphics.Typeface.create(
                        android.graphics.Typeface.SANS_SERIF,
                        android.graphics.Typeface.BOLD
                    )
                    color = glowColor.copy(alpha = 1f).toArgb()
                }
                // Label text – regular
                tipLabelPaint.apply {
                    isAntiAlias = true
                    textSize    = labelTextSizePx * 1.05f
                    textAlign   = Paint.Align.CENTER
                    typeface    = android.graphics.Typeface.SANS_SERIF
                    color       = Color(0xFF1C1B1F).toArgb()   // M3 on-surface
                }

                val textW   = tipValuePaint.measureText(displayText)
                val padX    = 12.dp.toPx()
                val padY    = 7.dp.toPx()
                val chipW   = textW + padX * 2f
                val chipH   = tipValuePaint.textSize + padY * 2f
                val chipR   = chipH / 2f          // pill-shaped chip

                // Constrain chip inside chart bounds
                var chipCx = barCenterX
                chipCx = chipCx.coerceIn(chartLeft + chipW / 2f, chartRight - chipW / 2f)
                var chipY = barTop - chipH / 2f - 10.dp.toPx()
                if (chipY < chartTop) chipY = barTop + chipH + 6.dp.toPx()

                val bgRect = android.graphics.RectF(
                    chipCx - chipW / 2f,
                    chipY - chipH / 2f,
                    chipCx + chipW / 2f,
                    chipY + chipH / 2f
                )

                // Chip background – M3 surface tint
                tipBgPaint.apply {
                    isAntiAlias = true
                    color       = Color(0xFFF3EFF4).toArgb()   // M3 surface variant
                    setShadowLayer(8.dp.toPx(), 0f, 2.dp.toPx(), Color(0x33000000).toArgb())
                }
                canvas.nativeCanvas.drawRoundRect(bgRect, chipR, chipR, tipBgPaint)

                // Coloured left stripe (M3 Expressive accent)
                val stripeW = 3.dp.toPx()
                val stripePaint = Paint().apply {
                    isAntiAlias = true
                    color       = glowColor.toArgb()
                    style       = Paint.Style.FILL
                }
                val stripeRect = android.graphics.RectF(
                    bgRect.left,
                    bgRect.top,
                    bgRect.left + stripeW * 2f,
                    bgRect.bottom
                )
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.clipRect(stripeRect)
                canvas.nativeCanvas.drawRoundRect(bgRect, chipR, chipR, stripePaint)
                canvas.nativeCanvas.restore()

                // Text
                val textBaseline = chipY + tipValuePaint.textSize * 0.36f
                canvas.nativeCanvas.drawText(displayText, chipCx, textBaseline, tipValuePaint)

                // Connector dot on top of bar
                val dotPaint = Paint().apply {
                    isAntiAlias = true
                    color = glowColor.toArgb()
                    style = Paint.Style.FILL
                }
                canvas.nativeCanvas.drawCircle(barCenterX, barTop, 4.5.dp.toPx(), dotPaint)
                val dotRingPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.White.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 2.dp.toPx()
                }
                canvas.nativeCanvas.drawCircle(barCenterX, barTop, 4.5.dp.toPx(), dotRingPaint)
            }
        }

        // ── Axis labels ───────────────────────────────────────────────────────
        drawIntoCanvas { canvas ->
            if (yLabels.isNotEmpty()) {
                yLabelPaint.apply {
                    isAntiAlias = true
                    color       = labelColor.toArgb()
                    textSize    = labelTextSizePx
                }
                yLabels.forEachIndexed { index, label ->
                    val tickValue = yTickValues[index]
                    val y = chartBottom - ((tickValue - axisMin) / range) * chartHeight
                    canvas.nativeCanvas.drawText(
                        label,
                        chartLeft - labelPaddingPx,
                        y + labelTextSizePx * 0.35f,
                        yLabelPaint
                    )
                }
            }

            if (xAxisLabels.isNotEmpty()) {
                xLabelPaint.apply {
                    isAntiAlias = true
                    color       = labelColor.toArgb()
                    textSize    = labelTextSizePx
                }
                val xStep = if (xAxisLabels.size == dataPoints.size)
                    totalBarWidth
                else
                    chartWidth / (xAxisLabels.size - 1).coerceAtLeast(1)
                val estimatedLabelW = labelTextSizePx * 4f
                val maxLabels = (chartWidth / estimatedLabelW).toInt().coerceAtLeast(2)
                val skipStep  = (xAxisLabels.size / maxLabels).coerceAtLeast(1)

                xAxisLabels.forEachIndexed { index, label ->
                    if (index % skipStep != 0 && index != xAxisLabels.lastIndex) return@forEachIndexed
                    val x = if (xAxisLabels.size == dataPoints.size) {
                        chartLeft + index * totalBarWidth + gap / 2f + barWidth / 2f
                    } else {
                        chartLeft + index * xStep
                    }
                    xLabelPaint.textAlign = when (index) {
                        0                      -> Paint.Align.LEFT
                        xAxisLabels.lastIndex  -> Paint.Align.RIGHT
                        else                   -> Paint.Align.CENTER
                    }
                    // Highlight x-label of selected bar
                    xLabelPaint.typeface = if (selectedIndex == index)
                        android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    else
                        android.graphics.Typeface.SANS_SERIF
                    xLabelPaint.color = if (selectedIndex == index)
                        glowColor.toArgb()
                    else
                        labelColor.toArgb()
                    canvas.nativeCanvas.drawText(label, x, chartBottom + labelTextSizePx * 1.8f, xLabelPaint)
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

/**
 * Material 3 Expressive stacked bar chart.
 *
 * Design principles:
 * - Spring-physics staggered entry per bar (both segments)
 * - Rounded top, flat bottom segments
 * - Luminous glow halo on selected bar
 * - Floating M3 chip tooltip with 2-line breakdown
 */
@Composable
fun LuminousStackedBarChart(
    dataPoints: List<Pair<Float, Float>>, // Pair(BottomValue, TopValue)
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
    bottomColor: Color = Color(0xFF37474F),
    topColor: Color = Color(0xFFFF3D00),
    barSpacing: Float = 0.28f,
    xAxisLabels: List<String> = emptyList(),
    yAxisTicks: Int = 5,
    yAxisLabelFormatter: (Float) -> String = { value -> formatAxisValue(value) },
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    axisColor: Color = OutlineVariant,
    labelColor: Color = OnSurfaceVariant,
    labelTextSize: TextUnit = 10.sp,
    minY: Float? = null,
    maxY: Float? = null
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(height),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No data available", color = labelColor, fontSize = 14.sp)
        }
        return
    }

    // ── Spring-entry animations ───────────────────────────────────────
    val animatables = remember(dataPoints.size) { List(dataPoints.size) { Animatable(0f) } }
    LaunchedEffect(dataPoints) {
        animatables.forEachIndexed { index, anim ->
            launch {
                delay(index * 45L)
                anim.animateTo(1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ))
            }
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val yLabelPaint    = remember { Paint().apply { textAlign = Paint.Align.RIGHT } }
    val xLabelPaint    = remember { Paint() }
    val tipValuePaint  = remember { Paint() }
    val tipDetailPaint = remember { Paint() }
    val tipBgPaint     = remember { Paint().apply { style = Paint.Style.FILL } }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val labelTextSizePx = with(density) { labelTextSize.toPx() }
    val labelPaddingPx  = with(density) { 6.dp.toPx() }
    val rightPaddingPx  = with(density) { 8.dp.toPx() }
    val topPaddingPx    = with(density) { 16.dp.toPx() }
    val bottomPaddingPx = if (xAxisLabels.isNotEmpty()) {
        with(density) { maxOf(34.dp.toPx(), labelTextSizePx * 3.5f) }
    } else {
        with(density) { 12.dp.toPx() }
    }
    val dp8Px  = with(density) { 8.dp.toPx() }
    val dp40Px = with(density) { 40.dp.toPx() }

    val rawMax  = dataPoints.maxOfOrNull { it.first + it.second } ?: 0f
    val axisMax = if (maxY != null) maxY else niceAxisMax(rawMax, yAxisTicks)
    val axisMin = minY ?: 0f
    val range   = (axisMax - axisMin).coerceAtLeast(1f)
    val tickCount = yAxisTicks.coerceAtLeast(2)

    val yTickValues = remember(dataPoints, yAxisTicks, minY, maxY) {
        (0 until tickCount).map { i -> axisMin + range * (i / (tickCount - 1f)) }
    }
    val yLabels = remember(yTickValues, yAxisLabelFormatter) {
        yTickValues.map(yAxisLabelFormatter)
    }
    val leftPaddingPx = remember(yLabels, showAxes, labelTextSizePx, labelPaddingPx, dp8Px, dp40Px) {
        val p = Paint().apply { textSize = labelTextSizePx }
        val maxW = if (yLabels.isNotEmpty()) yLabels.maxOf { p.measureText(it) } else 0f
        if (showAxes || yLabels.isNotEmpty()) maxOf(dp40Px, maxW + labelPaddingPx * 1.5f) else dp8Px
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    val chartLeft  = leftPaddingPx
                    val chartRight = size.width - rightPaddingPx
                    val cw  = (chartRight - chartLeft).coerceAtLeast(1f)
                    val tbw = cw / dataPoints.size
                    if (offset.x in chartLeft..chartRight) {
                        val idx = ((offset.x - chartLeft) / tbw).toInt().coerceIn(0, dataPoints.lastIndex)
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                }
            }
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = {}, onDragEnd = {}, onDragCancel = {},
                    onDrag = { change, _ ->
                        val chartLeft  = leftPaddingPx
                        val chartRight = size.width - rightPaddingPx
                        val cw  = (chartRight - chartLeft).coerceAtLeast(1f)
                        val tbw = cw / dataPoints.size
                        if (change.position.x in chartLeft..chartRight) {
                            selectedIndex = ((change.position.x - chartLeft) / tbw).toInt().coerceIn(0, dataPoints.lastIndex)
                        }
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height

        val chartLeft   = leftPaddingPx
        val chartTop    = topPaddingPx
        val chartRight  = w - rightPaddingPx
        val chartBottom = h - bottomPaddingPx
        val chartWidth  = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

        val totalBarWidth = chartWidth / dataPoints.size
        val gap       = totalBarWidth * barSpacing
        val barWidth  = totalBarWidth - gap
        val pillRadius = barWidth / 2f

        // ── Grid ────────────────────────────────────────────────────────
        if (showGrid) {
            yTickValues.forEachIndexed { idx, value ->
                val y = chartBottom - ((value - axisMin) / range) * chartHeight
                drawLine(
                    color = axisColor.copy(alpha = if (idx % 2 == 0) 0.09f else 0.04f),
                    start = Offset(chartLeft, y), end = Offset(chartRight, y),
                    strokeWidth = if (idx % 2 == 0) 1.2f else 0.7f,
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                )
            }
        }

        // ── Baseline ───────────────────────────────────────────────
        if (showAxes) {
            drawLine(
                color = axisColor.copy(alpha = 0.25f),
                start = Offset(chartLeft, chartBottom), end = Offset(chartRight, chartBottom),
                strokeWidth = 1.5f
            )
        }

        // ── Stacked bars ─────────────────────────────────────────────
        dataPoints.forEachIndexed { i, valuePair ->
            val progress   = animatables[i].value
            val bottomVal  = valuePair.first
            val topVal     = valuePair.second
            val isSelected = selectedIndex == i

            val animBotH = ((bottomVal / axisMax) * chartHeight * progress).coerceAtLeast(0f)
            val animTopH = ((topVal   / axisMax) * chartHeight * progress).coerceAtLeast(0f)

            val barLeft    = chartLeft + i * totalBarWidth + gap / 2f
            val barCenterX = barLeft + barWidth / 2f

            // Glow halo on selected
            if (isSelected) {
                val totalAnimH = animBotH + animTopH
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(topColor.copy(alpha = 0.20f), Color.Transparent),
                        startY = chartBottom - totalAnimH - 8f, endY = chartBottom
                    ),
                    topLeft      = Offset(barLeft - 4f, chartBottom - totalAnimH - 8f),
                    size         = Size(barWidth + 8f, totalAnimH + 8f),
                    cornerRadius = CornerRadius(pillRadius + 4f, pillRadius + 4f)
                )
            }

            // Bottom segment – flat top when topVal > 0, rounded top if it’s the only segment
            if (animBotH > 0f) {
                val botTop    = chartBottom - animBotH
                val botCorner = if (animTopH > 0f) 0f else pillRadius
                drawPath(
                    path = Path().apply {
                        addRoundRect(RoundRect(
                            left = barLeft, top = botTop,
                            right = barLeft + barWidth, bottom = chartBottom,
                            topLeftCornerRadius     = CornerRadius(botCorner),
                            topRightCornerRadius    = CornerRadius(botCorner),
                            bottomRightCornerRadius = CornerRadius(0f),
                            bottomLeftCornerRadius  = CornerRadius(0f)
                        ))
                    },
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            bottomColor.copy(alpha = if (isSelected) 0.95f else 0.70f),
                            bottomColor.copy(alpha = if (isSelected) 0.55f else 0.35f)
                        ),
                        startY = botTop, endY = chartBottom
                    )
                )
            }

            // Top segment – always rounded top, flat bottom (sits on bottom segment)
            if (animTopH > 0f) {
                val topTop    = chartBottom - animBotH - animTopH
                val topBottom = chartBottom - animBotH
                drawPath(
                    path = Path().apply {
                        addRoundRect(RoundRect(
                            left = barLeft, top = topTop,
                            right = barLeft + barWidth, bottom = topBottom,
                            topLeftCornerRadius     = CornerRadius(pillRadius),
                            topRightCornerRadius    = CornerRadius(pillRadius),
                            bottomRightCornerRadius = CornerRadius(0f),
                            bottomLeftCornerRadius  = CornerRadius(0f)
                        ))
                    },
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            topColor.copy(alpha = if (isSelected) 1.0f else 0.85f),
                            topColor.copy(alpha = if (isSelected) 0.60f else 0.45f)
                        ),
                        startY = topTop, endY = topBottom
                    )
                )
                // Shimmer cap on top segment
                val capH = minOf(pillRadius * 2f, animTopH)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = if (isSelected) 0.28f else 0.10f), Color.Transparent),
                        startY = topTop, endY = topTop + capH
                    ),
                    topLeft      = Offset(barLeft, topTop),
                    size         = Size(barWidth, capH),
                    cornerRadius = CornerRadius(pillRadius, pillRadius)
                )
            }
        }

        // ── M3 chip tooltip (2-line: total + breakdown) ──────────────────
        selectedIndex?.let { idx ->
            val vp         = dataPoints[idx]
            val totalVal   = vp.first + vp.second
            val totalH     = (totalVal / axisMax) * chartHeight
            val barLeft    = chartLeft + idx * totalBarWidth + gap / 2f
            val barCenterX = barLeft + barWidth / 2f
            val barTop     = chartBottom - totalH

            val labelStr = if (idx < xAxisLabels.size) xAxisLabels[idx] else ""
            val line1 = if (labelStr.isNotEmpty()) "$labelStr  ·  ${yAxisLabelFormatter(totalVal)}" else yAxisLabelFormatter(totalVal)
            val line2 = "Idle ${yAxisLabelFormatter(vp.first)}  |  Active ${yAxisLabelFormatter(vp.second)}"

            drawIntoCanvas { canvas ->
                tipValuePaint.apply {
                    isAntiAlias = true
                    textSize    = labelTextSizePx * 1.1f
                    textAlign   = Paint.Align.CENTER
                    typeface    = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    color       = topColor.toArgb()
                }
                tipDetailPaint.apply {
                    isAntiAlias = true
                    textSize    = labelTextSizePx * 0.95f
                    textAlign   = Paint.Align.CENTER
                    typeface    = android.graphics.Typeface.SANS_SERIF
                    color       = Color(0xFF49454F).toArgb()
                }

                val textW1 = tipValuePaint.measureText(line1)
                val textW2 = tipDetailPaint.measureText(line2)
                val padX   = 12.dp.toPx(); val padY = 6.dp.toPx(); val lineGap = 3.dp.toPx()
                val chipW  = maxOf(textW1, textW2) + padX * 2f
                val chipH  = tipValuePaint.textSize + lineGap + tipDetailPaint.textSize + padY * 2f
                val chipR  = 10.dp.toPx()

                var chipCx = barCenterX
                chipCx = chipCx.coerceIn(chartLeft + chipW / 2f, chartRight - chipW / 2f)
                var chipTop = barTop - chipH - 10.dp.toPx()
                if (chipTop < chartTop) chipTop = barTop + 6.dp.toPx()

                val bgRect = android.graphics.RectF(
                    chipCx - chipW / 2f, chipTop,
                    chipCx + chipW / 2f, chipTop + chipH
                )
                tipBgPaint.apply {
                    isAntiAlias = true; color = Color(0xFFF3EFF4).toArgb()
                    setShadowLayer(8.dp.toPx(), 0f, 2.dp.toPx(), Color(0x33000000).toArgb())
                }
                canvas.nativeCanvas.drawRoundRect(bgRect, chipR, chipR, tipBgPaint)

                // Left accent stripe
                val stripeRect = android.graphics.RectF(bgRect.left, bgRect.top, bgRect.left + 6.dp.toPx(), bgRect.bottom)
                val stripePaint = Paint().apply { isAntiAlias = true; color = topColor.toArgb(); style = Paint.Style.FILL }
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.clipRect(stripeRect)
                canvas.nativeCanvas.drawRoundRect(bgRect, chipR, chipR, stripePaint)
                canvas.nativeCanvas.restore()

                val line1Y = chipTop + padY + tipValuePaint.textSize * 0.85f
                val line2Y = line1Y + lineGap + tipDetailPaint.textSize
                canvas.nativeCanvas.drawText(line1, chipCx, line1Y, tipValuePaint)
                canvas.nativeCanvas.drawText(line2, chipCx, line2Y, tipDetailPaint)

                // Connector dot
                val dotPaint = Paint().apply { isAntiAlias = true; color = topColor.toArgb(); style = Paint.Style.FILL }
                canvas.nativeCanvas.drawCircle(barCenterX, barTop, 4.5.dp.toPx(), dotPaint)
                val dotRingPaint = Paint().apply { isAntiAlias = true; color = Color.White.toArgb(); style = Paint.Style.STROKE; strokeWidth = 2.dp.toPx() }
                canvas.nativeCanvas.drawCircle(barCenterX, barTop, 4.5.dp.toPx(), dotRingPaint)
            }
        }

        // ── Axis labels ──────────────────────────────────────────────
        drawIntoCanvas { canvas ->
            if (yLabels.isNotEmpty()) {
                yLabelPaint.apply { isAntiAlias = true; color = labelColor.toArgb(); textSize = labelTextSizePx }
                yLabels.forEachIndexed { index, label ->
                    val tickVal = yTickValues[index]
                    val y = chartBottom - ((tickVal - axisMin) / range) * chartHeight
                    canvas.nativeCanvas.drawText(label, chartLeft - labelPaddingPx, y + labelTextSizePx * 0.35f, yLabelPaint)
                }
            }
            if (xAxisLabels.isNotEmpty()) {
                xLabelPaint.apply { isAntiAlias = true; color = labelColor.toArgb(); textSize = labelTextSizePx }
                val xStep = if (xAxisLabels.size == dataPoints.size) totalBarWidth
                            else chartWidth / (xAxisLabels.size - 1).coerceAtLeast(1)
                val estimatedLabelW = labelTextSizePx * 4f
                val maxLabels = (chartWidth / estimatedLabelW).toInt().coerceAtLeast(2)
                val skipStep  = (xAxisLabels.size / maxLabels).coerceAtLeast(1)
                xAxisLabels.forEachIndexed { index, label ->
                    if (index % skipStep != 0 && index != xAxisLabels.lastIndex) return@forEachIndexed
                    val x = if (xAxisLabels.size == dataPoints.size)
                        chartLeft + index * totalBarWidth + gap / 2f + barWidth / 2f
                    else chartLeft + index * xStep
                    xLabelPaint.textAlign = when (index) {
                        0 -> Paint.Align.LEFT; xAxisLabels.lastIndex -> Paint.Align.RIGHT; else -> Paint.Align.CENTER
                    }
                    xLabelPaint.typeface = if (selectedIndex == index)
                        android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    else android.graphics.Typeface.SANS_SERIF
                    xLabelPaint.color = if (selectedIndex == index) topColor.toArgb() else labelColor.toArgb()
                    canvas.nativeCanvas.drawText(label, x, chartBottom + labelTextSizePx * 1.8f, xLabelPaint)
                }
            }
        }
    }
}

/**
 * Rounds [maxVal] up to the nearest "nice" human-readable ceiling for axis ticks.
 * E.g. 187 → 200, 4300 → 5000, 0.73 → 0.8
 */
private fun niceAxisMax(maxVal: Float, ticks: Int): Float {
    if (maxVal <= 0f) return 1f
    val rawStep   = maxVal / (ticks - 1).coerceAtLeast(1)
    val magnitude = Math.pow(10.0, Math.floor(Math.log10(rawStep.toDouble()))).toFloat()
    val niceStep  = when {
        rawStep / magnitude <= 1f -> magnitude
        rawStep / magnitude <= 2f -> 2f * magnitude
        rawStep / magnitude <= 5f -> 5f * magnitude
        else                      -> 10f * magnitude
    }
    return (niceStep * (ticks - 1)).coerceAtLeast(maxVal)
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
