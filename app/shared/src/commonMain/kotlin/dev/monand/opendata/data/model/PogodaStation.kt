package dev.monand.opendata.data.model

import dev.monand.opendata.api.model.MonitoringJakosciPowietrzaItem
import dev.monand.opendata.api.model.PogodaItem

data class PogodaStation(
    val lokalizacja: String,
    val lat: Double,
    val lon: Double,
    val tempPow: Double,
    val tempNaw: Double? = null,
    val wilgPow: Double? = null,
    val cisnienie: Double? = null,
    val predWiatru: Double? = null,
    val kierWiatru: Double? = null,
    val intOpadu: Double? = null,
    val ijp: String? = null,
    val czas: String? = null,
)

fun PogodaItem.toDomainOrNull(): PogodaStation? {
    val name = lokalizacja?.takeIf { it.isNotBlank() } ?: return null
    val latitude = lat ?: return null
    val longitude = lon ?: return null
    val airTemp = tempPow ?: tempNaw ?: return null
    return PogodaStation(
        lokalizacja = name,
        lat = latitude,
        lon = longitude,
        tempPow = airTemp,
        tempNaw = tempNaw,
        wilgPow = wilgPow,
        predWiatru = predWiatru,
        kierWiatru = kierWiatru,
        intOpadu = intOpadu,
        czas = czas,
    )
}

fun MonitoringJakosciPowietrzaItem.toDomainOrNull(): PogodaStation? {
    val latitude = lat ?: return null
    val longitude = lon ?: return null
    val readings = data ?: return null
    val tempEntry =
        readings.firstOrNull {
            it.paramCode.equals("T", ignoreCase = true) && it.value != null
        } ?: return null
    val airTemp = tempEntry.value ?: return null
    val humidity = readings.firstOrNull { it.paramCode.equals("H", ignoreCase = true) }?.value
    val pressure = readings.firstOrNull { it.paramCode.equals("P", ignoreCase = true) }?.value

    val streetOrStation =
        station?.takeIf { it.isNotBlank() } ?: address?.street?.takeIf { it.isNotBlank() }
    val codeName = name?.takeIf { it.isNotBlank() }
    val label =
        when {
            streetOrStation != null && codeName != null && streetOrStation != codeName ->
                "$streetOrStation ($codeName)"

            streetOrStation != null -> streetOrStation

            codeName != null -> codeName

            else -> return null
        }

    return PogodaStation(
        lokalizacja = label,
        lat = latitude,
        lon = longitude,
        tempPow = airTemp,
        wilgPow = humidity,
        cisnienie = pressure,
        ijp = ijp?.name,
        czas = tempEntry.time,
    )
}
