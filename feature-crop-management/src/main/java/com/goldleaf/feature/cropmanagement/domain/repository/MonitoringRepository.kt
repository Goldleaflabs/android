package com.goldleaf.feature.cropmanagement.domain.repository


import com.goldleaf.core.data.local.CropMonitoringRecord
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    fun getAllMonitoringRecords(): Flow<List<CropMonitoringRecord>>
    suspend fun getMonitoringRecordById(id: String): CropMonitoringRecord?
    suspend fun getMonitoringRecordsByCropId(cropId: String):  Result<List<CropMonitoringRecord>>

    suspend fun insertMonitoringRecord(record: CropMonitoringRecord): Result<CropMonitoringRecord>
    suspend fun updateMonitoringRecord(record: CropMonitoringRecord): Result<CropMonitoringRecord>
    suspend fun deleteMonitoringRecord(id: String): Result<Unit>
}

data class HealthBreakdown(
    val excellent: Int = 0,
    val good: Int = 0,
    val fair: Int = 0,
    val poor: Int = 0,
    val critical: Int = 0
)

data class OverviewMetrics(
    val totalCrops: Int = 0,
    val activeCrops: Int = 0,
    val completedCrops: Int = 0,
    val totalArea: Double = 0.0,
    val expectedYield: Double = 0.0,
    val actualYield: Double = 0.0,
    val overdueTasks: Int = 0,
    val upcomingTasks: Int = 0,
    val averageHealth: String = "Unknown"
)