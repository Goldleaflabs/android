package com.goldleaf.core.data.dto

import com.google.gson.annotations.SerializedName

data class PipelineStageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stageKey") val stageKey: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isHarvestStage") val isHarvestStage: Boolean,
    @SerializedName("isTerminal") val isTerminal: Boolean,
)

data class PipelineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PipelineStageDto>,
)
