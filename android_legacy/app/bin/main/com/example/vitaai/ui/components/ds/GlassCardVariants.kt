package com.example.vitaai.ui.components.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Glass Card — Elevated variant.
 *
 * Stronger drop shadow (elevPx 18f) and a thicker 1.5dp border to convey
 * hierarchy above the standard [GlassCard]. Use sparingly for hero cards.
 */
@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = Color.Black.copy(alpha = 0.18f).toArgb()
    val elevPx = 18f

    Column(
        modifier = modifier
            .drawBehind {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(elevPx * 2.2f, 0f, elevPx * 0.8f, shadowColor)
                    }
                }
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
            .graphicsLayer(
                shadowElevation = 18f,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.16f),
                spotShadowColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .background(Color.White.copy(alpha = 0.82f))
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
 * Outlined Glass Card — no fill, no shadow, only a strong border.
 * Use for secondary cards that sit on top of a colored surface.
 */
@Composable
fun GlassCardOutlined(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 20.dp,
    borderColor: Color = Color.Black.copy(alpha = 0.12f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .background(Color.Transparent)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

/**
 * Inset / sunken Glass Card — no outer shadow, light surface tint, thin inset border.
 * Use for embedded/secondary panels (e.g. inside a hero card).
 */
@Composable
fun GlassCardInset(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .background(Color(0xFFF1F5F9).copy(alpha = 0.7f))
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.10f),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}
