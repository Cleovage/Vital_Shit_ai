package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.vitaai.ui.theme.*

/**
 * Atmospheric Ink Background. Deep dark theme with subtle mesh glows.
 * No Haze/blur overhead for optimal performance.
 */
@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    showGrid: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InkBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Glow 1: Deep Purple
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlebPurple.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(width * (0.2f + 0.1f * phase), height * 0.2f),
                    radius = width * 0.8f
                ),
                center = Offset(width * (0.2f + 0.1f * phase), height * 0.2f),
                radius = width * 0.8f
            )

            // Glow 2: Emerald
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlebEmerald.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(width * (0.8f - 0.1f * phase), height * 0.8f),
                    radius = width * 0.9f
                ),
                center = Offset(width * (0.8f - 0.1f * phase), height * 0.8f),
                radius = width * 0.9f
            )
        }
        content()
    }
}
