package com.goldleaf.feature.trainingextension.di

import com.goldleaf.core.data.api.ApiService
import com.goldleaf.feature.trainingextension.data.local.VideoDao
import com.goldleaf.feature.trainingextension.data.repository.VideoRepositoryImpl
import com.goldleaf.feature.trainingextension.domain.repository.VideoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * CORRECT Dependency Injection Module
 * Uses core ApiService with goldleaflabs.co.ke endpoints
 */
@Module
@InstallIn(SingletonComponent::class)
object TrainingModule {

    @Provides
    @Singleton
    fun provideVideoRepository(
        apiService: ApiService,
        videoDao: VideoDao
    ): VideoRepository {
        return VideoRepositoryImpl(apiService, videoDao)
    }
}