package com.example.vitaai.ui.components.shader

/**
 * AGSL fragment shader source for the Vita animated background.
 *
 * Adapts the spirit of the React ShaderBackground.tsx (spectral modulation
 * of three drifting color blobs) to AGSL. Produces three metaballs with
 * smooth-min blending whose centers drift via sinusoidal paths.
 *
 * This string is loaded by [com.example.vitaai.ui.components.ShaderBackground]
 * on API 33+ via [android.graphics.RuntimeShader]. On API 26-32 the
 * Canvas-based fallback is used instead — see the ShaderBackground file.
 */
const val VITA_BACKGROUND_AGSL = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float3 color1;
    uniform float3 color2;
    uniform float3 color3;
    uniform float uIntensity;

    float smin(float a, float b, float k) {
        float h = max(k - abs(a - b), 0.0) / k;
        return min(a, b) - h * h * k * 0.25;
    }

    float metaball(float2 p, float2 center, float radius) {
        float d = length(p - center);
        return d / radius;
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        float t = iTime * 0.08;

        // three drifting centers (normalized 0..1)
        float2 c1 = float2(0.30 + 0.20 * sin(t * 0.9), 0.25 + 0.18 * cos(t * 0.7));
        float2 c2 = float2(0.75 + 0.18 * cos(t * 1.1), 0.30 + 0.20 * sin(t * 0.6));
        float2 c3 = float2(0.50 + 0.25 * sin(t * 0.5), 0.80 + 0.12 * cos(t * 0.8));

        float d1 = metaball(uv, c1, 0.55);
        float d2 = metaball(uv, c2, 0.55);
        float d3 = metaball(uv, c3, 0.65);

        float k = 0.18;
        float s12 = smin(d1, d2, k);
        float s123 = smin(s12, d3, k);

        // turn distance into alpha: closer = brighter
        float field = clamp(1.0 - s123, 0.0, 1.0);
        field = pow(field, 1.6);

        // base canvas color
        float3 base = float3(1.0, 1.0, 1.0);

        // mix the three colors weighted by per-center contribution
        float w1 = clamp(1.0 - d1, 0.0, 1.0);
        float w2 = clamp(1.0 - d2, 0.0, 1.0);
        float w3 = clamp(1.0 - d3, 0.0, 1.0);
        float wsum = w1 + w2 + w3 + 1e-5;
        float3 mix = (color1 * w1 + color2 * w2 + color3 * w3) / wsum;

        float3 col = base + (mix - base) * field * uIntensity;

        return half4(half3(col), 1.0);
    }
"""
