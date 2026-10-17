package dev.monand.opendata.core.map

import dev.monand.opendata.core.geo.IdwInterpolator
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.feature.heatmap.components.heatColorForNormalizedTemp
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.math.pow
import kotlin.math.roundToInt

data class IdwHeatmapTexture(
    val width: Int,
    val height: Int,
    val rgbaBytes: ByteArray,
    val northLat: Double = DEFAULT_NORTH_LAT,
    val southLat: Double = DEFAULT_SOUTH_LAT,
    val westLon: Double = DEFAULT_WEST_LON,
    val eastLon: Double = DEFAULT_EAST_LON,
) {
    companion object {
        const val DEFAULT_NORTH_LAT = 52.38
        const val DEFAULT_SOUTH_LAT = 52.08
        const val DEFAULT_WEST_LON = 20.82
        const val DEFAULT_EAST_LON = 21.28
    }
}

object MapOverlayGeoJson {
    fun generateIdwTexture(
        stations: List<PogodaStation>,
        minTemp: Double,
        maxTemp: Double,
        zoom: Double = 11.5,
        width: Int = 80,
        height: Int = 80,
        northLat: Double = IdwHeatmapTexture.DEFAULT_NORTH_LAT,
        southLat: Double = IdwHeatmapTexture.DEFAULT_SOUTH_LAT,
        westLon: Double = IdwHeatmapTexture.DEFAULT_WEST_LON,
        eastLon: Double = IdwHeatmapTexture.DEFAULT_EAST_LON,
    ): IdwHeatmapTexture {
        val bytes = ByteArray(width * height * 4)
        if (stations.isEmpty()) {
            return IdwHeatmapTexture(
                width = width,
                height = height,
                rgbaBytes = bytes,
                northLat = northLat,
                southLat = southLat,
                westLon = westLon,
                eastLon = eastLon,
            )
        }

        val range = (maxTemp - minTemp).coerceAtLeast(1e-5)
        val maxInfluenceRadiusKm = (10.0 / 1.25.pow((zoom - 10.0).coerceAtLeast(0.0))).coerceIn(1.8, 12.0)
        var idx = 0

        for (y in 0 until height) {
            val v = (y + 0.5) / height
            val lat = northLat - v * (northLat - southLat)
            for (x in 0 until width) {
                val u = (x + 0.5) / width
                val lon = westLon + u * (eastLon - westLon)
                val res = IdwInterpolator.interpolate(lat, lon, stations)
                if (res.minDistanceKm > maxInfluenceRadiusKm) {
                    bytes[idx++] = 0
                    bytes[idx++] = 0
                    bytes[idx++] = 0
                    bytes[idx++] = 0
                } else {
                    val falloff = (1.0f - (res.minDistanceKm / maxInfluenceRadiusKm).toFloat()).coerceIn(0f, 1f)
                    val alphaDecay = falloff * falloff
                    val t = ((res.temp - minTemp) / range).toFloat().coerceIn(0f, 1f)
                    val color = heatColorForNormalizedTemp(t, alpha = alphaDecay * 0.72f)
                    bytes[idx++] = (color.red * 255f).roundToInt().coerceIn(0, 255).toByte()
                    bytes[idx++] = (color.green * 255f).roundToInt().coerceIn(0, 255).toByte()
                    bytes[idx++] = (color.blue * 255f).roundToInt().coerceIn(0, 255).toByte()
                    bytes[idx++] = (color.alpha * 255f).roundToInt().coerceIn(0, 255).toByte()
                }
            }
        }
        return IdwHeatmapTexture(
            width = width,
            height = height,
            rgbaBytes = bytes,
            northLat = northLat,
            southLat = southLat,
            westLon = westLon,
            eastLon = eastLon,
        )
    }

    fun buildStationsGeoJson(
        stations: List<PogodaStation>,
        selectedStation: PogodaStation?,
        minTemp: Double,
        maxTemp: Double,
    ): String {
        val range = (maxTemp - minTemp).coerceAtLeast(1e-5)
        val featureCollection =
            buildJsonObject {
                put("type", "FeatureCollection")
                putJsonArray("features") {
                    for (s in stations) {
                        val isSelected = selectedStation?.lokalizacja == s.lokalizacja
                        val t = ((s.tempPow - minTemp) / range).toFloat().coerceIn(0f, 1f)
                        val color = heatColorForNormalizedTemp(t, alpha = 1f)
                        val hexColor = colorToHex(color.red, color.green, color.blue)
                        add(
                            buildJsonObject {
                                put("type", "Feature")
                                putJsonObject("geometry") {
                                    put("type", "Point")
                                    put(
                                        "coordinates",
                                        buildJsonArray {
                                            add(kotlinx.serialization.json.JsonPrimitive(s.lon))
                                            add(kotlinx.serialization.json.JsonPrimitive(s.lat))
                                        },
                                    )
                                }
                                putJsonObject("properties") {
                                    put("lokalizacja", s.lokalizacja)
                                    put("tempPow", s.tempPow)
                                    put("color", hexColor)
                                    put("radius", if (isSelected) 10.0 else 7.0)
                                    put("strokeWidth", if (isSelected) 3.0 else 2.0)
                                }
                            },
                        )
                    }
                }
            }
        return featureCollection.toString()
    }

    private fun colorToHex(
        r: Float,
        g: Float,
        b: Float,
    ): String {
        val ri = (r * 255f).roundToInt().coerceIn(0, 255)
        val gi = (g * 255f).roundToInt().coerceIn(0, 255)
        val bi = (b * 255f).roundToInt().coerceIn(0, 255)
        return "#" +
            ri.toString(16).padStart(2, '0') +
            gi.toString(16).padStart(2, '0') +
            bi.toString(16).padStart(2, '0')
    }
}
