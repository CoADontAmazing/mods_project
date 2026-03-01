package ru.moderators.studytogether.api

interface ApiStorage : ClaimManager, UserManager, MessageManager, MeetingManager, RatingManager

interface UserManager {
    suspend fun registerUser(request: UserRegisterRequest): User
    suspend fun loginUser(request: UserLoginRequest): User?

    suspend fun findUserByEmail(email: String): Pair<User, String>?
    suspend fun findUserById(id: String): User?

    suspend fun updateUserRating(userId: String, newAverage: Double)
    suspend fun updateUserLikes(userId: String, likes: Int)
    suspend fun updateUserAvatar(userId: String, avatarUrl: String)

    suspend fun findAllUsers(): List<User>
}

interface MeetingManager {
    suspend fun submitMeeting(meeting: Meeting)

    suspend fun findMeetingByUserId(userId: String): List<Meeting>
    suspend fun findMeetingById(id: String): Meeting?
    suspend fun updateMeetingStatus(id: String, status: MeetingStatus)
}

interface ClaimManager {
    suspend fun submitClaim(claim: Claim): Claim

    suspend fun findAllClaims(): List<Claim>
    suspend fun findClaimsByUserId(userId: String): List<Claim>
    suspend fun findClaimById(id: String): Claim?
    suspend fun findClaimsBySubjectAndGrade(subject: String, grade: Int): List<Claim>
    suspend fun findMatches(claimId: String): List<Match>
}

interface RatingManager {
    suspend fun submitRating(request: SubmitRatingRequest): Rating

    suspend fun getUserRating(userId: String): Double
    suspend fun hasUserRatedMeeting(meetingId: String, fromUserId: String): Boolean
}

interface MessageManager {
    suspend fun sendMessage(request: SendMessageRequest): Message

    suspend fun findAllMessagesByMeetingId(meetingId: String): List<Message>
}