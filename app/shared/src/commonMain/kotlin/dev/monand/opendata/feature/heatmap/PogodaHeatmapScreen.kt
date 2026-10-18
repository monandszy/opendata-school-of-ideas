package dev.monand.opendata.feature.heatmap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.monand.opendata.api.model.MiejsceChloduItem
import dev.monand.opendata.api.model.ToaletaFeature
import dev.monand.opendata.core.map.MapTileSource
import dev.monand.opendata.core.map.PlatformMapView
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.feature.heatmap.components.ApiErrorDialog
import dev.monand.opendata.feature.heatmap.components.HeatmapTopBar
import dev.monand.opendata.feature.heatmap.components.LocationInspectionCard
import dev.monand.opendata.feature.heatmap.components.MapAttributionBadge
import dev.monand.opendata.feature.heatmap.components.MiejsceChloduDetailCard
import dev.monand.opendata.feature.heatmap.components.StationDetailCard
import dev.monand.opendata.feature.heatmap.components.ToaletaDetailCard

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
        onToggleStationsVisibility = viewModel::onToggleStationsVisibility,
        onToggleToiletsVisibility = viewModel::onToggleToiletsVisibility,
        onToggleMiejscaChloduVisibility = viewModel::onToggleMiejscaChloduVisibility,
        onToggleFilterOutliers = viewModel::onToggleFilterOutliers,
        onSelectTileSource = viewModel::onSelectTileSource,
        onSelectStation = viewModel::onSelectStation,
        onSelectToaleta = viewModel::onSelectToaleta,
        onSelectMiejsceChlodu = viewModel::onSelectMiejsceChlodu,
        onMapClicked = viewModel::onMapClicked,
        onClearInspection = viewModel::onClearInspection,
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
    onToggleStationsVisibility: () -> Unit,
    onToggleToiletsVisibility: () -> Unit,
    onToggleMiejscaChloduVisibility: () -> Unit,
    onToggleFilterOutliers: () -> Unit,
    onSelectTileSource: (MapTileSource) -> Unit,
    onSelectStation: (PogodaStation?) -> Unit,
    onSelectToaleta: (ToaletaFeature?) -> Unit,
    onSelectMiejsceChlodu: (MiejsceChloduItem?) -> Unit,
    onMapClicked: (Double, Double) -> Unit,
    onClearInspection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.errorMessage != null) {
        ApiErrorDialog(
            errorMessage = uiState.errorMessage,
            onRetry = onRefreshStations,
            onDismiss = onDismissError,
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
            HeatmapTopBar(
                minTemp = uiState.measuredMinTemp,
                maxTemp = uiState.measuredMaxTemp,
                stationCount = uiState.stations.size,
                isLoading = uiState.isLoadingStations,
                lastUpdated = uiState.lastUpdated,
                tileSource = uiState.tileSource,
                showHeatmap = uiState.showHeatmap,
                showStations = uiState.showStations,
                showToilets = uiState.showToilets,
                showMiejscaChlodu = uiState.showMiejscaChlodu,
                filterOutliers = uiState.filterOutliers,
                is3dMode = uiState.is3dMode,
                onSelectTileSource = onSelectTileSource,
                onToggleHeatmap = onToggleHeatmap,
                onToggleStationsVisibility = onToggleStationsVisibility,
                onToggleToiletsVisibility = onToggleToiletsVisibility,
                onToggleMiejscaChloduVisibility = onToggleMiejscaChloduVisibility,
                onToggleFilterOutliers = onToggleFilterOutliers,
                onToggle3dMode = onToggle3dMode,
                onRefreshStations = onRefreshStations,
            )

            PlatformMapView(
                stations = uiState.stations,
                selectedStation = uiState.selectedStation,
                toilets = uiState.toilets,
                selectedToaleta = uiState.selectedToaleta,
                miejscaChlodu = uiState.miejscaChlodu,
                selectedMiejsceChlodu = uiState.selectedMiejsceChlodu,
                activeInspection = uiState.activeInspection,
                tileSource = uiState.tileSource,
                centerLat = uiState.centerLat,
                centerLon = uiState.centerLon,
                zoom = uiState.zoom,
                pitch = uiState.pitch,
                bearing = uiState.bearing,
                is3dMode = uiState.is3dMode,
                showHeatmap = uiState.showHeatmap,
                showStations = uiState.showStations,
                showToilets = uiState.showToilets,
                showMiejscaChlodu = uiState.showMiejscaChlodu,
                minTemp = uiState.minTemp,
                maxTemp = uiState.maxTemp,
                cameraCommandId = uiState.cameraCommandId,
                onCameraMoved = onCameraMoved,
                onSelectStation = onSelectStation,
                onSelectToaleta = onSelectToaleta,
                onSelectMiejsceChlodu = onSelectMiejsceChlodu,
                onMapClicked = onMapClicked,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                val station = uiState.selectedStation
                val toilet = uiState.selectedToaleta
                val coolingPlace = uiState.selectedMiejsceChlodu
                val inspection = uiState.activeInspection

                when {
                    station != null -> {
                        StationDetailCard(
                            station = station,
                            onClose = { onSelectStation(null) },
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                    }
                    toilet != null -> {
                        ToaletaDetailCard(
                            toaleta = toilet,
                            onClose = { onSelectToaleta(null) },
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                    }
                    coolingPlace != null -> {
                        MiejsceChloduDetailCard(
                            place = coolingPlace,
                            onClose = { onSelectMiejsceChlodu(null) },
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                    }
                    inspection != null -> {
                        LocationInspectionCard(
                            inspection = inspection,
                            minTemp = uiState.minTemp,
                            maxTemp = uiState.maxTemp,
                            onClose = onClearInspection,
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                    }
                }

                MapAttributionBadge(
                    attribution = uiState.tileSource.attribution,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }
}
