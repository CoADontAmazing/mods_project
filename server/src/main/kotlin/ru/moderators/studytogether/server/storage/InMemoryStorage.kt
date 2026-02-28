package ru.moderators.studytogether.server.storage

import ru.moderators.studytogether.api.*
import java.util.concurrent.ConcurrentHashMap

data class StoredUser(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String
) {
    fun toUser(): User = User(id, name, email)
}

object InMemoryStorage {
    val users = ConcurrentHashMap<String, StoredUser>()
    val claims = ConcurrentHashMap<String, Claim>()
}