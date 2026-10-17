package dev.monand.opendata.core.geo

import dev.monand.opendata.data.model.PogodaStation
import kotlin.math.sqrt

data class IdwResult(
    val temp: Double,
    val minDistanceKm: Double,
)

object IdwInterpolator {
    private const val MIN_LAT = 52.10
    private const val MAX_LAT = 52.36
    private const val MIN_LON = 20.85
    private const val MAX_LON = 21.25

    fun normalizeLat(lat: Double): Float = ((MAX_LAT - lat) / (MAX_LAT - MIN_LAT)).toFloat().coerceIn(0f, 1f)

    fun normalizeLon(lon: Double): Float = ((lon - MIN_LON) / (MAX_LON - MIN_LON)).toFloat().coerceIn(0f, 1f)

    fun latAt(normY: Float): Double = MAX_LAT - normY * (MAX_LAT - MIN_LAT)

    fun lonAt(normX: Float): Double = MIN_LON + normX * (MAX_LON - MIN_LON)

    fun interpolate(
        lat: Double,
        lon: Double,
        stations: List<PogodaStation>,
    ): IdwResult {
        if (stations.isEmpty()) return IdwResult(0.0, 999.0)
        var weightedSum = 0.0
        var totalWeight = 0.0
        var minSqKm = Double.MAX_VALUE

        for (s in stations) {
            val dyKm = (lat - s.lat) * 111.2
            val dxKm = (lon - s.lon) * 68.1
            val distSq = dyKm * dyKm + dxKm * dxKm
            if (distSq < minSqKm) {
                minSqKm = distSq
            }
            if (distSq < 1e-6) {
                return IdwResult(s.tempPow, 0.0)
            }
            val weight = 1.0 / distSq
            weightedSum += weight * s.tempPow
            totalWeight += weight
        }
        return IdwResult(weightedSum / totalWeight, sqrt(minSqKm))
    }
}
