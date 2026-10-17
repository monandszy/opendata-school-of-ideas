package dev.monand.opendata.feature.heatmap.components

import androidx.compose.ui.graphics.Color

fun heatColorForNormalizedTemp(
    t: Float,
    alpha: Float = 0.60f,
): Color {
    val clamped = t.coerceIn(0f, 1f)
    return when {
        clamped < 0.25f -> {
            val k = clamped / 0.25f
            Color(red = 0f, green = 0.8f + 0.2f * k, blue = 1f - 0.6f * k, alpha = alpha)
        }

        clamped < 0.5f -> {
            val k = (clamped - 0.25f) / 0.25f
            Color(red = k, green = 1f, blue = 0.4f * (1f - k), alpha = alpha)
        }

        clamped < 0.75f -> {
            val k = (clamped - 0.5f) / 0.25f
            Color(red = 1f, green = 1f - 0.45f * k, blue = 0f, alpha = alpha)
        }

        else -> {
            val k = (clamped - 0.75f) / 0.25f
            Color(red = 1f, green = 0.55f * (1f - k), blue = 0.3f * k, alpha = alpha)
        }
    }
}
