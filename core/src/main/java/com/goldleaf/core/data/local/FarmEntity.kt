package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "farms",
    indices = [Index(value = ["farmerId"])]
)
data class FarmEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val name: String,
    val location: String,
    @SerializedName("totalSize") val size: Double,
    val sizeUnit: String = "acres",
    val boundaries: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    val farmType: String? = null,
    val soilType: String? = null,
    val registrationDate: Long? = null,
    val status: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val farmId: String? = null
)
