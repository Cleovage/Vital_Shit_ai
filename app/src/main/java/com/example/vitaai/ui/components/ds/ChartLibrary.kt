package com.example.vitaai.ui.components.ds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/* --------------------------- Color presets --------------------------- */

data class ChartColors(
    val primary: Color = Color(0xFF06B6D4),
    val secondary: Color = Color(0xFF3B82F6),
    val tertiary: Color = Color(0xFFF43F5E),
    val axis: Color = Color(0xFF475569),
    val grid: Color = Color(0xFFCBD5E1).copy(alpha = 0.5f)
)

/* ---------------------------- Line chart ----------------------------- */

@Composable
fun LineChart(
    data: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier,
    colors: ChartColors = ChartColors(),
    showDots: Boolean = true,
    smooth: Boolean = true
) {
    if (data.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                "Not enough data",
                color = colors.axis,
                fontSize = 12.sp
            )
        }
        return
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padLeft = 8f
        val padRight = 8f
        val padTop = 12f
        val padBottom = 24f

        val xs = data.map { it.first }
        val ys = data.map { it.second }
        val xMin = xs.min()
        val xMax = xs.max()
        val yMin = 0f
        val yMax = max(ys.max(), 1f)

        fun toScreenX(x: Float): Float {
            val t = if (xMax == xMin) 0f else (x - xMin) / (xMax - xMin)
            return padLeft + t * (w - padLeft - padRight)
        }

        fun toScreenY(y: Float): Float {
            val t = (y - yMin) / (yMax - yMin)
            return h - padBottom - t * (h - padTop - padBottom)
        }

        // baseline grid
        for (i in 0..3) {
            val y = padTop + (h - padTop - padBottom) * i / 3f
            drawLine(
                color = colors.grid,
                start = Offset(padLeft, y),
                end = Offset(w - padRight, y),
                strokeWidth = 1f
            )
        }

        // area fill
        val areaPath = Path().apply {
            val first = data.first()
            moveTo(toScreenX(first.first), h - padBottom)
            lineTo(toScreenX(first.first), toScreenY(first.second))
            data.forEach { p ->
                lineTo(toScreenX(p.first), toScreenY(p.second))
            }
            lineTo(toScreenX(data.last().first), h - padBottom)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.primary.copy(alpha = 0.3f),
                    colors.primary.copy(alpha = 0.0f)
                ),
                startY = padTop,
                endY = h - padBottom
            )
        )

        // line stroke
        val linePath = Path()
        if (smooth && data.size >= 3) {
            val pts = data.map { Offset(toScreenX(it.first), toScreenY(it.second)) }
            linePath.moveTo(pts[0].x, pts[0].y)
            for (i in 0 until pts.size - 1) {
                val p0 = if (i == 0) pts[0] else pts[i - 1]
                val p1 = pts[i]
                val p2 = pts[i + 1]
                val p3 = if (i + 2 < pts.size) pts[i + 2] else p2
                val cp1x = p1.x + (p2.x - p0.x) / 6f
                val cp1y = p1.y + (p2.y - p0.y) / 6f
                val cp2x = p2.x - (p3.x - p1.x) / 6f
                val cp2y = p2.y - (p3.y - p1.y) / 6f
                linePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
            }
        } else {
            data.forEachIndexed { i, p ->
                val sx = toScreenX(p.first)
                val sy = toScreenY(p.second)
                if (i == 0) linePath.moveTo(sx, sy) else linePath.lineTo(sx, sy)
            }
        }
        drawPath(
            path = linePath,
            color = colors.primary,
            style = Stroke(width = 3f)
        )

        if (showDots) {
            data.forEach { p ->
                val cx = toScreenX(p.first)
                val cy = toScreenY(p.second)
                drawCircle(color = Color.White, radius = 5f, center = Offset(cx, cy))
                drawCircle(color = colors.primary, radius = 3.5f, center = Offset(cx, cy))
            }
        }
    }
}

/* ---------------------------- Bar chart ------------------------------ */

@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    colors: ChartColors = ChartColors(),
    barColor: Color = colors.primary
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", color = colors.axis, fontSize = 12.sp)
        }
        return
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
        ) {
            val w = size.width
            val h = size.height
            val padBottom = 24f
            val yMax = max(data.maxOf { it.second }, 1f)
            val barCount = data.size
            val gap = w / (barCount * 4f)
            val barWidth = w / barCount - gap

            for (i in 0..3) {
                val y = (h - padBottom) * i / 3f
                drawLine(
                    color = colors.grid,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            data.forEachIndexed { i, (_, v) ->
                val barHeight = (v / yMax) * (h - padBottom - 8f)
                val x = i * (barWidth + gap) + gap / 2f
                val y = h - padBottom - barHeight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColor.copy(alpha = 0.6f)),
                        startY = y,
                        endY = h - padBottom
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (label, _) ->
                Text(
                    text = label.take(3),
                    color = colors.axis,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/* ---------------------------- Radial gauge --------------------------- */

@Composable
fun RadialGauge(
    value: Float,
    label: String,
    modifier: Modifier = Modifier,
    colors: ChartColors = ChartColors(),
    size: Dp = 180.dp,
    trackColor: Color = Color(0xFFE2E8F0)
) {
    val clamped = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14f
            val r = (this.size.minDimension - stroke) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val topLeft = Offset(center.x - r, center.y - r)
            val arcSize = Size(r * 2f, r * 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(colors.primary, colors.secondary, colors.primary),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(clamped * 100).toInt()}%",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = label,
                color = Color(0xFF475569),
                fontSize = 12.sp
            )
        }
    }
}

/* ----------------------------- Sparkline ----------------------------- */

@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    colors: ChartColors = ChartColors()
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (values.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val yMin = values.min()
        val yMax = max(values.max(), yMin + 1f)

        val path = Path()
        values.forEachIndexed { i, v ->
            val x = w * i / (values.size - 1).toFloat()
            val y = h - ((v - yMin) / (yMax - yMin)) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = colors.primary,
            style = Stroke(width = 2.5f)
        )
    }
}

/* -------------------------- Heatmap calendar ------------------------- */

@Composable
fun HeatmapCalendar(
    intensities: List<Float>,
    modifier: Modifier = Modifier,
    cellSize: Dp = 12.dp,
    cellGap: Dp = 3.dp,
    columns: Int = 7
) {
    val clamped = intensities.map { it.coerceIn(0f, 1f) }
    val palette = listOf(
        Color(0xFFE2E8F0), // empty
        Color(0xFF67E8F9), // light cyan
        Color(0xFF06B6D4), // cyan
        Color(0xFF3B82F6), // blue
        Color(0xFF8B5CF6), // purple
        Color(0xFFF43F5E)  // rose
    )

    fun colorFor(v: Float): Color {
        if (v <= 0f) return palette[0]
        val idx = ((v * (palette.size - 1)).toInt()).coerceIn(1, palette.size - 1)
        return palette[idx]
    }

    val rows = (clamped.size + columns - 1) / columns
    Column(modifier = modifier) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(cellGap)) {
                for (c in 0 until columns) {
                    val idx = r * columns + c
                    if (idx < clamped.size) {
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colorFor(clamped[idx]))
                        )
                    } else {
                        Spacer(modifier = Modifier.size(cellSize))
                    }
                }
            }
            if (r < rows - 1) Spacer(modifier = Modifier.height(cellGap))
        }
    }
}
