package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harvest_records")
data class HarvestRecordEntity(
    @PrimaryKey val id: String,
    val cropId: String,
    val farmId: String,
    val harvestDate: Long = System.currentTimeMillis(),
    val quantityHarvested: Double = 0.0,
    val unit: String? = null,
    val expectedYield: Double? = null,
    val actualYield: Double? = null,
    val yieldQuality: String? = null,
    val weatherCondition: String? = null,
    val laborCost: Double? = null,
    val notes: String? = null,
    val status: String? = null,
    val farmerId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
