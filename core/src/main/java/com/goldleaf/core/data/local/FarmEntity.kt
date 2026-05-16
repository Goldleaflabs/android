package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "farms",
    indices = [Index(value = ["farmerId"])]  // ADD THIS LINE
)
data class FarmEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val name: String,
    val location: String,
    val size: Double,
    val sizeUnit: String = "acres",
    val boundaries: String? = null,  // ADD THIS
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val farmId: String? = null
)
