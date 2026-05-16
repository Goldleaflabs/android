package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.MarketPriceEntity

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_prices ORDER BY date DESC")
    suspend fun getAllMarketPrices(): List<MarketPriceEntity>

    @Query("SELECT * FROM market_prices WHERE cropName = :cropName ORDER BY date DESC")
    suspend fun getMarketPricesByCrop(cropName: String): List<MarketPriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrice(marketPrice: MarketPriceEntity)

    @Query("DELETE FROM market_prices WHERE date < :cutoffDate")
    suspend fun deleteOldPrices(cutoffDate: Long)
}
