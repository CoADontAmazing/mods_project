package ru.moderators.studytogether.api

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String,
    val email: String,
    val avatarUrl: String = "",
    val rating: Double = 0.0,
    val likes: Int = 0
)


@Serializable
enum class ClaimType { OFFER, NEED }

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
enum class MeetingStatus { PLANNED, COMPLETED, CANCELLED }

@Serializable
data class Meeting(
    val id: String = "",
    val user1Id: String,
    val user2Id: String,
    val claimId: String? = null,
    val dateTime: Long,
    val isOnline: Boolean,
    val location: String = "",
    val status: MeetingStatus = MeetingStatus.PLANNED
)

@Serializable
data class Rating(
    val id: String = "",
    val meetingId: String,
    val fromUserId: String,
    val toUserId: String,
    val rating: Int, // 1..5
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class CreateMeetingRequest(
    val user2Id: String,
    val claimId: String?,
    val dateTime: Long,
    val isOnline: Boolean,
    val location: String = ""
)

@Serializable
data class UserRegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class UserLoginRequest(val email: String, val password: String)

@Serializable
data class SubmitRatingRequest(
    val meetingId: String,
    val fromUserId: String,
    val toUserId: String,
    val rating: Int,
    val comment: String = ""
)

@Serializable
data class Message(
    val id: String = "",
    val meetingId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SendMessageRequest(
    val meetingId: String,
    val senderId: String,
    val text: String
)
