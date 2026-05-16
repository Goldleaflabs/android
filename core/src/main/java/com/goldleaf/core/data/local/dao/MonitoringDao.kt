package com.goldleaf.core.data.local.dao

import com.goldleaf.core.data.local.MonitoringRecordEntity
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoringDao {

    @Query("SELECT * FROM monitoring_records WHERE cropId = :cropId ORDER BY recordDate DESC")
    fun getMonitoringRecordsByCropId(cropId: String): Flow<List<MonitoringRecordEntity>>

    @Query("SELECT * FROM monitoring_records WHERE id = :id")
    suspend fun getMonitoringRecordById(id: String): MonitoringRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonitoringRecord(record: MonitoringRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonitoringRecords(records: List<MonitoringRecordEntity>)

    @Update
    suspend fun updateMonitoringRecord(record: MonitoringRecordEntity)

    @Delete
    suspend fun deleteMonitoringRecord(record: MonitoringRecordEntity)

    @Query("DELETE FROM monitoring_records WHERE cropId = :cropId")
    suspend fun deleteMonitoringRecordsByCropId(cropId: String)
}
