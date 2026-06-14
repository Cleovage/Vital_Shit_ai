package com.example.vitaai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.*

/**
 * Premium Button matching the dark slate styling of the Shader Dash spec.
 * Uses a solid Slate-900 fill (#0F172A) with white text,
 * and a ghost variant with a thin border and Slate text.
 */
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ghost: Boolean = false
) {
    val shape = RoundedCornerShape(24.dp) // pill-like

    if (ghost) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = shape,
            border = BorderStroke(
                width = 1.dp,
                color = Color(0x1F0F172A) // 12% Slate
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF0F172A)
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}
