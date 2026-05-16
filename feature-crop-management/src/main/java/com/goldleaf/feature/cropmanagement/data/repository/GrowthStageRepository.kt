package com.goldleaf.feature.cropmanagement.data.repository


import com.goldleaf.core.data.local.CropGrowthStage
import com.goldleaf.core.data.local.GrowthStageEntity
import kotlinx.coroutines.flow.Flow

interface GrowthStageRepository {
    suspend fun getGrowthStagesByCropId(cropId: String): Result<List<CropGrowthStage>>
    suspend fun updateGrowthStage(stage: CropGrowthStage): Result<CropGrowthStage>
    fun getGrowthStagesFlow(cropId: String): Flow<List<CropGrowthStage>>
    // Add this line:
    suspend fun transitionStage( cropId: String, currentStageId: String?, nextStageName: String,  transitionDate: String): Result<Unit>
}