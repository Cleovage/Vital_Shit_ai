package com.example.vitaai.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.draw.drawWithCache

/**
 * Draws a glassmorphic double-border:
 * 1. An outer subtle dark border around the entire shape (simulates shadow edge)
 * 2. A specular top-left highlight path (simulates light hitting frosted glass)
 *
 * This should be applied as a Modifier on the container element (e.g. Box, Surface).
 */
fun Modifier.glassmorphicBorder(
    cornerRadius: Dp,
    borderWidth: Dp = 1.dp,
    outerColor: Color = Color.Black.copy(alpha = 0.07f),
    innerHighlightColor: Color = Color.White.copy(alpha = 0.8f)
): Modifier = this.drawWithCache {
    val r = cornerRadius.toPx()
    val w = borderWidth.toPx()
    val halfW = w / 2f

    // --- Outer border: full rounded rect path ---
    val outerPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = halfW,
                top = halfW,
                right = size.width - halfW,
                bottom = size.height - halfW,
                cornerRadius = CornerRadius(r)
            )
        )
    }

    // --- Inner specular highlight: top-left arc path only ---
    val highlightPath = Path().apply {
        moveTo(halfW, r + halfW)
        arcTo(
            rect = Rect(halfW, halfW, r * 2 + halfW, r * 2 + halfW),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(size.width - r - halfW, halfW)
        arcTo(
            rect = Rect(size.width - r * 2 - halfW, halfW, size.width - halfW, r * 2 + halfW),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 45f,
            forceMoveTo = false
        )
    }

    onDrawBehind {
        drawPath(
            path = outerPath,
            color = outerColor,
            style = Stroke(width = w)
        )
        drawPath(
            path = highlightPath,
            color = innerHighlightColor,
            style = Stroke(width = w)
        )
    }
}
