package com.goldleaf.core.data.api

import android.net.Uri
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Header
import javax.inject.Inject
import javax.inject.Singleton
import com.goldleaf.core.auth.UserSessionManager

// Backend API Interface
interface AIBackendService {
    @POST("api/ai/completion")
    suspend fun createCompletion(
        @Header("Authorization") token: String,
        @Body request: CompletionRequest
    ): Response<CompletionResponse>

    @POST("api/ai/farming-advice")
    suspend fun getFarmingAdvice(
        @Header("Authorization") token: String,
        @Body request: FarmingAdviceRequest
    ): Response<CompletionResponse>

    @POST("api/ai/analyze-pest")
    suspend fun analyzePest(
        @Header("Authorization") token: String,
        @Body request: PestAnalysisRequest
    ): Response<CompletionResponse>

    @POST("api/ai/market-advice")
    suspend fun getMarketAdvice(
        @Header("Authorization") token: String,
        @Body request: MarketAdviceRequest
    ): Response<CompletionResponse>

    @POST("api/ai/optimize-irrigation")
    suspend fun optimizeIrrigation(
        @Header("Authorization") token: String,
        @Body request: IrrigationRequest
    ): Response<CompletionResponse>

    @POST("api/ai/identify-plant")
    suspend fun identifyPlant(
        @Header("Authorization") token: String,
        @Body request: PlantIdentificationRequest
    ): Response<PlantIdentificationResponse>
}

// Request/Response Data Classes
data class CompletionRequest(
    val prompt: String,
    val maxTokens: Int = 800,
    val temperature: Float = 0.7f,
    val useGPT4: Boolean = false
)

data class FarmingAdviceRequest(
    val cropType: String,
    val location: String,
    val growthStage: String,
    val weatherConditions: String
)

data class PestAnalysisRequest(
    val symptoms: String,
    val cropType: String,
    val location: String,
    val image: String? = null
)

data class MarketAdviceRequest(
    val crops: List<String>,
    val location: String,
    val harvestPeriod: String
)

data class IrrigationRequest(
    val cropType: String,
    val soilType: String,
    val weatherForecast: String,
    val farmSize: String
)

data class PlantIdentificationRequest(
    val images: List<String>,
    val known_merch: List<String> = emptyList(),
    val similar_images: Boolean = false,
    val threshold: Int = 0,
    val language: String = "en"
)

data class CompletionResponse(
    val completion: String,
    val tokensUsed: Int
)


// Service Implementation
@Singleton
class OpenAIService @Inject constructor(
    private val apiService: AIBackendService,
    private val userSession: UserSessionManager
) {
    suspend fun createCompletion(
        prompt: String,
        maxTokens: Int = 800,
        temperature: Float = 0.7f,
        useGPT4: Boolean = false
    ): Result<String> {
        return try {

            val request = CompletionRequest(
                prompt = prompt,
                maxTokens = maxTokens,
                temperature = temperature,
                useGPT4 = useGPT4
            )
            val token = userSession.getBearerToken()

            val response = apiService.createCompletion("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.completion)
            } else {
                Result.failure(Exception("AI service error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFarmingAdvice(
        cropType: String,
        location: String,
        growthStage: String,
        weatherConditions: String
    ): Result<String> {
        return try {
            val token = userSession.getBearerToken()


            val request = FarmingAdviceRequest(
                cropType = cropType,
                location = location,
                growthStage = growthStage,
                weatherConditions = weatherConditions
            )

            val response = apiService.getFarmingAdvice("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.completion)
            } else {
                Result.failure(Exception("Failed to get farming advice: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzePest(
        symptoms: String,
        cropType: String,
        location: String,
        imageUri: Uri? = null
    ): Result<String> {
        return try {
            val token = userSession.getBearerToken()


            val request = PestAnalysisRequest(
                symptoms = symptoms,
                cropType = cropType,
                location = location,
                image = imageUri?.toString()
            )

            val response = apiService.analyzePest("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.completion)
            } else {
                Result.failure(Exception("Failed to analyze pest: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMarketAdvice(
        crops: List<String>,
        location: String,
        harvestPeriod: String
    ): Result<String> {
        return try {
            val token = userSession.getBearerToken()


            val request = MarketAdviceRequest(
                crops = crops,
                location = location,
                harvestPeriod = harvestPeriod
            )

            val response = apiService.getMarketAdvice("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.completion)
            } else {
                Result.failure(Exception("Failed to get market advice: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun optimizeIrrigation(
        cropType: String,
        soilType: String,
        weatherForecast: String,
        farmSize: String
    ): Result<String> {
        return try {
            val token = userSession.getBearerToken()


            val request = IrrigationRequest(
                cropType = cropType,
                soilType = soilType,
                weatherForecast = weatherForecast,
                farmSize = farmSize
            )

            val response = apiService.optimizeIrrigation("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.completion)
            } else {
                Result.failure(Exception("Failed to optimize irrigation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun identifyPlant(
        images: List<String>,
        knownMerch: List<String> = emptyList(),
        similarImages: Boolean = false,
        threshold: Int = 0,
        language: String = "en"
    ): Result<PlantIdentificationResponse> {
        return try {
            val token = userSession.getBearerToken()


            val request = PlantIdentificationRequest(
                images = images,
                known_merch = knownMerch,
                similar_images = similarImages,
                threshold = threshold,
                language = language
            )

            val response = apiService.identifyPlant("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to identify plant: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}