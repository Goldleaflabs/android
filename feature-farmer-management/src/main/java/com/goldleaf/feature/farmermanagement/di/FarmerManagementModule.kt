package com.goldleaf.feature.farmermanagement.di

import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.AppDatabase
import com.goldleaf.feature.farmermanagement.data.repository.FarmerRepositoryImpl
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.goldleaf.core.auth.UserSessionManager

@Module
@InstallIn(SingletonComponent::class)
object FarmerManagementModule {

    @Provides
    @Singleton
    fun provideFarmerRepository(
        apiService: ApiService,
        database: AppDatabase,
        sessionManager: UserSessionManager , // Add this parameterm
    ): FarmerRepository {
        return FarmerRepositoryImpl(  // ✅ Remove <Any?>
            apiService = apiService,
            farmerDao = database.farmerDao(),
            farmDao = database.farmDao(),
            certificationDao = database.certificationDao(),  // ✅ Add this
            sessionManager = sessionManager , // ✅ Add this
            cropDao = database.cropDao(),
            plotDao = database.plotDao()
        )
    }
}