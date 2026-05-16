package com.goldleaf.core.data.dto.farm


import java.time.LocalDateTime


enum class WaterRequirement {
    LOW,
    MODERATE,
    HIGH
}

enum class SoilType {
    CLAY,
    SANDY,
    LOAM,
    CLAY_LOAM,
    SANDY_LOAM,
    SILT_LOAM
}


data class CropCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null
)