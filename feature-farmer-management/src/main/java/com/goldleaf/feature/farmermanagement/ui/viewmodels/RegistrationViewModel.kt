package com.goldleaf.feature.farmermanagement.ui.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.dto.auth.OTPAction
import com.goldleaf.core.data.dto.auth.OTPRequest
import com.goldleaf.core.data.dto.auth.OTPVerificationRequest
import com.goldleaf.core.data.dto.farm.Address
import com.goldleaf.core.data.dto.farm.ContactInfo
import com.goldleaf.core.data.dto.farm.FarmInfo
import com.goldleaf.core.data.dto.farm.FarmerRegistrationRequest
import com.goldleaf.core.data.dto.farm.PersonalInfo
import com.goldleaf.core.util.Result
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import com.goldleaf.feature.farmermanagement.domain.repository.GeocodingRepository
import com.goldleaf.feature.farmermanagement.ui.screens.RegistrationStep
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val geocodingRepository: GeocodingRepository // Add this
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private var otpTimerJob: Job? = null

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, error = null) }
    }


    fun sendOTP() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = farmerRepository.sendOTP(
                OTPRequest(
                    phoneNumber = _uiState.value.phoneNumber,
                    action = OTPAction.REGISTRATION
                )
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentStep = RegistrationStep.OTP_VERIFICATION
                        )
                    }
                    startOtpTimer()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun updateOtpCode(code: String) {
        _uiState.update { it.copy(otpCode = code, error = null) }
    }

    fun verifyOTP() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = farmerRepository.verifyOTP(
                OTPVerificationRequest(
                    phoneNumber = _uiState.value.phoneNumber,
                    otpCode = _uiState.value.otpCode,
                    action = OTPAction.REGISTRATION
                )
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentStep = RegistrationStep.FARMER_DETAILS
                        )
                    }
                    otpTimerJob?.cancel()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun resendOTP() {
        sendOTP()
    }

    fun updateFirstName(name: String) {
        _uiState.update { it.copy(firstName = name, error = null) }
    }

    fun updateLastName(name: String) {
        _uiState.update { it.copy(lastName = name, error = null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun updateDistrict(district: String) {
        _uiState.update { it.copy(district = district, error = null) }
    }

    fun updateRegion(region: String) {
        _uiState.update { it.copy(region = region, error = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }


    fun updateFarmName(name: String) {
        _uiState.update { it.copy(farmName = name) }
    }

    fun updateLocation(loc: String) {
        _uiState.update { it.copy(location = loc) }
    }

    fun registerFarmer() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.password != currentState.confirmPassword) {
                _uiState.update { it.copy(error = "Passwords do not match") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = FarmerRegistrationRequest(
                personalInfo = PersonalInfo(
                    firstName = currentState.firstName,
                    lastName = currentState.lastName
                ),
                contactInfo = ContactInfo(
                    primaryPhone = currentState.phoneNumber,
                    email = currentState.email.takeIf { it.isNotBlank() },
                    address = Address(
                        district = currentState.district,
                        region = currentState.region,
                        latitude = currentState.latitude ?: 0.0,
                        longitude = currentState.longitude ?: 0.0,
                        country = "Kenya"
                    )
                ),
                farmInfo = FarmInfo(farmName = currentState.farmName),
                password = currentState.password

            )

            val result = farmerRepository.registerFarmer(request)

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRegistrationComplete = true)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun goBackToPhoneInput() {
        _uiState.update {
            it.copy(
                currentStep = RegistrationStep.PHONE_INPUT,
                otpCode = "",
                error = null
            )
        }
        otpTimerJob?.cancel()
    }

    fun goBackToOtpVerification() {
        _uiState.update {
            it.copy(
                currentStep = RegistrationStep.OTP_VERIFICATION,
                error = null
            )
        }
    }

    private fun startOtpTimer() {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(otpTimeRemaining = i) }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        otpTimerJob?.cancel()
    }

    // Add this method to RegistrationViewModel
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(fusedLocationClient: FusedLocationProviderClient) {
        _uiState.update { it.copy(isLoading = true) } // Show loader on the button

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _uiState.update { it.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    isUsingGps = true,
                    isLoading = false
                )}
                updateAddressFromCoordinates(location.latitude, location.longitude)
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "GPS signal weak. Please try again or enter manually."
                )}
            }
        }.addOnFailureListener { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    private fun updateAddressFromCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch {
            val addressData = geocodingRepository.getAddress(lat, lng)
            _uiState.update { it.copy(
                district = addressData.subCounty,
                region = addressData.county,
                location = addressData.fullAddress
            )}
        }
    }
}

data class RegistrationUiState(
    val currentStep: RegistrationStep = RegistrationStep.PHONE_INPUT,
    val phoneNumber: String = "",
    val otpCode: String = "",
    val otpTimeRemaining: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val district: String = "",
    val region: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationComplete: Boolean = false,
    val farmName: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isUsingGps: Boolean = false
)