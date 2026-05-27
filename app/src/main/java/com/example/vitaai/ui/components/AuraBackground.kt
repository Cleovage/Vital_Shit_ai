package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.vitaai.ui.theme.Background
import com.example.vitaai.ui.theme.SurfaceContainerLowest

/**
 * Full-screen background for Apex Vitality.
 * 
 * Replaces the animated blobs with a solid, high-performance technical base.
 */
@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        SurfaceContainerLowest
                    )
                )
            )
    ) {
        // Content on top
        content()
    }
}
