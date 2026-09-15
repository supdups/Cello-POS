package com.example.myapplication.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val isCheckingSession: Boolean = true,
    val loggedInUsername: String? = null,
    val loggedInRole: String? = null
) {
    val isLoggedIn: Boolean get() = loggedInUsername != null
    val isAdmin: Boolean get() = loggedInRole == "admin"
}

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        // Watch the persisted session. This fires once on startup (restoring a
        // previous login) and again immediately whenever logout() clears it.
        viewModelScope.launch {
            sessionManager.loggedInUsername.collect { username ->
                val role = if (username != null) lookupRole(username) else null
                _uiState.update {
                    it.copy(
                        loggedInUsername = username,
                        loggedInRole = role,
                        isCheckingSession = false
                    )
                }
            }
        }
    }

    private suspend fun lookupRole(username: String): String? {
        return userRepository.getUser(username)?.role
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            val user = userRepository.authenticate(state.username.trim(), state.password)
            if (user != null) {
                sessionManager.saveSession(user.username)
                _uiState.update { it.copy(password = "", error = null) }
            } else {
                _uiState.update { it.copy(error = "Invalid username or password") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.update { it.copy(username = "", password = "") }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val userDao = AppDatabase.getInstance(context).userDao()
            val repository = UserRepository(userDao)
            val session = UserSessionManager(context)
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository, session) as T
        }
    }
}
