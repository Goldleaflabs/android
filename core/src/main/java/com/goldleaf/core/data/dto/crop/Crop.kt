package com.goldleaf.core.data.dto.crop


import androidx.compose.ui.graphics.Color
import com.goldleaf.core.data.local.CropStatus

data class Crop(
    val id: String,
    val name: String,
    val variety: String?,
    val status: CropStatus?,
    val plantedDate: String?,
    val expectedHarvestDate: String?,
    val area: Double?,
    val growthStage: String,
    val progressPercentage: Int,
    val farmerId: String
) {

    val statusColor: Color
        get() = when (status) {
            CropStatus.PLANNED -> Color(0xFF9E9E9E)
            CropStatus.PLANTED -> Color(0xFF2196F3)
            CropStatus.GROWING -> Color(0xFF4CAF50)
            CropStatus.HARVESTED -> Color(0xFFFF9800)
            CropStatus.COMPLETED -> Color(0xFF4CAF50)
            CropStatus.FAILED -> Color(0xFFF44336)
            null -> Color(0xFF9E9E9E) // 👈 This fixes the "Exhaustive" error
        }

    val daysToHarvest: Int
        get() {
            // Calculate days from expectedHarvestDate
            // This is simplified - use your date parsing logic
            return 0
        }
}

