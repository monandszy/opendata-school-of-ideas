package dev.monand.opendata.api

import dev.monand.opendata.api.model.SensorCommunityItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface SensorCommunityApi {
    suspend fun getBoxData(
        latMin: Double = DEFAULT_LAT_MIN,
        lonMin: Double = DEFAULT_LON_MIN,
        latMax: Double = DEFAULT_LAT_MAX,
        lonMax: Double = DEFAULT_LON_MAX,
    ): List<SensorCommunityItem>

    companion object {
        const val DEFAULT_LAT_MIN = 52.10
        const val DEFAULT_LON_MIN = 20.85
        const val DEFAULT_LAT_MAX = 52.38
        const val DEFAULT_LON_MAX = 21.28
    }
}

class KtorSensorCommunityApi(
    private val client: HttpClient = HttpClientFactory.create(),
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val proxyUrl: String = DEFAULT_PROXY_URL,
) : SensorCommunityApi {
    override suspend fun getBoxData(
        latMin: Double,
        lonMin: Double,
        latMax: Double,
        lonMax: Double,
    ): List<SensorCommunityItem> {
        val path = "/airrohr/v1/filter/box=$latMin,$lonMin,$latMax,$lonMax"
        val primaryResult =
            runCatching {
                client.get("$baseUrl$path").body<List<SensorCommunityItem>>()
            }
        return primaryResult.getOrElse {
            client.get("$proxyUrl$path").body<List<SensorCommunityItem>>()
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://data.sensor.community"
        const val DEFAULT_PROXY_URL = "/sensor-community-api"
    }
}
