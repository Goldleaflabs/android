package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val humidity: Int,
    val weatherCondition: String,
    val weatherDescription: String,
    val windSpeed: Double,
    val rainfall: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)

