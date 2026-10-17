package dev.monand.opendata.data.repository

import dev.monand.opendata.api.KtorWarsawOpenDataApi
import dev.monand.opendata.api.WarsawOpenDataApi
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.data.model.toDomainOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface PogodaRepository {
    suspend fun fetchStations(): Result<List<PogodaStation>>
}

class DefaultPogodaRepository(
    private val api: WarsawOpenDataApi = KtorWarsawOpenDataApi(),
) : PogodaRepository {
    override suspend fun fetchStations(): Result<List<PogodaStation>> = runCatching {
        coroutineScope {
            val pogodaDeferred = async { api.getPogoda() }
            val airQualityDeferred = async { api.getMonitoringJakosciPowietrza() }

            val pogodaStations = pogodaDeferred.await().mapNotNull { it.toDomainOrNull() }
            val airQualityStations = airQualityDeferred.await().mapNotNull { it.toDomainOrNull() }

            val combined =
                (pogodaStations + airQualityStations)
                    .groupBy { it.lokalizacja }
                    .map { (_, readings) ->
                        readings.maxByOrNull { it.czas.orEmpty() } ?: readings.last()
                    }

            check(combined.isNotEmpty()) { "Warsaw OpenData API returned no valid temperature stations" }
            combined
        }
    }
}
