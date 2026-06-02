package com.goldleaf.core.data.dto.crop


import com.goldleaf.core.data.dto.farm.SoilType
import com.google.gson.annotations.SerializedName

// Crop

data class CropDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("farmerId")
    val farmerId: String,

    // from old CropDto
    @SerializedName("variety")
    val variety: String? = null,

    @SerializedName("plantingDate")
    val plantingDate: String? = null,

    @SerializedName("expectedHarvestDate")
    val expectedHarvestDate: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("area")
    val area: Double? = null,

    @SerializedName("farmId")
    val farmId: String? = null,

    // from new CropDto
    @SerializedName("scientific_name")
    val scientificName: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("growing_season_days")
    val growingSeasonDays: Int? = null,

    @SerializedName("ideal_temperature_min")
    val idealTemperatureMin: Double? = null,

    @SerializedName("ideal_temperature_max")
    val idealTemperatureMax: Double? = null,

    @SerializedName("water_requirement")
    val waterRequirement: String? = null,

    @SerializedName("soil_types")
    val soilType: SoilType, // Clay, Sandy, Loam, Clay Loam, Sandy Loam, Silt Loam

    @SerializedName("planting_depth")
    val plantingDepth: Double? = null,

    @SerializedName("spacing_between_plants")
    val spacingBetweenPlants: Double? = null,

    @SerializedName("spacing_between_rows")
    val spacingBetweenRows: Double? = null,

    @SerializedName("expected_yield_per_acre")
    val expectedYieldPerAcre: Double? = null,

    @SerializedName("yield_unit")
    val yieldUnit: String? = null,

    @SerializedName("pipeline_stage_id")
    val pipelineStageId: Int? = null,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: String? = null
)

data class FarmCropDto(
    @SerializedName("id") val id: String,
    @SerializedName("farm_id") val farmId: String,
    @SerializedName("crop_id") val cropId: String,
    @SerializedName("crop") val crop: CropDto,
    @SerializedName("planting_date") val plantingDate: String? = null,
    @SerializedName("expected_harvest_date") val expectedHarvestDate: String? = null,
    @SerializedName("area_allocated") val areaAllocated: Double? = null,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String
)

data class CropCategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class CropStatsDto(
    @SerializedName("total_crops") val totalCrops: Int,
    @SerializedName("active_crops") val activeCrops: Int,
    @SerializedName("planned_crops") val plannedCrops: Int,
    @SerializedName("harvested_crops") val harvestedCrops: Int,
    @SerializedName("total_area_allocated") val totalAreaAllocated: Double,
    @SerializedName("crops_by_category") val cropsByCategory: Map<String, Int>
)

data class CropMasterDto(
    val id: String,
    val name: String,
    val scientificName: String?,
    val category: String?,
    val typicalGrowingPeriod: Int?,
    val waterRequirement: String?,
    val baseImageUrl: String?,
    val isActive: Boolean,
    @SerializedName("rotation_group")
    val rotationGroup: String? = null
)