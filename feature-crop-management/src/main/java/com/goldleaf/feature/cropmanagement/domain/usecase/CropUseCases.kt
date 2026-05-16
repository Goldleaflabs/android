package com.goldleaf.feature.cropmanagement.domain.usecase

import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.YieldAnalytics
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetCropsUseCase @Inject constructor(
    private val cropRepository: CropRepository
) {
    suspend operator fun invoke(): Flow<List<CropEntity>> {
        return cropRepository.getAllMyCrops()
    }
}
/**
 * Use case for getting all crops
 */
class GetAllCropsUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(): Flow<List<CropEntity>> {
        return repository.getAllMyCrops()
    }
}

/**
 * Use case for getting a single crop by ID
 */
class GetCropByIdUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(cropId: String): CropEntity? {
        return repository.getCropById(cropId)
    }
}



/**
 * Use case for creating a new crop
 */
class CreateCropUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(crop: CropEntity): Result<CropEntity> {
        // Add validation logic here if needed
        return repository.createCrop(crop)
    }
}

/**
 * Use case for updating an existing crop
 */
class UpdateCropUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(crop: CropEntity): Result<CropEntity> {
        // Add validation logic here if needed
        return repository.updateCrop(crop)
    }
}

/**
 * Use case for deleting a crop
 */
class DeleteCropUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(cropId: String): Result<Unit> {
        return repository.deleteCrop(cropId)
    }
}

/**
 * Use case for syncing crops with the server
 */
class SyncCropsUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncCrops()
    }
}

/**
 * Use case for getting yield analytics
 */
class GetYieldAnalyticsUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(): Result<YieldAnalytics> {
        return repository.getYieldAnalytics()
    }
}

/**
 * Use case for getting active crops (planted or growing)
 */
// File: GetCropsByStatusUseCase.kt
class GetCropsByStatusUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(statuses: List<CropStatus>): List<CropEntity> {
        return repository.getCropsByStatus(statuses)
    }
}

// File: GetActiveCropsUseCase.kt — NOW PERFECT
class GetActiveCropsUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(): List<CropEntity> {
        return repository.getCropsByStatus(
            listOf(CropStatus.PLANTED, CropStatus.GROWING)
        )
    }
}



// File: GetFailedCropsUseCase.kt (bonus — you’ll need this later)
class GetFailedCropsUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(): List<CropEntity> {
        return repository.getCropsByStatus(listOf(CropStatus.FAILED))
    }
}



/**
 * Use case for marking a crop as harvested
 */
class HarvestCropUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(
        cropId: String,
        actualYield: Double,
        harvestDate: String
    ): Result<CropEntity> {
        val crop = repository.getCropById(cropId) ?: return Result.failure(
            Exception("Crop not found")
        )

        val updatedCrop = crop.copy(
            status = CropStatus.HARVESTED,
            actualYield = actualYield,
            harvestDate = harvestDate,
            updatedAt = System.currentTimeMillis().toString()
        )

        return repository.updateCrop(updatedCrop)
    }
}

/**
 * Use case for updating crop status
 */
class UpdateCropStatusUseCase @Inject constructor(
    private val repository: CropRepository
) {
    suspend operator fun invoke(cropId: String, status: CropStatus): Result<CropEntity> {
        val crop = repository.getCropById(cropId) ?: return Result.failure(
            Exception("Crop not found")
        )

        val updatedCrop = crop.copy(
            status = status,
            updatedAt = System.currentTimeMillis().toString()
        )

        return repository.updateCrop(updatedCrop)
    }
}