package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun SloshingWaterCapsule(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sloshing")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // 1. Draw capsule shape path
        val path = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = androidx.compose.ui.geometry.Rect(0f, 0f, width, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            )
        }
        
        // Clip to capsule shape
        clipPath(path) {
            // Draw background fill (semi-transparent dark)
            drawRect(color = Color.Black.copy(alpha = 0.04f))
            
            // 2. Draw water fill with sloshing wave
            val waterHeight = height * progress
            val waveHeight = 4.dp.toPx()
            val waveFreq = (2 * Math.PI).toFloat() / width
            
            val waterPath = Path().apply {
                moveTo(0f, height)
                if (progress > 0f) {
                    val topY = height - waterHeight
                    for (x in 0..width.toInt()) {
                        val y = topY + waveHeight * sin(x * waveFreq + waveOffset)
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, topY) // line to top right
                    lineTo(width, height)
                }
                close()
            }
            
            drawPath(
                path = waterPath,
                color = Color(0xFF00B8D4)
            )
            
            // Draw highlight sheen inside water
            if (progress > 0.05f) {
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), height - waterHeight + 8.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(4.dp.toPx(), height - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        // Draw border outline
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.07f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
