package com.goldleaf.feature.cropmanagement.di

import com.goldleaf.feature.cropmanagement.data.repository.CropRepositoryImpl
import com.goldleaf.feature.cropmanagement.data.repository.GrowthStageRepository
import com.goldleaf.feature.cropmanagement.data.repository.GrowthStageRepositoryImpl
import com.goldleaf.feature.cropmanagement.data.repository.MonitoringRepositoryImpl
import com.goldleaf.feature.cropmanagement.data.repository.TaskRepositoryImpl
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.domain.repository.MonitoringRepository
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCropRepository(
        impl: CropRepositoryImpl
    ): CropRepository

    @Binds
    @Singleton
    abstract fun bindMonitoringRepository(
        impl: MonitoringRepositoryImpl
    ): MonitoringRepository

    @Binds
    @Singleton
    abstract fun bindGrowthStageRepository(
        impl: GrowthStageRepositoryImpl
    ): GrowthStageRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository
}
