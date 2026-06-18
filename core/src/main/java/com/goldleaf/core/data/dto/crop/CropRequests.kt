package com.goldleaf.core.data.dto.crop



import com.google.gson.annotations.SerializedName

data class UpdateFarmCropsRequest(
    @SerializedName("farm_id") val farmId: String,
    @SerializedName("crop_ids") val cropIds: List<String>
)

data class AddCropToFarmRequest(
    @SerializedName("cropId") val cropId: String,
    @SerializedName("farmerId") val farmerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("variety") val variety: String?,
    @SerializedName("plantingDate") val plantingDate: String?,
    @SerializedName("area") val area: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("plotId") val plotId: String?,
    @SerializedName("notes") val notes: String?
)

data class UpdateCropStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?
)