package com.goldleaf.feature.weatherclimate.di


import com.goldleaf.feature.weatherclimate.data.repository.WeatherRepositoryImpl
import com.goldleaf.feature.weatherclimate.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherModule {
    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository
}