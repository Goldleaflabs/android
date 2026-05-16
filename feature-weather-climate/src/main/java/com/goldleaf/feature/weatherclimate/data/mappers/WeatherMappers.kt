package com.goldleaf.feature.weatherclimate.data.mappers

import com.goldleaf.core.data.api.Weather
import com.goldleaf.core.data.local.WeatherEntity


fun WeatherEntity.toDomain(): Weather = Weather(
    location = "Lat:$latitude, Lon:$longitude",
    temperature = temperature,
    feelsLike = temperature,
    condition = weatherCondition,
    humidity = humidity,
    windSpeed = windSpeed,
    precipitation = rainfall,
    pressure = 0.0,
    visibility = 0.0,
    uvIndex = 0,
    cloudCover = 0,
    sunrise = "",
    sunset = "",
    timestamp = timestamp
)

fun Weather.toEntity(lat: Double, lon: Double): WeatherEntity = WeatherEntity(
    latitude = lat,
    longitude = lon,
    temperature = temperature,
    humidity = humidity,
    weatherCondition = condition,
    weatherDescription = condition,
    windSpeed = windSpeed,
    rainfall = precipitation,
    timestamp = timestamp
)
