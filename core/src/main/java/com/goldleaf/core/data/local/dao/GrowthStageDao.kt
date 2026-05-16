package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.CropGrowthStage
import com.goldleaf.core.data.local.GrowthStageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthStageDao {

    @Query("SELECT * FROM growth_stages WHERE cropId = :cropId AND startDate <= :currentDate AND (endDate IS NULL OR endDate >= :currentDate)")
    suspend fun getCurrentGrowthStage(cropId: String, currentDate: String): GrowthStageEntity?

    @Query("SELECT * FROM growth_stages WHERE cropId = :cropId AND startDate > :currentDate ORDER BY startDate ASC LIMIT 1")
    suspend fun getNextGrowthStage(cropId: String, currentDate: String): GrowthStageEntity?

    @Query("SELECT * FROM growth_stages WHERE cropId = :cropId ORDER BY startDate ASC")
    fun getGrowthStagesByCrop(cropId: String): Flow<List<GrowthStageEntity>>

    @Query("SELECT * FROM growth_stages WHERE cropId = :cropId ORDER BY startDate ASC")
    suspend fun getGrowthStagesByCropSync(cropId: String): List<GrowthStageEntity>

    @Query("SELECT * FROM growth_stages")
    fun getAllGrowthStages(): Flow<List<GrowthStageEntity>>

    @Query("SELECT * FROM growth_stages WHERE cropId = :cropId ORDER BY startDate ASC")
    fun getGrowthStagesByCropId(cropId: String): Flow<List<GrowthStageEntity>>

    @Query("SELECT * FROM growth_stages WHERE id = :id  LIMIT 1")
    suspend fun getGrowthStageById(id: String): GrowthStageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthStage(stage: GrowthStageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthStages(stages: List<GrowthStageEntity>)

    @Update
    suspend fun updateGrowthStage(stage: GrowthStageEntity)

    @Delete
    suspend fun deleteGrowthStage(stage: GrowthStageEntity)

    @Query("DELETE FROM growth_stages WHERE cropId = :cropId")
    suspend fun deleteGrowthStagesByCropId(cropId: String)

    @Query("DELETE FROM growth_stages")
    suspend fun deleteAllGrowthStages()

    }