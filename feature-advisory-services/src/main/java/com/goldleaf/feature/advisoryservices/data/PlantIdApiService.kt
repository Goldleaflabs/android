package com.goldleaf.feature.advisoryservices.data


import com.goldleaf.core.data.api.PlantIdentificationRequest
import com.goldleaf.core.data.api.PlantIdentificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PlantIdApiService {
    @POST("v2/identify")
    suspend fun identifyPlant(
        @Body request: PlantIdentificationRequest,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<PlantIdentificationResponse>
}


data class PlantSuggestion(
    val id: String,
    val name: String,
    val probability: Double,
    val similar_images: List<String> = emptyList()
)

data class PlantProbability(
    val probability: Double
)