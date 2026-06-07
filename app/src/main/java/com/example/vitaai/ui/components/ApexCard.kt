package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.example.vitaai.ui.theme.OnSurfaceVariant
import com.example.vitaai.ui.theme.OutlineVariant
import com.example.vitaai.ui.theme.Primary
import com.example.vitaai.ui.theme.SurfaceContainer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight

/**
 * Standard technical card for Apex Vitality.
 */
@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = Color(0xFF1E1F2E).copy(alpha = 0.6f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.35f),
                        Color(0xFF2979FF).copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
    ) {
        Column {
            if (title != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00E5FF).copy(alpha = 0.08f))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            shape = RectangleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E5FF),
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
