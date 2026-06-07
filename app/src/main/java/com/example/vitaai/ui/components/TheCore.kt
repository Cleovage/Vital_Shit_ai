package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.*

/**
 * The Living Core: A central high-fidelity visual entity (AI Orb).
 */
@Composable
fun LivingCore(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val baseRadius = this.size.width * 0.35f * pulseScale

            // External Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlebPurple.copy(alpha = auraAlpha), Color.Transparent),
                    center = center,
                    radius = this.size.width * 0.5f
                ),
                center = center,
                radius = this.size.width * 0.5f
            )

            // Inner Plasma Nucleus
            rotate(rotation, center) {
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(GlebPurple, GlebCyan, GlebEmerald),
                        start = Offset(0f, 0f),
                        end = Offset(this.size.width, this.size.height)
                    ),
                    center = center,
                    radius = baseRadius
                )
            }

            // High-Fidelity Lens Flare / Specular
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                    center = center - Offset(baseRadius * 0.3f, baseRadius * 0.3f),
                    radius = baseRadius * 0.6f
                ),
                center = center - Offset(baseRadius * 0.3f, baseRadius * 0.3f),
                radius = baseRadius * 0.6f
            )
        }
    }
}
