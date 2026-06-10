package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.*

/**
 * Pill-shaped chip/tag with a low-opacity tinted background.
 * Used for category labels like "Sleep", "Cardio", "Heart Rate".
 */
@Composable
fun StatChip(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Primary,
    textColor: Color = color.copy(alpha = 1f)
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = PillShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
