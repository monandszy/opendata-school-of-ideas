@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class, kotlin.io.encoding.ExperimentalEncodingApi::class)

package dev.monand.opendata.core.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import dev.monand.opendata.api.model.MiejsceChloduItem
import dev.monand.opendata.api.model.ToaletaFeature
import dev.monand.opendata.data.model.LocationInspection
import dev.monand.opendata.data.model.PogodaStation
import kotlin.io.encoding.Base64

@JsFun(
    """(leftPx, topPx, widthPx, heightPx) => {
        if (window.warsawMapBridge) {
            window.warsawMapBridge.setViewport(leftPx, topPx, widthPx, heightPx);
        }
    }""",
)
private external fun jsSetViewport(
    leftPx: Double,
    topPx: Double,
    widthPx: Double,
    heightPx: Double,
)

@JsFun(
    """(styleUrl, lat, lon, zoom, pitch, bearing) => {
        if (window.warsawMapBridge) {
            window.warsawMapBridge.ensureMap(styleUrl, lat, lon, zoom, pitch, bearing);
        }
    }""",
)
private external fun jsEnsureMap(
    styleUrl: String,
    lat: Double,
    lon: Double,
    zoom: Double,
    pitch: Double,
    bearing: Double,
)

@JsFun(
    """(base64Rgba, width, height, north, south, west, east, showHeatmap, showStations, showToilets, showMiejscaChlodu, is3dMode, stationsGeoJsonStr, inspectionGeoJsonStr, toaletyGeoJsonStr, miejscaChloduGeoJsonStr) => {
        if (window.warsawMapBridge) {
            window.warsawMapBridge.updateOverlays(
                base64Rgba, width, height, north, south, west, east, showHeatmap, showStations, showToilets, showMiejscaChlodu, is3dMode, stationsGeoJsonStr, inspectionGeoJsonStr, toaletyGeoJsonStr, miejscaChloduGeoJsonStr
            );
        }
    }""",
)
private external fun jsUpdateOverlays(
    base64Rgba: String,
    width: Int,
    height: Int,
    north: Double,
    south: Double,
    west: Double,
    east: Double,
    showHeatmap: Boolean,
    showStations: Boolean,
    showToilets: Boolean,
    showMiejscaChlodu: Boolean,
    is3dMode: Boolean,
    stationsGeoJsonStr: String,
    inspectionGeoJsonStr: String,
    toaletyGeoJsonStr: String,
    miejscaChloduGeoJsonStr: String,
)

@JsFun(
    """(lat, lon, zoom, pitch, bearing) => {
        if (window.warsawMapBridge) {
            window.warsawMapBridge.animateCamera(lat, lon, zoom, pitch, bearing);
        }
    }""",
)
private external fun jsAnimateCamera(
    lat: Double,
    lon: Double,
    zoom: Double,
    pitch: Double,
    bearing: Double,
)

@JsFun(
    """(dxPhysical, dyPhysical) => {
        return window.warsawMapBridge ? window.warsawMapBridge.panBy(dxPhysical, dyPhysical) : '';
    }""",
)
private external fun jsPanBy(
    dxPhysical: Double,
    dyPhysical: Double,
): String

@JsFun(
    """(dxPhysical, dyPhysical) => {
        return window.warsawMapBridge ? window.warsawMapBridge.orbitBy(dxPhysical, dyPhysical) : '';
    }""",
)
private external fun jsOrbitBy(
    dxPhysical: Double,
    dyPhysical: Double,
): String

@JsFun(
    """(scrollDeltaY, cursorXPhysical, cursorYPhysical) => {
        return window.warsawMapBridge
            ? window.warsawMapBridge.zoomByScroll(scrollDeltaY, cursorXPhysical, cursorYPhysical)
            : '';
    }""",
)
private external fun jsZoomByScroll(
    scrollDeltaY: Double,
    cursorXPhysical: Double,
    cursorYPhysical: Double,
): String

@JsFun(
    """(xPhysical, yPhysical) => {
        return window.warsawMapBridge ? window.warsawMapBridge.queryStationAt(xPhysical, yPhysical) : '';
    }""",
)
private external fun jsQueryStationAt(
    xPhysical: Double,
    yPhysical: Double,
): String

@JsFun(
    """(xPhysical, yPhysical) => {
        return window.warsawMapBridge ? window.warsawMapBridge.queryToaletaAt(xPhysical, yPhysical) : '';
    }""",
)
private external fun jsQueryToaletaAt(
    xPhysical: Double,
    yPhysical: Double,
): String

@JsFun(
    """(xPhysical, yPhysical) => {
        return window.warsawMapBridge ? window.warsawMapBridge.queryMiejsceChloduAt(xPhysical, yPhysical) : '';
    }""",
)
private external fun jsQueryMiejsceChloduAt(
    xPhysical: Double,
    yPhysical: Double,
): String

