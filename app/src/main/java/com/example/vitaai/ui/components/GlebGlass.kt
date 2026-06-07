package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.LocalGlebColors

/**
 * High-Fidelity Glass Card matching Gleb Kuznetsov's aesthetic.
 * Performant implementation (no heavy Haze/backdrop-filter to prevent lag).
 */
@Composable
fun GlebGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp, // Mobile proportion
    glowColor: Color = Color.Transparent,
    innerPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalGlebColors.current
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .shadow(
                elevation = if (glowColor != Color.Transparent) 24.dp else 16.dp,
                shape = shape,
                clip = false,
                spotColor = if (glowColor != Color.Transparent) glowColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(colors.cardFill) // 0.05f alpha white
            // Rim Light (Sharp Highlight)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = shape
            )
            .padding(innerPadding),
        content = content
    )
}
