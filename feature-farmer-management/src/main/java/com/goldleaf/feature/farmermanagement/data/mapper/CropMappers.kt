package com.goldleaf.feature.farmermanagement.data.mapper


import com.goldleaf.core.data.dto.crop.CropCategoryDto
import com.goldleaf.core.data.dto.crop.CropDto
import com.goldleaf.core.data.dto.crop.CropStatsDto
import com.goldleaf.core.data.dto.crop.FarmCropDto
import com.goldleaf.core.data.dto.farm.CropCategory
import com.goldleaf.core.data.dto.farm.SoilType
import com.goldleaf.core.data.dto.farm.WaterRequirement
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.feature.farmermanagement.domain.repository.*
import java.time.LocalDateTime

fun CropDto.toDomainModel(): CropEntity {
    return CropEntity(
        id = id,
        name = name,
        farmerId= farmerId,
        scientificName = scientificName,
        category = category,
        description = description,
        imageUrl = imageUrl,
        growingSeasonDays = growingSeasonDays,
        idealTemperatureMin = idealTemperatureMin,
        idealTemperatureMax = idealTemperatureMax,
        waterRequirement = waterRequirement?.let {
            try { WaterRequirement.valueOf(it.uppercase()) }
            catch (e: Exception) { null }
        },
        plantingDepth = plantingDepth,
        spacingBetweenPlants = spacingBetweenPlants,
        spacingBetweenRows = spacingBetweenRows,
        expectedYieldPerAcre = expectedYieldPerAcre,
        yieldUnit = yieldUnit,
        isActive = isActive,
        createdAt = createdAt ?: System.currentTimeMillis().toString(),
        area = area ?: 0.0,
        farmId = farmId ?: "",
        plantingDate = plantingDate ?: "",
        soilType = SoilType.valueOf(soilType.toString() ),
        status = CropStatus.valueOf(status ?: "ACTIVE"),
        updatedAt = System.currentTimeMillis().toString(),
        variety = variety ?: "N/A",
        location = "",
        pipelineStageId = pipelineStageId


    )
}

fun FarmCropDto.toDomainModel(): FarmCrop {
    return FarmCrop(
        id = id,
        farmId = farmId,
        cropId = cropId,
        crop = crop.toDomainModel(),
        plantingDate = plantingDate?.let { parseDateTime(it) },
        expectedHarvestDate = expectedHarvestDate?.let { parseDateTime(it) },
        areaAllocated = areaAllocated,
        status = CropStatus.valueOf(status),
        createdAt = parseDateTime(createdAt)
    )
}




fun CropCategoryDto.toDomainModel(): CropCategory {
    return CropCategory(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl
    )
}

fun CropStatsDto.toDomainModel(): FarmCropStats {
    return FarmCropStats(
        totalCrops = totalCrops,
        activeCrops = activeCrops,
        plannedCrops = plannedCrops,
        harvestedCrops = harvestedCrops,
        totalAreaAllocated = totalAreaAllocated,
        cropsByCategory = cropsByCategory
    )
}

private fun parseDateTime(dateString: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateString)
    } catch (e: Exception) {
        LocalDateTime.now()
    }
}