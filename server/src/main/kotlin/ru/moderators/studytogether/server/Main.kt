package ru.moderators.studytogether.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.moderators.studytogether.api.*
import ru.moderators.studytogether.server.storage.InMemoryStorage
import java.util.*

fun main() {
    InMemoryStorage.users.put("0", User("0", "ADMIN", "ADMIN"))
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        configureRouting()
    }.start(wait = true)
}


fun Application.configureRouting() {
    routing {
        post("/user") {
            val user = call.receive<User>()
            val newUser = user.copy(id = UUID.randomUUID().toString())
            InMemoryStorage.users[newUser.id] = newUser
            call.respond(newUser)
        }

        get("/user/{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText("Missing id", status = io.ktor.http.HttpStatusCode.BadRequest)
            val user = InMemoryStorage.users[id]
            if (user != null)
                call.respond(user)
            else
                call.respondText("User not found", status = io.ktor.http.HttpStatusCode.NotFound)
        }

        get("/users") {
            call.respond(InMemoryStorage.users.values)
        }

        post("/claim") {
            val claim = call.receive<Claim>()
            val newClaim = claim.copy(id = UUID.randomUUID().toString())
            InMemoryStorage.claims[newClaim.id] = newClaim
            call.respond(newClaim)
        }

        get("/claims") {
            call.respond(InMemoryStorage.claims.values.stream().toList())
        }

        get("/claims/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing userId")
            val userClaims = InMemoryStorage.claims.values.stream().filter { it.userId == userId }
            call.respond(userClaims)
        }

        get("/match/{claimId}") {
            val claimId = call.parameters["claimId"] ?: return@get call.respondText("Missing claimId")
            val need = InMemoryStorage.claims[claimId] ?: return@get call.respondText("Claim not found", status = io.ktor.http.HttpStatusCode.NotFound)

            if (need.type != ClaimType.NEED) {
                return@get call.respondText("Claim is not a NEED", status = io.ktor.http.HttpStatusCode.BadRequest)
            }

            // Ищем все OFFER с тем же предметом и классом
            val matches = InMemoryStorage.claims.values.stream()
                .filter { it.type == ClaimType.OFFER && it.subject == need.subject && it.grade == need.grade }
                .map { offer ->
                    Match(
                        claimId = need.id,
                        matchedClaimId = offer.id,
                        userId = need.userId,
                        matchedUserId = offer.userId,
                        subject = need.subject,
                        grade = need.grade,
                        score = 1.0f // простейший случай
                    )
                }

            call.respond(matches)
        }
    }
}