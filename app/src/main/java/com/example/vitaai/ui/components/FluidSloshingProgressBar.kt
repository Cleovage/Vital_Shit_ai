package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * A horizontal progress bar where the filled contents slosh like water.
 */
@Composable
fun FluidSloshingProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    liquidColor: Color = Color(0xFF06B6D4), // Cyan liquid
    backgroundColor: Color = Color.Black.copy(alpha = 0.05f),
    cornerRadius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    // Smooth progress spring transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .height(24.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .glassmorphicBorder(cornerRadius = cornerRadius, borderWidth = 1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val fillHeight = h * animatedProgress
            
            val path = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                )
            }

            clipPath(path) {
                if (animatedProgress > 0f) {
                    val wavePath = Path().apply {
                        moveTo(0f, h)
                        
                        val frequency = 1.6f
                        val amplitude = 5.dp.toPx() // Height of wave crests
                        
                        for (x in 0..w.toInt()) {
                            val pct = x.toFloat() / w
                            val waveY = (h - fillHeight) + 
                                        sin(pct * frequency * 2 * Math.PI + phase).toFloat() * amplitude
                            
                            lineTo(x.toFloat(), waveY.coerceIn(0f, h))
                        }
                        
                        lineTo(w, h)
                        close()
                    }
                    
                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                liquidColor,
                                liquidColor.copy(alpha = 0.65f)
                            )
                        )
                    )
                }
            }
        }
    }
}
