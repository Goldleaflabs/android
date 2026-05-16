package com.goldleaf.feature.advisoryservices.di

// File: AdvisoryModule.kt
// Location: com.goldleaf.feature.advisoryservices.di


import com.goldleaf.feature.advisoryservices.data.GeminiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import com.goldleaf.core.di.MainRetrofit // Import the qualifier from core/di
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdvisoryModule {

    // This method tells Hilt exactly how to create a GeminiApi instance.
    // It takes the Retrofit instance (which Hilt already knows how to create from CoreApiModule)
    // and calls the .create() method.
    @Provides
    @Singleton
    fun provideGeminiApi(@MainRetrofit retrofit: Retrofit): GeminiApi {
        return retrofit.create(GeminiApi::class.java)
    }
}