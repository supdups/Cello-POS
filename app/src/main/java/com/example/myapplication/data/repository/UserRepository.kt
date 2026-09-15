package com.example.myapplication.data.repository

import com.example.myapplication.data.local.User
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    /** Look up a user by username only — used to restore role info for an existing session. */
    suspend fun getUser(username: String): User? = userDao.getByUsername(username)

    /** Returns the User if the username/password match, or null otherwise. */
    suspend fun authenticate(username: String, password: String): User? {
        val user = userDao.getByUsername(username) ?: return null
        return if (PasswordHasher.matches(password, user.salt, user.passwordHash)) user else null
    }

    suspend fun createUser(username: String, password: String, role: String): Result<Unit> {
        if (userDao.getByUsername(username) != null) {
            return Result.failure(IllegalArgumentException("Username already exists"))
        }
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        userDao.insert(User(username = username, passwordHash = hash, salt = salt, role = role))
        return Result.success(Unit)
    }

    suspend fun updateRole(user: User, newRole: String) {
        userDao.update(user.copy(role = newRole))
    }

    suspend fun resetPassword(user: User, newPassword: String) {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(newPassword, salt)
        userDao.update(user.copy(passwordHash = hash, salt = salt))
    }

    suspend fun deleteUser(user: User) {
        userDao.delete(user)
    }
}
