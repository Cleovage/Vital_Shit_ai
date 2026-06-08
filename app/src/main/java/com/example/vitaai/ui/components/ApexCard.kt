package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium card for VitaAI — Shader Dash light-cream aesthetic.
 *
 * Features:
 * - Raised appearance via soft Gaussian shadow (Paint.setShadowLayer)
 * - Subtle glassmorphic border (thin outer stroke + specular top highlight)
 * - Frosted glass fill (semi-transparent white)
 * - Optional title header bar
 */
@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = Color.White.copy(alpha = 0.82f),
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shadowColor = Color.Black.copy(alpha = 0.10f).toArgb()
    val elev = elevation.value

    Box(
        modifier = modifier
            // Gaussian drop shadow behind card
            .drawBehind {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(elev * 2.5f, 0f, elev, shadowColor)
                    }
                }
                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        radiusX = 30.dp.toPx(),
                        radiusY = 30.dp.toPx(),
                        paint = paint
                    )
                }
            }
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.07f),
                shape = shape
            )
    ) {
        Column {
            if (title != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.025f))
                        .border(
                            width = 1.dp,
                            color = Color.Black.copy(alpha = 0.04f),
                            shape = RectangleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0F172A).copy(alpha = 0.7f),
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
