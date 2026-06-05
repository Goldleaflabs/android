package com.goldleaf.core.domain.model

import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.LabTest
import com.goldleaf.core.data.local.ProductBatchEntity

data class VerificationResult(
    val isValid: Boolean,
    val message: String,
    val batch: ProductBatchEntity?,
    val blockchainRecord: BlockchainRecord?,
    val labTests: List<LabTest>
)

