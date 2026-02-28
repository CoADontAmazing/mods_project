package ru.moderators.studytogether.api

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String,
    val email: String,
    val rating: Double = 0.0,
    val balance: Int = 0
)

@Serializable
data class Claim(
    val id: String = "",
    val userId: String,
    val type: ClaimType,
    val subject: String,
    val topic: String = "",
    val grade: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class ClaimType { OFFER, NEED }

@Serializable
data class Match(
    val claimId: String,
    val matchedClaimId: String,
    val userId: String,
    val matchedUserId: String,
    val subject: String,
    val grade: Int,
    val score: Float
)

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)
