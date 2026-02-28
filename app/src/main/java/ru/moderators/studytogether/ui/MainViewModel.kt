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

    fun createUser(name: String, email: String) {
        viewModelScope.launch {
            val user = User(name = name, email = email)
            val created = ApiClient.createUser(user)
            _currentUser.value = created
        }
    }

    fun createClaim(type: ClaimType, subject: String, grade: Int, topic: String = "") {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            val claim = Claim(
                userId = userId,
                type = type,
                subject = subject,
                grade = grade,
                topic = topic
            )
            ApiClient.createClaim(claim)
            loadAllClaims() // обновляем список
        }
    }

    fun loadAllClaims() {
        viewModelScope.launch {
            _claims.value = ApiClient.getAllClaims()
        }
    }

    fun findMatchesForClaim(claimId: String) {
        viewModelScope.launch {
            _matches.value = ApiClient.getMatchesForClaim(claimId)
        }
    }
}