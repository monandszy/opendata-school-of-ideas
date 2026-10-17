package dev.monand.opendata.data.model

data class LocationInspection(
    val lat: Double,
    val lon: Double,
    val estimatedTemp: Double,
    val minDistanceKm: Double,
    val nearestStationName: String,
)
