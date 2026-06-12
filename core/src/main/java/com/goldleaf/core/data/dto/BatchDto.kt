package com.goldleaf.core.data.dto

import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.BlockchainStatus
import com.goldleaf.core.data.local.LabTest
import com.goldleaf.core.data.local.ProductBatchEntity
import com.goldleaf.core.domain.model.VerificationResult



// ===== DTO Definition for Update (Encapsulating fields into a single object) =====
// This object captures the raw fields being edited by the ViewModel.
data class FarmerUpdateDto(
    val name: String,
    val email: String,
    val phone: String,
    val district: String,
    val region: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)


data class CreateBatchRequest(
    val batchNumber: String,
    val productType: String,
    val quantity: Double,
    val unit: String,
    val harvestDate: Long,
    val farmerId: String,
    val farmerName: String
)

data class BatchDto(
    val id: String,
    val batchNumber: String,
    val productType: String,
    val quantity: Double,
    val unit: String,
    val harvestDate: Long,
    val farmerId: String,
    val farmerName: String,
    val qualityGrade: String?,
    val blockchainHash: String?,
    val blockchainStatus: BlockchainStatus,
    val blockchainTimestamp: String?,
    val updatedAt: String
)

data class BatchDetailsDto(
    val batch: BatchDto,
    val labTests: List<LabTestDto>,
    val blockchainRecord: BlockchainRecordDto?
)

data class LabTestDto(
    val id: String,
    val batchId: String,
    val testType: String,
    val testDate: String,
    val labName: String,
    val status: String,
    val isPassed: Boolean?,
    val resultUrl: String?,
    val notes: String?,
    val certificationNumber: String?,
    val results: String? = null
)

data class BlockchainRecordDto(
    val id: String,
    val batchId: String,
    val transactionHash: String,
    val blockNumber: Long,
    val network: String,
    val timestamp: String,
    val status: String,
    val explorerUrl: String
)


data class VerificationResponseDto(
    val isValid: Boolean,
    val message: String,
    val batch: BatchDto?,
    val blockchainRecord: BlockchainRecordDto?,
    val labTests: List<LabTestDto>?
)


// Extension function to convert DTO to domain model
fun VerificationResponseDto.toDomainModel(): VerificationResult {
    val batchEntity = this.batch?.let { dto ->
        ProductBatchEntity(
            id = dto.id,
            batchNumber = dto.batchNumber,
            productType = dto.productType,
            quantity = dto.quantity,
            unit = dto.unit,
            harvestDate = dto.harvestDate,
            farmerId = dto.farmerId,
            farmerName = dto.farmerName,
            qualityGrade = dto.qualityGrade,
            blockchainHash = dto.blockchainHash,
            blockchainStatus = dto.blockchainStatus
        )
    }
    return VerificationResult(
        isValid = this.isValid,
        message = this.message,
        batch = batchEntity,
        blockchainRecord = this.blockchainRecord?.toDomainModel(),
        labTests = this.labTests?.map { it.toDomainModel() } ?: emptyList()
    )
}

// Helper: Convert BlockchainRecordDto to BlockchainRecord domain model
fun BlockchainRecordDto.toDomainModel(): BlockchainRecord {
    return BlockchainRecord(
        id = this.id,
        batchId = this.batchId,
        transactionHash = this.transactionHash,
        blockNumber = this.blockNumber,
        network = this.network,
        timestamp = this.timestamp,
        status =  BlockchainStatus.valueOf(this.status)
    )
}

// Helper: Convert LabTestDto to LabTest domain model
fun LabTestDto.toDomainModel(): LabTest {
    return LabTest(
        id = this.id,
        batchId = this.batchId,
        testType = this.testType,
        testDate = this.testDate,
        labName = this.labName,
        status = this.status,
        isPassed = this.isPassed,
        resultUrl = this.resultUrl,
        notes = this.notes
    )
}