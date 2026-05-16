package com.goldleaf.feature.cropmanagement.data.remote.dto

// CropDto.kt - Remote API Data Transfer Object

import com.google.gson.annotations.SerializedName
import com.goldleaf.core.data.local.CropStatus

data class CropDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("variety")
    val variety: String,
    @SerializedName("planting_date")
    val plantingDate: String,
    @SerializedName("harvest_date")
    val harvestDate: String?,
    @SerializedName("location")
    val location: String,
    @SerializedName("area")
    val area: Double,
    @SerializedName("status")
    val status: CropStatus,
    @SerializedName("expected_yield")
    val expectedYield: Double?,
    @SerializedName("actual_yield")
    val actualYield: Double?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)
