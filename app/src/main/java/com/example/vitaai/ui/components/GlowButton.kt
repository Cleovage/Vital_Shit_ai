package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.*

/**
 * Primary button with a teal-to-violet gradient fill and white text.
 * Ghost variant uses a luminous stroke with transparent fill.
 */
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ghost: Boolean = false
) {
    val shape = PillShape

    if (ghost) {
        // Ghost button with luminous stroke
        androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.linearGradient(listOf(PrimaryContainer, Secondary))
            ),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = Primary
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        // Primary button with gradient fill
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = shape,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(PrimaryContainer, Secondary)
                        ),
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}
