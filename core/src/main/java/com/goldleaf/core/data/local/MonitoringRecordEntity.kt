package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitoring_records")
data class MonitoringRecordEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val recordDate: String,
    val healthStatus: String,
    val pestObservations: String?,
    val diseaseObservations: String?,
    val growthObservations: String?,
    val soilMoisture: Double?,
    val temperature: Double?,
    val rainfall: Double?,
    val notes: String?,
    val photoUrls: String?, // JSON string of list
    val recordedBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val farmerId: String? = null,
    val farmId: String? = null
)
