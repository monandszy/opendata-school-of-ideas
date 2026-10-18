package dev.monand.opendata.feature.heatmap

import dev.monand.opendata.api.model.MiejsceChloduItem
import dev.monand.opendata.api.model.MiejsceChloduSeed
import dev.monand.opendata.api.model.ToaletaFeature
import dev.monand.opendata.core.map.MapTileSource
import dev.monand.opendata.data.model.LocationInspection
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.data.repository.DefaultPogodaRepository

data class HeatmapUiState(
    val rawStations: List<PogodaStation> = emptyList(),
    val filterOutliers: Boolean = false,
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
    val showStations: Boolean = true,
    val showToilets: Boolean = true,
    val showMiejscaChlodu: Boolean = true,
    val toilets: List<ToaletaFeature> = emptyList(),
    val miejscaChlodu: List<MiejsceChloduItem> = MiejsceChloduSeed.items,
    val tileSource: MapTileSource = MapTileSource.OpenFreeMap3D,
    val selectedStation: PogodaStation? = null,
    val selectedToaleta: ToaletaFeature? = null,
    val selectedMiejsceChlodu: MiejsceChloduItem? = null,
    val activeInspection: LocationInspection? = null,
) {
    val stations: List<PogodaStation>
        get() =
            if (filterOutliers) {
                DefaultPogodaRepository.filterTemperatureOutliers(rawStations)
            } else {
                rawStations
            }

    // Constant color scale boundaries (0°C = Blue, 30°C = Red)
    val minTemp: Double
        get() = CONSTANT_MIN_TEMP

    val maxTemp: Double
        get() = CONSTANT_MAX_TEMP

    val measuredMinTemp: Double
        get() = stations.minOfOrNull { it.tempPow } ?: 0.0

    val measuredMaxTemp: Double
        get() = stations.maxOfOrNull { it.tempPow } ?: 0.0

    companion object {
        const val CONSTANT_MIN_TEMP = 0.0
        const val CONSTANT_MAX_TEMP = 30.0
        const val WARSAW_CENTER_LAT = 52.2297
        const val WARSAW_CENTER_LON = 21.0122
        const val DEFAULT_ZOOM = 11.5
        const val MIN_ZOOM = 9.0
        const val MAX_ZOOM = 18.0
        const val DEFAULT_3D_PITCH = 52.0
        const val DEFAULT_3D_BEARING = -15.0
        const val MIN_3D_ZOOM = 14.5
        const val MIN_PAN_LAT = 51.90
        const val MAX_PAN_LAT = 52.55
        const val MIN_PAN_LON = 20.50
        const val MAX_PAN_LON = 21.50
    }
}
