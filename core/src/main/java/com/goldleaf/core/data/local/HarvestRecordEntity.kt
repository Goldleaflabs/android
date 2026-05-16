package com.goldleaf.core.data.local



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harvest_records")
data class HarvestRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropId: String,
    val cropName: String = "",           // optional, for reports
    val variety: String = "",            // optional
    val areaHa: Double = 0.0,            // optional
    val bags: Int,
    val weightKg: Double,                // = bags × 90
    val pricePerKg: Double,
    val totalRevenue: Double,            // = weightKg × pricePerKg
    val harvestDate: Long = System.currentTimeMillis(),
    val batchId: String = "",
    val farmerId: String? = null,
    val farmId: String? = null
)
