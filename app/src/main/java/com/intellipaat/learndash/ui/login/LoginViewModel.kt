package com.intellipaat.learndash.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intellipaat.learndash.domain.repo.AuthRepository
import com.intellipaat.learndash.domain.repo.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUi(
    val email: String = "learner@intellipaat.com",
    val password: String = "learn123",
    val busy: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUi())
    val ui: StateFlow<LoginUi> = _ui.asStateFlow()

    init {
        // Returning session? Bounce straight to the dashboard.
        viewModelScope.launch {
            auth.loggedInEmail.collect { email ->
                if (!email.isNullOrBlank()) _ui.value = _ui.value.copy(loggedIn = true)
            }
        }
    }

    fun onEmail(v: String) { _ui.value = _ui.value.copy(email = v, error = null) }
    fun onPassword(v: String) { _ui.value = _ui.value.copy(password = v, error = null) }

    fun canSubmit(): Boolean {
        val s = _ui.value
        return !s.busy && s.email.isNotBlank() && s.password.isNotBlank()
    }

    fun login() {
        val cur = _ui.value
        if (!canSubmit()) return
        viewModelScope.launch {
            _ui.value = cur.copy(busy = true, error = null)
            when (val res = auth.login(cur.email, cur.password)) {
                is LoginResult.Ok -> _ui.value = _ui.value.copy(busy = false, loggedIn = true)
                is LoginResult.Rejected -> _ui.value = _ui.value.copy(busy = false, error = res.message)
            }
        }
    }
}