@JsFun(
    """(xPhysical, yPhysical) => {
        return window.warsawMapBridge ? window.warsawMapBridge.unproject(xPhysical, yPhysical) : '';
    }""",
)
private external fun jsUnproject(
    xPhysical: Double,
    yPhysical: Double,
): String

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
    val currentStations by rememberUpdatedState(stations)
    val currentToilets by rememberUpdatedState(toilets)
    val currentMiejscaChlodu by rememberUpdatedState(miejscaChlodu)
    val currentOnCameraMoved by rememberUpdatedState(onCameraMoved)
    val currentOnSelectStation by rememberUpdatedState(onSelectStation)
    val currentOnSelectToaleta by rememberUpdatedState(onSelectToaleta)
    val currentOnSelectMiejsceChlodu by rememberUpdatedState(onSelectMiejsceChlodu)
    val currentOnMapClicked by rememberUpdatedState(onMapClicked)

    var lastAppliedCommandId by remember { mutableIntStateOf(cameraCommandId) }
    var isOrbitModifierDown by remember { mutableStateOf(false) }

    val idwTexture =
        remember(stations, minTemp, maxTemp) {
            MapOverlayGeoJson.generateIdwTexture(
                stations = stations,
                minTemp = minTemp,
                maxTemp = maxTemp,
            )
        }
    val base64Texture =
        remember(idwTexture, stations) {
            if (stations.isEmpty()) "" else Base64.Default.encode(idwTexture.rgbaBytes)
        }
    val stationsGeoJson =
        remember(stations, selectedStation, minTemp, maxTemp) {
            MapOverlayGeoJson.buildStationsGeoJson(
                stations = stations,
                selectedStation = selectedStation,
                minTemp = minTemp,
                maxTemp = maxTemp,
            )
        }
    val toaletyGeoJson =
        remember(toilets, selectedToaleta) {
            MapOverlayGeoJson.buildToaletyGeoJson(
                toilets = toilets,
                selectedToaletaId = selectedToaleta?.id,
            )
        }
    val miejscaChloduGeoJson =
        remember(miejscaChlodu, selectedMiejsceChlodu) {
            MapOverlayGeoJson.buildMiejscaChloduGeoJson(
                places = miejscaChlodu,
                selectedPlaceId = selectedMiejsceChlodu?.id,
            )
        }
    val inspectionGeoJson =
        remember(activeInspection, minTemp, maxTemp) {
            MapOverlayGeoJson.buildInspectionGeoJson(
                activeInspection = activeInspection,
                minTemp = minTemp,
                maxTemp = maxTemp,
            )
        }
    LaunchedEffect(tileSource.styleUrl) {
        jsEnsureMap(tileSource.styleUrl, centerLat, centerLon, zoom, pitch, bearing)
        jsUpdateOverlays(
            base64Rgba = base64Texture,
            width = idwTexture.width,
            height = idwTexture.height,
            north = idwTexture.northLat,
            south = idwTexture.southLat,
            west = idwTexture.westLon,
            east = idwTexture.eastLon,
            showHeatmap = showHeatmap,
            showStations = showStations,
            showToilets = showToilets,
            showMiejscaChlodu = showMiejscaChlodu,
            is3dMode = is3dMode,
            stationsGeoJsonStr = stationsGeoJson,
            inspectionGeoJsonStr = inspectionGeoJson,
            toaletyGeoJsonStr = toaletyGeoJson,
            miejscaChloduGeoJsonStr = miejscaChloduGeoJson,
        )
    }

    LaunchedEffect(base64Texture, showHeatmap, showStations, showToilets, showMiejscaChlodu, is3dMode, stationsGeoJson, inspectionGeoJson, toaletyGeoJson, miejscaChloduGeoJson) {
        jsUpdateOverlays(
            base64Rgba = base64Texture,
            width = idwTexture.width,
            height = idwTexture.height,
            north = idwTexture.northLat,
            south = idwTexture.southLat,
            west = idwTexture.westLon,
            east = idwTexture.eastLon,
            showHeatmap = showHeatmap,
            showStations = showStations,
            showToilets = showToilets,
            showMiejscaChlodu = showMiejscaChlodu,
            is3dMode = is3dMode,
            stationsGeoJsonStr = stationsGeoJson,
            inspectionGeoJsonStr = inspectionGeoJson,
            toaletyGeoJsonStr = toaletyGeoJson,
            miejscaChloduGeoJsonStr = miejscaChloduGeoJson,
        )
    }

    LaunchedEffect(cameraCommandId) {
        if (cameraCommandId != lastAppliedCommandId) {
            lastAppliedCommandId = cameraCommandId
            jsAnimateCamera(centerLat, centerLon, zoom, pitch, bearing)
        }
    }

    Box(
        modifier =
        modifier
            .clip(RoundedCornerShape(12.dp))
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInWindow()
                val size = coordinates.size
                jsSetViewport(
                    leftPx = pos.x.toDouble(),
                    topPx = pos.y.toDouble(),
                    widthPx = size.width.toDouble(),
                    heightPx = size.height.toDouble(),
                )
            },
    ) {
        Canvas(
            modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        var isDragging = false
                        var downPos = Offset.Zero
                        var lastPos = Offset.Zero
                        var isOrbitDrag = false

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: continue
                            val mods = event.keyboardModifiers
                            val buttons = event.buttons

                            when (event.type) {
                                PointerEventType.Press -> {
                                    isDragging = true
                                    downPos = change.position
                                    lastPos = change.position
                                    isOrbitDrag = buttons.isSecondaryPressed ||
                                        buttons.isTertiaryPressed ||
                                        mods.isShiftPressed ||
                                        mods.isCtrlPressed ||
                                        mods.isAltPressed
                                }

                                PointerEventType.Move -> {
                                    if (isDragging) {
                                        val currentPos = change.position
                                        val dragAmount = currentPos - lastPos
                                        lastPos = currentPos

                                        if (dragAmount.x != 0f || dragAmount.y != 0f) {
                                            change.consume()
                                            val stateStr = if (isOrbitDrag) {
                                                jsOrbitBy(dragAmount.x.toDouble(), dragAmount.y.toDouble())
                                            } else {
                                                jsPanBy(dragAmount.x.toDouble(), dragAmount.y.toDouble())
                                            }
                                            dispatchCameraState(stateStr, currentOnCameraMoved)
                                        }
                                    }
                                }

                                PointerEventType.Release -> {
                                    if (isDragging) {
                                        isDragging = false
                                        val totalDist = (change.position - downPos).getDistance()
                                        if (totalDist < 12f && !isOrbitDrag) {
                                            val posX = change.position.x.toDouble()
                                            val posY = change.position.y.toDouble()
                                            val stationName = jsQueryStationAt(posX, posY)
                                            val toiletIdStr = jsQueryToaletaAt(posX, posY)
                                            val coolingIdStr = jsQueryMiejsceChloduAt(posX, posY)

                                            if (stationName.isNotEmpty()) {
                                                val tapped = currentStations.find { it.lokalizacja == stationName }
                                                currentOnSelectStation(tapped)
                                            } else if (toiletIdStr.isNotEmpty()) {
                                                val tapped = currentToilets.find { it.id == toiletIdStr }
                                                currentOnSelectToaleta(tapped)
                                            } else if (coolingIdStr.isNotEmpty()) {
                                                val tapped = currentMiejscaChlodu.find { it.id == coolingIdStr }
                                                currentOnSelectMiejsceChlodu(tapped)
                                            } else {
                                                val unprojected = jsUnproject(posX, posY)
                                                if (unprojected.isNotEmpty()) {
                                                    val parts = unprojected.split(',')
                                                    if (parts.size == 2) {
                                                        val lat = parts[0].toDoubleOrNull()
                                                        val lon = parts[1].toDoubleOrNull()
                                                        if (lat != null && lon != null) {
                                                            currentOnMapClicked(lat, lon)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                PointerEventType.Scroll -> {
                                    val scrollY = change.scrollDelta.y.toDouble()
                                    if (scrollY != 0.0) {
                                        change.consume()
                                        val stateStr =
                                            jsZoomByScroll(
                                                scrollDeltaY = scrollY,
                                                cursorXPhysical = change.position.x.toDouble(),
                                                cursorYPhysical = change.position.y.toDouble(),
                                            )
                                        dispatchCameraState(stateStr, currentOnCameraMoved)
                                    }
                                }
                            }
                        }
                    }
                },
        ) {
            drawRect(
                color = Color.Transparent,
                blendMode = BlendMode.Clear,
            )
        }

        overlays()
    }
}

private fun dispatchCameraState(
    stateStr: String,
    onCameraMoved: (lat: Double, lon: Double, zoom: Double, pitch: Double, bearing: Double) -> Unit,
) {
    if (stateStr.isEmpty()) return
    val parts = stateStr.split(',')
    if (parts.size == 5) {
        val lat = parts[0].toDoubleOrNull() ?: return
        val lon = parts[1].toDoubleOrNull() ?: return
        val zoom = parts[2].toDoubleOrNull() ?: return
        val pitch = parts[3].toDoubleOrNull() ?: return
        val bearing = parts[4].toDoubleOrNull() ?: return
        onCameraMoved(lat, lon, zoom, pitch, bearing)
    }
}
