package dev.monand.opendata.core.map

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.monand.opendata.api.model.MiejsceChloduItem
import dev.monand.opendata.api.model.ToaletaFeature
import dev.monand.opendata.data.model.LocationInspection
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.feature.heatmap.HeatmapUiState
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngQuad
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.coalesce
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.toColor
import org.maplibre.android.style.expressions.Expression.toNumber
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circlePitchAlignment
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillExtrusionBase
import org.maplibre.android.style.layers.PropertyFactory.fillExtrusionColor
import org.maplibre.android.style.layers.PropertyFactory.fillExtrusionHeight
import org.maplibre.android.style.layers.PropertyFactory.fillExtrusionOpacity
import org.maplibre.android.style.layers.PropertyFactory.rasterOpacity
import org.maplibre.android.style.layers.PropertyFactory.rasterResampling
import org.maplibre.android.style.layers.PropertyFactory.visibility
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.ImageSource

private const val IDW_SOURCE_ID = "idw-heatmap-source"
private const val IDW_LAYER_ID = "idw-heatmap-layer"
private const val BUILDINGS_3D_LAYER_ID = "warsaw-3d-buildings-layer"
private const val STATIONS_SOURCE_ID = "stations-source"
private const val STATIONS_LAYER_ID = "stations-circle-layer"
private const val TOILETS_SOURCE_ID = "toalety-source"
private const val TOILETS_LAYER_ID = "toalety-circle-layer"
private const val MIEJSCA_CHLODU_SOURCE_ID = "miejsca-chlodu-source"
private const val MIEJSCA_CHLODU_LAYER_ID = "miejsca-chlodu-circle-layer"
private const val INSPECTION_SOURCE_ID = "inspection-source"
private const val INSPECTION_LAYER_ID = "inspection-circle-layer"

