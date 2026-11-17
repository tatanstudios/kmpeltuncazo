package com.tatanstudios.eltuncazometapan.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Configuración central de red (equivalente al antiguo RetrofitBuilder).
 * El motor HTTP lo pone cada plataforma: OkHttp en Android, Darwin (NSURLSession) en iOS.
 */
object ApiConfig {
    //** SERVIDOR (todavía sin Laravel Sanctum)
    // const val BASE_URL = "http://165.22.176.229/api/"
    // const val URL_IMAGENES = "http://165.22.176.229/storage/imagenes/"

    //** LOCAL: php artisan serve --host=192.168.1.4 --port=8080
    const val BASE_URL = "http://192.168.1.4:8080/api/"
    const val URL_IMAGENES = "http://192.168.1.4:8080/storage/imagenes/"
}

/** JSON tolerante, igual de permisivo que Gson con las respuestas del backend Laravel. */
val ApiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

fun createHttpClient(): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json(ApiJson)
        // Laravel a veces responde con text/html cuando hay arrays planos
        json(ApiJson, ContentType.Text.Html)
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    install(Logging) {
        level = LogLevel.NONE // Cambiar a LogLevel.BODY para depurar
    }
    defaultRequest {
        url(ApiConfig.BASE_URL)
        headers.append(HttpHeaders.Accept, "application/json")
    }
}
