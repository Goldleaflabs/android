package com.goldleaf.feature.cropmanagement.data.repository

import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.AppDatabase
import com.goldleaf.core.data.local.GrowthStageEntity
import com.goldleaf.core.data.local.CropGrowthStage
import com.goldleaf.core.data.local.GrowthStage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GrowthStageRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) : GrowthStageRepository {

    override suspend fun getGrowthStagesByCropId(cropId: String): Result<List<CropGrowthStage>> {
        return try {
            val response = apiService.getGrowthStagesByCropId(cropId)

            if (response.isSuccessful) {
                val stages = response.body() ?: emptyList()

                // Pre-format timestamps once outside the loop for efficiency
                val currentTimestamp = java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                // Sync Database: Map domain objects to Entities
                val entities = stages.map { stage ->
                    GrowthStageEntity(
                        id = stage.id,
                        cropId = stage.cropId,
                        stage = stage.stage,
                        startDate = stage.startDate,
                        endDate = stage.endDate,
                        description = stage.description,
                        milestones = null,
                        expectedDuration = null,
                        isCompleted = stage.isCompleted,
                        createdAt = currentTimestamp,
                        updatedAt = currentTimestamp
                    )
                }

                database.growthStageDao().insertGrowthStages(entities)
                Result.success(stages)
            } else {
                // API failed, fallback to local but wrap in success to show we have cached data
                fetchLocalStages(cropId)
            }
        } catch (e: Exception) {
            // Network error or parsing error, fallback to local
            fetchLocalStages(cropId)
        }
    }

    /**
     * Helper to fetch and map local entities to domain models
     */
    private suspend fun fetchLocalStages(cropId: String): Result<List<CropGrowthStage>> {
        val local = database.growthStageDao().getGrowthStagesByCropSync(cropId)
        return if (local.isNotEmpty()) {
            Result.success(local.map { entity ->
                CropGrowthStage(
                    id = entity.id,
                    cropId = entity.cropId,
                    stage = entity.stage,
                    description = entity.description ?: "",
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    isCompleted = entity.isCompleted,
                    tasks = emptyList()
                )
            })
        } else {
            Result.failure(Exception("No data available locally or from network."))
        }
    }


    override suspend fun updateGrowthStage(stage: CropGrowthStage): Result<CropGrowthStage> {
        return try {
            val response = apiService.updateGrowthStage(stage.id, stage)
            if (response.isSuccessful) {
                val updated = response.body() ?: stage
                database.growthStageDao().updateGrowthStage(
                    GrowthStageEntity(
                        id = updated.id,
                        cropId = updated.cropId,
                        stage = updated.stage,
                        description = updated.description,
                        startDate = updated.startDate,
                        endDate = updated.endDate,
                        milestones = null,  // ✅ Required property
                        expectedDuration = null,  // ✅ Required property
                        isCompleted = updated.isCompleted,  // ✅ Required property
                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(  Date()  ),
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())   // ✅ Required

                    )
                )
                Result.success(updated)
            } else {
                database.growthStageDao().updateGrowthStage(
                    GrowthStageEntity(
                        id = stage.id,
                        cropId = stage.cropId,
                        stage = stage.stage,
                        description = stage.description,
                        startDate = stage.startDate,
                        endDate = stage.endDate,
                        milestones = null,  // ✅ Required property
                        expectedDuration = null,  // ✅ Required property
                        isCompleted = stage.isCompleted,  // ✅ Required property
                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(  Date()  ),
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())   // ✅ Required

                    )
                )
                Result.success(stage)
            }
        } catch (e: Exception) {
            database.growthStageDao().updateGrowthStage(
                GrowthStageEntity(
                    id = stage.id,
                    cropId = stage.cropId,
                    stage = stage.stage,
                    description = stage.description,
                    startDate = stage.startDate,
                    endDate = stage.endDate,
                    milestones = null,  // ✅ Required property
                    expectedDuration = null,  // ✅ Required property
                    isCompleted = stage.isCompleted,  // ✅ Required property
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(  Date()  ),
                    updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())   // ✅ Required
              )
            )
            Result.success(stage)
        }
    }

    override fun getGrowthStagesFlow(cropId: String): Flow<List<CropGrowthStage>> {
        return database.growthStageDao().getGrowthStagesByCrop(cropId).map { list ->
            list.map {
                CropGrowthStage(
                    id = it.id,
                    cropId = it.cropId,
                    stage = it.stage,  // ✅ "stage" not "name", convert String to Enum
                    description = it.description ?: "",
                    startDate = it.startDate,
                    endDate = it.endDate,
                    isCompleted = it.isCompleted,
                    tasks = emptyList()
                )
            }
        }
    }

    override suspend fun transitionStage(
        cropId: String,
        currentStageId: String?,
        nextStageName: String,
        transitionDate: String
    ): Result<Unit> {
        return try {
            // 1. Close the current stage if it exists
            currentStageId?.let { id ->
                val currentEntity = database.growthStageDao().getGrowthStageById(id)
                currentEntity?.let {
                    database.growthStageDao().updateGrowthStage(
                        it.copy(
                            endDate = transitionDate,
                            isCompleted = true,
                            updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                    )
                }
            }

            // 2. Prepare the new stage entity
            val newStageEntity = GrowthStageEntity(
                id = java.util.UUID.randomUUID().toString(),
                cropId = cropId,
                stage = GrowthStage.valueOf(nextStageName), // e.g., "VEGETATIVE"
                description = "Transitioned to $nextStageName",
                startDate = transitionDate,
                endDate = null,
                isCompleted = false,
                milestones = null,
                expectedDuration = null,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
          )

            // 3. Save locally
            database.growthStageDao().insertGrowthStage(newStageEntity)

            // 4. (Optional) Sync to API
             apiService.createGrowthStage(newStageEntity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
