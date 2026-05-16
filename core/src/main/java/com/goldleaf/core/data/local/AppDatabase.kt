package com.goldleaf.core.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.goldleaf.core.data.local.dao.AdvisoryDao
import com.goldleaf.core.data.local.dao.AuditRecordDao
import com.goldleaf.core.data.local.dao.BlockchainDao
import com.goldleaf.core.data.local.dao.CertificationDao
import com.goldleaf.core.data.local.dao.CertificationRequirementDao
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.core.data.local.dao.CropMasterDao
import com.goldleaf.core.data.local.dao.CropMonitoringDao
import com.goldleaf.core.data.local.dao.CropTaskDao
import com.goldleaf.core.data.local.dao.CropVarietyDao
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.core.data.local.dao.FarmerDao
import com.goldleaf.core.data.local.dao.GrowthStageDao
import com.goldleaf.core.data.local.dao.HarvestDao
import com.goldleaf.core.data.local.dao.JourneyEventDao
import com.goldleaf.core.data.local.dao.LabTestDao
import com.goldleaf.core.data.local.dao.MarketDao
import com.goldleaf.core.data.local.dao.MonitoringDao
import com.goldleaf.core.data.local.dao.ProductBatchDao
import com.goldleaf.core.data.local.dao.ProductJourneyDao
import com.goldleaf.core.data.local.dao.QualityDao
import com.goldleaf.core.data.local.dao.QualityParameterDao
import com.goldleaf.core.data.local.dao.SoilDao
import com.goldleaf.core.data.local.dao.TaskDao
import com.goldleaf.core.data.local.dao.WeatherDao

@Database(
    entities = [
        HarvestRecordEntity::class,
        FarmerEntity::class,
        FarmEntity::class,
        CropEntity::class,
        TaskEntity::class,
        AdvisoryEntity::class,
        MarketPriceEntity::class,
        WeatherEntity::class,
        CertificationEntity::class,
        CertificationRequirementEntity::class,
        AuditRecordEntity::class,
        ProductBatchEntity::class,
        QualityParameterEntity::class,
        LabTestEntity::class,
        BlockchainRecordEntity::class,
        ProductJourneyEntity::class,
        JourneyEventEntity::class,
        SoilTestEntity::class,
        MonitoringRecordEntity::class,
        GrowthStageEntity::class,
        CropVarietyEntity::class,
        CropTaskEntity::class,
        CropMonitoringRecordEntity::class,
        CropActivity::class,
        Officer::class,
        CropMasterEntity::class
    ],
    version = 12,
    exportSchema = false
)

@TypeConverters(Converters::class,
    CropTypeConverters::class,
    CertificationStatusConverter::class,
    ProductStatusConverter::class,
    JourneyStatusConverter::class,
    QualityStatusConverter::class,
    TestStatusConverter::class,
    BlockchainStatusConverter::class,
    CropPerformanceConverters::class,
    FarmerPreferencesConverter::class)


abstract class AppDatabase : RoomDatabase() {
    abstract fun cropMasterDao(): CropMasterDao
    abstract fun farmerDao(): FarmerDao
    abstract fun farmDao(): FarmDao
    abstract fun cropDao(): CropDao
    abstract fun taskDao(): TaskDao
    abstract fun advisoryDao(): AdvisoryDao
    abstract fun marketDao(): MarketDao
    abstract fun weatherDao(): WeatherDao
    abstract fun certificationDao(): CertificationDao
    abstract fun certificationRequirementDao(): CertificationRequirementDao
    abstract fun auditRecordDao(): AuditRecordDao
    abstract fun qualityDao(): QualityDao
    abstract fun qualityParameterDao(): QualityParameterDao
    abstract fun labTestDao(): LabTestDao
    abstract fun blockchainDao(): BlockchainDao
    abstract fun productJourneyDao(): ProductJourneyDao
    abstract fun journeyEventDao(): JourneyEventDao
    abstract fun productBatchDao(): ProductBatchDao
    abstract fun soilDao(): SoilDao
    abstract fun monitoringDao(): MonitoringDao
    abstract fun growthStageDao(): GrowthStageDao
    abstract fun cropVarietyDao(): CropVarietyDao
    abstract fun cropTaskDao(): CropTaskDao
    abstract fun cropMonitoringDao(): CropMonitoringDao
    abstract fun harvestDao(): HarvestDao
}

// DATABASE BUILDER
object AppDatabaseBuilder {

    private const val DATABASE_NAME = "goldleaf_database"

    fun buildDatabase(
        context: android.content.Context,
        enableLogging: Boolean = false
    ): AppDatabase {
        // Migration to add structured address columns to farmers table
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns with NULL default so existing rows are preserved
                database.execSQL("ALTER TABLE farmers ADD COLUMN district TEXT")
                database.execSQL("ALTER TABLE farmers ADD COLUMN region TEXT")
                database.execSQL("ALTER TABLE farmers ADD COLUMN street TEXT")
                database.execSQL("ALTER TABLE farmers ADD COLUMN country TEXT")
                database.execSQL("ALTER TABLE farmers ADD COLUMN latitude REAL")
                database.execSQL("ALTER TABLE farmers ADD COLUMN longitude REAL")
            }
        }
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ) .apply {
                if (enableLogging) {
                    setQueryCallback({ sqlQuery, bindArgs ->
                        android.util.Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
                    }, java.util.concurrent.Executors.newSingleThreadExecutor())
                }
            }
            .addMigrations(MIGRATION_11_12)
            .fallbackToDestructiveMigration()
            .build()
    }



}

