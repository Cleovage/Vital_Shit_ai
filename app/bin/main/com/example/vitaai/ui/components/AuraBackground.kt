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
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.Background
import com.example.vitaai.ui.theme.OutlineVariant
import com.example.vitaai.ui.theme.SurfaceContainerLowest

/**
 * Full-screen background for Apex Vitality.
 */
@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        SurfaceContainerLowest
                    )
                )
            )
    ) {
        if (showGrid) {
            val gridColor = OutlineVariant.copy(alpha = 0.05f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 40.dp.toPx()
                // Horizontal lines
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y * step),
                        end = Offset(size.width, y * step),
                        strokeWidth = 1f
                    )
                }
                // Vertical lines
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, size.height),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Content on top
        content()
    }
}
