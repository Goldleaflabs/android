package com.goldleaf.core.data.dto.crop



import com.google.gson.annotations.SerializedName

data class UpdateFarmCropsRequest(
    @SerializedName("farm_id") val farmId: String,
    @SerializedName("crop_ids") val cropIds: List<String>
)

data class AddCropToFarmRequest(
    @SerializedName("crop_id") val cropId: String,
    @SerializedName("planting_date") val plantingDate: String?,
    @SerializedName("area_allocated") val areaAllocated: Double?,
    @SerializedName("notes") val notes: String?
)

data class UpdateCropStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?
)