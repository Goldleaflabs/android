package com.goldleaf.core.data.local.dao


import androidx.room.*
import com.goldleaf.core.data.local.CropMonitoringRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropMonitoringDao {

    @Query("SELECT * FROM crop_monitoring_records ORDER BY recordDate DESC")
    fun getAllRecords(): Flow<List<CropMonitoringRecordEntity>>

    @Query("SELECT * FROM crop_monitoring_records WHERE cropId = :cropId")
    fun getRecordsByCropId(cropId: String): Flow<List<CropMonitoringRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CropMonitoringRecordEntity)

    @Update
    suspend fun updateRecord(record: CropMonitoringRecordEntity)

    @Delete
    suspend fun deleteRecord(record: CropMonitoringRecordEntity)
}