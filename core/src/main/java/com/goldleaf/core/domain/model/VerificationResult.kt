package com.goldleaf.core.domain.model

import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.LabTest


/**
 * Complete product verification result
 * Contains all information needed to verify product authenticity
 */
data class VerificationResult(
    val isValid: Boolean,                    // Is product authentic?
    val message: String,                     // User-friendly message
    val batch: String?,                // Product batch info (null if invalid)
    val blockchainRecord: BlockchainRecord?, // Blockchain proof (null if not found)
    val labTests: List<LabTest>              // Lab test results (empty if none)
)

