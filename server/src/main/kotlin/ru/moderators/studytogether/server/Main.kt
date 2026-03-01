package ru.moderators.studytogether.server

import ru.moderators.studytogether.api.*

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.Level
import ru.moderators.studytogether.server.InMemoryStorage.registerUser



fun main() {
    val apiStorage: ApiStorage = InMemoryStorage

    embeddedServer(Netty, port = 8080) {
        registerUser(UserRegisterRequest("Admin", "noreply@admin.ru", "1234"))

        configureSerialization()
        install(CallLogging) {
            level = Level.INFO
            format { call ->
                "${call.request.httpMethod} ${call.request.uri} - ${call.response.status()}"
            }
        }

        configureRouting(apiStorage)
    }.start(wait = true)
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureRouting(apiStorage: ApiStorage) {
    routing {
        post("/register") {
            val request = call.receive<UserRegisterRequest>()
            try {
                val user = apiStorage.registerUser(request)
                call.respond(HttpStatusCode.Created, user)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "Email already exists")
            }
        }

        post("/login") {
            val request = call.receive<UserLoginRequest>()
            val user = apiStorage.loginUser(request)
            if (user != null) {
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
            }
        }

        get("/user/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val user = apiStorage.findUserById(id)
            if (user != null) call.respond(user) else call.respond(HttpStatusCode.NotFound, "User not found")
        }

        get("/users") {
            val users = apiStorage.findAllUsers()
            call.respond(users)
        }

        get("/user/by-email") {
            val email = call.request.queryParameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing email")
            val result = apiStorage.findUserByEmail(email)
            if (result != null) {
                val (user, hash) = result
                call.response.headers.append("X-Password-Hash", hash)
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }

        put("/user/{userId}/rating") {
            val userId = call.parameters["userId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val newRating = call.receive<Map<String, Double>>()["rating"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            apiStorage.updateUserRating(userId, newRating)
            call.respond(HttpStatusCode.OK)
        }

        put("/user/{userId}/avatar") {
            val userId = call.parameters["userId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val avatarUrl = call.receive<Map<String, String>>()["avatarUrl"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            apiStorage.updateUserAvatar(userId, avatarUrl)
            call.respond(HttpStatusCode.OK)
        }

        post("/user/{userId}/like") {
            val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            try {
                apiStorage.updateUserLikes(userId,
                    apiStorage.findUserById(userId)?.likes?.plus(1) ?: 0
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, e.message ?: "User not found")
            }
        }

        post("/claim") {
            val claim = call.receive<Claim>()
            val newClaim = apiStorage.submitClaim(claim)
            call.respond(HttpStatusCode.Created, newClaim)
        }

        get("/claims") {
            call.respond(InMemoryStorage.findAllClaims())
        }

        get("/claims/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(apiStorage.findClaimsByUserId(userId))
        }

        get("/claim/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val claim = apiStorage.findClaimById(id)
            if (claim != null) call.respond(claim) else call.respond(HttpStatusCode.NotFound)
        }

        get("/claims/filter") {
            val subject = call.request.queryParameters["subject"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val grade = call.request.queryParameters["grade"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val claims = apiStorage.findClaimsBySubjectAndGrade(subject, grade)
            call.respond(claims)
        }

        get("/match/{claimId}") {
            val claimId = call.parameters["claimId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val matches = apiStorage.findMatches(claimId)
            call.respond(matches)
        }

        post("/meeting") {
            val meeting = call.receive<Meeting>()
            apiStorage.submitMeeting(meeting)
            call.respond(HttpStatusCode.Created)
        }

        get("/meetings/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val meetings = apiStorage.findMeetingByUserId(userId)
            call.respond(meetings)
        }

        get("/meeting/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val meeting = apiStorage.findMeetingById(id)
            if (meeting != null) call.respond(meeting) else call.respond(HttpStatusCode.NotFound)
        }

        put("/meeting/{id}/status") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val status = call.receive<MeetingStatus>()
            apiStorage.updateMeetingStatus(id, status)
            call.respond(HttpStatusCode.OK)
        }

        post("/message") {
            val request = call.receive<SendMessageRequest>()
            val message = apiStorage.sendMessage(request)
            call.respond(HttpStatusCode.Created, message)
        }

        get("/messages/{meetingId}") {
            val meetingId = call.parameters["meetingId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val messages = apiStorage.findAllMessagesByMeetingId(meetingId)
            call.respond(messages)
        }

        post("/rating") {
            val request = call.receive<SubmitRatingRequest>()
            try {
                val rating = apiStorage.submitRating(request)
                call.respond(HttpStatusCode.Created, rating)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid rating")
            }
        }

        get("/rating/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val avg = apiStorage.getUserRating(userId)
            call.respond(avg)
        }

        get("/rating/check") {
            val meetingId = call.request.queryParameters["meetingId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val fromUserId = call.request.queryParameters["fromUserId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val hasRated = apiStorage.hasUserRatedMeeting(meetingId, fromUserId)
            call.respond(hasRated)
        }
    }
}