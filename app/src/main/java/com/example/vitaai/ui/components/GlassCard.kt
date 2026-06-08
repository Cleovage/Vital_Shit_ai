package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.055f),
                spotColor = Color.Black.copy(alpha = 0.055f)
            )
            .clip(shape)
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
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                ambientColor = glowColor.copy(alpha = 0.08f),
                spotColor = glowColor.copy(alpha = 0.08f)
            )
            .clip(shape)
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

