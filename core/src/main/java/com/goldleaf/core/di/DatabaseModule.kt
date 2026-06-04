package com.goldleaf.core.di


import android.content.Context
import androidx.room.Room
import com.goldleaf.core.data.local.*
import com.goldleaf.core.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "goldleaf_database"
        ) .fallbackToDestructiveMigration()
            .build()
    }

    // 2. Add this specific method to fix your error
    @Provides
    @Singleton
    fun provideCropMasterDao(database: AppDatabase): CropMasterDao {
        return database.cropMasterDao() // Ensure this method exists in your RoomDatabase class
    }

    @Provides
    @Singleton
    fun provideFarmerDao(database: AppDatabase): FarmerDao {
        return database.farmerDao()
    }

    @Provides
    @Singleton
    fun provideFarmDao(database: AppDatabase): FarmDao {
        return database.farmDao()
    }

    @Provides
    @Singleton
    fun provideCropDao(database: AppDatabase): CropDao {
        return database.cropDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }


    @Provides
    @Singleton
    fun provideCropMonitoringDao(appDatabase: AppDatabase): CropMonitoringDao {
        return appDatabase.cropMonitoringDao()
    }

    @Provides
    @Singleton
    fun provideAdvisoryDao(database: AppDatabase): AdvisoryDao {
        return database.advisoryDao()
    }

    @Provides
    @Singleton
    fun provideMarketDao(database: AppDatabase): MarketDao {
        return database.marketDao()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(database: AppDatabase): WeatherDao {
        return database.weatherDao()
    }

    @Provides
    @Singleton
    fun provideSoilDao(database: AppDatabase): SoilDao {
        return database.soilDao()
    }

    @Provides
    @Singleton
    fun providePlotDao(database: AppDatabase): PlotDao {
        return database.plotDao()
    }

    @Provides @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao { return database.paymentDao() }

    @Provides @Singleton
    fun provideHarvestDeliveryDao(database: AppDatabase): HarvestDeliveryDao { return database.harvestDeliveryDao() }

    @Provides @Singleton
    fun provideBatchSalesDao(database: AppDatabase): BatchSalesDao { return database.batchSalesDao() }

    @Provides @Singleton
    fun provideDeductionDao(database: AppDatabase): DeductionDao { return database.deductionDao() }

    @Provides @Singleton
    fun provideFarmerPayoutInfoDao(database: AppDatabase): FarmerPayoutInfoDao { return database.farmerPayoutInfoDao() }

    @Provides
    @Singleton
    fun provideCertificationDao(database: AppDatabase): CertificationDao {
        return database.certificationDao()
    }

    @Provides
    @Singleton
    fun provideCertificationRequirementDao(database: AppDatabase): CertificationRequirementDao {
        return database.certificationRequirementDao()
    }

    @Provides
    @Singleton
    fun provideAuditRecordDao(database: AppDatabase): AuditRecordDao {
        return database.auditRecordDao()
    }

    @Provides
    @Singleton
    fun provideQualityDao(database: AppDatabase): QualityDao {
        return database.qualityDao()
    }

    @Provides
    @Singleton
    fun provideQualityParameterDao(database: AppDatabase): QualityParameterDao {
        return database.qualityParameterDao()
    }

    @Provides
    @Singleton
    fun provideLabTestDao(database: AppDatabase): LabTestDao {
        return database.labTestDao()
    }

    @Provides
    @Singleton
    fun provideBlockchainDao(database: AppDatabase): BlockchainDao {
        return database.blockchainDao()
    }

    @Provides
    @Singleton
    fun provideProductJourneyDao(database: AppDatabase): ProductJourneyDao {
        return database.productJourneyDao()
    }

    @Provides
    @Singleton
    fun provideJourneyEventDao(database: AppDatabase): JourneyEventDao {
        return database.journeyEventDao()
    }

    @Provides
    @Singleton
    fun provideProductBatchDao(database: AppDatabase): ProductBatchDao {
        return database.productBatchDao()
    }

    @Provides
    @Singleton
    fun provideMonitoringDao(database: AppDatabase): MonitoringDao {
        return database.monitoringDao()
    }


    @Provides
    @Singleton
    fun provideHarvestDao(database: AppDatabase): HarvestDao  {
        return database.harvestDao()
    }

    @Provides
    @Singleton
    fun provideInputDao(database: AppDatabase): InputDao {
        return database.inputDao()
    }

    @Provides
    @Singleton
    fun provideSeasonalPlanDao(database: AppDatabase): SeasonalPlanDao {
        return database.seasonalPlanDao()
    }

    @Provides
    @Singleton
    fun provideComplianceChecklistDao(database: AppDatabase): ComplianceChecklistDao {
        return database.complianceChecklistDao()
    }
}