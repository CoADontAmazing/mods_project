package ru.moderators.studytogether.server

import ru.moderators.studytogether.api.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.text.get

/*@Serializable
data class StoredUser(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val avatarUrl: String = "",
    val rating: Double = 0.0,
    val likes: Int = 0
) {
    fun toUser(): User = User(id, name, email, avatarUrl, rating, likes)
}*/

object InMemoryStorage : ApiStorage {
    val users = ConcurrentHashMap<String, Pair<User, String>>()         // userId -> (user, passwordHash)
    val claims = ConcurrentHashMap<String, Claim>()                     // claimId -> Claim
    val meetings = ConcurrentHashMap<String, Meeting>()                 // meetingId -> Meeting
    val messages = ConcurrentHashMap<String, Message>()                 // messageId -> Message
    val ratings = ConcurrentHashMap<String, Rating>()                   // ratingId -> Rating

    val userByEmail = ConcurrentHashMap<String, String>()               // email -> userId
    val claimsByUser = ConcurrentHashMap<String, MutableSet<String>>()  // userId -> claim ids
    val meetingsByUser = ConcurrentHashMap<String, MutableSet<String>>() // userId -> meeting ids
    val messagesByMeeting = ConcurrentHashMap<String, MutableSet<String>>() // meetingId -> message ids
    val ratingsByUser = ConcurrentHashMap<String, MutableSet<String>>() // userId (target) -> set rating ids


    override suspend fun registerUser(request: UserRegisterRequest): User {
        if (userByEmail.containsKey(request.email)) throw IllegalArgumentException("Email already exists")

        val userId = UUID.randomUUID().toString()
        val user = User(
            id = userId,
            name = request.name,
            email = request.email,
            avatarUrl = "",
            rating = 0.0,
            likes = 0
        )
        users[userId] = user to request.password
        userByEmail[request.email] = userId
        return user
    }

    override suspend fun loginUser(request: UserLoginRequest): User? {
        val userId = userByEmail[request.email] ?: return null
        val (user, passwordHash) = users[userId] ?: return null
        return if (passwordHash == request.password) user else null
    }

    override suspend fun findUserByEmail(email: String): Pair<User, String>? {
        val userId = userByEmail[email] ?: return null
        return users[userId]
    }

    override suspend fun findUserById(id: String): User? = users[id]?.first

    override suspend fun updateUserRating(userId: String, newAverage: Double) {
        users[userId] = users[userId]?.let { (user, hash) ->
            user.copy(rating = newAverage) to hash
        } ?: return
    }

    override suspend fun updateUserLikes(userId: String, likes: Int) {
        users[userId] = users[userId]?.let { (user, hash) ->
            user.copy(likes = likes) to hash
        } ?: return
    }

    override suspend fun updateUserAvatar(userId: String, avatarUrl: String) {
        users[userId] = users[userId]?.let { (user, hash) ->
            user.copy(avatarUrl = avatarUrl) to hash
        } ?: return
    }

    override suspend fun submitClaim(claim: Claim): Claim {
        val newClaim = claim.copy(id = UUID.randomUUID().toString())
        claims[newClaim.id] = newClaim
        claimsByUser.computeIfAbsent(newClaim.userId) { ConcurrentHashMap.newKeySet() }.add(newClaim.id)
        return newClaim
    }

    override suspend fun findAllClaims(): List<Claim> = claims.values.toList()

    override suspend fun findClaimsByUserId(userId: String): List<Claim> =
        claimsByUser[userId]?.mapNotNull { claims[it] } ?: emptyList()

    override suspend fun findClaimById(id: String): Claim? = claims[id]

    override suspend fun findClaimsBySubjectAndGrade(subject: String, grade: Int): List<Claim> =
        claims.values.filter { it.subject == subject && it.grade == grade }

    override suspend fun submitMeeting(meeting: Meeting) {
        val newMeeting = meeting.copy(id = UUID.randomUUID().toString())
        meetings[newMeeting.id] = newMeeting
        meetingsByUser.computeIfAbsent(newMeeting.user1Id) { ConcurrentHashMap.newKeySet() }.add(newMeeting.id)
        meetingsByUser.computeIfAbsent(newMeeting.user2Id) { ConcurrentHashMap.newKeySet() }.add(newMeeting.id)
    }

    override suspend fun findMeetingByUserId(userId: String): List<Meeting> =
        meetingsByUser[userId]?.mapNotNull { meetings[it] } ?: emptyList()

    override suspend fun findMeetingById(id: String): Meeting? = meetings[id]

    override suspend fun updateMeetingStatus(id: String, status: MeetingStatus) {
        meetings[id] = meetings[id]?.copy(status = status)?: return
    }

    override suspend fun sendMessage(request: SendMessageRequest): Message {
        val message = Message(
            id = UUID.randomUUID().toString(),
            meetingId = request.meetingId,
            senderId = request.senderId,
            text = request.text,
            timestamp = System.currentTimeMillis()
        )
        messages[message.id] = message
        messagesByMeeting.computeIfAbsent(message.meetingId) { ConcurrentHashMap.newKeySet() }.add(message.id)
        return message
    }

    override suspend fun findAllMessagesByMeetingId(meetingId: String): List<Message> =
        messagesByMeeting[meetingId]?.mapNotNull { messages[it] }?.sortedByDescending { it.timestamp } ?: emptyList()

    override suspend fun submitRating(request: SubmitRatingRequest): Rating {
        val meeting = findMeetingById(request.meetingId) ?: throw IllegalArgumentException("Meeting not found")

        if (meeting.status != MeetingStatus.COMPLETED) throw IllegalArgumentException("Cannot rate unfinished meeting")
        if (hasUserRatedMeeting(request.meetingId, request.fromUserId)) throw IllegalArgumentException("Already rated this meeting")

        val rating = Rating(
            id = UUID.randomUUID().toString(),
            meetingId = request.meetingId,
            fromUserId = request.fromUserId,
            toUserId = request.toUserId,
            rating = request.rating,
            comment = request.comment,
            createdAt = System.currentTimeMillis()
        )
        ratings[rating.id] = rating
        ratingsByUser.computeIfAbsent(rating.toUserId) { ConcurrentHashMap.newKeySet() }.add(rating.id)
        updateUserRating(request.toUserId, getUserRating(request.toUserId))

        return rating
    }

    override suspend fun getUserRating(userId: String): Double {
        val userRatings = ratingsByUser[userId]?.mapNotNull { ratings[it] } ?: emptyList()
        if (userRatings.isEmpty()) return 0.0
        return userRatings.map { it.rating }.average()
    }

    override suspend fun hasUserRatedMeeting(meetingId: String, fromUserId: String): Boolean =
        ratings.values.any { it.meetingId == meetingId && it.fromUserId == fromUserId }

    override suspend fun findAllUsers(): List<User> = users.values.map { it.first }

    override suspend fun findMatches(claimId: String): List<Match> {
        val need = claims[claimId] ?: return emptyList()
        if (need.type != ClaimType.NEED) return emptyList()
        return claims.values
            .filter { it.type == ClaimType.OFFER && it.subject == need.subject && it.grade == need.grade }
            .map { offer ->
                Match(
                    claimId = need.id,
                    matchedClaimId = offer.id,
                    userId = need.userId,
                    matchedUserId = offer.userId,
                    subject = need.subject,
                    grade = need.grade,
                    score = findUserById(need.userId)?.rating?.toFloat() ?: 1.0f
                )
            }.sortedByDescending { it.score }
    }
}