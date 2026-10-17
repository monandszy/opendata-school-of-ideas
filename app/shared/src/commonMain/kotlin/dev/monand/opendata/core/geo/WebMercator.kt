package dev.monand.opendata.core.geo

import androidx.compose.ui.geometry.Offset
import dev.monand.opendata.core.map.TileCoord
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tan

object WebMercator {
    const val TILE_SIZE = 256.0
    private const val MIN_LAT = -85.05112878
    private const val MAX_LAT = 85.05112878

    fun lonToTileX(
        lon: Double,
        zoom: Int,
    ): Double {
        val n = (1 shl zoom).toDouble()
        return ((lon + 180.0) / 360.0) * n
    }

    fun latToTileY(
        lat: Double,
        zoom: Int,
    ): Double {
        val clampedLat = lat.coerceIn(MIN_LAT, MAX_LAT)
        val latRad = clampedLat * PI / 180.0
        val n = (1 shl zoom).toDouble()
        return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n
    }

    fun tileXToLon(
        tileX: Double,
        zoom: Int,
    ): Double {
        val n = (1 shl zoom).toDouble()
        return (tileX / n) * 360.0 - 180.0
    }

    fun tileYToLat(
        tileY: Double,
        zoom: Int,
    ): Double {
        val n = (1 shl zoom).toDouble()
        val yNorm = PI * (1.0 - 2.0 * tileY / n)
        return (180.0 / PI) * atan(sinh(yNorm))
    }

    fun latLonToScreenOffset(
        lat: Double,
        lon: Double,
        centerLat: Double,
        centerLon: Double,
        zoom: Int,
        widthPx: Float,
        heightPx: Float,
    ): Offset {
        val centerTileX = lonToTileX(centerLon, zoom)
        val centerTileY = latToTileY(centerLat, zoom)
        val pointTileX = lonToTileX(lon, zoom)
        val pointTileY = latToTileY(lat, zoom)
        val screenX = (widthPx / 2.0 + (pointTileX - centerTileX) * TILE_SIZE).toFloat()
        val screenY = (heightPx / 2.0 + (pointTileY - centerTileY) * TILE_SIZE).toFloat()
        return Offset(screenX, screenY)
    }

    fun screenOffsetToLatLon(
        screenX: Float,
        screenY: Float,
        centerLat: Double,
        centerLon: Double,
        zoom: Int,
        widthPx: Float,
        heightPx: Float,
    ): Pair<Double, Double> {
        val centerTileX = lonToTileX(centerLon, zoom)
        val centerTileY = latToTileY(centerLat, zoom)
        val tileX = centerTileX + (screenX - widthPx / 2.0) / TILE_SIZE
        val tileY = centerTileY + (screenY - heightPx / 2.0) / TILE_SIZE
        return tileYToLat(tileY, zoom) to tileXToLon(tileX, zoom)
    }

    fun visibleTilesForViewport(
        centerLat: Double,
        centerLon: Double,
        zoom: Int,
        widthPx: Int,
        heightPx: Int,
    ): List<TileCoord> {
        val w = widthPx.coerceAtLeast(256).toDouble()
        val h = heightPx.coerceAtLeast(256).toDouble()
        val centerTileX = lonToTileX(centerLon, zoom)
        val centerTileY = latToTileY(centerLat, zoom)
        val halfTilesX = (w / 2.0) / TILE_SIZE + 1.0
        val halfTilesY = (h / 2.0) / TILE_SIZE + 1.0
        val maxTileIndex = (1 shl zoom) - 1
        val minX = floor(centerTileX - halfTilesX).toInt().coerceIn(0, maxTileIndex)
        val maxX = floor(centerTileX + halfTilesX).toInt().coerceIn(0, maxTileIndex)
        val minY = floor(centerTileY - halfTilesY).toInt().coerceIn(0, maxTileIndex)
        val maxY = floor(centerTileY + halfTilesY).toInt().coerceIn(0, maxTileIndex)
        return buildList {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    add(TileCoord(zoom, x, y))
                }
            }
        }
    }
}
