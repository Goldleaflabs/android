package com.goldleaf.certification.di


import com.goldleaf.core.data.local.dao.ProductBatchDao
import com.goldleaf.certification.data.remote.CertificationApiService
import com.goldleaf.certification.data.repository.BatchRepositoryImpl
import com.goldleaf.certification.data.repository.VerificationRepositoryImpl
import com.goldleaf.certification.domain.repository.BatchRepository
import com.goldleaf.certification.domain.repository.VerificationRepository
import com.goldleaf.core.di.MainRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CertificationModule {

    @Provides
    @Singleton
    fun provideCertificationApiService(@MainRetrofit retrofit: Retrofit): CertificationApiService {
        return retrofit.create(CertificationApiService::class.java)
    }



    @Provides
    @Singleton
    fun provideBatchRepository(
        apiService: CertificationApiService,
        batchDao: ProductBatchDao
    ): BatchRepository {
        return BatchRepositoryImpl(apiService, batchDao)
    }

    @Provides
    @Singleton
    fun provideVerificationRepository(
        apiService: CertificationApiService
    ): VerificationRepository {
        return VerificationRepositoryImpl(apiService)
    }
}