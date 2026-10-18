package dev.monand.opendata.api

import dev.monand.opendata.BuildEnv
import dev.monand.opendata.api.model.MonitoringJakosciPowietrzaItem
import dev.monand.opendata.api.model.PogodaItem
import dev.monand.opendata.api.model.ToaletaFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface WarsawOpenDataApi {
    suspend fun getPogoda(): List<PogodaItem>

    suspend fun getMonitoringJakosciPowietrza(): List<MonitoringJakosciPowietrzaItem>

    suspend fun getToalety(): List<ToaletaFeature>
}

class KtorWarsawOpenDataApi(
    private val client: HttpClient = HttpClientFactory.create(),
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val proxyUrl: String = DEFAULT_PROXY_URL,
    private val apiKey: String = BuildEnv.WARSAW_API_KEY,
) : WarsawOpenDataApi {
    override suspend fun getPogoda(): List<PogodaItem> = postAction(POGODA_PATH)

    override suspend fun getMonitoringJakosciPowietrza(): List<MonitoringJakosciPowietrzaItem> = postAction(AIR_QUALITY_PATH)

    override suspend fun getToalety(): List<ToaletaFeature> = postAction(TOALETY_PATH)

    private suspend inline fun <reified T> postAction(path: String): T {
        val primaryResult =
            runCatching {
                client
                    .post("$baseUrl$path") {
                        if (apiKey.isNotBlank()) {
                            header("Authorization", apiKey)
                        }
                        contentType(ContentType.Application.Json)
                        setBody("{}")
                    }.body<T>()
            }
        return primaryResult.getOrElse {
            client
                .post("$proxyUrl$path") {
                    if (apiKey.isNotBlank()) {
                        header("Authorization", apiKey)
                    }
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }.body<T>()
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://dane.um.warszawa.pl"
        const val DEFAULT_PROXY_URL = "/warsaw-api"
        const val POGODA_PATH = "/api/action/get_zom_pogoda"
        const val AIR_QUALITY_PATH = "/api/action/get_bopipk_monitoring_jakosci_powietrza"
        const val TOALETY_PATH = "/api/action/get_m_toalety"
    }
}
