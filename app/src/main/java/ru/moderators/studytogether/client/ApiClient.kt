package ru.moderators.studytogether.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import ru.moderators.studytogether.api.*

object ApiClient {
    private const val BASE_URL = "http://127.0.0.1:8080" // для эмулятора Android; для реальных устройств нужен IP

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun createUser(user: User): User = client.post("$BASE_URL/user") {
        contentType(ContentType.Application.Json)
        setBody(user)
    }.body()

    suspend fun createClaim(claim: Claim): Claim = client.post("$BASE_URL/claim") {
        contentType(ContentType.Application.Json)
        setBody(claim)
    }.body()

    suspend fun getAllClaims(): List<Claim> = client.get("$BASE_URL/claims").body()

    suspend fun getMatchesForClaim(claimId: String): List<Match> = client.get("$BASE_URL/match/$claimId").body()
}