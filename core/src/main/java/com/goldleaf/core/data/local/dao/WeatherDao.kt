package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    fun getWeather(lat: Double, lon: Double): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather")
    suspend fun clearWeather()
}
