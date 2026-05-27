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
import com.example.vitaai.ui.theme.AuraGradientPrimary
import com.example.vitaai.ui.theme.AuraGradientSecondary
import com.example.vitaai.ui.theme.Background

/**
 * Full-screen background with the "Atmospheric Clarity" aesthetic.
 *
 * - Deep space base color (#0b1326)
 * - Two radial gradient "aura" blobs (teal + violet) at 20% opacity
 * - Slow animated drift for organic feel
 */
@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")

    // Slow horizontal drift for primary blob
    val primaryOffsetX by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "primaryX"
    )
    val primaryOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "primaryY"
    )

    // Slow drift for secondary blob
    val secondaryOffsetX by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondaryX"
    )
    val secondaryOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondaryY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Aura gradient blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Primary teal blob — top-left area
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuraGradientPrimary, Color.Transparent),
                    center = Offset(w * primaryOffsetX, h * primaryOffsetY),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w * primaryOffsetX, h * primaryOffsetY)
            )

            // Secondary violet blob — bottom-right area
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuraGradientSecondary, Color.Transparent),
                    center = Offset(w * secondaryOffsetX, h * secondaryOffsetY),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * secondaryOffsetX, h * secondaryOffsetY)
            )
        }

        // Content on top
        content()
    }
}

private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
