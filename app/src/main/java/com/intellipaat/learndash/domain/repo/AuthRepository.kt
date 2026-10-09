package com.intellipaat.learndash.domain.repo

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val loggedInEmail: Flow<String?>
    suspend fun login(email: String, password: String): LoginResult
    suspend fun logout()
}

sealed interface LoginResult {
    data class Ok(val email: String) : LoginResult
    data class Rejected(val message: String) : LoginResult
}
