package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A highly blurred vector blob that oscillates in scale and opacity,
 * creating a floating ambient backdrop.
 */
@Composable
fun AmbientGlowBlob(
    color: Color,
    modifier: Modifier = Modifier,
    initialSize: Dp = 160.dp,
    durationMillis: Int = 6000,
    delayMillis: Int = 0
) {
    val transition = rememberInfiniteTransition(label = "blobAnim")
    
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(initialSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .blur(50.dp) // Soft diffuse blur
            .background(color.copy(alpha = alpha), shape = CircleShape)
    )
}

/**
 * Custom modifier that draws a moving linear gradient overlay.
 * Bypasses recomposition by rendering directly on the canvas.
 */
fun Modifier.shimmerOverlay(
    baseColor: Color = Color.Black.copy(alpha = 0.05f),
    shimmerColor: Color = Color.White.copy(alpha = 0.7f),
    durationMillis: Int = 1500
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    this.drawBehind {
        val w = size.width
        val h = size.height
        
        // Draw backing color
        drawRect(color = baseColor)
        
        // Sweep coordinates offset
        val xStart = w * progress
        val yStart = h * (progress - 0.5f)
        val xEnd = w * (progress + 0.5f)
        val yEnd = h * (progress + 1.0f)
        
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                shimmerColor.copy(alpha = 0.15f),
                shimmerColor,
                shimmerColor.copy(alpha = 0.15f),
                Color.Transparent
            ),
            start = Offset(xStart, yStart),
            end = Offset(xEnd, yEnd)
        )
        
        drawRect(brush = brush)
    }
}

/**
 * Glassmorphic Bento Card wrapper presenting skeleton shimmers.
 */
@Composable
fun SkeletonBentoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp
) {
    GlassmorphicBentoCard(
        modifier = modifier.height(180.dp),
        cornerRadius = cornerRadius
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerOverlay()
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerOverlay()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerOverlay()
                )
            }
        }
    }
}

@Composable
fun GlassmorphicBentoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = 0.78f))
            .glassmorphicBorder(cornerRadius = cornerRadius),
        content = content
    )
}
