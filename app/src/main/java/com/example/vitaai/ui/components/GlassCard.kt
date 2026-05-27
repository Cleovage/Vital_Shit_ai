package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.LocalVitaColors

/**
 * Glassmorphism card matching the Luminous Sanctuary design spec.
 *
 * - Semi-transparent fill (white 3%)
 * - Asymmetric border: top-left brighter (15% white), bottom-right dimmer (5%)
 * - Rounded corners (16dp default)
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .background(vitaColors.glassFill, shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        vitaColors.glassBorderLight,
                        vitaColors.glassBorderLight.copy(alpha = 0.10f),
                        vitaColors.glassBorderDark
                    )
                ),
                shape = shape
            )
            .padding(20.dp),
        content = content
    )
}

/**
 * Variant with active glow — used for interactive or "active" cards.
 * Adds a soft outer glow using the primary neon color.
 */
@Composable
fun GlassCardGlow(
    modifier: Modifier = Modifier,
    glowColor: Color = LocalVitaColors.current.neonTeal,
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .background(vitaColors.glassFill, shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.3f),
                        glowColor.copy(alpha = 0.1f),
                        vitaColors.glassBorderDark
                    )
                ),
                shape = shape
            )
            .padding(20.dp),
        content = content
    )
}
