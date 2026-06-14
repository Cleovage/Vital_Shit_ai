package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
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
    
    val colors = remember(shimmerColor) {
        listOf(
            Color.Transparent,
            shimmerColor.copy(alpha = 0.15f),
            shimmerColor,
            shimmerColor.copy(alpha = 0.15f),
            Color.Transparent
        )
    }

    val argbColors = remember(colors) {
        colors.map { it.toArgb() }.toIntArray()
    }
    
    this.drawWithCache {
        val w = size.width
        val h = size.height
        
        val shader = android.graphics.LinearGradient(
            0f, 0f,
            w * 0.5f, h * 1.5f,
            argbColors,
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        val matrix = android.graphics.Matrix()
        val brush = ShaderBrush(shader)
        
        onDrawBehind {
            // Draw backing color
            drawRect(color = baseColor)
            
            // Translate matrix based on progress
            matrix.reset()
            val dx = w * progress
            val dy = h * (progress - 0.5f)
            matrix.setTranslate(dx, dy)
            shader.setLocalMatrix(matrix)
            
            // Draw shimmer using cached shader brush
            drawRect(brush = brush)
        }
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

/**
 * Reusable empty state with icon, title, message, and optional action button.
 * Rendered on a translucent glassmorphic surface.
 */
@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.65f))
            .glassmorphicBorder(cornerRadius = 24.dp)
            .padding(32.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = Color.Black.copy(alpha = 0.65f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            androidx.compose.material3.Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.TextButton(onClick = onAction) {
                    androidx.compose.material3.Text(
                        text = actionText,
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Inline loading state card — shimmer surface with centered progress text.
 */
@Composable
fun LoadingStateCard(
    message: String = "Loading…",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .shimmerOverlay()
            .glassmorphicBorder(cornerRadius = 24.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.Black.copy(alpha = 0.35f)
            )
            androidx.compose.material3.Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.45f)
            )
        }
    }
}
