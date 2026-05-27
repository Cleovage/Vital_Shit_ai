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
import com.example.vitaai.ui.theme.OutlineVariant
import com.example.vitaai.ui.theme.SurfaceContainer

/**
 * Standard technical card for Apex Vitality.
 */
@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(SurfaceContainer)
            .border(1.dp, OutlineVariant, MaterialTheme.shapes.large)
    ) {
        content()
    }
}
