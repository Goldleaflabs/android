package com.goldleaf.core.data.mapper


import com.goldleaf.core.data.dto.crop.Crop
import com.goldleaf.core.data.dto.farm.SoilType
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.getDaysSincePlanting

// Extension function to map CropEntity to Crop domain model
fun CropEntity.toCrop(): Crop {
    return Crop(
        id = this.id,
        name = this.name,
        farmerId = this.farmerId,
        variety = this.variety ,
        status = this.status,
        plantedDate = this.plantingDate ,
        expectedHarvestDate = this.harvestDate ,
        area = this.area ,
        growthStage = calculateGrowthStage(),
        progressPercentage = calculateProgressPercentage()
    )
}



// Extension function to map Crop domain model to CropEntity
fun Crop.toCropEntity( farmId: String, createdAt: String,updatedAt: String,soilType: SoilType): CropEntity {
    return CropEntity(
        id = this.id,
        farmId = farmId,
        farmerId= farmerId,
        name = this.name,
        variety = this.variety,
        plantingDate = this.plantedDate,
        harvestDate = this.expectedHarvestDate?.ifEmpty { null },
        location = "" ,
        area = this.area,
        status = this.status,
        soilType = soilType,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Helper functions for CropEntity
private fun CropEntity.calculateGrowthStage(): String {
    val daysSincePlanting = getDaysSincePlanting()
    val totalGrowingDays = growingSeasonDays ?: 90

    return when {
        daysSincePlanting < totalGrowingDays * 0.25 -> "Germination"
        daysSincePlanting < totalGrowingDays * 0.5 -> "Vegetative"
        daysSincePlanting < totalGrowingDays * 0.75 -> "Flowering"
        daysSincePlanting < totalGrowingDays -> "Maturation"
        else -> "Ready for Harvest"
    }
}

private fun CropEntity.calculateProgressPercentage(): Int {
    val daysSincePlanting = getDaysSincePlanting()
    val totalGrowingDays = growingSeasonDays ?: 90

    return ((daysSincePlanting.toFloat() / totalGrowingDays) * 100)
        .coerceIn(0f, 100f)
        .toInt()
}


