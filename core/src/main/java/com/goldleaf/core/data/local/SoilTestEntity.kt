package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.goldleaf.core.data.dto.farm.SoilType

@Entity(
    tableName = "soil_tests",
    indices = [Index(value = ["farmId"])]
)
data class SoilTestEntity(
    @PrimaryKey
    val id: String,
    val farmId: String,
    val testDate: Long,
    val soilType: String, // Clay, Sandy, Loam, Clay Loam, Sandy Loam, Silt Loam
    val ph: Double,
    val nitrogen: Double, // ppm or %
    val phosphorus: Double, // ppm
    val potassium: Double, // ppm
    val organicMatter: Double? = null, // %
    val moisture: Double? = null, // %
    val ec: Double? = null, // Electrical Conductivity (dS/m)
    val calcium: Double? = null, // ppm
    val magnesium: Double? = null, // ppm
    val sulfur: Double? = null, // ppm
    val zinc: Double? = null, // ppm
    val iron: Double? = null, // ppm
    val manganese: Double? = null, // ppm
    val copper: Double? = null, // ppm
    val boron: Double? = null, // ppm
    val testLocation: String? = null, // Specific field/plot location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val depth: String? = null, // e.g., "0-20cm"
    val labName: String? = null,
    val recommendations: String? = null, // JSON string of recommendations
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null
)
