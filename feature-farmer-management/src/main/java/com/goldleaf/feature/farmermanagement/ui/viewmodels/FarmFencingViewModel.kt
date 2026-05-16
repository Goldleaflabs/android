package com.goldleaf.feature.farmermanagement.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmFacility
import com.goldleaf.core.data.dto.farm.FarmLocation
import com.goldleaf.core.data.dto.farm.FarmStatus
import com.goldleaf.core.data.dto.farm.FarmingType
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.data.dto.farm.LandUnit
import com.goldleaf.core.data.dto.farm.WaterSource
import com.goldleaf.core.util.Result
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import com.goldleaf.feature.farmermanagement.domain.repository.GeocodingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Math.abs
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@HiltViewModel
class FarmFencingViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val geocodingRepository: GeocodingRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmFencingUiState())
    val uiState: StateFlow<FarmFencingUiState> = _uiState.asStateFlow()
    // =====================================================
    // Farm Loading and Initialization
    // =====================================================

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> get() = _searchResults

    fun searchLocation(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // --- Fetch suggestions from Nominatim ---
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url =
                    "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=5"
                val response = URL(url).readText()
                val jsonArray = JSONArray(response)

                val suggestions = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    suggestions.add(obj.getString("display_name"))
                }

                _searchResults.value = suggestions

                // --- Get coordinates of the first suggestion to center map ---
                if (suggestions.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = true) }

                    val coordsResult = geocodingRepository.getCoordinates(suggestions[0])
                    when (coordsResult) {
                        is Result.Success -> {
                            val coords = coordsResult.data
                            val newCenter = GeoPoint(coords.latitude, coords.longitude)
                            _uiState.update {
                                it.copy(
                                    mapCenter = newCenter,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Location not found"
                                )
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
                _uiState.update { it.copy(isLoading = false, error = "Failed to fetch location") }
            }
        }
    }

    fun loadFarm(farmId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val response = apiService.getFarmById(farmId)

                if (response.isSuccessful) {
                    val dto = response.body()
                    if (dto != null) {
                        // BRIDGE: Convert the "flat" DTO to your complex "Farm" Entity
                        val farmEntity = Farm(
                            id = dto.id ,
                            name = dto.name ,
                            location = FarmLocation(
                                centerLatitude = dto.location.latitude ,
                                centerLongitude = dto.location.longitude
                            ),
                            totalSize = dto.totalSize,
                            boundaries = dto.boundaries.map { locationDto ->
                                GeoPoint(
                                    latitude = locationDto.latitude,
                                    longitude = locationDto.longitude
                                )
                            }
                        )

                        _uiState.update { it.copy(isLoading = false) }
                        updateStateFromFarm(farmEntity)
                    }
                } else {
                    _uiState.update { it.copy(error = "Farm not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    // =====================================================
    // Boundary Management
    // =====================================================

    fun addBoundaryPoint(point: GeoPoint) {
        val currentBoundaries = _uiState.value.boundaries.toMutableList()
        currentBoundaries.add(point)

        _uiState.update {
            it.copy(
                boundaries = currentBoundaries,
                calculatedArea = calculateArea(currentBoundaries),
                calculatedPerimeter = calculatePerimeter(currentBoundaries),
                mapCenter = if (it.mapCenter == null) point else it.mapCenter
            )
        }
    }

    fun removeBoundaryPoint(index: Int) {
        val currentBoundaries = _uiState.value.boundaries.toMutableList()
        if (index in currentBoundaries.indices) {
            currentBoundaries.removeAt(index)
            _uiState.update {
                it.copy(
                    boundaries = currentBoundaries,
                    calculatedArea = calculateArea(currentBoundaries),
                    calculatedPerimeter = calculatePerimeter(currentBoundaries)
                )
            }
        }
    }

    private fun updateStateFromFarm(farm: Farm) {
        _uiState.update {
            it.copy(
                farm = farm,
                farmName = farm.name,
                farmDescription = farm.description ?: "",
                farmType = farm.farmType,
                soilType = farm.soilType ?: "",
                boundaries = farm.boundaries,
                waterSources = farm.waterSources,
                facilities = farm.facilities,
                mapCenter = GeoPoint(
                    farm.location.centerLatitude,
                    farm.location.centerLongitude
                ),
                calculatedArea = farm.totalSize,
                calculatedPerimeter = calculatePerimeter(farm.boundaries)
            )
        }
    }

    fun updateBoundaryPoint(index: Int, newPoint: GeoPoint) {
        val currentBoundaries = _uiState.value.boundaries.toMutableList()
        if (index in currentBoundaries.indices) {
            currentBoundaries[index] = newPoint

            _uiState.update {
                it.copy(
                    boundaries = currentBoundaries,
                    calculatedArea = calculateArea(currentBoundaries),
                    calculatedPerimeter = calculatePerimeter(currentBoundaries)
                )
            }
        }
    }


    fun saveFarmFencing() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Basic validation
            if (currentState.boundaries.size < 3) {
                _uiState.update { it.copy(error = "A valid fence requires at least 3 points") }
                return@launch
            }

            // ✅ CRITICAL: Ensure farm data is loaded before saving
            if (currentState.farm == null) {
                _uiState.update { it.copy(error = "Farm data not loaded. Please wait or refresh.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Calculate the new center based on the boundary points
            val newCenter = calculateCenterPoint(currentState.boundaries)

            // 2. Prepare the updated farm object
            val currentFarm = currentState.farm  // Smart cast - null check above ensures it's non-null
            val updatedFarm = currentFarm.copy(
                boundaries = currentState.boundaries,
                totalSize = currentState.calculatedArea,
                location = currentFarm.location.copy(
                    centerLatitude = newCenter.latitude,
                    centerLongitude = newCenter.longitude
                )
            )

            // 3. Push to repository
            when (val result = farmerRepository.updateFarm(updatedFarm)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true,
                            farm = result.data
                        )
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


    fun clearBoundaries() {
        _uiState.update {
            it.copy(
                boundaries = emptyList(),
                calculatedArea = 0.0,
                calculatedPerimeter = 0.0
            )
        }
    }

    fun completeBoundary() {
        val boundaries = _uiState.value.boundaries
        if (boundaries.size >= 3) {
            _uiState.update {
                it.copy(
                    calculatedArea = calculateArea(boundaries),
                    calculatedPerimeter = calculatePerimeter(boundaries)
                )
            }
        }
    }

    // =====================================================
    // Location and Map Management
    // =====================================================

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                _uiState.update { it.copy(mapCenter = geoPoint, currentLocation = geoPoint) }
            }
        }
    }

    fun updateMapCenter(center: GeoPoint) {
        _uiState.update { it.copy(mapCenter = center) }
    }

    // =====================================================
    // Farm Details Management
    // =====================================================

    fun updateFarmName(name: String) {
        _uiState.update { it.copy(farmName = name, error = null) }
    }

    fun updateFarmDescription(description: String) {
        _uiState.update { it.copy(farmDescription = description) }
    }

    fun updateFarmType(type: FarmingType) {
        _uiState.update { it.copy(farmType = type) }
    }

    fun updateSoilType(soilType: String) {
        _uiState.update { it.copy(soilType = soilType) }
    }

    // =====================================================
    // Water Sources Management
    // =====================================================

    fun addWaterSource(waterSource: WaterSource) {
        val currentSources = _uiState.value.waterSources.toMutableList()
        val newSource = waterSource.copy(
            id = generateId("water"),
            location = _uiState.value.mapCenter ?: GeoPoint(0.0, 0.0)
        )
        currentSources.add(newSource)
        _uiState.update { it.copy(waterSources = currentSources) }
    }

    fun removeWaterSource(waterSourceId: String) {
        val currentSources = _uiState.value.waterSources.toMutableList()
        currentSources.removeIf { it.id == waterSourceId }
        _uiState.update { it.copy(waterSources = currentSources) }
    }

    fun updateWaterSource(waterSourceId: String, updatedSource: WaterSource) {
        val currentSources = _uiState.value.waterSources.toMutableList()
        val index = currentSources.indexOfFirst { it.id == waterSourceId }
        if (index != -1) {
            currentSources[index] = updatedSource
            _uiState.update { it.copy(waterSources = currentSources) }
        }
    }

    // =====================================================
    // Farm Facilities Management
    // =====================================================

    fun addFacility(facility: FarmFacility) {
        val currentFacilities = _uiState.value.facilities.toMutableList()
        val newFacility = facility.copy(
            id = generateId("facility"),
            location = _uiState.value.mapCenter ?: GeoPoint(0.0, 0.0)
        )
        currentFacilities.add(newFacility)
        _uiState.update { it.copy(facilities = currentFacilities) }
    }

    fun removeFacility(facilityId: String) {
        val currentFacilities = _uiState.value.facilities.toMutableList()
        currentFacilities.removeIf { it.id == facilityId }
        _uiState.update { it.copy(facilities = currentFacilities) }
    }

    fun updateFacility(facilityId: String, updatedFacility: FarmFacility) {
        val currentFacilities = _uiState.value.facilities.toMutableList()
        val index = currentFacilities.indexOfFirst { it.id == facilityId }
        if (index != -1) {
            currentFacilities[index] = updatedFacility
            _uiState.update { it.copy(facilities = currentFacilities) }
        }
    }

    // =====================================================
    // Farm Saving and Validation
    // =====================================================
// Now, your "extract" functions become very simple:
    private suspend fun getDetails(location: GeoPoint): AddressData {
        return geocodingRepository.getAddress(location.latitude, location.longitude)
    }


    fun saveFarm() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Validation
            if (currentState.farmName.isBlank()) {
                _uiState.update { it.copy(error = "Farm name is required") }
                return@launch
            }

            if (currentState.boundaries.size < 3) {
                _uiState.update { it.copy(error = "Please mark at least 3 boundary points to create a valid farm area") }
                return@launch
            }

            if (currentState.calculatedArea <= 0) {
                _uiState.update { it.copy(error = "Invalid farm area calculated. Please check boundary points") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val centerPoint = calculateCenterPoint(currentState.boundaries)
            val addressDetails = getDetails(centerPoint) // Fetch all info at once!

            val farm = currentState.farm?.let {
                Farm(
                    id = currentState.farm.id,
                    name = currentState.farmName,
                    description = currentState.farmDescription.takeIf { it.isNotBlank() },
                    location = FarmLocation(
                        centerLatitude = centerPoint.latitude,
                        centerLongitude = centerPoint.longitude,
                        village = addressDetails.village,
                        district = addressDetails.subCounty,
                        region = addressDetails.county,
                        address = "${addressDetails.fullAddress} Farm",
                        country = "Kenya"
                    ),
                    totalSize = currentState.calculatedArea,
                    sizeUnit = LandUnit.ACRES,
                    farmType = currentState.farmType,
                    boundaries = currentState.boundaries,
                    waterSources = currentState.waterSources,
                    facilities = currentState.facilities,
                    soilType = currentState.soilType.takeIf { it.isNotBlank() },
                    registrationDate = it.registrationDate,
                    status = FarmStatus.ACTIVE
                )
            }

            val result = if (currentState.farm?.id?.isNotBlank() == true) {
                farmerRepository.updateFarm(farm!!.copy(id = currentState.farm.id))
            } else {
                farm?.let { farmerRepository.addFarm(it) }
            }

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            farm = result.data,
                            isSaved = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                null -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to create farm object for saving.")
                    }
                }
            }
        }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // =====================================================
    // Calculation Methods
    // =====================================================



    private fun calculateArea(boundaries: List<GeoPoint>): Double {
        if (boundaries.size < 3) return 0.0

        // This is a simplified version of the algorithm used by SphericalUtil
        // but written in pure Kotlin to avoid Google dependencies.
        var totalArea = 0.0
        val r = 6378137.0 // Earth's radius in meters

        for (i in boundaries.indices) {
            val p1 = boundaries[i]
            val p2 = boundaries[(i + 1) % boundaries.size]

            val lat1 = Math.toRadians(p1.latitude)
            val lon1 = Math.toRadians(p1.longitude)
            val lat2 = Math.toRadians(p2.latitude)
            val lon2 = Math.toRadians(p2.longitude)

            totalArea += (lon2 - lon1) * (2 + sin(lat1) + sin(lat2))
        }

        val areaInSqMeters = abs(totalArea * r * r / 2.0)
        return areaInSqMeters * 0.000247105 // Convert to Acres
    }




    private fun calculatePerimeter(boundaries: List<GeoPoint>): Double {
        if (boundaries.size < 2) return 0.0

        var perimeter = 0.0

        for (i in 0 until boundaries.size - 1) {
            perimeter += distanceBetween(boundaries[i], boundaries[i + 1])
        }

        if (boundaries.size >= 3) {
            perimeter += distanceBetween(boundaries.last(), boundaries.first())
        }

        return perimeter
    }

    private fun calculateCenterPoint(boundaries: List<GeoPoint>): GeoPoint {
        if (boundaries.isEmpty()) return GeoPoint(0.0, 0.0)

        val avgLat = boundaries.sumOf { it.latitude } / boundaries.size
        val avgLng = boundaries.sumOf { it.longitude } / boundaries.size

        return GeoPoint(avgLat, avgLng)
    }

    private fun distanceBetween(point1: GeoPoint, point2: GeoPoint): Double {
        val R = 6371.0
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat/2) * sin(deltaLat/2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon/2) * sin(deltaLon/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))

        return R * c
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    private fun generateId(prefix: String): String {
        return "${prefix}_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    // =====================================================
    // Utility Methods (Reverse Geocoding)
    // =====================================================


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

    // =====================================================
    // Validation Methods
    // =====================================================

    fun validateFarmData(): List<String> {
        val errors = mutableListOf<String>()
        val currentState = _uiState.value

        if (currentState.farmName.isBlank()) {
            errors.add("Farm name is required")
        }

        if (currentState.boundaries.size < 3) {
            errors.add("At least 3 boundary points are required")
        }

        if (currentState.calculatedArea <= 0) {
            errors.add("Invalid farm area")
        }

        if (currentState.farmName.length > 100) {
            errors.add("Farm name is too long (maximum 100 characters)")
        }

        if (currentState.farmDescription.length > 500) {
            errors.add("Farm description is too long (maximum 500 characters)")
        }

        return errors
    }

    fun isFormValid(): Boolean {
        return validateFarmData().isEmpty()
    }

    // =====================================================
    // State Management
    // =====================================================

    fun resetForm() {
        _uiState.update {
            FarmFencingUiState()
        }
    }

    fun setEditMode(farm: Farm) {
        updateStateFromFarm(farm)
        _uiState.update { it.copy(isEditMode = true) }
    }

    // =====================================================
    // Export/Import Methods
    // =====================================================

    fun exportFarmData(): String {
        val currentState = _uiState.value
        return "Farm: ${currentState.farmName}, Area: ${currentState.calculatedArea} acres"
    }

    fun importBoundariesFromFile(boundaries: List<GeoPoint>) {
        _uiState.update {
            it.copy(
                boundaries = boundaries,
                calculatedArea = calculateArea(boundaries),
                calculatedPerimeter = calculatePerimeter(boundaries),
                mapCenter = calculateCenterPoint(boundaries)
            )
        }
    }



    fun undoLastPoint() {
        val currentBoundaries = _uiState.value.boundaries.toMutableList()
        if (currentBoundaries.isNotEmpty()) {
            currentBoundaries.removeAt(currentBoundaries.size - 1)
            _uiState.update {
                it.copy(
                    boundaries = currentBoundaries,
                    calculatedArea = calculateArea(currentBoundaries),
                    calculatedPerimeter = calculatePerimeter(currentBoundaries)
                )
            }
        }
    }

    // In FarmFencingViewModel.kt
    fun GeoPoint.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }

    fun LatLng.toGeoPoint(): GeoPoint {
        return GeoPoint(this.latitude, this.longitude)
    }

    override fun onCleared() {
        super.onCleared()
    }
}

// =====================================================
// UI State Data Class
// =====================================================

data class FarmFencingUiState(
    val farm: Farm? = null,
    val farmName: String = "",
    val farmDescription: String = "",
    val farmType: FarmingType = FarmingType.MIXED,
    val soilType: String = "",
    val boundaries: List<GeoPoint> = emptyList(),
    val mapCenter: GeoPoint? = null,
    val currentLocation: GeoPoint? = null,
    val calculatedArea: Double = 0.0,
    val calculatedPerimeter: Double = 0.0,
    val waterSources: List<WaterSource> = emptyList(),
    val facilities: List<FarmFacility> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false,
    val showBoundaryInstructions: Boolean = true,
    val selectedTool: MapTool = MapTool.BOUNDARY,
    val zoomLevel: Float = 15f
)

data class AddressData(
    val county: String,
    val subCounty: String,
    val ward: String,
    val village: String,
    val fullAddress: String
)

enum class MapTool {
    BOUNDARY, WATER_SOURCE, FACILITY, MEASURE
}

