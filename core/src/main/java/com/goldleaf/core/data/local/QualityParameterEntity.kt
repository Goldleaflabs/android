package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quality_parameters")
data class QualityParameterEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val parameterName: String,
    val value: String,
    val unit: String?,
    val standard: String?,
    val status: qualitystatus, // PASS, FAIL, WARNING
    val measuredAt: Long,
    val measuredBy: String?,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)

enum class qualitystatus {
    PASS, FAIL, WARNING
}
