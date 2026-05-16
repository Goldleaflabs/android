package com.goldleaf.feature.farmermanagement.ui.auth.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.OTPRequest
import com.goldleaf.core.data.dto.auth.OTPVerificationRequest
import com.goldleaf.core.data.dto.auth.ResetPasswordRequest
import com.goldleaf.core.data.dto.auth.OTPAction
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.Idle)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _resetToken = MutableStateFlow("")

    fun onPhoneChange(value: String) { _phone.value = value }
    fun onOTPChange(value: String) { _otp.value = value }
    fun onNewPasswordChange(value: String) { _newPassword.value = value }

    // Step 1: Request OTP via phone
    fun requestOTP() {
        viewModelScope.launch {
            if (_phone.value.isBlank()) {
                _recoveryState.value = RecoveryState.Error("Please enter your phone number")
                return@launch
            }

            _recoveryState.value = RecoveryState.Loading

            val request = OTPRequest(
                phoneNumber = _phone.value,
                action = OTPAction.PASSWORD_RESET
            )

            val result = farmerRepository.sendOTP(request)

            when (result) {
                is com.goldleaf.core.util.Result.Success -> {
                    _recoveryState.value = RecoveryState.OTPSent("OTP sent to your phone number")
                }
                is com.goldleaf.core.util.Result.Error -> {
                    _recoveryState.value = RecoveryState.Error(result.message)
                }
            }
        }
    }

    // Step 2: Verify OTP
    fun verifyOTP() {
        viewModelScope.launch {
            if (_otp.value.isBlank()) {
                _recoveryState.value = RecoveryState.Error("Please enter OTP")
                return@launch
            }

            _recoveryState.value = RecoveryState.Loading

            val request = OTPVerificationRequest(
                phoneNumber = _phone.value,
                otpCode = _otp.value,
                action = OTPAction.PASSWORD_RESET
            )

            val result = farmerRepository.verifyOTP(request)

            when (result) {
                is com.goldleaf.core.util.Result.Success -> {
                    _resetToken.value = _phone.value  // Use phone as identifier for password reset
                    _recoveryState.value = RecoveryState.OTPVerified
                }
                is com.goldleaf.core.util.Result.Error -> {
                    _recoveryState.value = RecoveryState.Error(result.message)
                }
            }
        }
    }

    // Step 3: Reset Password
    fun resetPassword() {
        viewModelScope.launch {
            if (_newPassword.value.length < 6) {
                _recoveryState.value = RecoveryState.Error("Password must be at least 6 characters")
                return@launch
            }

            _recoveryState.value = RecoveryState.Loading

            try {
                // ✅ Call API to reset password with reset token + new password
                val request = ResetPasswordRequest(
                    resetToken = _resetToken.value,
                    newPassword = _newPassword.value
                )
                
                val response = apiService.resetPassword(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _recoveryState.value = RecoveryState.PasswordResetSuccess
                } else {
                    _recoveryState.value = RecoveryState.Error(
                        response.body()?.message ?: "Password reset failed"
                    )
                }
            } catch (e: Exception) {
                _recoveryState.value = RecoveryState.Error(e.message ?: "Password reset failed")
            }
        }
    }

    fun clearError() {
        if (_recoveryState.value is RecoveryState.Error) {
            _recoveryState.value = RecoveryState.Idle
        }
    }
}

sealed class RecoveryState {
    object Idle : RecoveryState()
    object Loading : RecoveryState()
    data class OTPSent(val message: String) : RecoveryState()
    object OTPVerified : RecoveryState()
    object PasswordResetSuccess : RecoveryState()
    data class Error(val message: String) : RecoveryState()
}