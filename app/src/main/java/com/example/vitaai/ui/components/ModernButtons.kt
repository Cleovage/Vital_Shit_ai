package com.example.vitaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.Primary

/**
 * Primary action button featuring:
 * - Tactile spring-press scale interaction
 * - Ambient ambient glow rendered via drawBehind (correctly z-ordered, no offset misplacement)
 * - Gaussian blur simulated via Paint.asFrameworkPaint shadowLayer
 */
@Composable
fun GlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = Primary,
    isEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile spring scaling
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    // Glow radius grows when pressed
    val glowRadius by animateFloatAsState(
        targetValue = if (isPressed) 28f else 18f,
        animationSpec = tween(durationMillis = 200),
        label = "glowRadius"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.55f else 0.32f,
        animationSpec = tween(durationMillis = 200),
        label = "glowAlpha"
    )

    val glowColorArgb = Color.Black.copy(alpha = glowAlpha).toArgb()

    Button(
        onClick = onClick,
        enabled = isEnabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0F172A), // Slate 900
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF0F172A).copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // Fix: use drawBehind to paint glow BEHIND the button surface — no offset displacement
            .drawBehind {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(glowRadius, 0f, 0f, glowColorArgb)
                    }
                }
                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        radiusX = 20.dp.toPx(),
                        radiusY = 20.dp.toPx(),
                        paint = paint
                    )
                }
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        glowColor.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Secondary CTA with glassmorphic border and spring press.
 */
@Composable
fun GlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = isEnabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xBAFFFFFF),
            contentColor = Color(0xFF0F172A)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isPressed) 2f else 6f
            }
            .glassmorphicBorder(cornerRadius = 20.dp, borderWidth = 1.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}
