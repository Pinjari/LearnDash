package com.intellipaat.learndash.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionStore by preferencesDataStore("session")

/** Just the logged-in email (mock auth — no token to keep). Survives restarts. */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val EMAIL = stringPreferencesKey("email")
    }

    val email: Flow<String?> = context.sessionStore.data.map { it[Keys.EMAIL] }

    suspend fun saveEmail(email: String) {
        context.sessionStore.edit { it[Keys.EMAIL] = email }
    }

    suspend fun clear() {
        context.sessionStore.edit { it.remove(Keys.EMAIL) }
    }
}
