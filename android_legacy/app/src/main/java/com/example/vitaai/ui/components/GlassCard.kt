package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Frosted Glass Card matching the Shader Dash light interface.
 * Features a translucent white background (78% opacity), thin border (7% black),
 * and soft drop shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    contentPadding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = Color.Black.copy(alpha = 0.12f).toArgb()
    val elevPx = 12f

    Column(
        modifier = modifier
            // Paint-based Gaussian shadow — renders OUTSIDE the clip boundary for real depth
            .drawWithCache {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(elevPx * 2.2f, 0f, elevPx * 0.8f, shadowColor)
                    }
                }
                onDrawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = cornerRadius.toPx(),
                            radiusY = cornerRadius.toPx(),
                            paint = paint
                        )
                    }
                }
            }
            // Hardware-accelerated layer shadow for API 28+ devices
            .graphicsLayer(
                shadowElevation = 12f,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.12f),
                spotShadowColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.07f),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

/**
 * Variant with active shadow highlight or colored outline borders.
 */
@Composable
fun GlassCardGlow(
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF06B6D4), // Cyan 500
    cornerRadius: Dp = 30.dp,
    contentPadding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = Color.Black.copy(alpha = 0.18f).toArgb()
    val elevPx = 14f

    Column(
        modifier = modifier
            // Paint-based Gaussian shadow with glow tint — renders outside clip boundary
            .drawWithCache {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(elevPx * 2.2f, 0f, elevPx * 0.8f, shadowColor)
                    }
                }
                onDrawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = cornerRadius.toPx(),
                            radiusY = cornerRadius.toPx(),
                            paint = paint
                        )
                    }
                }
            }
            // Hardware-accelerated layer shadow with glow color
            .graphicsLayer(
                shadowElevation = 14f,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.12f),
                spotShadowColor = Color.Black.copy(alpha = 0.18f)
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = glowColor.copy(alpha = 0.2f),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}


/**
 * Elevated GlassCard — higher elevation for prominent surfaces.
 * Uses increased shadowLayer radius and border for more depth.
 */
@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    contentPadding: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = Color.Black.copy(alpha = 0.15f).toArgb()
    val elevPx = 20f

    Column(
        modifier = modifier
            .drawWithCache {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(elevPx * 2.5f, 0f, elevPx * 1.2f, shadowColor)
                    }
                }
                onDrawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = cornerRadius.toPx(),
                            radiusY = cornerRadius.toPx(),
                            paint = paint
                        )
                    }
                }
            }
            .graphicsLayer(
                shadowElevation = 24f,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.15f),
                spotShadowColor = Color.Black.copy(alpha = 0.20f)
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .background(Color.White.copy(alpha = 0.85f))
            .border(
                width = 1.5.dp,
                color = Color.Black.copy(alpha = 0.08f),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

/**
 * Compact GlassCard — smaller padding for tighter UI elements.
 */
@Composable
fun GlassCardCompact(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        contentPadding = contentPadding,
        onClick = onClick,
        content = content
    )
}
