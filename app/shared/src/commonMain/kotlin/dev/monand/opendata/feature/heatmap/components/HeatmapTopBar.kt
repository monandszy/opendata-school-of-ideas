package dev.monand.opendata.feature.heatmap.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.monand.opendata.core.map.MapTileSource

@Composable
fun HeatmapTopBar(
    minTemp: Double,
    maxTemp: Double,
    stationCount: Int,
    isLoading: Boolean,
    lastUpdated: String?,
    tileSource: MapTileSource,
    showHeatmap: Boolean,
    is3dMode: Boolean,
    onSelectTileSource: (MapTileSource) -> Unit,
    onToggleHeatmap: () -> Unit,
    onToggle3dMode: () -> Unit,
    onRefreshStations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Warszawa – Temp. Powietrza (API)",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle =
                    when {
                        isLoading -> "Pobieranie danych..."

                        stationCount > 0 ->
                            "$minTemp°C – $maxTemp°C • ${lastUpdated ?: "aktualne"} ($stationCount stacji)"

                        else -> "Brak danych z API"
                    }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = onRefreshStations,
                enabled = !isLoading,
            ) {
                Text(if (isLoading) "..." else "Odśwież")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = is3dMode,
                onClick = onToggle3dMode,
                label = { Text(if (is3dMode) "3D: ON" else "3D: OFF") },
            )
            FilterChip(
                selected = showHeatmap,
                onClick = onToggleHeatmap,
                label = { Text(if (showHeatmap) "Heatmap: ON" else "Heatmap: OFF") },
            )
        }
    }
}