@Composable
actual fun PlatformMapView(
    stations: List<PogodaStation>,
    selectedStation: PogodaStation?,
    toilets: List<ToaletaFeature>,
    selectedToaleta: ToaletaFeature?,
    miejscaChlodu: List<MiejsceChloduItem>,
    selectedMiejsceChlodu: MiejsceChloduItem?,
    activeInspection: LocationInspection?,
    tileSource: MapTileSource,
    centerLat: Double,
    centerLon: Double,
    zoom: Double,
    pitch: Double,
    bearing: Double,
    is3dMode: Boolean,
    showHeatmap: Boolean,
    showStations: Boolean,
    showToilets: Boolean,
    showMiejscaChlodu: Boolean,
    minTemp: Double,
    maxTemp: Double,
    cameraCommandId: Int,
    onCameraMoved: (lat: Double, lon: Double, zoom: Double, pitch: Double, bearing: Double) -> Unit,
    onSelectStation: (PogodaStation?) -> Unit,
    onSelectToaleta: (ToaletaFeature?) -> Unit,
    onSelectMiejsceChlodu: (MiejsceChloduItem?) -> Unit,
    onMapClicked: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier,
    overlays: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember(context) {
        MapLibre.getInstance(context)
    }

    val currentStations by rememberUpdatedState(stations)
    val currentToilets by rememberUpdatedState(toilets)
    val currentMiejscaChlodu by rememberUpdatedState(miejscaChlodu)
    val currentOnSelectStation by rememberUpdatedState(onSelectStation)
    val currentOnSelectToaleta by rememberUpdatedState(onSelectToaleta)
    val currentOnSelectMiejsceChlodu by rememberUpdatedState(onSelectMiejsceChlodu)
    val currentOnMapClicked by rememberUpdatedState(onMapClicked)
    val currentOnCameraMoved by rememberUpdatedState(onCameraMoved)

    val mapView =
        remember(context) {
            MapView(context).apply {
                onCreate(null)
            }
        }

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var loadedStyleUrl by remember { mutableStateOf<String?>(null) }
    var styleReadyCounter by remember { mutableStateOf(0) }

    val idwBitmap =
        remember(stations, minTemp, maxTemp, zoom) {
            val texture = MapOverlayGeoJson.generateIdwTexture(stations, minTemp, maxTemp, zoom = zoom)
            textureToBitmap(texture)
        }

    val stationsGeoJson =
        remember(stations, selectedStation, minTemp, maxTemp) {
            MapOverlayGeoJson.buildStationsGeoJson(stations, selectedStation, minTemp, maxTemp)
        }

    val toaletyGeoJson =
        remember(toilets, selectedToaleta) {
            MapOverlayGeoJson.buildToaletyGeoJson(toilets, selectedToaleta?.id)
        }

    val miejscaChloduGeoJson =
        remember(miejscaChlodu, selectedMiejsceChlodu) {
            MapOverlayGeoJson.buildMiejscaChloduGeoJson(miejscaChlodu, selectedMiejsceChlodu?.id)
        }

    val inspectionGeoJson =
        remember(activeInspection, minTemp, maxTemp) {
            MapOverlayGeoJson.buildInspectionGeoJson(activeInspection, minTemp, maxTemp)
        }

    LaunchedEffect(mapInstance, tileSource) {
        val map = mapInstance ?: return@LaunchedEffect
        if (loadedStyleUrl != tileSource.styleUrl) {
            loadedStyleUrl = tileSource.styleUrl
            map.setStyle(Style.Builder().fromUri(tileSource.styleUrl)) { style ->
                setupOverlayLayers(
                    style = style,
                    idwBitmap = idwBitmap,
                    stationsGeoJson = stationsGeoJson,
                    toaletyGeoJson = toaletyGeoJson,
                    miejscaChloduGeoJson = miejscaChloduGeoJson,
                    inspectionGeoJson = inspectionGeoJson,
                    showHeatmap = showHeatmap,
                    showStations = showStations,
                    showToilets = showToilets,
                    showMiejscaChlodu = showMiejscaChlodu,
                    is3dMode = is3dMode,
                )
                styleReadyCounter++
            }
        }
    }

    LaunchedEffect(
        styleReadyCounter, idwBitmap, stationsGeoJson, toaletyGeoJson, miejscaChloduGeoJson,
        inspectionGeoJson, showHeatmap, showStations, showToilets, showMiejscaChlodu, is3dMode
    ) {
        val style = mapInstance?.style ?: return@LaunchedEffect
        if (!style.isFullyLoaded) return@LaunchedEffect
        updateOverlayLayers(
            style = style,
            idwBitmap = idwBitmap,
            stationsGeoJson = stationsGeoJson,
            toaletyGeoJson = toaletyGeoJson,
            miejscaChloduGeoJson = miejscaChloduGeoJson,
            inspectionGeoJson = inspectionGeoJson,
            showHeatmap = showHeatmap,
            showStations = showStations,
            showToilets = showToilets,
            showMiejscaChlodu = showMiejscaChlodu,
            is3dMode = is3dMode,
        )
    }

    LaunchedEffect(mapInstance, cameraCommandId) {
        val map = mapInstance ?: return@LaunchedEffect
        if (cameraCommandId > 0) {
            val position =
                CameraPosition
                    .Builder()
                    .target(LatLng(centerLat, centerLon))
                    .zoom(zoom)
                    .tilt(pitch)
                    .bearing(bearing)
                    .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 650)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        map.uiSettings.isAttributionEnabled = false
                        map.uiSettings.isLogoEnabled = false
                        map.uiSettings.isTiltGesturesEnabled = true
                        map.uiSettings.isRotateGesturesEnabled = true
                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true

                        map.setLatLngBoundsForCameraTarget(
                            org.maplibre.android.geometry.LatLngBounds.from(
                                HeatmapUiState.MAX_PAN_LAT,
                                HeatmapUiState.MAX_PAN_LON,
                                HeatmapUiState.MIN_PAN_LAT,
                                HeatmapUiState.MIN_PAN_LON,
                            ),
                        )
                        map.setMinZoomPreference(HeatmapUiState.MIN_ZOOM)
                        map.setMaxZoomPreference(HeatmapUiState.MAX_ZOOM)

                        val initialPos =
                            CameraPosition
                                .Builder()
                                .target(LatLng(centerLat, centerLon))
                                .zoom(zoom)
                                .tilt(pitch)
                                .bearing(bearing)
                                .build()
                        map.cameraPosition = initialPos

                        map.addOnCameraIdleListener {
                            val pos = map.cameraPosition
                            val target = pos.target
                            if (target != null) {
                                currentOnCameraMoved(
                                    target.latitude,
                                    target.longitude,
                                    pos.zoom,
                                    pos.tilt,
                                    pos.bearing,
                                )
                            }
                        }

                        map.addOnMapClickListener { latLng ->
                            val screenPoint = map.projection.toScreenLocation(latLng)
                            val hitRect =
                                RectF(
                                    screenPoint.x - 24f,
                                    screenPoint.y - 24f,
                                    screenPoint.x + 24f,
                                    screenPoint.y + 24f,
                                )

                            val stationFeatures = map.queryRenderedFeatures(hitRect, STATIONS_LAYER_ID)
                            val clickedStationName = stationFeatures.firstOrNull()?.getStringProperty("lokalizacja")

                            val toiletFeatures = map.queryRenderedFeatures(hitRect, TOILETS_LAYER_ID)
                            val clickedToiletId = toiletFeatures.firstOrNull()?.getStringProperty("id")

                            val coolingFeatures = map.queryRenderedFeatures(hitRect, MIEJSCA_CHLODU_LAYER_ID)
                            val clickedCoolingId = coolingFeatures.firstOrNull()?.getStringProperty("id")

                            when {
                                clickedStationName != null -> {
                                    val matched = currentStations.find { it.lokalizacja == clickedStationName }
                                    currentOnSelectStation(matched)
                                }
                                clickedToiletId != null -> {
                                    val matched = currentToilets.find { it.id == clickedToiletId }
                                    currentOnSelectToaleta(matched)
                                }
                                clickedCoolingId != null -> {
                                    val matched = currentMiejscaChlodu.find { it.id == clickedCoolingId }
                                    currentOnSelectMiejsceChlodu(matched)
                                }
                                else -> {
                                    currentOnMapClicked(latLng.latitude, latLng.longitude)
                                }
                            }
                            true
                        }

                        mapInstance = map
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        overlays()
    }
}

