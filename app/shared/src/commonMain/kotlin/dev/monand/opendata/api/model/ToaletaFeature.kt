package dev.monand.opendata.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeometryPoint(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList(),
) {
    val longitude: Double? get() = coordinates.getOrNull(0)
    val latitude: Double? get() = coordinates.getOrNull(1)
}

@Serializable
data class ToaletaProperties(
    val zarzadca: String? = null,
    val typ: String? = null,
    val lokalizacja: String? = null,
    val dzielnica: String? = null,
    val dostepnosc: Int = 0,
    val platnosc: Int = 0,
    val przewijak: Int = 0,
    @SerialName("aktu_dan")
    val aktuDan: String? = null,
    @SerialName("poch_dan")
    val pochDan: String? = null,
    val godziny: String? = null,
    val uwagi: String? = null,
) {
    val isFree: Boolean get() = platnosc == 0
    val isAccessible: Boolean get() = dostepnosc == 1
    val hasBabyChangingTable: Boolean get() = przewijak == 1
}

@Serializable
data class ToaletaFeature(
    val type: String = "Feature",
    val geometry: GeometryPoint = GeometryPoint(),
    val properties: ToaletaProperties = ToaletaProperties(),
) {
    val id: String
        get() = "${geometry.longitude}_${geometry.latitude}_${properties.lokalizacja?.hashCode()}"
}
