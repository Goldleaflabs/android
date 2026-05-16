package com.goldleaf.feature.cropmanagement.domain.repository

import androidx.room.Query
import com.goldleaf.core.data.local.CropActivity
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropMasterEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.HarvestRecordEntity
import com.goldleaf.core.data.local.YieldAnalytics
import kotlinx.coroutines.flow.Flow

interface CropRepository {
    fun getAllCrops():Flow<List<CropMasterEntity>>
    fun getAllMyCrops():Flow<List<CropEntity>>
    suspend fun getCropById(cropId: String): CropEntity?
    suspend fun getFarmById(farmId: String): FarmEntity?
    suspend fun getCropsByStatus(status: List<CropStatus>): List<CropEntity>
    suspend fun getCropsByStatusAndFarmer(status: List<CropStatus>, farmerId: String): List<CropEntity>
    suspend fun createCrop(crop: CropEntity): Result<CropEntity>
    suspend fun updateCrop(crop: CropEntity): Result<CropEntity>
    suspend fun deleteCrop(cropId: String): Result<Unit>
    suspend fun syncCrops(): Result<Unit>
    suspend fun getYieldAnalytics(): Result<YieldAnalytics>
    suspend fun insertHarvestRecord(harvest: HarvestRecordEntity)
    suspend fun updateCropStatusAndYield(cropId: String, newStatus: CropStatus, actualYieldKg: Double)
    fun getCropsForHarvest(): Flow<List<CropEntity>>
    suspend fun getActivitiesForCrops(cropIds: List<String>): List<CropActivity>
    suspend fun getCropsByFarmId(farmId: String): List<CropEntity>
    // ADD ACTIVITY METHODS
    suspend fun insertActivity(activity: CropActivity): Result<Unit>
    suspend fun getActivitiesByCropId(cropId: String): Result<List<CropActivity>>
    fun getActivitiesByCropIdFlow(cropId: String): Flow<List<CropActivity>>
    suspend fun deleteActivity(activityId: String): Result<Unit>


}
