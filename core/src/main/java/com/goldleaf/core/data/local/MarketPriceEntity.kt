package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_prices")
data class MarketPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropName: String,
    val price: Double,
    val currency: String = "KES",
    val unit: String = "kg",
    val market: String,
    val date: Long = System.currentTimeMillis(),
    val source: String? = null,
    val farmerId: String? = null,
    val farmId: String? = null
)