private fun setupOverlayLayers(
    style: Style,
    idwBitmap: Bitmap,
    stationsGeoJson: String,
    toaletyGeoJson: String,
    miejscaChloduGeoJson: String,
    inspectionGeoJson: String,
    showHeatmap: Boolean,
    showStations: Boolean,
    showToilets: Boolean,
    showMiejscaChlodu: Boolean,
    is3dMode: Boolean,
) {
    val quad =
        LatLngQuad(
            LatLng(IdwHeatmapTexture.DEFAULT_NORTH_LAT, IdwHeatmapTexture.DEFAULT_WEST_LON),
            LatLng(IdwHeatmapTexture.DEFAULT_NORTH_LAT, IdwHeatmapTexture.DEFAULT_EAST_LON),
            LatLng(IdwHeatmapTexture.DEFAULT_SOUTH_LAT, IdwHeatmapTexture.DEFAULT_EAST_LON),
            LatLng(IdwHeatmapTexture.DEFAULT_SOUTH_LAT, IdwHeatmapTexture.DEFAULT_WEST_LON),
        )

    if (style.getSource(IDW_SOURCE_ID) == null) {
        style.addSource(ImageSource(IDW_SOURCE_ID, quad, idwBitmap))
    }

    if (style.getLayer(IDW_LAYER_ID) == null) {
        val rasterLayer =
            org.maplibre.android.style.layers
                .RasterLayer(IDW_LAYER_ID, IDW_SOURCE_ID)
                .withProperties(
                    rasterOpacity(0.55f),
                    rasterResampling(Property.RASTER_RESAMPLING_LINEAR),
                    visibility(if (showHeatmap) Property.VISIBLE else Property.NONE),
                )
        style.addLayer(rasterLayer)
    }

    val vectorSource = style.sources.firstOrNull { it is org.maplibre.android.style.sources.VectorSource }
    if (vectorSource != null && style.getLayer(BUILDINGS_3D_LAYER_ID) == null) {
        val buildingColor = if (showHeatmap) Color.WHITE else Color.parseColor("#363C4A")
        val buildingOpacity = if (showHeatmap) 0.60f else 0.85f

        val extrusionLayer =
            FillExtrusionLayer(BUILDINGS_3D_LAYER_ID, vectorSource.id)
                .withSourceLayer("building")
                .withProperties(
                    fillExtrusionColor(buildingColor),
                    fillExtrusionHeight(coalesce(toNumber(get("render_height")), toNumber(get("height")), literal(12.0f))),
                    fillExtrusionBase(coalesce(toNumber(get("render_min_height")), toNumber(get("min_height")), literal(0.0f))),
                    fillExtrusionOpacity(buildingOpacity),
                    visibility(if (is3dMode) Property.VISIBLE else Property.NONE),
                ).apply {
                    minZoom = 12.0f
                }
        style.addLayer(extrusionLayer)
    }

    if (style.getSource(STATIONS_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(STATIONS_SOURCE_ID, stationsGeoJson))
    }

    if (style.getLayer(STATIONS_LAYER_ID) == null) {
        val circleLayer =
            CircleLayer(STATIONS_LAYER_ID, STATIONS_SOURCE_ID)
                .withProperties(
                    circleColor(toColor(get("color"))),
                    circleRadius(toNumber(get("radius"))),
                    circleStrokeColor(Color.WHITE),
                    circleStrokeWidth(toNumber(get("strokeWidth"))),
                    circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                    visibility(if (showStations) Property.VISIBLE else Property.NONE),
                )
        style.addLayer(circleLayer)
    }

    if (style.getSource(TOILETS_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(TOILETS_SOURCE_ID, toaletyGeoJson))
    }

    if (style.getLayer(TOILETS_LAYER_ID) == null) {
        val circleLayer =
            CircleLayer(TOILETS_LAYER_ID, TOILETS_SOURCE_ID)
                .withProperties(
                    circleColor(toColor(get("color"))),
                    circleRadius(toNumber(get("radius"))),
                    circleStrokeColor(Color.WHITE),
                    circleStrokeWidth(toNumber(get("strokeWidth"))),
                    circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                    visibility(if (showToilets) Property.VISIBLE else Property.NONE),
                )
        style.addLayer(circleLayer)
    }

    if (style.getSource(MIEJSCA_CHLODU_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(MIEJSCA_CHLODU_SOURCE_ID, miejscaChloduGeoJson))
    }

    if (style.getLayer(MIEJSCA_CHLODU_LAYER_ID) == null) {
        val circleLayer =
            CircleLayer(MIEJSCA_CHLODU_LAYER_ID, MIEJSCA_CHLODU_SOURCE_ID)
                .withProperties(
                    circleColor(toColor(get("color"))),
                    circleRadius(toNumber(get("radius"))),
                    circleStrokeColor(Color.WHITE),
                    circleStrokeWidth(toNumber(get("strokeWidth"))),
                    circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                    visibility(if (showMiejscaChlodu) Property.VISIBLE else Property.NONE),
                )
        style.addLayer(circleLayer)
    }

    if (style.getSource(INSPECTION_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(INSPECTION_SOURCE_ID, inspectionGeoJson))
    }

    if (style.getLayer(INSPECTION_LAYER_ID) == null) {
        val inspectionLayer =
            CircleLayer(INSPECTION_LAYER_ID, INSPECTION_SOURCE_ID)
                .withProperties(
                    circleColor(toColor(get("color"))),
                    circleRadius(toNumber(get("radius"))),
                    circleStrokeColor(Color.WHITE),
                    circleStrokeWidth(toNumber(get("strokeWidth"))),
                    circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                )
        style.addLayer(inspectionLayer)
    }
}

