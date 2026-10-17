package dev.monand.opendata

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.monand.opendata.feature.heatmap.PogodaHeatmapScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        PogodaHeatmapScreen()
    }
}
