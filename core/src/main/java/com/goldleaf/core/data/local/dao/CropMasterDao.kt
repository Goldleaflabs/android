package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.CropMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropMasterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCrops(crops: List<CropMasterEntity>)

    @Query("SELECT * FROM crop_master")
    fun getAllCrops(): Flow<List<CropMasterEntity>>
}
