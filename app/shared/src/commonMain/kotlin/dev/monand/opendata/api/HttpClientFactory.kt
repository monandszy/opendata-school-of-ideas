package dev.monand.opendata.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    val defaultJson: Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(defaultJson)
        }
    }
}
