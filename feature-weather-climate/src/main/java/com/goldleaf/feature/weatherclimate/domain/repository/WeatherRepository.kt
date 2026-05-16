package com.goldleaf.feature.weatherclimate.domain.repository


import com.goldleaf.core.data.api.*
import kotlinx.coroutines.flow.Flow

/**
 * Weather Repository Interface
 * Uses Flow for reactive data updates
 */
interface WeatherRepository {

    /**
     * Get current weather conditions
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return Flow of Result containing current weather data
     */
    fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Flow<Result<Weather>>

    /**
     * Get weather forecast
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param days Number of days to forecast
     * @return Flow of Result containing weather forecast
     */
    fun getWeatherForecast(
        latitude: Double,
        longitude: Double,
        days: Int = 7
    ): Flow<Result<WeatherForecast>>

    /**
     * Get weather alerts for location
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return Flow of Result containing list of weather alerts
     */
    fun getWeatherAlerts(
        latitude: Double,
        longitude: Double
    ): Flow<Result<List<WeatherAlert>>>
}