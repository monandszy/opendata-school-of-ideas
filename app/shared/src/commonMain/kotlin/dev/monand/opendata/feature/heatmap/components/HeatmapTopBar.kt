package dev.monand.opendata.feature.heatmap.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
    showStations: Boolean = true,
    showToilets: Boolean = true,
    showMiejscaChlodu: Boolean = true,
    filterOutliers: Boolean = false,
    is3dMode: Boolean,
    onSelectTileSource: (MapTileSource) -> Unit,
    onToggleHeatmap: () -> Unit,
    onToggleStationsVisibility: () -> Unit = {},
    onToggleToiletsVisibility: () -> Unit = {},
    onToggleMiejscaChloduVisibility: () -> Unit = {},
    onToggleFilterOutliers: () -> Unit = {},
    onToggle3dMode: () -> Unit,
    onRefreshStations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Warszawa – Temp. Powietrza, Toalety & Chłód",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OutlinedButton(
                    onClick = onRefreshStations,
                    enabled = !isLoading,
                    colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (isLoading) "..." else "Odśwież")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val chipColors =
                    FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    )
                FilterChip(
                    selected = showToilets,
                    onClick = onToggleToiletsVisibility,
                    label = { Text(if (showToilets) "🚻 Toalety: ON" else "🚻 Toalety: OFF") },
                    colors = chipColors,
                )
                FilterChip(
                    selected = showMiejscaChlodu,
                    onClick = onToggleMiejscaChloduVisibility,
                    label = { Text(if (showMiejscaChlodu) "❄️ Chłód: ON" else "❄️ Chłód: OFF") },
                    colors = chipColors,
                )
                FilterChip(
                    selected = is3dMode,
                    onClick = onToggle3dMode,
                    label = { Text(if (is3dMode) "3D: ON" else "3D: OFF") },
                    colors = chipColors,
                )
                FilterChip(
                    selected = showHeatmap,
                    onClick = onToggleHeatmap,
                    label = { Text(if (showHeatmap) "Heatmap: ON" else "Heatmap: OFF") },
                    colors = chipColors,
                )
                FilterChip(
                    selected = showStations,
                    onClick = onToggleStationsVisibility,
                    label = { Text(if (showStations) "Stacje: ON" else "Stacje: OFF") },
                    colors = chipColors,
                )
                FilterChip(
                    selected = filterOutliers,
                    onClick = onToggleFilterOutliers,
                    label = { Text(if (filterOutliers) "Filtr: ON" else "Filtr: OFF") },
                    colors = chipColors,
                )
            }
        }
    }
}
