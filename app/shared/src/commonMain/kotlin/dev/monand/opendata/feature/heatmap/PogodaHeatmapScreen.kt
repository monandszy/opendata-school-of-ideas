package dev.monand.opendata.feature.heatmap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.monand.opendata.core.map.MapTileSource
import dev.monand.opendata.core.map.PlatformMapView
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.feature.heatmap.components.ApiErrorDialog
import dev.monand.opendata.feature.heatmap.components.HeatmapTopBar
import dev.monand.opendata.feature.heatmap.components.MapAttributionBadge
import dev.monand.opendata.feature.heatmap.components.MapZoomControls
import dev.monand.opendata.feature.heatmap.components.StationDetailCard

@Composable
fun PogodaHeatmapScreen(viewModel: HeatmapViewModel = viewModel { HeatmapViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    PogodaHeatmapContent(
        uiState = uiState,
        onRefreshStations = viewModel::refreshStations,
        onDismissError = viewModel::onDismissError,
        onCameraMoved = viewModel::onCameraMoved,
        onZoomIn = viewModel::onZoomIn,
        onZoomOut = viewModel::onZoomOut,
        onResetView = viewModel::onResetView,
        onToggle3dMode = viewModel::onToggle3dMode,
        onToggleHeatmap = viewModel::onToggleHeatmap,
        onSelectTileSource = viewModel::onSelectTileSource,
        onSelectStation = viewModel::onSelectStation,
    )
}

@Composable
fun PogodaHeatmapContent(
    uiState: HeatmapUiState,
    onRefreshStations: () -> Unit,
    onDismissError: () -> Unit,
    onCameraMoved: (Double, Double, Double, Double, Double) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    onToggle3dMode: () -> Unit,
    onToggleHeatmap: () -> Unit,
    onSelectTileSource: (MapTileSource) -> Unit,
    onSelectStation: (PogodaStation?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.errorMessage != null) {
        ApiErrorDialog(
            errorMessage = uiState.errorMessage,
            onRetry = onRefreshStations,
            onDismiss = onDismissError,
        )
    }

    Column(modifier = modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
        HeatmapTopBar(
            minTemp = uiState.minTemp,
            maxTemp = uiState.maxTemp,
            stationCount = uiState.stations.size,
            isLoading = uiState.isLoadingStations,
            lastUpdated = uiState.lastUpdated,
            tileSource = uiState.tileSource,
            showHeatmap = uiState.showHeatmap,
            is3dMode = uiState.is3dMode,
            onSelectTileSource = onSelectTileSource,
            onToggleHeatmap = onToggleHeatmap,
            onToggle3dMode = onToggle3dMode,
            onRefreshStations = onRefreshStations,
        )

        PlatformMapView(
            stations = uiState.stations,
            selectedStation = uiState.selectedStation,
            tileSource = uiState.tileSource,
            centerLat = uiState.centerLat,
            centerLon = uiState.centerLon,
            zoom = uiState.zoom,
            pitch = uiState.pitch,
            bearing = uiState.bearing,
            is3dMode = uiState.is3dMode,
            showHeatmap = uiState.showHeatmap,
            minTemp = uiState.minTemp,
            maxTemp = uiState.maxTemp,
            cameraCommandId = uiState.cameraCommandId,
            onCameraMoved = onCameraMoved,
            onSelectStation = onSelectStation,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            MapZoomControls(
                zoom = uiState.zoom,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onResetView = onResetView,
                modifier = Modifier.align(Alignment.TopEnd),
            )

            val station = uiState.selectedStation
            if (station != null) {
                StationDetailCard(
                    station = station,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }

            MapAttributionBadge(
                attribution = uiState.tileSource.attribution,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}
