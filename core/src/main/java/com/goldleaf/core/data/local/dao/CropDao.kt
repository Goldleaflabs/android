package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.CropActivity
import com.goldleaf.core.data.local.CropMasterEntity
import com.goldleaf.core.data.local.Officer
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops WHERE status IN (:statuses)")
    fun getCropsByStatuses(statuses: List<CropStatus>): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE status IN (:statuses) AND farmerId = :farmerId ORDER BY plantingDate DESC")
    suspend fun getCropsByStatusAndFarmer(statuses: List<CropStatus>, farmerId: String): List<CropEntity>

    @Query("SELECT * FROM crops WHERE farmId = :farmId")
    suspend fun getCropsByFarmId(farmId: String): List<CropEntity>

    @Query("SELECT * FROM crops WHERE farmId = :farmId")
    fun getCropsByFarmIdFlow(farmId: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :cropId")
    suspend fun getCropById(cropId: String): CropEntity?

    @Query("SELECT * FROM crops WHERE plotId = :plotId")
    suspend fun getCropsByPlotId(plotId: String): List<CropEntity>

    @Query("SELECT * FROM crops")
    fun getAllCrops(): Flow<List<CropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertallCrop(crop: List<CropEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropMasterEntity>)

    @Update
    suspend fun updateCrop(crop: CropEntity)

    @Delete
    suspend fun deleteCrop(crop: CropEntity)

    @Query("DELETE FROM crops WHERE id = :cropId")
    suspend fun deleteCropById(cropId: String)

    @Query("UPDATE crops SET actualYieldKg = :actualYield, status = :status WHERE id = :cropId")
    suspend fun updateYieldAndStatus(cropId: String, actualYield: Double, status: CropStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: CropActivity)

    @Query("SELECT * FROM crop_activities WHERE cropId = :cropId ORDER BY date DESC")
    fun getActivitiesByCropId(cropId: String): Flow<List<CropActivity>>

    @Query("SELECT * FROM crop_activities WHERE cropId IN (:cropIds) ORDER BY date DESC")
    suspend fun getActivitiesForMultipleCrops(cropIds: List<String>): List<CropActivity>

    @Query("SELECT * FROM crop_activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<CropActivity>>

    @Query("SELECT a.* FROM crop_activities a INNER JOIN crops c ON a.cropId = c.id WHERE c.farmerId = :farmerId ORDER BY a.createdAt DESC")
    fun getActivitiesByFarmerId(farmerId: String): Flow<List<CropActivity>>

    @Query("SELECT * FROM crop_activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): CropActivity?

    @Query("DELETE FROM crop_activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: String)

    @Query("SELECT COUNT(*) FROM crops WHERE status = :status")
    suspend fun getCropsCountByStatus(status: CropStatus): Int

    @Query("SELECT SUM(area) FROM crops WHERE status = :status")
    suspend fun getTotalAreaByStatus(status: CropStatus): Double?

    @Query("""
        SELECT 
            COALESCE(SUM(area), 0.0) as totalArea,
            COALESCE(SUM(expectedYield), 0.0) as totalExpectedYield,
            COALESCE(SUM(actualYield), 0.0) as totalActualYield,
            COALESCE(AVG(CASE WHEN area > 0 THEN actualYield / area ELSE 0 END), 0.0) as averageYieldPerHectare,
            COUNT(CASE WHEN status = 'HARVESTED' THEN 1 END) as completedCrops,
            COUNT(CASE WHEN status IN ('GROWING', 'PLANTED') THEN 1 END) as activeCrops,
            COALESCE((SUM(actualYield) * 100.0 / NULLIF(SUM(expectedYield), 0)), 0.0) as yieldEfficiency,
            '' as topPerformingCrops
        FROM crops
    """)
    suspend fun getYieldAnalytics(): YieldAnalytics

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOfficers(officers: List<Officer>)

    @Query("SELECT phone FROM officers WHERE county = :county LIMIT 1")
    suspend fun getOfficerPhoneByCounty(county: String): String?

    // Data class for analytics query
    data class YieldAnalytics(
        val totalArea: Double,
        val totalExpectedYield: Double,
        val totalActualYield: Double,
        val averageYieldPerHectare: Double,
        val completedCrops: Int,
        val activeCrops: Int,
        val yieldEfficiency: Double,
        val topPerformingCrops: String
    )
}
