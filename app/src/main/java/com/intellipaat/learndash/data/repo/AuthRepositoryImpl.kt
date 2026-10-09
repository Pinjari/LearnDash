package com.intellipaat.learndash.data.repo

import android.util.Patterns
import com.intellipaat.learndash.data.prefs.SessionStore
import com.intellipaat.learndash.domain.repo.AuthRepository
import com.intellipaat.learndash.domain.repo.LoginResult
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock auth: any well-formed email + 6-char password signs in after a beat.
 * Seeded demo account (learner@intellipaat.com / learn123) works too.
 */
class AuthRepositoryImpl @Inject constructor(
    private val session: SessionStore
) : AuthRepository {

    override val loggedInEmail = session.email

    override suspend fun login(email: String, password: String): LoginResult {
        val clean = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(clean).matches()) {
            return LoginResult.Rejected("Enter a valid email address.")
        }
        if (password.length < 6) {
            return LoginResult.Rejected("Password needs at least 6 characters.")
        }
        delay(800) // mock network
        session.saveEmail(clean)
        return LoginResult.Ok(clean)
    }

    override suspend fun logout() = session.clear()
}
