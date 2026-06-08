package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.*

/**
 * Wellbeing progress ring with neon glow effect.
 *
 * - Thick stroke with rounded caps
 * - Background track at low opacity
 * - Neon glow: a wider, blurred arc drawn behind the main arc
 * - Slow pulse animation when progress >= 1.0 (goal reached)
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 16.dp,
    glowWidth: Dp = 28.dp,
    trackColor: Color = OnSurfaceVariant.copy(alpha = 0.12f),
    colors: List<Color> = listOf(PrimaryContainer, Primary),
    glowColor: Color = GlowPrimary,
    startAngle: Float = -225f,
    sweepAngle: Float = 270f
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    // Pulse animation when goal reached
    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (clampedProgress >= 1f) 0.3f else 0f,
        targetValue = if (clampedProgress >= 1f) 0.7f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animated progress fill
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 800, easing = EaseInOut),
        label = "progressAnim"
    )

    val sweepBrush = remember(colors) {
        Brush.sweepGradient(colors)
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size
            val stroke = strokeWidth.toPx()
            val glow = glowWidth.toPx()
            val padding = glow / 2
            val arcSize = Size(canvasSize.width - glow, canvasSize.height - glow)
            val arcOffset = Offset(padding, padding)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Glow arc (wider, semi-transparent behind main arc)
            drawArc(
                color = glowColor.copy(alpha = glowColor.alpha + pulseAlpha),
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedProgress,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = glow, cap = StrokeCap.Round)
            )

            // Main progress arc
            drawArc(
                brush = sweepBrush,
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedProgress,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
