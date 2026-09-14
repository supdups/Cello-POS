package com.example.myapplication.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        // TODO: replace with a real check against a cashier account (local DB or backend)
        if (state.username.isNotBlank() && state.password.length >= 4) {
            _uiState.update { it.copy(isLoggedIn = true, error = null) }
        } else {
            _uiState.update { it.copy(error = "Enter a username and a password (4+ characters)") }
        }
    }
}
