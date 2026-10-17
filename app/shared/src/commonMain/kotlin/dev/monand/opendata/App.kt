package dev.monand.opendata

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.monand.opendata.feature.heatmap.PogodaHeatmapScreen

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF00325B),
        primaryContainer = Color(0xFF004881),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary = Color(0xFF80CBC4),
        onSecondary = Color(0xFF003733),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE2E2E2),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFF0F0F0),
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFC8C8C8),
    )

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        PogodaHeatmapScreen()
    }
}
