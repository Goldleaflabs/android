package com.goldleaf.feature.cropmanagement.di


import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.AppDatabase
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.feature.cropmanagement.data.repository.CropRepositoryImpl
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CropManagementModule {

    @Provides
    @Singleton
    fun provideGetCropsUseCase(
        cropRepository: CropRepository
    ): GetCropsUseCase {
        return GetCropsUseCase(cropRepository)
    }

    @Provides
    @Singleton
    fun provideCreateCropUseCase(
        cropRepository: CropRepository
    ): CreateCropUseCase {
        return CreateCropUseCase(cropRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateCropUseCase(
        cropRepository: CropRepository
    ): UpdateCropUseCase {
        return UpdateCropUseCase(cropRepository)
    }

    @Provides
    @Singleton
    fun provideGetYieldAnalyticsUseCase(
        cropRepository: CropRepository
    ): GetYieldAnalyticsUseCase {
        return GetYieldAnalyticsUseCase(cropRepository)
    }
}