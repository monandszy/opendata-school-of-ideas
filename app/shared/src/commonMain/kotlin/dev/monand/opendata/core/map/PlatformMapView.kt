package dev.monand.opendata.core.map

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.monand.opendata.data.model.LocationInspection
import dev.monand.opendata.data.model.PogodaStation

@Composable
expect fun PlatformMapView(
    stations: List<PogodaStation>,
    selectedStation: PogodaStation?,
    activeInspection: LocationInspection? = null,
    tileSource: MapTileSource,
    centerLat: Double,
    centerLon: Double,
    zoom: Double,
    pitch: Double,
    bearing: Double,
    is3dMode: Boolean,
    showHeatmap: Boolean,
    showStations: Boolean = true,
    minTemp: Double,
    maxTemp: Double,
    cameraCommandId: Int,
    onCameraMoved: (lat: Double, lon: Double, zoom: Double, pitch: Double, bearing: Double) -> Unit,
    onSelectStation: (PogodaStation?) -> Unit,
    onMapClicked: (lat: Double, lon: Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    overlays: @Composable BoxScope.() -> Unit = {},
)
