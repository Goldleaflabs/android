package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_inputs")
data class InputEntity(
    @PrimaryKey val id: String,
    val cropId: String,
    val farmId: String,
    val farmerId: String,
    val type: InputType,
    val name: String,
    val quantity: Double,
    val unit: String,
    val applicationDate: String,
    val cost: Double = 0.0,
    val supplier: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class InputType {
    SEED, FERTILIZER, PESTICIDE, HERBICIDE, FUNGICIDE, OTHER
}
