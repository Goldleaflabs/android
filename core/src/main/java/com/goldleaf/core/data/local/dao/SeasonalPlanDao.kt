package com.goldleaf.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.goldleaf.core.data.local.SeasonalPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonalPlanDao {
    @Query("SELECT * FROM seasonal_plans WHERE farmId = :farmId ORDER BY startDate ASC")
    fun getPlansByFarm(farmId: String): Flow<List<SeasonalPlanEntity>>

    @Query("SELECT * FROM seasonal_plans WHERE farmId = :farmId AND season = :season ORDER BY startDate ASC")
    fun getPlansByFarmAndSeason(farmId: String, season: String): Flow<List<SeasonalPlanEntity>>

    @Query("SELECT * FROM seasonal_plans WHERE farmId = :farmId AND startDate >= :startDate AND startDate <= :endDate ORDER BY startDate ASC")
    fun getPlansByFarmAndDateRange(farmId: String, startDate: Long, endDate: Long): Flow<List<SeasonalPlanEntity>>

    @Query("SELECT * FROM seasonal_plans WHERE id = :id")
    suspend fun getPlanById(id: String): SeasonalPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: SeasonalPlanEntity)

    @Update
    suspend fun updatePlan(plan: SeasonalPlanEntity)

    @Delete
    suspend fun deletePlan(plan: SeasonalPlanEntity)

    @Query("DELETE FROM seasonal_plans WHERE id = :id")
    suspend fun deletePlanById(id: String)
}
