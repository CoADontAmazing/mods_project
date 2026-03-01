package ru.moderators.studytogether.client

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.lifecycle.AndroidViewModel
import ru.moderators.studytogether.api.*

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.moderators.studytogether.dataStore

class ApiViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                ApiViewModel(application)
            }
        }
    }

    private val sessionManager = SessionManager(app)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _claims = MutableStateFlow<List<Claim>>(emptyList())
    val claims: StateFlow<List<Claim>> = _claims

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            sessionManager.userFlow.collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = ApiClient.register(name, email, password)
                _currentUser.value = user
                sessionManager.saveUser(user)
                _snackbarMessage.value = "Регистрация успешна"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearUser()
            _currentUser.value = null
            _claims.value = emptyList()
            _meetings.value = emptyList()
            _messages.value = emptyList()
            _matches.value = emptyList()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _snackbarMessage.value = null
            try {
                val user = ApiClient.login(email, password)
                _currentUser.value = user
                sessionManager.saveUser(user)
                _snackbarMessage.value = "Вход выполнен"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка входа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllClaims() {
        viewModelScope.launch {
            try {
                _claims.value = ApiClient.findAllClaims()
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка загрузки заявок"
            }
        }
    }

    fun createClaim(type: ClaimType, subject: String, grade: Int, topic: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = _currentUser.value?.id ?: throw Exception("Пользователь не авторизован")
                val claim = Claim(userId = userId, type = type, subject = subject, grade = grade, topic = topic)
                ApiClient.submitClaim(claim)
                loadAllClaims()
                _snackbarMessage.value = "Заявка создана"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun incrementLikes(userId: String) {
        viewModelScope.launch {
            try {
                ApiClient.incrementLikes(userId)
                refreshCurrentUser() // если лайк получен текущим пользователем
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun findMatches(claimId: String) {
        viewModelScope.launch {
            try {
                _matches.value = ApiClient.findMatches(claimId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка поиска"
            }
        }
    }

    fun loadMeetings() {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: return@launch
                _meetings.value = ApiClient.findMeetingByUserId(userId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка загрузки встреч"
            }
        }
    }

    fun createMeeting(user2Id: String, claimId: String?, dateTime: Long, isOnline: Boolean, location: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = _currentUser.value?.id ?: throw Exception("Нет пользователя")
                val meeting = Meeting(
                    user1Id = userId,
                    user2Id = user2Id,
                    claimId = claimId,
                    dateTime = dateTime,
                    isOnline = isOnline,
                    location = location
                )
                ApiClient.submitMeeting(meeting)
                loadMeetings()
                _snackbarMessage.value = "Встреча запланирована"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessages(meetingId: String) {
        viewModelScope.launch {
            try {
                _messages.value = ApiClient.findAllMessagesByMeetingId(meetingId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка загрузки сообщений"
            }
        }
    }

    fun sendMessage(meetingId: String, text: String) {
        viewModelScope.launch {
            try {
                val senderId = _currentUser.value?.id ?: throw Exception("Нет пользователя")
                val request = SendMessageRequest(meetingId, senderId, text)
                ApiClient.sendMessage(request)
                loadMessages(meetingId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка отправки"
            }
        }
    }

    fun submitRating(meetingId: String, toUserId: String, rating: Int, comment: String = "") {
        viewModelScope.launch {
            try {
                val fromUserId = _currentUser.value?.id ?: throw Exception("Нет пользователя")
                val request = SubmitRatingRequest(meetingId, fromUserId, toUserId, rating, comment)
                ApiClient.submitRating(request)
                _snackbarMessage.value = "Спасибо за оценку!"
                refreshCurrentUser()
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun getUser(userId: String): Flow<User?> = flow {
        emit(ApiClient.findUserById(userId))
    }.flowOn(Dispatchers.IO)

    fun loadMeetingsForChat() {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: return@launch
                _meetings.value = ApiClient.findMeetingByUserId(userId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка загрузки чатов"
            }
        }
    }

    suspend fun getUserRating(userId: String): Double = ApiClient.getUserRating(userId)

    private suspend fun refreshCurrentUser() {
        _currentUser.value?.id?.let { id ->
            _currentUser.value = ApiClient.findUserById(id)
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

class SessionManager(private val context: Context) {
    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_AVATAR = stringPreferencesKey("user_avatar")
        private val USER_RATING = doublePreferencesKey("user_rating")
        private val USER_BALANCE = intPreferencesKey("user_balance")
    }

    val userFlow: Flow<User?> = context.dataStore.data.map { preferences ->
            val id = preferences[USER_ID] ?: return@map null
            User(
                id = id,
                name = preferences[USER_NAME] ?: "",
                email = preferences[USER_EMAIL] ?: "",
                avatarUrl = preferences[USER_AVATAR] ?: "",
                rating = preferences[USER_RATING] ?: 0.0,
                likes = preferences[USER_BALANCE] ?: 0
            )
        }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences[USER_AVATAR] = user.avatarUrl
            preferences[USER_RATING] = user.rating
            preferences[USER_BALANCE] = user.likes
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }
}
