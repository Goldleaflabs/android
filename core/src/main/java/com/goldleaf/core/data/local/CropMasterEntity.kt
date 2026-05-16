package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goldleaf.core.data.dto.crop.CropMasterDto


@Entity(tableName = "crop_master")
data class CropMasterEntity(
    @PrimaryKey
    val cropId: String,
    val cropName: String,
    val category: String,
    val isActive: Boolean,
    val farmerId: String? = null,
    val farmId: String? = null
)

fun CropMasterDto.toEntity(): CropMasterEntity {
    return CropMasterEntity(
        cropId = this.id,
        cropName = this.name,
        category = this.category?: "UnKnown",
        isActive = this.isActive
    )
}
