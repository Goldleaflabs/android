package com.goldleaf.feature.weatherclimate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.*
import com.goldleaf.feature.weatherclimate.domain.repository.WeatherRepository
import com.goldleaf.core.location.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Tracking internal coordinates
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var locationJob: Job? = null

    init {
        // Start finding the farmer's location immediately on startup
        loadDeviceLocation()
    }

    /**
     * Triggered by the init block or a "Refresh" button.
     * Uses GPS to find local coordinates.
     */
    fun loadDeviceLocation() {
        locationJob?.cancel()

        locationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            locationService.getCurrentLocation()
                .take(1)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "GPS error: ${e.message}")
                    }
                }
                .collect { (lat, lon) ->
                    currentLatitude = lat
                    currentLongitude = lon

                    // Update UI state so the Map shows the marker at the user's GPS spot
                    _uiState.update { it.copy(selectedLocation = Pair(lat, lon)) }

                    // Fetch the weather for this GPS spot
                    loadWeather(lat, lon)
                }
        }
    }

    /**
     * Called when the farmer taps a new location on the OpenStreetMap.
     */
    fun updateLocationFromMap(lat: Double, lon: Double) {
        currentLatitude = lat
        currentLongitude = lon

        // Update marker position on map immediately
        _uiState.update { it.copy(selectedLocation = Pair(lat, lon)) }

        loadWeather(lat, lon)
    }

    /**
     * Core function to fetch all weather-related data.
     */
    fun loadWeather(latitude: Double, longitude: Double) {
        loadCurrentWeather(latitude, longitude)
        loadForecast(latitude, longitude)
        loadAlerts(latitude, longitude)
    }

    private fun loadCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            weatherRepository.getCurrentWeather(lat, lon)
                .collect { result ->
                    result.onSuccess { weather ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentWeather = weather,
                                error = null
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load current weather"
                            )
                        }
                    }
                }
        }
    }

    private fun loadForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.getWeatherForecast(lat, lon, days = 7)
                .collect { result ->
                    result.onSuccess { forecast ->
                        _uiState.update { it.copy(forecast = forecast) }
                    }.onFailure {
                        _uiState.update { it.copy(forecast = null) }
                    }
                }
        }
    }

    private fun loadAlerts(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.getWeatherAlerts(lat, lon)
                .collect { result ->
                    result.onSuccess { alerts ->
                        _uiState.update { it.copy(alerts = alerts) }
                    }.onFailure {
                        _uiState.update { it.copy(alerts = emptyList()) }
                    }
                }
        }
    }
}

/**
 * UI State that holds data for both the Map and the Weather Cards.
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentWeather: Weather? = null,
    val forecast: WeatherForecast? = null,
    val alerts: List<WeatherAlert> = emptyList(),
    // Pair(Latitude, Longitude) used to position the OpenStreetMap marker
    val selectedLocation: Pair<Double, Double>? = null
)
