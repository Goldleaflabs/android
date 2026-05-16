package com.goldleaf.feature.farmermanagement.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userSession: UserSessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        viewModelScope.launch {
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _loginState.value = LoginState.Error("Please fill all fields")
                return@launch
            }

            _loginState.value = LoginState.Loading


            try {
                val response = apiService.login(LoginRequest(_email.value, _password.value))
            // First: check HTTP status
                if (!response.isSuccessful) {
                    _loginState.value = LoginState.Error(
                        response.message().ifBlank { "Login failed" }
                    )
                    return@launch
                }

                val body = response.body()
                    ?: run {
                        _loginState.value = LoginState.Error("Empty response")
                        return@launch
                    }

              //   Second: check business logic success
                if (!body.success) {
                    _loginState.value = LoginState.Error(body.message )
                    return@launch
                }

             //    Now we know it's real success
                val farmerId = body.farmer?.id
                    ?: run {
                        _loginState.value = LoginState.Error("Missing farmer data")
                        return@launch
                    }

                val token = body.token
                    ?: run {
                        _loginState.value = LoginState.Error("Missing token")
                        return@launch
                    }

                userSession.startSession(userId = farmerId, authToken = token)
                _loginState.value = LoginState.Success(farmerId)

            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Network error")
            }

        }
    }

    fun logout() {
        viewModelScope.launch {
            userSession.clearSession()
            _loginState.value = LoginState.Idle
        }
    }

    fun clearError() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val farmerId: String) : LoginState()
    data class Error(val message: String) : LoginState()
}