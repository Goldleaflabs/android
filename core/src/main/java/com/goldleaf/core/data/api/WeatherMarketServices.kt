package com.goldleaf.core.data.api

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import retrofit2.http.GET
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton



data class Weather(
    val location: String,
    val temperature: Double,
    val feelsLike: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val precipitation: Double,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Int,
    val cloudCover: Int,
    val sunrise: String,
    val sunset: String,
    val timestamp: Long = System.currentTimeMillis()
)


data class WeatherAlert(
    val id: String,
    val type: String,
    val severity: AlertSeverity,
    val title: String,
    val description: String,
    val validFrom: String,
    val validTo: String
)


enum class AlertSeverity {
    LOW,
    MODERATE,
    HIGH,
    SEVERE,
    EXTREME
}


// Weather API Data Models
data class WeatherResponse(
    val current: CurrentWeather,
    val forecast: ForecastData
)



data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    val precipitation: Double,
    val wind_speed: Double,
    val weather_description: String,
    val uv_index: Double
)


// Data classes for Weather UI
data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val rainfall: Double,
    val visibility: Int,
    val condition: WeatherCondition,
    val location: String,
    val timestamp: String
)



enum class WeatherCondition(
    val icon: ImageVector,
    val displayName: String,
    val color: Color
) {
    CLEAR(
        icon = Icons.Default.WbSunny,
        displayName = "Clear",
        color = Color(0xFFFFB300)
    ),
    CLOUDY(
        icon = Icons.Default.Cloud,
        displayName = "Cloudy",
        color = Color(0xFF90A4AE)
    ),
    RAIN(
        icon = Icons.Default.Thunderstorm,
        displayName = "Rainy",
        color = Color(0xFF1976D2)
    ),
    STORM(
        icon = Icons.Default.Bolt,
        displayName = "Storm",
        color = Color(0xFF424242)
    ),
    SNOW(
        icon = Icons.Default.AcUnit,
        displayName = "Snow",
        color = Color(0xFF81D4FA)
    ),
    FOG(
        icon = Icons.Default.Cloud,
        displayName = "Fog",
        color = Color(0xFFBDBDBD)
    );

    companion object {
        fun fromString(value: String): WeatherCondition {
            return when (value.lowercase()) {
                "clear", "sunny" -> CLEAR
                "cloudy", "clouds" -> CLOUDY
                "rain", "rainy" -> RAIN
                "storm", "thunderstorm" -> STORM
                "snow" -> SNOW
                "mist", "fog" -> FOG
                else -> CLOUDY
            }
        }
    }
}


data class HourlyForecast(
    val time: String,
    val temperature: Int,
    val condition: WeatherCondition,
    val rainfall: Double,
    val precipitation: Double
)


data class DailyForecast(
    val day: String,
    val date: String,
    val tempMax: Double,      // merged (was tempMax / maxTemp)
    val tempMin: Double,      // merged (was tempMin / minTemp)
    val condition: WeatherCondition,
    val rainfall: Double, // merged (was rainfall / precipitation)
    val probability: Int,
    val dayOfWeek: String
)



data class WeatherForecast(
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>
)

data class ForecastData(
    val daily: List<DailyForecast>
)





// Market Data Models

data class MarketPriceData(
    val cropName: String,
    val currentPrice: Double,
    val previousPrice: Double,
    val priceHistory: List<Double>,
    val market: String,
    val quality: String,        // ← ADD THIS
    val unit: String,           // ← ADD THIS
    val lastUpdated: String
)

data class MarketInsightData(
    val crop: String,
    val recommendation: String,
    val trend: String,
    val confidence: Float
)

// Weather API Interface - calls YOUR backend server
interface WeatherAPIInterface {
    @GET("api/weather/current")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): WeatherResponse

    @GET("api/weather/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): WeatherResponse
}

@Singleton
class WeatherAPIService @Inject constructor(
    private val weatherApi: WeatherAPIInterface
) {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<WeatherResponse> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(weatherApi.getCurrentWeather(latitude, longitude))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getWeatherForecast(latitude: Double, longitude: Double): Result<WeatherResponse> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(weatherApi.getForecast(latitude, longitude))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

// Market Data API Interface - calls YOUR backend server
interface MarketDataAPIInterface {
    @GET("api/market/prices")
    suspend fun getCropPrices(
        @Query("crops") crops: String,
        @Query("location") location: String
    ): List<MarketPriceData>

    @GET("api/market/insights")
    suspend fun getMarketInsights(
        @Query("crops") crops: String,
        @Query("region") region: String
    ): List<MarketInsightData>
}

@Singleton
class MarketDataService @Inject constructor(
    private val marketApi: MarketDataAPIInterface
) {

    suspend fun getMarketPrices(crops: List<String>, location: String = "Kenya"): Result<List<MarketPriceData>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(marketApi.getCropPrices(crops.joinToString(","), location))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMarketInsights(crops: List<String>, region: String = "East Africa"): Result<List<MarketInsightData>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(marketApi.getMarketInsights(crops.joinToString(","), region))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}