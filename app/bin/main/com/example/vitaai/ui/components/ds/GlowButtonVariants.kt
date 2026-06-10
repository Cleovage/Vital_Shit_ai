package com.example.vitaai.ui.components.ds

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.ripple.LocalRippleTheme

/**
 * Primary Glow Button — filled with Primary cyan, white text, with an
 * animated pulsing outer glow.
 */
@Composable
fun GlowButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 16.dp,
    contentDescription: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "primaryGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "primaryGlowAlpha"
    )

    val primary = Color(0xFF06B6D4)
    val glowColor = primary.copy(alpha = glowAlpha).toArgb()
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
                role = Role.Button
            }
            .defaultMinSize(minHeight = 48.dp)
            .drawBehind {
                if (enabled) {
                    val paint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(20f, 0f, 0f, glowColor)
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
            }
            .clip(shape)
            .background(if (enabled) primary else primary.copy(alpha = 0.4f))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

/**
 * Secondary outlined Glow Button — 1.5dp cyan border, cyan text, subtle glow.
 */
@Composable
fun GlowButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 14.dp,
    contentDescription: String? = null
) {
    val primary = Color(0xFF06B6D4)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
                role = Role.Button
            }
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (enabled) primary else primary.copy(alpha = 0.4f),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) primary else primary.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

/**
 * Circular icon glow button — single icon, pulsing ring.
 */
@Composable
fun GlowButtonIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    tint: Color = Color(0xFF06B6D4)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconGlow")
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconGlowAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .clip(CircleShape)
            .drawBehind {
                val cx = size.toPx() / 2f
                drawCircle(
                    color = tint.copy(alpha = ringAlpha),
                    radius = size.toPx() / 2f + 6f,
                    center = androidx.compose.ui.geometry.Offset(cx, cx)
                )
            }
            .background(Color.White.copy(alpha = 0.78f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}
