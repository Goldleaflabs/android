package com.goldleaf.certification.presentation.batch

import android.util.Log
import com.goldleaf.certification.domain.repository.BatchRepository
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.core.data.local.CropActivity
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.HarvestRecordEntity
import com.goldleaf.core.data.local.ProductBatchEntity
import java.util.UUID

object BatchProcessor {
    private const val TAG = "BatchProcessor"

    data class ProcessResult(
        val batchResult: Result<ProductBatchEntity>,
        val cropUpdateResult: Result<com.goldleaf.core.data.local.CropEntity>? = null
    )

    suspend fun createBatchAndRecordHarvest(
        batchRepository: BatchRepository,
        cropRepository: CropRepository,
        crop: CropEntity,
        bags: Int,
        qtyKg: Double,
        batchId: String,
        farmerId: String,
        farmerName: String,
        harvestDateIso: String
    ): ProcessResult {
        val batchResult = batchRepository.createBatch(
            batchId,
            "${crop.name} ${crop.variety}",
            qtyKg,
            "kg",
            harvestDateIso,
            farmerId,
            farmerName,
            crop.id
        )

        var cropUpdateResult: Result<com.goldleaf.core.data.local.CropEntity>? = null

        batchResult.onSuccess { created ->
            try {
                val updatedCrop = crop.copy(
                    status = CropStatus.HARVESTED,
                    actualYield = qtyKg,
                    actualYieldKg = qtyKg,
                    harvestDate = harvestDateIso,
                    updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(java.util.Date())
                )

                // Attempt server-syncing update
                cropUpdateResult = cropRepository.updateCrop(updatedCrop)

                // Persist a local harvest record (best-effort)
                val harvest = HarvestRecordEntity(
                    id = UUID.randomUUID().toString(),
                    cropId = crop.id,
                    farmId = crop.farmId,
                    harvestDate = System.currentTimeMillis(),
                    quantityHarvested = qtyKg,
                    unit = "kg",
                    expectedYield = crop.expectedYield,
                    actualYield = qtyKg,
                    yieldQuality = null,
                    weatherCondition = null,
                    laborCost = null,
                    notes = "Harvested ${qtyKg} kg (${bags} bags) from batch ${created.batchNumber}",
                    status = "COMPLETED",
                    farmerId = farmerId
                )

                try {
                    cropRepository.insertHarvestRecord(harvest)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to insert harvest record locally: ${e.message}")
                }

                // Persist a crop activity entry so Activity/Monitoring timelines stay in sync
                try {
                    val harvestActivity = CropActivity(
                        id = UUID.randomUUID().toString(),
                        cropId = crop.id,
                        activityType = "HARVESTING",
                        date = harvestDateIso,
                        description = "Harvested ${qtyKg} kg (${bags} bags)",
                        quantity = qtyKg,
                        unit = "kg",
                        createdAt = System.currentTimeMillis(),
                        farmerId = crop.farmerId,
                        farmId = crop.farmId
                    )
                    cropRepository.insertActivity(harvestActivity)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to insert harvest activity locally: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during post-batch processing: ${e.message}", e)
            }
        }

        return ProcessResult(batchResult = batchResult, cropUpdateResult = cropUpdateResult)
    }
}
