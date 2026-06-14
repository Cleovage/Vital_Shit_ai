package com.example.vitaai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithCache

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
        // SVG Ambient Glow Blobs and warm gradient combined in a single Spacer with drawWithCache
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    
                    val greenBrush = Brush.radialGradient(
                        colors = listOf(Color(0xFF34D399).copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(-w * 0.1f, -h * 0.1f),
                        radius = w * 1.1f
                    )
                    
                    val blueBrush = Brush.radialGradient(
                        colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(w * 1.1f, 0f),
                        radius = w * 1.1f
                    )
                    
                    val cyanBrush = Brush.radialGradient(
                        colors = listOf(Color(0xFF67E8F9).copy(alpha = 0.05f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 1.1f),
                        radius = w * 1.3f
                    )
                    
                    val verticalBrush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF1F5F9).copy(alpha = 0.60f)),
                        startY = h * 0.67f,
                        endY = h
                    )
                    
                    onDrawBehind {
                        // Green readiness
                        drawCircle(
                            brush = greenBrush,
                            center = Offset(-w * 0.1f, -h * 0.1f),
                            radius = w * 1.1f
                        )
                        
                        // Blue sleep
                        drawCircle(
                            brush = blueBrush,
                            center = Offset(w * 1.1f, 0f),
                            radius = w * 1.1f
                        )
                        
                        // Cyan AI
                        drawCircle(
                            brush = cyanBrush,
                            center = Offset(w * 0.5f, h * 1.1f),
                            radius = w * 1.3f
                        )
                        
                        // Bottom warm gradient
                        drawRect(
                            brush = verticalBrush
                        )
                    }
                }
        )

        // Foreground Content
        content()
    }
}
