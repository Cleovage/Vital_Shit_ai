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

/**
 * Standard technical card for Apex Vitality.
 */
@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = SurfaceContainer,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, OutlineVariant.copy(alpha = 0.5f), shape)
    ) {
        Column {
            if (title != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OutlineVariant.copy(alpha = 0.1f))
                        .border(
                            width = 1.dp,
                            color = OutlineVariant.copy(alpha = 0.3f),
                            shape = RectangleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                }
            }
            Box(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
