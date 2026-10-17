package dev.monand.opendata.feature.heatmap.components

import androidx.compose.ui.graphics.Color

fun heatColorForTemperatureCelsius(
    temp: Double,
    alpha: Float = 0.60f,
): Color = when {
    temp <= 0.0 -> {
        Color(red = 0f / 255f, green = 150f / 255f, blue = 255f / 255f, alpha = alpha)
    }

    temp <= 10.0 -> {
        val k = (temp - 0.0) / 10.0
        val r = (0f + k * (51f - 0f)).toFloat()
        val g = (150f + k * (220f - 150f)).toFloat()
        val b = (255f + k * (102f - 255f)).toFloat()
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = alpha)
    }

    temp <= 20.0 -> {
        val k = (temp - 10.0) / 10.0
        val r = (51f + k * (255f - 51f)).toFloat()
        val g = (220f + k * (255f - 220f)).toFloat()
        val b = (102f + k * (0f - 102f)).toFloat()
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = alpha)
    }

    temp <= 25.0 -> {
        val k = (temp - 20.0) / 5.0
        val r = 255f
        val g = (255f + k * (140f - 255f)).toFloat()
        val b = 0f
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = alpha)
    }

    temp <= 30.0 -> {
        val k = (temp - 25.0) / 5.0
        val r = 255f
        val g = (140f + k * (30f - 140f)).toFloat()
        val b = 0f
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = alpha)
    }

    else -> {
        Color(red = 255f / 255f, green = 30f / 255f, blue = 0f / 255f, alpha = alpha)
    }
}

fun heatColorForNormalizedTemp(
    t: Float,
    alpha: Float = 0.60f,
): Color {
    val temp = t.coerceIn(0f, 1f) * 30.0
    return heatColorForTemperatureCelsius(temp, alpha = alpha)
}
