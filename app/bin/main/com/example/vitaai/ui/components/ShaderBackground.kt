package com.example.vitaai.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import com.example.vitaai.ui.components.shader.VITA_BACKGROUND_AGSL

data class ShaderColors(
    val topLeft: Color = Color(0xFF34D399),     // green
    val topRight: Color = Color(0xFF60A5FA),    // blue
    val bottomCenter: Color = Color(0xFF67E8F9) // cyan
) {
    companion object {
        fun Default() = ShaderColors()
    }
}

/**
 * Animated background inspired by shader_dash_design/ShaderBackground.tsx.
 *
 * - On API 33+ it renders via [RuntimeShader] (AGSL) with three drifting
 *   metaballs blended by smooth-min.
 * - On API 26-32 it falls back to a Canvas implementation that animates
 *   three large radial-gradient blobs along sinusoidal paths using
 *   `withFrameNanos` for smooth frame-locked motion.
 */
@Composable
fun ShaderBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 0.6f,
    colors: ShaderColors = ShaderColors.Default(),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AgslShaderLayer(intensity = intensity, colors = colors)
        } else {
            CanvasFallbackLayer(intensity = intensity, colors = colors)
        }
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AgslShaderLayer(intensity: Float, colors: ShaderColors) {
    val shader = remember { RuntimeShader(VITA_BACKGROUND_AGSL) }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (startNanos == 0L) startNanos = nanos
                time = (nanos - startNanos) / 1_000_000_000f
            }
        }
    }

    val brush = remember(colors, intensity) {
        object : ShaderBrush() {
            override fun createShader(size: androidx.compose.ui.geometry.Size) = shader.apply {
                setFloatUniform("iResolution", size.width, size.height)
                setFloatUniform("uIntensity", intensity)
                setFloatUniform(
                    "color1",
                    (colors.topLeft.red),
                    (colors.topLeft.green),
                    (colors.topLeft.blue)
                )
                setFloatUniform(
                    "color2",
                    (colors.topRight.red),
                    (colors.topRight.green),
                    (colors.topRight.blue)
                )
                setFloatUniform(
                    "color3",
                    (colors.bottomCenter.red),
                    (colors.bottomCenter.green),
                    (colors.bottomCenter.blue)
                )
            }
        }
    }

    LaunchedEffect(time, intensity, colors) {
        shader.setFloatUniform("iTime", time)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = time // Force Compose to register state read and invalidate on frame updates
        drawRect(brush = brush, size = size)
    }
}

@Composable
private fun CanvasFallbackLayer(intensity: Float, colors: ShaderColors) {
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (startNanos == 0L) startNanos = nanos
                time = (nanos - startNanos) / 1_000_000_000f
            }
        }
    }

    val alphaScale = intensity.coerceIn(0f, 1f) * 0.10f

    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val radiusBase = minOf(w, h) * 0.55f

                val brush1 = Brush.radialGradient(
                    colors = listOf(colors.topLeft.copy(alpha = alphaScale), Color.Transparent),
                    center = Offset.Zero,
                    radius = radiusBase
                )
                val brush2 = Brush.radialGradient(
                    colors = listOf(colors.topRight.copy(alpha = alphaScale), Color.Transparent),
                    center = Offset.Zero,
                    radius = radiusBase
                )
                val brush3 = Brush.radialGradient(
                    colors = listOf(colors.bottomCenter.copy(alpha = alphaScale * 0.8f), Color.Transparent),
                    center = Offset.Zero,
                    radius = radiusBase * 1.2f
                )
                val verticalGradient = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFF1F5F9).copy(alpha = 0.45f)),
                    startY = 0f,
                    endY = h * 0.3f
                )

                onDrawBehind {
                    val t = time * 0.25f

                    // 3 metaballs
                    val c1 = Offset(
                        x = (0.30f + 0.20f * kotlin.math.sin(t * 0.9f)) * w,
                        y = (0.25f + 0.18f * kotlin.math.cos(t * 0.7f)) * h
                    )
                    val c2 = Offset(
                        x = (0.75f + 0.18f * kotlin.math.cos(t * 1.1f)) * w,
                        y = (0.30f + 0.20f * kotlin.math.sin(t * 0.6f)) * h
                    )
                    val c3 = Offset(
                        x = (0.50f + 0.25f * kotlin.math.sin(t * 0.5f)) * w,
                        y = (0.80f + 0.12f * kotlin.math.cos(t * 0.8f)) * h
                    )

                    // Draw metaball 1
                    drawContext.canvas.save()
                    drawContext.canvas.translate(c1.x, c1.y)
                    drawCircle(
                        brush = brush1,
                        radius = radiusBase,
                        center = Offset.Zero
                    )
                    drawContext.canvas.restore()

                    // Draw metaball 2
                    drawContext.canvas.save()
                    drawContext.canvas.translate(c2.x, c2.y)
                    drawCircle(
                        brush = brush2,
                        radius = radiusBase,
                        center = Offset.Zero
                    )
                    drawContext.canvas.restore()

                    // Draw metaball 3
                    drawContext.canvas.save()
                    drawContext.canvas.translate(c3.x, c3.y)
                    drawCircle(
                        brush = brush3,
                        radius = radiusBase * 1.2f,
                        center = Offset.Zero
                    )
                    drawContext.canvas.restore()

                    // bottom warm gradient
                    drawContext.canvas.save()
                    drawContext.canvas.translate(0f, h * 0.7f)
                    drawRect(
                        brush = verticalGradient,
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(w, h * 0.3f)
                    )
                    drawContext.canvas.restore()
                }
            }
    )
}
