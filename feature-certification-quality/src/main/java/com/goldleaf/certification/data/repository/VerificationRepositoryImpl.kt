package com.goldleaf.certification.data.repository


import com.goldleaf.certification.data.remote.CertificationApiService
import com.goldleaf.certification.domain.repository.VerificationRepository
import com.goldleaf.core.data.dto.toDomainModel
import com.goldleaf.core.domain.model.VerificationResult  // ✅ Import interface
import javax.inject.Inject

// ✅ ADD: : VerificationRepository
class VerificationRepositoryImpl @Inject constructor(
    private val apiService: CertificationApiService
) : VerificationRepository {

    override suspend fun verifyProduct(batchNumber: String): Result<VerificationResult> {
        return try {
            val response = apiService.verifyProduct(batchNumber)

            if (response.isSuccessful && response.body() != null) {
                val responseDto = response.body()
                val verificationResult = responseDto!!.toDomainModel()
                 Result.success(verificationResult)
            } else {
                Result.failure(Exception("Verification failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}