private fun updateOverlayLayers(
    style: Style,
    idwBitmap: Bitmap,
    stationsGeoJson: String,
    toaletyGeoJson: String,
    miejscaChloduGeoJson: String,
    inspectionGeoJson: String,
    showHeatmap: Boolean,
    showStations: Boolean,
    showToilets: Boolean,
    showMiejscaChlodu: Boolean,
    is3dMode: Boolean,
) {
    (style.getSourceAs<ImageSource>(IDW_SOURCE_ID))?.setImage(idwBitmap)
    style.getLayer(IDW_LAYER_ID)?.setProperties(
        visibility(if (showHeatmap) Property.VISIBLE else Property.NONE),
    )

    val buildingColor = if (showHeatmap) Color.WHITE else Color.parseColor("#363C4A")
    val buildingOpacity = if (showHeatmap) 0.60f else 0.85f

    style.getLayer(BUILDINGS_3D_LAYER_ID)?.setProperties(
        visibility(if (is3dMode) Property.VISIBLE else Property.NONE),
        fillExtrusionColor(buildingColor),
        fillExtrusionOpacity(buildingOpacity),
    )
    style.getLayer("building-3d")?.setProperties(
        visibility(if (is3dMode) Property.VISIBLE else Property.NONE),
        fillExtrusionColor(buildingColor),
        fillExtrusionOpacity(buildingOpacity),
    )

    (style.getSourceAs<GeoJsonSource>(STATIONS_SOURCE_ID))?.setGeoJson(stationsGeoJson)
    style.getLayer(STATIONS_LAYER_ID)?.setProperties(
        visibility(if (showStations) Property.VISIBLE else Property.NONE),
    )

    (style.getSourceAs<GeoJsonSource>(TOILETS_SOURCE_ID))?.setGeoJson(toaletyGeoJson)
    style.getLayer(TOILETS_LAYER_ID)?.setProperties(
        visibility(if (showToilets) Property.VISIBLE else Property.NONE),
    )

    (style.getSourceAs<GeoJsonSource>(MIEJSCA_CHLODU_SOURCE_ID))?.setGeoJson(miejscaChloduGeoJson)
    style.getLayer(MIEJSCA_CHLODU_LAYER_ID)?.setProperties(
        visibility(if (showMiejscaChlodu) Property.VISIBLE else Property.NONE),
    )

    (style.getSourceAs<GeoJsonSource>(INSPECTION_SOURCE_ID))?.setGeoJson(inspectionGeoJson)
}

private fun textureToBitmap(texture: IdwHeatmapTexture): Bitmap {
    val pixels = IntArray(texture.width * texture.height)
    val bytes = texture.rgbaBytes
    var bIdx = 0
    for (i in pixels.indices) {
        val r = bytes[bIdx++].toInt() and 0xFF
        val g = bytes[bIdx++].toInt() and 0xFF
        val b = bytes[bIdx++].toInt() and 0xFF
        val a = bytes[bIdx++].toInt() and 0xFF
        pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    return Bitmap.createBitmap(pixels, texture.width, texture.height, Bitmap.Config.ARGB_8888)
}
