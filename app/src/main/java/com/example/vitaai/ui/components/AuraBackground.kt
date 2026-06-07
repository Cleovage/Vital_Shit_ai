package com.example.vitaai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
2. Design System: AuraBackground
- Base Background: Pure white #ffffff
- Ambient Glow Blobs: Three large, blurred, absolute-positioned decorative elements in the background:
    * Green Readiness Blob (Top Left): bg-emerald-400/[0.06] (approx. #34d399 at 6% opacity)
    * Blue Sleep Blob (Top Right): bg-blue-400/[0.06] (approx. #60a5fa at 6% opacity)
    * Cyan AI Blob (Bottom Center): bg-cyan-300/[0.05] (approx. #67e8f9 at 5% opacity)
    * Bottom Warm Gradient: bg-gradient-to-t from-slate-50/60 to-transparent covering the bottom 1/3 of the screen.
*/
@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    showGrid: Boolean = false, // disabled for light theme
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // SVG Ambient Glow Blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Green Readiness Blob (Top Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF34D399).copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(-size.width * 0.1f, -size.height * 0.1f),
                    radius = size.width * 1.1f
                ),
                center = Offset(-size.width * 0.1f, -size.height * 0.1f),
                radius = size.width * 1.1f
            )

            // Blue Sleep Blob (Top Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 1.1f, 0f),
                    radius = size.width * 1.1f
                ),
                center = Offset(size.width * 1.1f, 0f),
                radius = size.width * 1.1f
            )

            // Cyan AI Blob (Bottom Center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF67E8F9).copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 1.1f),
                    radius = size.width * 1.3f
                ),
                center = Offset(size.width * 0.5f, size.height * 1.1f),
                radius = size.width * 1.3f
            )
        }

        // Bottom Warm Gradient: bg-gradient-to-t from-slate-50/60 to-transparent covering the bottom 1/3 of the screen
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFF1F5F9).copy(alpha = 0.60f)),
                    startY = size.height * 0.67f,
                    endY = size.height
                )
            )
        }

        // Foreground Content
        content()
    }
}
