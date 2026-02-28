package ru.moderators.studytogether.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import ru.moderators.studytogether.client.ApiClient
import ru.moderators.studytogether.api.*


class MainViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _claims = MutableStateFlow<List<Claim>>(emptyList())
    val claims: StateFlow<List<Claim>> = _claims

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun createUser(name: String, email: String) {
        viewModelScope.launch {
            _snackbarMessage.value = null // сбрасываем предыдущее
            try {
                val user = User(name = name, email = email)
                val created = ApiClient.createUser(user)
                _currentUser.value = created
                _snackbarMessage.value = "Пользователь создан: ${created.name}"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка подключения: ${e.message}"
            }
        }
    }

    // Аналогично для других запросов (createClaim, loadAllClaims, findMatchesForClaim)
    fun createClaim(type: ClaimType, subject: String, grade: Int, topic: String = "") {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: throw Exception("Нет пользователя")
                val claim = Claim(userId = userId, type = type, subject = subject, grade = grade, topic = topic)
                ApiClient.createClaim(claim)
                loadAllClaims()
                _snackbarMessage.value = "Заявка создана"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            }
        }
    }

    // Для обновления списка заявок тоже добавим уведомление
    fun loadAllClaims() {
        viewModelScope.launch {
            try {
                _claims.value = ApiClient.getAllClaims()
                _snackbarMessage.value = "Заявки обновлены"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun findMatchesForClaim(claimId: String) {
        viewModelScope.launch {
            try {
                _matches.value = ApiClient.getMatchesForClaim(claimId)
                _snackbarMessage.value = "Найдено ${_matches.value.size} пар"
            } catch (e: Exception) {
                _snackbarMessage.value = "Ошибка поиска: ${e.message}"
            }
        }
    }

    // Функция для очистки сообщения после показа
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
