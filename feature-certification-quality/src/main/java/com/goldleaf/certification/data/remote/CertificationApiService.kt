package com.goldleaf.certification.data.remote

// FILE: feature-certification-quality/src/main/java/com/goldleaf/certification/data/remote/CertificationApiService.kt
import com.goldleaf.core.data.dto.*

import retrofit2.Response
import retrofit2.http.*

interface CertificationApiService {

    @POST("batches")
    suspend fun createBatch(
        @Header("Authorization") token: String,
        @Body request: CreateBatchRequest
    ): Response<BatchDto>

    @GET("batches/farmer/{farmerId}")
    suspend fun getFarmerBatches(
        @Header("Authorization") token: String,
        @Path("farmerId") farmerId: String
    ): Response<List<BatchDto>>

    @GET("batches/{batchId}")
    suspend fun getBatchDetails(
        @Header("Authorization") token: String,
        @Path("batchId") batchId: String
    ): Response<BatchDetailsDto>

    @GET("batches/{batchId}/lab-tests")
    suspend fun getLabTests(
        @Header("Authorization") token: String,
        @Path("batchId") batchId: String
    ): Response<List<LabTestDto>>

    @GET("blockchain/{batchId}")
    suspend fun getBlockchainRecord(
        @Header("Authorization") token: String,
        @Path("batchId") batchId: String
    ): Response<BlockchainRecordDto>

    @GET("verify/{batchNumber}")
    suspend fun verifyProduct(
        @Path("batchNumber") batchNumber: String
    ): Response<VerificationResponseDto>
}