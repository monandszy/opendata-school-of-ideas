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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
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
    """(base64Rgba, width, height, north, south, west, east, showHeatmap, is3dMode, stationsGeoJsonStr) => {
        if (window.warsawMapBridge) {
            window.warsawMapBridge.updateOverlays(
                base64Rgba, width, height, north, south, west, east, showHeatmap, is3dMode, stationsGeoJsonStr
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
    is3dMode: Boolean,
    stationsGeoJsonStr: String,
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

@Composable
actual fun PlatformMapView(
    stations: List<PogodaStation>,
    selectedStation: PogodaStation?,
    tileSource: MapTileSource,
    centerLat: Double,
    centerLon: Double,
    zoom: Double,
    pitch: Double,
    bearing: Double,
    is3dMode: Boolean,
    showHeatmap: Boolean,
    minTemp: Double,
    maxTemp: Double,
    cameraCommandId: Int,
    onCameraMoved: (lat: Double, lon: Double, zoom: Double, pitch: Double, bearing: Double) -> Unit,
    onSelectStation: (PogodaStation?) -> Unit,
    modifier: Modifier,
    overlays: @Composable BoxScope.() -> Unit,
) {
    val currentStations by rememberUpdatedState(stations)
    val currentOnCameraMoved by rememberUpdatedState(onCameraMoved)
    val currentOnSelectStation by rememberUpdatedState(onSelectStation)

    var lastAppliedCommandId by remember { mutableIntStateOf(cameraCommandId) }
    var isOrbitModifierDown by remember { mutableStateOf(false) }

    val idwTexture =
        remember(stations, minTemp, maxTemp, zoom) {
            MapOverlayGeoJson.generateIdwTexture(
                stations = stations,
                minTemp = minTemp,
                maxTemp = maxTemp,
                zoom = zoom,
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
            is3dMode = is3dMode,
            stationsGeoJsonStr = stationsGeoJson,
        )
    }

    LaunchedEffect(base64Texture, showHeatmap, is3dMode, stationsGeoJson) {
        jsUpdateOverlays(
            base64Rgba = base64Texture,
            width = idwTexture.width,
            height = idwTexture.height,
            north = idwTexture.northLat,
            south = idwTexture.southLat,
            west = idwTexture.westLon,
            east = idwTexture.eastLon,
            showHeatmap = showHeatmap,
            is3dMode = is3dMode,
            stationsGeoJsonStr = stationsGeoJson,
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
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val mods = event.keyboardModifiers
                            isOrbitModifierDown =
                                mods.isShiftPressed || mods.isCtrlPressed || mods.isAltPressed
                            if (event.type == PointerEventType.Scroll) {
                                val change = event.changes.firstOrNull()
                                if (change != null) {
                                    val scrollY = change.scrollDelta.y.toDouble()
                                    if (scrollY != 0.0) {
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
                }.pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val stateStr =
                            if (isOrbitModifierDown) {
                                jsOrbitBy(dragAmount.x.toDouble(), dragAmount.y.toDouble())
                            } else {
                                jsPanBy(dragAmount.x.toDouble(), dragAmount.y.toDouble())
                            }
                        dispatchCameraState(stateStr, currentOnCameraMoved)
                    }
                }.pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val stationName = jsQueryStationAt(offset.x.toDouble(), offset.y.toDouble())
                        val tapped =
                            if (stationName.isNotEmpty()) {
                                currentStations.find { it.lokalizacja == stationName }
                            } else {
                                null
                            }
                        currentOnSelectStation(tapped)
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
