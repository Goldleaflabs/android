package com.goldleaf.feature.cropmanagement.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.CropActivityRequest
import com.goldleaf.core.data.local.*
import com.goldleaf.core.data.local.dao.*
import com.goldleaf.core.data.local.toEntity
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CropRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val cropDao: CropDao,
    private val cropMasterDao: CropMasterDao,
    private val harvestDao: HarvestDao,
    private val taskRepository: TaskRepository,      // ADD THIS
    private val farmDao: FarmDao
) : CropRepository {

    override suspend fun insertActivity(activity: CropActivity): Result<Unit> {
        return try {
            // 1. Save to local CropActivity table
            cropDao.insertActivity(activity)

            // 2. Get the farm ID from the crop
            val crop = cropDao.getCropById(activity.cropId)

            // 3. Create corresponding TaskEntity for server sync
            val task = TaskEntity(
                id = activity.id, // Same ID for consistency
                cropId = activity.cropId,
                farmId = crop?.farmId ?: "",
                title = activity.activityType,
                taskName = activity.activityType,
                taskType = activity.activityType,
                category = mapActivityTypeToCategory(activity.activityType),
                description = buildDescription(activity),
                notes = buildNotes(activity),
                dueDate = activity.date,
                completedDate = activity.date,
                isCompleted = true,
                status = "COMPLETED",
                priority = TaskPriority.MEDIUM,
                createdAt = activity.createdAt,
                updatedAt = activity.createdAt,
                assignedTo = null,
                estimatedDuration = null,
                actualDuration = null,
                completedAt = System.currentTimeMillis()
            )

            // 4. Sync to server via TaskRepository
            taskRepository.addTask(activity.cropId,task)

            // 5. Best-effort sync to crop activity endpoint
            runCatching {
                apiService.addCropActivity(
                    activity.cropId,
                    CropActivityRequest(
                        type = activity.activityType,
                        description = activity.description,
                        date = activity.date,
                        cost = activity.cost
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivitiesByCropId(cropId: String): Result<List<CropActivity>> {
        return try {
            val activities = cropDao.getActivitiesByCropId(cropId).first()
            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActivitiesByCropIdFlow(cropId: String): Flow<List<CropActivity>> {
        return cropDao.getActivitiesByCropId(cropId)
    }

    override suspend fun deleteActivity(activityId: String): Result<Unit> {
        return try {
            cropDao.deleteActivity(activityId)
             // Also delete corresponding task from server
            taskRepository.deleteTask(activityId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Inside CropRepositoryImpl.kt
    private fun buildDescription(activity: CropActivity): String {
        return buildString {
            append(activity.description)
            if (activity.quantity != null && activity.unit != null) {
                append(" - ${activity.quantity} ${activity.unit}")
            }
        }
    }

    private fun buildNotes(activity: CropActivity): String? {
        return buildString {
            if (activity.cost != null) {
                append("Cost: KES ${activity.cost}")
            }
            if (!activity.notes.isNullOrBlank()) {
                if (activity.cost != null) append("\n")
                append(activity.notes)
            }
        }.ifBlank { null }
    }

    private fun mapActivityTypeToCategory(activityType: String): TaskCategory {
        return when (activityType.lowercase()) {
            "planting" -> TaskCategory.PLANTING
            "irrigation" -> TaskCategory.IRRIGATION
            "fertilization" -> TaskCategory.FERTILIZATION
            "pest_control" -> TaskCategory.PEST_CONTROL
            "disease_management", "disease control", "disease" -> TaskCategory.DISEASE_MANAGEMENT
            "weeding" -> TaskCategory.WEEDING
            "pruning", "trimming" -> TaskCategory.PRUNING
            "harvesting", "harvest" -> TaskCategory.HARVESTING
            "monitoring", "inspection", "health check" -> TaskCategory.MONITORING
            else -> TaskCategory.OTHER
        }
    }

    override fun getAllMyCrops(): Flow<List<CropEntity>> {
        return cropDao.getAllCrops()
    }
    // Harvest record operations
    override suspend fun insertHarvestRecord(harvest: HarvestRecordEntity) =
        harvestDao.insertHarvest(harvest)

    override suspend fun updateCropStatusAndYield(cropId: String, newStatus: CropStatus, actualYieldKg: Double) =
        cropDao.updateYieldAndStatus(cropId, actualYieldKg, newStatus)

    // Flow-based operations
    override fun getCropsForHarvest(): Flow<List<CropEntity>> =
        cropDao.getCropsByStatuses(listOf(CropStatus.PLANTED, CropStatus.GROWING))

    // This links the DAO's Flow directly to the ViewModel
   override fun getAllCrops(): Flow<List<CropMasterEntity>> {
        // Just pass the Flow directly. No conversion needed here.
        return cropMasterDao.getAllCrops()
    }
    // Single crop operations
    override suspend fun getCropById(cropId: String): CropEntity? {
        return cropDao.getCropById(cropId)
    }

    override suspend fun getFarmById(farmId: String): FarmEntity?{
        return farmDao.getFarmById(farmId)
    }

    // ✅ FIXED: Using the Batch query instead of .toString()
    override suspend fun getCropsByStatus(status: List<CropStatus>): List<CropEntity> {
        return cropDao.getCropsByStatusAndFarmer(status, "") // Using existing dao method
    }

    override suspend fun getCropsByStatusAndFarmer(status: List<CropStatus>, farmerId: String): List<CropEntity> {
        return cropDao.getCropsByStatusAndFarmer(status, farmerId)
    }

    // ✅ NEW: Added missing implementation for Dashboard optimization
    override suspend fun getActivitiesForCrops(cropIds: List<String>): List<CropActivity> {
        return if (cropIds.isEmpty()) emptyList()
        else cropDao.getActivitiesForMultipleCrops(cropIds)
    }

    // ✅ NEW: Added implementation to get crops by farm
    override suspend fun getCropsByFarmId(farmId: String): List<CropEntity> {
        return cropDao.getCropsByFarmId(farmId)
    }

    // CRUD operations with API sync
    override suspend fun createCrop(crop: CropEntity): Result<CropEntity> {
        return try {
            // 1. Save locally FIRST
            cropDao.insertCrop(crop)

            // 2. Try to sync to server
            try {
                val response = apiService.createCrop(crop)
                if (response.isSuccessful) {
                    Result.success(crop)
                } else {
                    // API failed but data is safe locally
                    Result.success(crop)
                }
            } catch (e: Exception) {
                // Network error - data is safe locally, will sync when online
                Result.success(crop)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateCrop(crop: CropEntity): Result<CropEntity> {
        return try {
            // 1. Update locally FIRST
            cropDao.updateCrop(crop)

            // 2. Try to sync to server
            try {
                val response = apiService.updateCrop(crop.id, crop)
                if (!response.isSuccessful) {
                    android.util.Log.w("CropRepository", "Server update failed for crop ${crop.id}: ${response.message()}")
                }
            } catch (e: Exception) {
                // Network error - data is safe locally
                // Will retry sync when online
            }

            Result.success(crop)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCrop(cropId: String): Result<Unit> {
        return try {
            // 1. Delete locally FIRST
            cropDao.deleteCropById(cropId)

            // 2. Try to sync deletion to server
            try {
                apiService.deleteCrop(cropId)
            } catch (e: Exception) {
                // Network error - deletion is safe locally
                // Will retry sync when online
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync operations
    override suspend fun syncCrops(): Result<Unit> {
        return try {
            val response = apiService.getAllCrops()
            if (response.isSuccessful) {
                val cropDtos = response.body() ?: emptyList()
                val cropEntities = cropDtos.map { it.toEntity() }
                // ✅ FIXED: Use the correct DAO method for List insertion
                cropDao.insertCrops(cropEntities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync crops: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Analytics
    override suspend fun getYieldAnalytics(): Result<YieldAnalytics> {
        return try {
            val a = cropDao.getYieldAnalytics()
            // Using the optimized DAO method we added earlier
            Result.success(
                YieldAnalytics(
                    totalArea = a.totalArea,
                    totalExpectedYield = a.totalExpectedYield,
                    totalActualYield = a.totalActualYield,
                    averageYieldPerHectare = a.averageYieldPerHectare,
                    completedCrops = a.completedCrops,
                    activeCrops = a.activeCrops,
                    yieldEfficiency = a.yieldEfficiency,
                    topPerformingCrops = emptyList()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
