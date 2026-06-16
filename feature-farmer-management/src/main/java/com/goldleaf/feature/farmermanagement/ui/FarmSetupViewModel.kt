package com.goldleaf.feature.farmermanagement.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.FarmCreateRequest
import com.goldleaf.core.data.dto.auth.LocationDto
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.feature.farmermanagement.domain.repository.GeocodingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject




import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.feature.farmermanagement.ui.viewmodels.AddressData
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject


@HiltViewModel
class FarmSetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val userSession: UserSessionManager,
    private val geocodingRepository: GeocodingRepository,
    private val farmDao: FarmDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmSetupUiState())
    val uiState: StateFlow<FarmSetupUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()

    fun searchLocation(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoadingLocation = true) }

                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=5&countrycodes=ke"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "GoldLeafFarmerPortal/1.0 (judy@goldleaflabs.co.ke)")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                val suggestions = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    suggestions.add(obj.getString("display_name"))
                }

                _searchResults.value = suggestions
                _uiState.update { it.copy(isLoadingLocation = false) }

            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        error = "Failed to search location: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectLocation(locationName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoadingLocation = true) }

                val encodedQuery = URLEncoder.encode(locationName, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=1"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "GoldLeafFarmerPortal/1.0 (judy@goldleaflabs.co.ke)")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                if (jsonArray.length() > 0) {
                    val obj = jsonArray.getJSONObject(0)
                    val lat = obj.getDouble("lat")
                    val lon = obj.getDouble("lon")
                    val address = obj.optJSONObject("address")

                    val district = address?.optString("county")
                        ?: address?.optString("state_district")
                        ?: address?.optString("suburb")
                        ?: "Unknown"

                    val region = address?.optString("state")
                        ?: address?.optString("region")
                        ?: "Kenya"

                    _uiState.update {
                        it.copy(
                            currentLocation = LatLng(lat, lon),
                            district = district,
                            region = region,
                            locationName = locationName,
                            isLoadingLocation = false,
                            error = null
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        error = "Failed to get location details: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearLocation() {
        _uiState.update {
            it.copy(
                currentLocation = null,
                district = "",
                region = "",
                locationName = "",
                error = null
            )
        }
        _searchResults.value = emptyList()
    }

    /**
     * Gets the current device location using GPS/network
     * - Requests fresh location (not just cached last location)
     * - Does reverse geocoding to fill locationName, district, region
     */
    fun getCurrentDeviceLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true, error = null) }

            // Check permission
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        error = "Location permission required. Please grant access in settings."
                    )
                }
                return@launch
            }

            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setDurationMillis(12000L)          // Wait up to 12 seconds for good fix
                    .setMaxUpdateAgeMillis(60000L)      // Accept location up to 1 min old if fresh not available
                    .build()

                val location = fusedLocationClient.getCurrentLocation(request, null).await()

                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)

                    // Get readable address using Nominatim reverse geocoding
                    val address = reverseGeocode(location.latitude, location.longitude)

                    _uiState.update {
                        it.copy(
                            currentLocation = latLng,
                            locationName = address?.fullAddress ?: "Current Location",
                            district = address?.county ?: "Unknown",
                            region = address?.ward ?: "Kenya",
                            isLoadingLocation = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            error = "Could not get current location. Make sure GPS is enabled."
                        )
                    }
                }
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        error = "Location permission denied."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        error = "Failed to get location: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }


    private suspend fun reverseGeocode(lat: Double, lng: Double): AddressData = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
            val url = URL(urlString)

            // Use HttpURLConnection to add the required User-Agent header
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "GoldLeafFarmerPortal/1.0 (judy@goldleaflabs.co.ke)")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val address = json.optJSONObject("address")

            AddressData(
                // Mapping for Kenyan Administrative boundaries
                county = address?.optString("county") ?: address?.optString("state") ?: "Kenya",
                subCounty = address?.optString("state_district") ?: address?.optString("suburb") ?: "Sub-County Unknown",
                ward = address?.optString("locality") ?: address?.optString("town") ?: address?.optString("village") ?: "Ward Unknown",
                village = address?.optString("village") ?: address?.optString("hamlet") ?: "Village Unknown",
                fullAddress = json.optString("display_name", "Lat: $lat, Lon: $lng")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AddressData("Kenya", "", "", "", "Lat: $lat, Lon: $lng")
        }
    }
    private suspend fun extractVillageFromLocation(location: GeoPoint): String {
        val result = reverseGeocode(location.latitude, location.longitude)
        return result.village.takeIf { it.isNotBlank() } ?: "Village Unknown"
    }

    private suspend fun extractDistrictFromLocation(location: GeoPoint): String {
        val result = reverseGeocode(location.latitude, location.longitude)
        return result.subCounty.takeIf { it.isNotBlank() } ?: "Sub-County Unknown"
    }

    private suspend fun extractRegionFromLocation(location: GeoPoint): String {
        val result = reverseGeocode(location.latitude, location.longitude)
        return result.county.takeIf { it.isNotBlank() } ?: "Kenya"
    }

    // UPDATED: Now uses coordinates and names from the UI state
    fun saveFarmManually(
        name: String,
        size: Double,
        referenceNumber: String
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val center = currentState.currentLocation
                    ?: throw IllegalStateException("Please get GPS location first")

                val boundary = generateApproximateBoundary(center, size)

                val response = apiService.createFarm(
                    farmerId = userSession.getCurrentUserId() ?: throw IllegalStateException("Not logged in"),
                    farm = FarmCreateRequest(
                        name = name,
                        size = size,
                        locationName = currentState.locationName,
                        region = currentState.region.takeIf { it.isNotBlank() },
                        documentReference = referenceNumber.takeIf { it.isNotBlank() },
                        location = LocationDto(center.latitude, center.longitude),
                        boundaries = boundary.map { LocationDto(it.latitude, it.longitude) }
                    )
                )

                if (response.isSuccessful) {
                    // EXTRACT THE FARM ID FROM THE RESPONSE
                    val createdFarm = response.body()

                    // ✅ Better: Check and provide helpful error
                    if (createdFarm == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Server returned empty response. Please try again."
                            )
                        }
                        return@launch
                    }

                    val farmId = createdFarm.id
                    if (farmId.isNullOrBlank()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Farm was created but no ID was returned. Please refresh the farm list."
                            )
                        }
                        return@launch
                    }

                    // Persist the created farm locally for offline access and coordinate lookup
                    farmDao.insertFarm(createdFarm.toEntity(userSession.getCurrentUserId() ?: farmId))

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            farmSaved = true,
                            newFarmId = farmId
                        )
                    }

                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Save failed: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetFarmSaved() {
        _uiState.update { it.copy(farmSaved = false, newFarmId = null) }
    }

    private fun generateApproximateBoundary(center: LatLng, sizeInAcres: Double): List<LatLng> {
        val areaSqM = sizeInAcres * 4046.86
        val side = kotlin.math.sqrt(areaSqM)
        val latOffset = (side / 2) / 111320.0
        val lngOffset = (side / 2) / (111320.0 * kotlin.math.cos(Math.toRadians(center.latitude)))

        return listOf(
            LatLng(center.latitude + latOffset, center.longitude - lngOffset),
            LatLng(center.latitude + latOffset, center.longitude + lngOffset),
            LatLng(center.latitude - latOffset, center.longitude + lngOffset),
            LatLng(center.latitude - latOffset, center.longitude - lngOffset)
        )
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun updateLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val address = reverseGeocode(latitude, longitude)
            _uiState.update {
                it.copy(
                    currentLocation = LatLng(latitude, longitude),
                    locationName = address.fullAddress,
                    district = address.county,
                    region = address.ward
                )
            }
        }
    }
}

data class FarmSetupUiState(
    val isLoading: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val error: String? = null,
    val currentLocation: LatLng? = null,
    val district: String = "",
    val region: String = "",
    val locationName: String = "",
    val farmSaved: Boolean = false,
    val isSuccess: Boolean = false,
    val newFarmId: String? = null
)
