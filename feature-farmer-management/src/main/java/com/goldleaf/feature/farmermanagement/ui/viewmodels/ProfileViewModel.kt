package com.goldleaf.feature.farmermanagement.ui.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.dto.FarmerUpdateDto
import com.goldleaf.core.util.Result
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import com.goldleaf.feature.farmermanagement.domain.repository.GeocodingRepository
import com.goldleaf.feature.farmermanagement.ui.FarmerProfile
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val userSession: UserSessionManager,
    private val geocodingRepository: GeocodingRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val farmerId: String = checkNotNull(savedStateHandle["farmerId"])
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                // Collect stream from Repository
                farmerRepository.getCurrentFarmer(farmerId).collect { farmer ->
                    if (farmer != null) {
                        val farms = farmerRepository.getFarmerFarms(farmer.id).firstOrNull() ?: emptyList()

                        _uiState.update { it.copy(
                            isLoading = false,
                            farmer = FarmerProfile(
                                id = farmer.id,
                                name = farmer.personalInfo.fullName,
                                email = farmer.contactInfo.email ?: "",
                                phone = farmer.contactInfo.primaryPhone,
                                profileImage = farmer.profileImageUrl,
                                location = "${farmer.contactInfo.address?.district}, ${farmer.contactInfo.address?.region}",
                                totalFarmSize = farmer.farmInfo.totalLandSize,
                                memberSince = farmer.registrationDate.toString().substringBefore("T")
                            ),
                            totalFarms = farms.size,
                            activeCrops = farmer.totalCrops,
                            primaryCrops = farmer.farmInfo.mainCrops
                        )}
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Profile not found") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateProfile(name: String, email: String, location: String) {
        val currentPhone = _uiState.value.farmer?.phone ?: ""

        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(error = "Name cannot be empty") }
                return@launch
            }

            _uiState.update { it.copy(isUpdating = true, error = null, successMessage = null) }

            // Location Logic from your snippet
            val parts = location.split(",").map { it.trim() }
            val district = parts.getOrNull(0) ?: ""
            val region = parts.getOrNull(1) ?: ""

            try {
                val updateDto = FarmerUpdateDto(name, email, currentPhone, district, region)
                val farmerIdToUpdate = _uiState.value.farmer?.id ?: farmerId
                val result = farmerRepository.updateFarmerProfile(farmerIdToUpdate, updateDto)

                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isUpdating = false, successMessage = "Profile updated!") }
                        delay(3000)
                        _uiState.update { it.copy(successMessage = null) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isUpdating = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false, error = e.message) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentDeviceLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true, locationError = null) }

            // Check permission
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                _uiState.update { it.copy(
                    isLoadingLocation = false,
                    locationError = "Location permission required. Please grant access in settings."
                )}
                return@launch
            }

            try {
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setDurationMillis(12000L)          // Wait up to 12 seconds for good fix
                    .setMaxUpdateAgeMillis(60000L)      // Accept location up to 1 min old if fresh not available
                    .build()

                val location = fusedLocationClient.getCurrentLocation(request, null).await()

                if (location != null) {
                    // Get readable address using reverse geocoding
                    val address = geocodingRepository.getAddress(location.latitude, location.longitude)
                    // Use most specific available fields for location
                    val district = address.subCounty.takeIf { it.isNotBlank() } ?: address.ward.takeIf { it.isNotBlank() } ?: address.village.takeIf { it.isNotBlank() } ?: "Unknown District"
                    val region = address.county.takeIf { it.isNotBlank() } ?: address.fullAddress.takeIf { it.isNotBlank() } ?: "Unknown Region"
                    val formattedLocation = "$district, $region"
                    _uiState.update { it.copy(
                        isLoadingLocation = false,
                        locationError = null,
                        gpsLocationRaw = formattedLocation,
                        gpsFormattedAddress = address
                    )}
                } else {
                    _uiState.update { it.copy(
                        isLoadingLocation = false,
                        locationError = "Could not get current location. Make sure GPS is enabled."
                    )}
                }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(
                    isLoadingLocation = false,
                    locationError = "Location permission denied."
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoadingLocation = false,
                    locationError = "Failed to get location: ${e.message ?: "Unknown error"}"
                )}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userSession.clearSession()
            farmerRepository.logoutFarmer()
        }
    }
}


// Combined State to support View + Edit
data class ProfileUiState(
    val farmer: FarmerProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val totalFarms: Int = 0,
    val activeCrops: Int = 0,
    val completedTrainings: Int = 0,
    val primaryCrops: List<String> = emptyList(),
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,
    val gpsLocationRaw: String = "",
    val gpsFormattedAddress: AddressData? = null
)
