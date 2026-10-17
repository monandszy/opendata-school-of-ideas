package dev.monand.opendata.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorCommunityItem(
    val id: Long? = null,
    val timestamp: String? = null,
    val location: SensorLocation? = null,
    val sensor: SensorInfo? = null,
    @SerialName("sensordatavalues")
    val sensordatavalues: List<SensorDataValue> = emptyList(),
)

@Serializable
data class SensorLocation(
    val id: Long? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val altitude: String? = null,
    val country: String? = null,
    val indoor: Int? = 0,
)

@Serializable
data class SensorInfo(
    val id: Long? = null,
    val name: String? = null,
    val pin: String? = null,
    @SerialName("sensor_type")
    val sensorType: SensorType? = null,
)

@Serializable
data class SensorType(
    val id: Long? = null,
    val name: String? = null,
    val manufacturer: String? = null,
)

@Serializable
data class SensorDataValue(
    val id: Long? = null,
    val value: String? = null,
    @SerialName("value_type")
    val valueType: String? = null,
)
