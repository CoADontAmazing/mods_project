package ru.moderators.studytogether.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import ru.moderators.studytogether.api.*

object ApiClient : ClaimManager, UserManager, MessageManager, MeetingManager, RatingManager {
    const val BASE_URL = "http://10.0.2.2:8080" // я умер на этом моменте

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun register(name: String, email: String, password: String): User = registerUser(UserRegisterRequest(name, email, password))

    suspend fun login(email: String, password: String): User = loginUser(UserLoginRequest(email, password))

    override suspend fun submitClaim(claim: Claim): Claim =
        client.post("$BASE_URL/claim") {
            contentType(ContentType.Application.Json)
            setBody(claim)
        }.body()

    override suspend fun findAllClaims(): List<Claim> =
        client.get("$BASE_URL/claims").body()

    override suspend fun findClaimsByUserId(userId: String): List<Claim> =
        client.get("$BASE_URL/claims/user/$userId").body()

    override suspend fun findClaimById(id: String): Claim? =
        try {
            client.get("$BASE_URL/claim/$id").body()
        } catch (e: Exception) {
            null
        }

    override suspend fun findClaimsBySubjectAndGrade(subject: String, grade: Int): List<Claim> =
        client.get("$BASE_URL/claims/filter") {
            parameter("subject", subject)
            parameter("grade", grade)
        }.body()

    override suspend fun registerUser(request: UserRegisterRequest): User =
        client.post("$BASE_URL/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun loginUser(request: UserLoginRequest): User =
        client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun findUserByEmail(email: String): Pair<User, String>? =
        try {
            val response: HttpResponse = client.get("$BASE_URL/user/by-email") {
                parameter("email", email)
            }
            if (response.status == HttpStatusCode.OK) {
                val user = response.body<User>()
                val hash = response.headers["X-Password-Hash"] ?: return null
                user to hash
            } else null
        } catch (e: Exception) {
            null
        }

    override suspend fun findUserById(id: String): User? =
        try {
            client.get("$BASE_URL/user/$id").body()
        } catch (e: Exception) {
            null
        }

    override suspend fun updateUserRating(userId: String, newAverage: Double) {
        client.put("$BASE_URL/user/$userId/rating") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("rating" to newAverage))
        }
    }

    override suspend fun updateUserLikes(userId: String, likes: Int) {
        TODO("Not yet implemented")
    }

    suspend fun incrementLikes(userId: String) {
        client.post("$BASE_URL/user/$userId/like") // эндпоинт нужно добавить на сервере
    }

    override suspend fun updateUserAvatar(userId: String, avatarUrl: String) {
        client.put("$BASE_URL/user/$userId/avatar") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("avatarUrl" to avatarUrl))
        }
    }

    override suspend fun findAllUsers(): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(request: SendMessageRequest): Message =
        client.post("$BASE_URL/message") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun findAllMessagesByMeetingId(meetingId: String): List<Message> =
        client.get("$BASE_URL/messages/$meetingId").body()

    override suspend fun submitMeeting(meeting: Meeting) {
        client.post("$BASE_URL/meeting") {
            contentType(ContentType.Application.Json)
            setBody(meeting)
        }
    }

    override suspend fun findMeetingByUserId(userId: String): List<Meeting> =
        client.get("$BASE_URL/meetings/user/$userId").body()

    override suspend fun findMeetingById(id: String): Meeting? =
        try {
            client.get("$BASE_URL/meeting/$id").body()
        } catch (e: Exception) {
            null
        }

    override suspend fun updateMeetingStatus(id: String, status: MeetingStatus) {
        client.put("$BASE_URL/meeting/$id/status") {
            contentType(ContentType.Application.Json)
            setBody(status)
        }
    }

    override suspend fun submitRating(request: SubmitRatingRequest): Rating =
        client.post("$BASE_URL/rating") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun getUserRating(userId: String): Double =
        client.get("$BASE_URL/rating/$userId").body()

    override suspend fun hasUserRatedMeeting(meetingId: String, fromUserId: String): Boolean =
        try {
            val result: Boolean = client.get("$BASE_URL/rating/check") {
                parameter("meetingId", meetingId)
                parameter("fromUserId", fromUserId)
            }.body()
            result
        } catch (e: Exception) {
            false
        }

    override suspend fun findMatches(claimId: String): List<Match> =
        client.get("$BASE_URL/match/$claimId").body()
}