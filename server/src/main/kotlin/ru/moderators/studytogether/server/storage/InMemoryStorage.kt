package ru.moderators.studytogether.server.storage

import ru.moderators.studytogether.api.*
import java.util.concurrent.ConcurrentHashMap

object InMemoryStorage {
    val users = ConcurrentHashMap<String, User>()
    val claims = ConcurrentHashMap<String, Claim>()
}