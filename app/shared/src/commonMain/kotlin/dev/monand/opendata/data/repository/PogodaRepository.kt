package dev.monand.opendata.data.repository

import dev.monand.opendata.api.KtorSensorCommunityApi
import dev.monand.opendata.api.KtorWarsawOpenDataApi
import dev.monand.opendata.api.SensorCommunityApi
import dev.monand.opendata.api.WarsawOpenDataApi
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.data.model.toDomainOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

interface PogodaRepository {
    suspend fun fetchStations(): Result<List<PogodaStation>>
}

class DefaultPogodaRepository(
    private val warsawApi: WarsawOpenDataApi = KtorWarsawOpenDataApi(),
    private val sensorCommunityApi: SensorCommunityApi = KtorSensorCommunityApi(),
) : PogodaRepository {
    override suspend fun fetchStations(): Result<List<PogodaStation>> = runCatching {
        coroutineScope {
            val pogodaDeferred = async { runCatching { warsawApi.getPogoda() }.getOrDefault(emptyList()) }
            val airQualityDeferred = async { runCatching { warsawApi.getMonitoringJakosciPowietrza() }.getOrDefault(emptyList()) }
            val sensorCommunityDeferred = async { runCatching { sensorCommunityApi.getBoxData() }.getOrDefault(emptyList()) }

            val pogodaStations = pogodaDeferred.await().mapNotNull { it.toDomainOrNull() }
            val airQualityStations = airQualityDeferred.await().mapNotNull { it.toDomainOrNull() }
            val communityStations = sensorCommunityDeferred.await().mapNotNull { it.toDomainOrNull() }

            val combined =
                (pogodaStations + airQualityStations + communityStations)
                    .groupBy { it.lokalizacja }
                    .map { (_, readings) ->
                        readings.maxByOrNull { it.czas.orEmpty() } ?: readings.last()
                    }

            check(combined.isNotEmpty()) { "No valid temperature stations were retrieved from any weather source" }
            combined
        }
    }

    companion object {
        const val DEFAULT_MAX_TEMP_DEVIATION = 4.5

        fun filterTemperatureOutliers(
            stations: List<PogodaStation>,
            maxDeviation: Double = DEFAULT_MAX_TEMP_DEVIATION,
        ): List<PogodaStation> {
            if (stations.size < 3) return stations
            val sortedTemps = stations.map { it.tempPow }.sorted()
            val medianTemp =
                if (sortedTemps.size % 2 == 1) {
                    sortedTemps[sortedTemps.size / 2]
                } else {
                    (sortedTemps[sortedTemps.size / 2 - 1] + sortedTemps[sortedTemps.size / 2]) / 2.0
                }

            return stations.filter { station ->
                abs(station.tempPow - medianTemp) <= maxDeviation
            }
        }
    }
}
