package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "growth_stages")
data class GrowthStageEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val stage: GrowthStage, // e.g., "Germination", "Vegetative", "Flowering", "Maturation"
    val startDate: String,
    val endDate: String?,
    val description: String?,
    val milestones: String?, // JSON or comma-separated
    val expectedDuration: Int?, // in days
    val isCompleted: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val farmerId: String? = null,
    val farmId: String? = null
)
