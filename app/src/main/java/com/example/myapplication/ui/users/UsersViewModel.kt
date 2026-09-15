package com.example.myapplication.ui.users

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.User
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewUserForm(
    val username: String = "",
    val password: String = "",
    val role: String = "cashier"
)

class UsersViewModel(
    private val repository: UserRepository
) : ViewModel() {

    val users: StateFlow<List<User>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _form = MutableStateFlow(NewUserForm())
    val form: StateFlow<NewUserForm> = _form

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onUsernameChange(value: String) = _form.update { it.copy(username = value) }
    fun onPasswordChange(value: String) = _form.update { it.copy(password = value) }
    fun onRoleChange(value: String) = _form.update { it.copy(role = value) }

    // Create
    fun addUser() {
        val current = _form.value
        if (current.username.isBlank() || current.password.length < 4) {
            _error.value = "Username required, password must be 4+ characters"
            return
        }
        viewModelScope.launch {
            val result = repository.createUser(current.username.trim(), current.password, current.role)
            result.onSuccess {
                _form.value = NewUserForm()
                _error.value = null
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    // Update
    fun toggleRole(user: User) {
        val newRole = if (user.role == "admin") "cashier" else "admin"
        viewModelScope.launch { repository.updateRole(user, newRole) }
    }

    // Delete
    fun deleteUser(user: User) {
        viewModelScope.launch { repository.deleteUser(user) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dao = AppDatabase.getInstance(context).userDao()
            @Suppress("UNCHECKED_CAST")
            return UsersViewModel(UserRepository(dao)) as T
        }
    }
}
