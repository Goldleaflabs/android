package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "farm_plots",
    indices = [Index(value = ["farmId"])]
)
data class PlotEntity(
    @PrimaryKey
    val id: String,
    val farmId: String,
    val name: String,
    val size: Double = 0.0,
    val sizeUnit: String = "acres",
    val soilType: String? = null,
    val notes: String? = null,
    val boundaries: String? = null,
    val color: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
