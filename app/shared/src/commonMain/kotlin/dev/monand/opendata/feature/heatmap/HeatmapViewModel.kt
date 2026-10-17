package dev.monand.opendata.feature.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.monand.opendata.core.geo.IdwInterpolator
import dev.monand.opendata.core.map.MapTileSource
import dev.monand.opendata.data.model.LocationInspection
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.data.repository.DefaultPogodaRepository
import dev.monand.opendata.data.repository.PogodaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeatmapViewModel(
    private val repository: PogodaRepository = DefaultPogodaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    init {
        refreshStations()
    }

    fun refreshStations() {
        if (_uiState.value.isLoadingStations) return
        _uiState.update { it.copy(isLoadingStations = true, errorMessage = null) }
        viewModelScope.launch {
            repository
                .fetchStations()
                .onSuccess { liveStations ->
                    val latestTimestamp = liveStations.mapNotNull { it.czas }.maxOrNull()
                    _uiState.update { current ->
                        val updatedSelected =
                            current.selectedStation?.let { selected ->
                                liveStations.find { it.lokalizacja == selected.lokalizacja }
                            }
                        current.copy(
                            rawStations = liveStations,
                            isLoadingStations = false,
                            errorMessage = null,
                            lastUpdated = latestTimestamp,
                            selectedStation = updatedSelected,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoadingStations = false,
                            errorMessage = error.message ?: "Nie udało się pobrać danych z API Warszawy",
                        )
                    }
                }
        }
    }

    fun onToggleFilterOutliers() {
        _uiState.update { current ->
            current.copy(filterOutliers = !current.filterOutliers)
        }
    }

    fun onDismissError() {
        _uiState.update { current ->
            current.copy(errorMessage = null)
        }
    }

    fun onCameraMoved(
        lat: Double,
        lon: Double,
        zoom: Double,
        pitch: Double,
        bearing: Double,
    ) {
        _uiState.update { current ->
            current.copy(
                centerLat = lat.coerceIn(HeatmapUiState.MIN_PAN_LAT, HeatmapUiState.MAX_PAN_LAT),
                centerLon = lon.coerceIn(HeatmapUiState.MIN_PAN_LON, HeatmapUiState.MAX_PAN_LON),
                zoom = zoom.coerceIn(HeatmapUiState.MIN_ZOOM, HeatmapUiState.MAX_ZOOM),
                pitch = pitch.coerceIn(0.0, 60.0),
                bearing = bearing,
            )
        }
    }

    fun onZoomIn() {
        _uiState.update { current ->
            current.copy(
                zoom = (current.zoom + 1.0).coerceAtMost(HeatmapUiState.MAX_ZOOM),
                cameraCommandId = current.cameraCommandId + 1,
            )
        }
    }

    fun onZoomOut() {
        _uiState.update { current ->
            current.copy(
                zoom = (current.zoom - 1.0).coerceAtLeast(HeatmapUiState.MIN_ZOOM),
                cameraCommandId = current.cameraCommandId + 1,
            )
        }
    }

    fun onResetView() {
        _uiState.update { current ->
            current.copy(
                centerLat = HeatmapUiState.WARSAW_CENTER_LAT,
                centerLon = HeatmapUiState.WARSAW_CENTER_LON,
                zoom = if (current.is3dMode) HeatmapUiState.MIN_3D_ZOOM else HeatmapUiState.DEFAULT_ZOOM,
                pitch = if (current.is3dMode) HeatmapUiState.DEFAULT_3D_PITCH else 0.0,
                bearing = if (current.is3dMode) HeatmapUiState.DEFAULT_3D_BEARING else 0.0,
                cameraCommandId = current.cameraCommandId + 1,
            )
        }
    }

    fun onToggle3dMode() {
        _uiState.update { current ->
            val next3d = !current.is3dMode
            current.copy(
                is3dMode = next3d,
                zoom = if (next3d) maxOf(current.zoom, HeatmapUiState.MIN_3D_ZOOM) else current.zoom,
                pitch = if (next3d) HeatmapUiState.DEFAULT_3D_PITCH else 0.0,
                bearing = if (next3d) HeatmapUiState.DEFAULT_3D_BEARING else 0.0,
                cameraCommandId = current.cameraCommandId + 1,
            )
        }
    }

    fun onToggleHeatmap() {
        _uiState.update { current ->
            current.copy(showHeatmap = !current.showHeatmap)
        }
    }

    fun onToggleStationsVisibility() {
        _uiState.update { current ->
            current.copy(showStations = !current.showStations)
        }
    }

    fun onSelectTileSource(source: MapTileSource) {
        _uiState.update { current ->
            current.copy(tileSource = source)
        }
    }

    fun onSelectStation(station: PogodaStation?) {
        _uiState.update { current ->
            current.copy(
                selectedStation = station,
                activeInspection = if (station != null) null else current.activeInspection,
            )
        }
    }

    fun onMapClicked(lat: Double, lon: Double) {
        val currentStations = _uiState.value.stations
        if (currentStations.isEmpty()) return
        val idwRes = IdwInterpolator.interpolate(lat, lon, currentStations)
        val nearest = currentStations.minByOrNull { s ->
            val dy = (lat - s.lat) * 111.2
            val dx = (lon - s.lon) * 68.1
            dy * dy + dx * dx
        }
        val inspection = LocationInspection(
            lat = lat,
            lon = lon,
            estimatedTemp = idwRes.temp,
            minDistanceKm = idwRes.minDistanceKm,
            nearestStationName = nearest?.lokalizacja ?: "Nieznana stacja",
        )
        _uiState.update { current ->
            current.copy(
                activeInspection = inspection,
                selectedStation = null,
            )
        }
    }

    fun onClearInspection() {
        _uiState.update { current ->
            current.copy(activeInspection = null)
        }
    }
}
