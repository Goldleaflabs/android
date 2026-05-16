package com.goldleaf.feature.weatherclimate.data.repository

import com.goldleaf.core.data.api.*
import com.goldleaf.feature.weatherclimate.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import com.goldleaf.feature.weatherclimate.data.mappers.toDomain
import com.goldleaf.feature.weatherclimate.data.mappers.toEntity
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale

private fun getDayOfWeek(dateString: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        date?.let { dayFormat.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}


class WeatherRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val weatherDao: com.goldleaf.core.data.local.dao.WeatherDao
) : WeatherRepository {

    override fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Flow<Result<Weather>> = flow {
        try {
            // STEP 1: Try to load from cache first (offline support)
            val cached = weatherDao.getWeather(latitude, longitude)
                .firstOrNull()
            if (cached != null) {
                emit(Result.success(cached.toDomain()))
            }
            
            // STEP 2: Fetch from API if available
            val response = apiService.getCurrentWeather(latitude, longitude)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val weather = Weather(
                        location = "${latitude},${longitude}",
                        temperature = dto.temperature,
                       feelsLike = dto.feelsLike,
                        condition = dto.condition,
                        humidity = dto.humidity,
                        windSpeed = dto.windSpeed,
                        precipitation = dto.precipitation,
                        pressure = dto.pressure,
                        visibility = dto.visibility,
                       uvIndex = dto.uvIndex,
                       cloudCover = dto.cloudCover,
                       sunrise = dto.sunrise,
                       sunset = dto.sunset,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    // STEP 3: Cache the result
                    weatherDao.insertWeather(weather.toEntity(latitude, longitude))
                    
                    emit(Result.success(weather))
                } ?: emit(Result.failure(Exception("No weather data received")))
            } else {
                // If API fails but we have cache, return cache
                if (cached != null) {
                    // Already emitted above
                } else {
                    emit(Result.failure(Exception("API error: ${response.message()}")))
                }
            }
        } catch (e: Exception) {
            // Try fallback to cache on any error
            try {
                val cached = weatherDao.getWeather(latitude, longitude)
                    .firstOrNull()
                if (cached != null) {
                    emit(Result.success(cached.toDomain()))
                } else {
                    emit(Result.failure(e))
                }
            } catch (cacheError: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    override fun getWeatherForecast(
        latitude: Double,
        longitude: Double,
        days: Int
    ): Flow<Result<WeatherForecast>> = flow {
        try {
            val response = apiService.getWeatherForecast(latitude, longitude, days)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val forecast = WeatherForecast(
                        hourlyForecast = emptyList(),
                        dailyForecast = dto.forecast.map { day ->
                            DailyForecast(
                                day = getDayOfWeek(day.date),
                                date = day.date,
                                dayOfWeek = getDayOfWeek(day.date),
                                tempMax = day.tempMax,
                                tempMin = day.tempMin,
                                condition = WeatherCondition.fromString(day.condition),  // ← Here,
                                rainfall = day.rainfall ?: 0.0,
                                probability =  1
                            )
                        }
                    )
                    emit(Result.success(forecast))
                } ?: emit(Result.failure(Exception("No forecast data received")))
            } else {
                emit(Result.failure(Exception("API error: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getWeatherAlerts(
        latitude: Double,
        longitude: Double
    ): Flow<Result<List<WeatherAlert>>> = flow {
        try {
            val response = apiService.getWeatherAlerts(latitude, longitude)
            if (response.isSuccessful) {
                response.body()?.let { dtoList ->
                    val alerts = dtoList.map { dto ->
                        WeatherAlert(
                            id=dto.id,
                            type = dto.type,
                            title = dto.title,
                            description = dto.description,
                            severity = AlertSeverity.valueOf(dto.severity.uppercase()),
                            validTo = dto.validTo,
                            validFrom = dto.validFrom
                        )
                    }
                    emit(Result.success(alerts))
                } ?: emit(Result.failure(Exception("No alerts data received")))
            } else {
                emit(Result.failure(Exception("API error: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
