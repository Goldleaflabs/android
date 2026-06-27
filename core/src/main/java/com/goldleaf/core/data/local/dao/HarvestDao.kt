package com.goldleaf.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goldleaf.core.data.local.HarvestRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: HarvestRecordEntity)

    @Query("SELECT * FROM harvest_records WHERE cropId = :cropId ORDER BY harvestDate DESC")
    fun getHarvestsForCrop(cropId: String): Flow<List<HarvestRecordEntity>>

    @Query("SELECT * FROM harvest_records ORDER BY harvestDate DESC")
    fun getAllHarvests(): Flow<List<HarvestRecordEntity>>

    @Query("SELECT COALESCE(SUM(quantityHarvested), 0.0) FROM harvest_records")
    fun getTotalHarvested(): Flow<Double>
}