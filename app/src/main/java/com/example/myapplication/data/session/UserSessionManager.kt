package com.example.myapplication.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

/**
 * Persists who's currently logged in, so the app doesn't ask for login again
 * every time it's reopened — only after an explicit logout.
 */
class UserSessionManager(private val context: Context) {

    private val usernameKey = stringPreferencesKey("logged_in_username")

    val loggedInUsername: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[usernameKey]
    }

    suspend fun saveSession(username: String) {
        context.dataStore.edit { prefs -> prefs[usernameKey] = username }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs -> prefs.remove(usernameKey) }
    }
}
