package dev.monand.opendata.feature.heatmap

import dev.monand.opendata.core.map.MapTileSource
import dev.monand.opendata.data.model.PogodaStation

data class HeatmapUiState(
    val stations: List<PogodaStation> = emptyList(),
    val isLoadingStations: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: String? = null,
    val centerLat: Double = WARSAW_CENTER_LAT,
    val centerLon: Double = WARSAW_CENTER_LON,
    val zoom: Double = DEFAULT_ZOOM,
    val pitch: Double = 0.0,
    val bearing: Double = 0.0,
    val is3dMode: Boolean = false,
    val cameraCommandId: Int = 0,
    val showHeatmap: Boolean = true,
    val tileSource: MapTileSource = MapTileSource.OpenFreeMap3D,
    val selectedStation: PogodaStation? = null,
) {
    val minTemp: Double
        get() = stations.minOfOrNull { it.tempPow } ?: 0.0

    val maxTemp: Double
        get() = stations.maxOfOrNull { it.tempPow } ?: 1.0

    companion object {
        const val WARSAW_CENTER_LAT = 52.2297
        const val WARSAW_CENTER_LON = 21.0122
        const val DEFAULT_ZOOM = 11.5
        const val MIN_ZOOM = 9.0
        const val MAX_ZOOM = 18.0
        const val DEFAULT_3D_PITCH = 52.0
        const val DEFAULT_3D_BEARING = -15.0
        const val MIN_3D_ZOOM = 14.5
        const val MIN_PAN_LAT = 51.8
        const val MAX_PAN_LAT = 52.6
        const val MIN_PAN_LON = 20.4
        const val MAX_PAN_LON = 21.6
    }
}
