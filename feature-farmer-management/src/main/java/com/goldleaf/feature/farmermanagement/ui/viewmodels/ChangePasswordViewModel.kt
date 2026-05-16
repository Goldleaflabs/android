package com.goldleaf.feature.farmermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.util.Result
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ===== UI STATE DATA CLASS =====
data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            try {
                // --- Validation Logic ---
                if (currentPassword.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Current password is required"
                        )
                    }
                    return@launch
                }

                if (newPassword.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "New password is required"
                        )
                    }
                    return@launch
                }

                if (newPassword.length < 8) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Password must be at least 8 characters"
                        )
                    }
                    return@launch
                }

                if (newPassword != confirmPassword) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Passwords do not match"
                        )
                    }
                    return@launch
                }

                if (currentPassword == newPassword) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "New password must be different from current password"
                        )
                    }
                    return@launch
                }

                if (!isPasswordStrong(newPassword)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Password must contain uppercase, lowercase, and numbers"
                        )
                    }
                    return@launch
                }

                // --- API Call ---
                val result: Result<Unit> = farmerRepository.changePassword(currentPassword, newPassword)

                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Password changed successfully"
                            )
                        }

                        // Clear success message after 3 seconds
                        delay(3000)
                        _uiState.update { it.copy(successMessage = null) }
                    }
                    is Result.Error -> {
                        val errorMessage = when {
                            result.message.contains("incorrect", ignoreCase = true) == true ->
                                "Current password is incorrect"
                            else -> result.message
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return hasUppercase && hasLowercase && hasDigit
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}