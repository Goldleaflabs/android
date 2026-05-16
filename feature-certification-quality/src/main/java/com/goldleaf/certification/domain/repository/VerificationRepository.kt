package com.goldleaf.certification.domain.repository


import com.goldleaf.core.domain.model.VerificationResult

/**
 * Repository interface for product verification
 */
interface VerificationRepository {

    /**
     * Verify a product by batch number
     * @param batchNumber The batch number to verify
     * @return Result containing VerificationResult or error
     */
    suspend fun verifyProduct(batchNumber: String): Result<VerificationResult>
}