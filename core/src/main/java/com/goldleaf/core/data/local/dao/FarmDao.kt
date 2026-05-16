package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.FarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE farmerId = :farmerId")
    fun getFarmsByFarmerIdlocal(farmerId: String): List<FarmEntity>

    @Query("SELECT * FROM farms WHERE farmerId = :farmerId")
    fun getFarmsByFarmerId(farmerId: String): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE id = :farmId")
    suspend fun getFarmById(farmId: String): FarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(farms: List<FarmEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)

    @Update
    suspend fun updateFarm(farm: FarmEntity)

    @Delete
    suspend fun deleteFarm(farm: FarmEntity)
}
