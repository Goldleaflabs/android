package com.goldleaf.core.data.local.dao

// core/data/local/dao/CropVarietyDao.kt

import androidx.room.*
import com.goldleaf.core.data.local.CropVarietyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropVarietyDao {

    @Query("SELECT * FROM crop_varieties")
    fun getAllVarieties(): Flow<List<CropVarietyEntity>>

    @Query("SELECT * FROM crop_varieties WHERE id = :varietyId")
    suspend fun getVarietyById(varietyId: String): CropVarietyEntity?

    @Query("SELECT * FROM crop_varieties WHERE cropType = :cropType")
    fun getVarietiesByCropType(cropType: String): Flow<List<CropVarietyEntity>>

    @Query("SELECT * FROM crop_varieties WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchVarieties(searchQuery: String): Flow<List<CropVarietyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariety(variety: CropVarietyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVarieties(varieties: List<CropVarietyEntity>)

    @Update
    suspend fun updateVariety(variety: CropVarietyEntity)

    @Delete
    suspend fun deleteVariety(variety: CropVarietyEntity)

    @Query("DELETE FROM crop_varieties WHERE id = :varietyId")
    suspend fun deleteVarietyById(varietyId: String)

    @Query("DELETE FROM crop_varieties")
    suspend fun deleteAllVarieties()
}