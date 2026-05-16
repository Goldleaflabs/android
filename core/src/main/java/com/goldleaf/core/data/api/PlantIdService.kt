package com.goldleaf.core.data.api

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

// =====================================================
// Request/Response Models for YOUR Backend
// =====================================================

data class PlantHealthResponse(
    @SerializedName("suggestions")
    val suggestions: List<PlantDiseaseDto>,

    @SerializedName("is_healthy")
    val isHealthy: Boolean,

    @SerializedName("confidence")
    val confidence: Double
)

data class PlantDiseaseDto(
    @SerializedName("disease_name")
    val diseaseName: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("probability")
    val probability: Double,

    @SerializedName("treatment")
    val treatment: String,

    @SerializedName("prevention")
    val prevention: String? = null
)

data class PlantIdentificationResponse(
    @SerializedName("plant_name")
    val plantName: String,

    @SerializedName("scientific_name")
    val scientificName: String? = null,

    @SerializedName("confidence")
    val confidence: Double,

    @SerializedName("is_edible")
    val isEdible: Boolean,

    @SerializedName("care_instructions")
    val careInstructions: List<String>,

    @SerializedName("edible_parts")
    val edibleParts: List<String>? = null
)


// =====================================================
// Retrofit Interface - Calls YOUR Backend
// =====================================================

interface PlantIdentificationApi {
    /**
     * POST /api/ai/plant/health-assessment
     * Analyze plant health issues via your backend
     */
    @Multipart
    @POST("api/ai/plant/health-assessment")
    suspend fun analyzeHealth(
        @Part image: MultipartBody.Part
    ): Response<PlantHealthResponse>

    /**
     * POST /api/ai/plant/identify
     * Identify plant species via your backend
     */
    @Multipart
    @POST("api/ai/plant/identify")
    suspend fun identifyPlant(
        @Part image: MultipartBody.Part
    ): Response<PlantIdentificationResponse>
}

// =====================================================
// Service Implementation
// =====================================================

@Singleton
class PlantIdService @Inject constructor(
    private val plantIdentificationApi: PlantIdentificationApi
) {

    /**
     * Identify plant health issues by sending image to YOUR backend
     */
    suspend fun identifyPlantHealthIssues(imageBytes: ByteArray): PlantAnalysisResult {
        return withContext(Dispatchers.IO) {
            try {
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    "plant_image.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )

                val response = plantIdentificationApi.analyzeHealth(imagePart)

                if (response.isSuccessful && response.body() != null) {
                    convertToAnalysisResult(response.body()!!)
                } else {
                    generateFallbackAnalysis()
                }
            } catch (e: Exception) {
                generateFallbackAnalysis()
            }
        }
    }

    /**
     * Identify plant species by sending image to YOUR backend
     */
    suspend fun identifyPlant(imageBytes: ByteArray): PlantIdentificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    "plant_image.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )

                val response = plantIdentificationApi.identifyPlant(imagePart)

                if (response.isSuccessful && response.body() != null) {
                    convertToIdentificationResult(response.body()!!)
                } else {
                    PlantIdentificationResult(
                        plantName = "Unknown Plant",
                        confidence = 0.0,
                        isEdible = false,
                        careInstructions = listOf("Unable to identify plant. Please consult an expert.")
                    )
                }
            } catch (e: Exception) {
                PlantIdentificationResult(
                    plantName = "Unknown Plant",
                    confidence = 0.0,
                    isEdible = false,
                    careInstructions = listOf("Unable to identify plant. Please consult an expert.")
                )
            }
        }
    }

    /**
     * Convert backend response to UI model
     */
    private fun convertToAnalysisResult(response: PlantHealthResponse): PlantAnalysisResult {
        val suggestions = response.suggestions.map { dto ->
            PlantSuggestion(
                name = dto.diseaseName,
                description = dto.description,
                probability = dto.probability,
                treatment = dto.treatment
            )
        }

        return PlantAnalysisResult(suggestions = suggestions)
    }

    /**
     * Convert backend response to UI model
     */
    private fun convertToIdentificationResult(response: PlantIdentificationResponse): PlantIdentificationResult {
        return PlantIdentificationResult(
            plantName = response.plantName,
            confidence = response.confidence,
            isEdible = response.isEdible,
            careInstructions = response.careInstructions
        )
    }

    /**
     * Fallback analysis when backend is unavailable
     */
    private fun generateFallbackAnalysis(): PlantAnalysisResult {
        return PlantAnalysisResult(
            suggestions = listOf(
                PlantSuggestion(
                    name = "General Plant Health Check",
                    description = "Unable to analyze image automatically. Please check for common issues.",
                    probability = 0.7,
                    treatment = "1. Check for yellowing or spotted leaves. 2. Look for insects or pests. 3. Ensure proper watering and drainage. 4. Consider fungal treatment if leaves show spots."
                )
            )
        )
    }
}

// =====================================================
// Result Data Class
// =====================================================

data class PlantIdentificationResult(
    val plantName: String,
    val confidence: Double,
    val isEdible: Boolean,
    val careInstructions: List<String>
)

data class PlantAnalysisResult(
    val suggestions: List<PlantSuggestion>
)

data class PlantSuggestion(
    val name: String,
    val description: String,
    val probability: Double,
    val treatment: String
)