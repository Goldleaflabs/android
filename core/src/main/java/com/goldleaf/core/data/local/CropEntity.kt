package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.goldleaf.core.data.dto.farm.SoilType
import com.goldleaf.core.data.dto.farm.WaterRequirement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "crops",
    indices = [Index(value = ["farmId"])]
)

data class CropEntity(
    // Primary identification
    @PrimaryKey
    val id: String,
    val farmId: String,
    val name: String,
    val farmerId: String,  // ✅ THIS MUST EXIST
    // General crop information (from Crop)
    val scientificName: String? = null,
    val category: String? = null,
    val description: String? = null,

    // Planting instance information (from original CropEntity)
    val variety: String? = null,
    val plantingDate: String? = null,
    val harvestDate: String? = null,
    val location: String? = null,
    val area: Double? = null,
    val status: CropStatus? = null,
    val actualYieldKg: Double = 0.0,     // ← ADD THIS

    // Yield information
    val expectedYield: Double? = null,
    val actualYield: Double? = null,
    val yieldUnit: String? = null,
    val expectedYieldPerAcre: Double? = null,

    // Growing requirements
    val growingSeasonDays: Int? = null,
    val idealTemperatureMin: Double? = null,
    val idealTemperatureMax: Double? = null,
    val waterRequirement: WaterRequirement? = null,
    val soilType: SoilType? = null, // Clay, Sandy, Loam, Clay Loam, Sandy Loam, Silt Loam
    // Planting specifications
    val plantingDepth: Double? = null,
    val spacingBetweenPlants: Double? = null,
    val spacingBetweenRows: Double? = null,

    // Additional information
    val notes: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)
// Extension functions
fun CropEntity.getDaysSincePlanting(): Int {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val plantDate = this.plantingDate?.let { dateFormat.parse(it) } ?: return 0
        val today = Date()
        val diff = today.time - plantDate.time
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        0
    }
}

fun CropEntity.getDaysUntilHarvest(): Int? {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val harvestDateParsed = this.harvestDate?.let { dateFormat.parse(it) } ?: return null
        val today = Date()
        val diff = harvestDateParsed.time - today.time
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        null
    }
}

fun CropEntity.getYieldEfficiency(): Double? {
    val expected = this.expectedYield ?: return null
    val actual = this.actualYield ?: return null
    if (expected == 0.0) return null
    return (actual / expected) * 100.0
}


