package com.goldleaf.feature.advisoryservices.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.goldleaf.feature.advisoryservices.ui.FarmingContext
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

// =====================================================
// Request/Response Models for YOUR Backend
// =====================================================

data class GeminiFarmingAdviceRequest(
    @SerializedName("location")
    val location: String,

    @SerializedName("farm_size")
    val farmSize: Double,

    @SerializedName("soil_type")
    val soilType: String,

    @SerializedName("soil_ph")
    val soilPH: Double,

    @SerializedName("crops")
    val crops: List<String>,

    @SerializedName("current_weather")
    val currentWeather: String,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("rainfall")
    val rainfall: Double,

    @SerializedName("season")
    val season: String,

    @SerializedName("challenges")
    val challenges: List<String>
)

data class GeminiQuestionRequest(
    @SerializedName("question")
    val question: String,

    @SerializedName("context")
    val context: FarmingContextDto
)

data class FarmingContextDto(
    @SerializedName("location")
    val location: String,

    @SerializedName("soil_type")
    val soilType: String,

    @SerializedName("crops")
    val crops: List<String>,

    @SerializedName("current_weather")
    val currentWeather: String,

    @SerializedName("season")
    val season: String
)

data class GeminiRecommendationsResponse(
    @SerializedName("recommendations")
    val recommendations: List<RecommendationDto>,

    @SerializedName("source")
    val source: String = "Gemini AI"
)

data class RecommendationDto(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("confidence")
    val confidence: Int,

    @SerializedName("steps")
    val steps: List<String>
)

data class GeminiAnswerResponse(
    @SerializedName("answer")
    val answer: String,

    @SerializedName("source")
    val source: String = "Gemini AI"
)

// =====================================================
// Retrofit Interface - Calls YOUR Backend
// =====================================================

interface GeminiApi {
    /**
     * POST /api/ai/gemini/recommendations
     * Get farming recommendations from Gemini AI via your backend
     */
    @POST("api/ai/gemini/recommendations")
    suspend fun getRecommendations(
        @Body request: GeminiFarmingAdviceRequest
    ): Response<GeminiRecommendationsResponse>

    /**
     * POST /api/ai/gemini/question
     * Ask a farming question via your backend
     */
    @POST("api/ai/gemini/question")
    suspend fun askQuestion(
        @Body request: GeminiQuestionRequest
    ): Response<GeminiAnswerResponse>
}

// =====================================================
// Service Implementation
// =====================================================

@Singleton
class GeminiAIService @Inject constructor(
    private val geminiApi: GeminiApi
) {

    /**
     * Generate farming recommendations via YOUR backend
     */
    suspend fun generateFarmingRecommendations(context: FarmingContext): List<AIRecommendation> {
        return withContext(Dispatchers.IO) {
            try {
                val request = GeminiFarmingAdviceRequest(
                    location = context.location,
                    farmSize = context.farmSize,
                    soilType = context.soilType,
                    soilPH = context.soilPH,
                    crops = context.crops,
                    currentWeather = context.currentWeather,
                    humidity = context.humidity,
                    rainfall = context.rainfall,
                    season = context.season,
                    challenges = context.challenges
                )

                val response = geminiApi.getRecommendations(request)

                if (response.isSuccessful && response.body() != null) {
                    convertToAIRecommendations(response.body()!!.recommendations)
                } else {
                    // Fallback to expert system recommendations
                    generateExpertSystemRecommendations(context)
                }
            } catch (e: Exception) {
                // Fallback to expert system recommendations
                generateExpertSystemRecommendations(context)
            }
        }
    }

    /**
     * Ask a farming question via YOUR backend
     */
    suspend fun askFarmingQuestion(question: String, context: FarmingContext): String {
        return withContext(Dispatchers.IO) {
            try {
                val request = GeminiQuestionRequest(
                    question = question,
                    context = FarmingContextDto(
                        location = context.location,
                        soilType = context.soilType,
                        crops = context.crops,
                        currentWeather = context.currentWeather,
                        season = context.season
                    )
                )

                val response = geminiApi.askQuestion(request)

                response.body()?.answer
                    ?: "I'm sorry, I couldn't process your question right now. Please try again later."

            } catch (e: Exception) {
                "I'm experiencing technical difficulties. Please try again later."
            }
        }
    }

    /**
     * Convert backend DTOs to UI models
     */
    private fun convertToAIRecommendations(dtos: List<RecommendationDto>): List<AIRecommendation> {
        return dtos.map { dto ->
            AIRecommendation(
                title = dto.title,
                description = dto.description,
                category = dto.category,
                priority = dto.priority,
                priorityColor = when (dto.priority.uppercase()) {
                    "HIGH" -> Color(0xFFE91E63)
                    "MEDIUM" -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                confidence = dto.confidence,
                steps = dto.steps,
                source = "Gemini AI",
                icon = getCategoryIcon(dto.category)
            )
        }
    }

    /**
     * Fallback expert system recommendations (offline mode)
     */
    private fun generateExpertSystemRecommendations(context: FarmingContext): List<AIRecommendation> {
        val recommendations = mutableListOf<AIRecommendation>()

        // Soil health recommendation
        if (context.soilPH < 6.0 || context.soilPH > 7.5) {
            recommendations.add(
                AIRecommendation(
                    title = "Soil pH Adjustment Needed",
                    description = "Your soil pH (${context.soilPH}) is outside the optimal range for most crops",
                    category = "SOIL_HEALTH",
                    priority = "HIGH",
                    priorityColor = Color(0xFFE91E63),
                    confidence = 90,
                    steps = listOf(
                        if (context.soilPH < 6.0) "Apply agricultural lime to raise pH" else "Apply sulfur to lower pH",
                        "Test soil again after 4-6 weeks",
                        "Adjust fertilizer program based on new pH levels"
                    ),
                    source = "Expert System",
                    icon = Icons.Default.Terrain
                )
            )
        }

        // Weather-based recommendations
        if (context.season == "Long Rains") {
            recommendations.add(
                AIRecommendation(
                    title = "Long Rains Season Management",
                    description = "Optimal time for planting and managing water-sensitive crops",
                    category = "WEATHER",
                    priority = "MEDIUM",
                    priorityColor = Color(0xFFFF9800),
                    confidence = 85,
                    steps = listOf(
                        "Plant water-loving crops like rice, sugarcane",
                        "Ensure proper drainage to prevent waterlogging",
                        "Monitor for fungal diseases due to high humidity"
                    ),
                    source = "Expert System",
                    icon = Icons.Default.WaterDrop
                )
            )
        }

        // Crop-specific recommendations
        if (context.crops.any { it.contains("maize", ignoreCase = true) }) {
            recommendations.add(
                AIRecommendation(
                    title = "Maize Growth Optimization",
                    description = "Key practices for healthy maize development",
                    category = "CROP_CARE",
                    priority = "MEDIUM",
                    priorityColor = Color(0xFFFF9800),
                    confidence = 88,
                    steps = listOf(
                        "Apply nitrogen fertilizer at planting and 6 weeks",
                        "Control weeds early to reduce competition",
                        "Monitor for fall armyworm and treat if necessary"
                    ),
                    source = "Expert System",
                    icon = Icons.Default.Agriculture
                )
            )
        }

        return recommendations
    }

    private fun getCategoryIcon(category: String) = when (category.uppercase()) {
        "PEST_DISEASE" -> Icons.Default.BugReport
        "SOIL_HEALTH" -> Icons.Default.Terrain
        "WEATHER" -> Icons.Default.WbSunny
        "CROP_CARE" -> Icons.Default.Agriculture
        "FERTILIZER" -> Icons.Default.Eco
        "IRRIGATION" -> Icons.Default.WaterDrop
        "HARVEST" -> Icons.Default.Agriculture
        "MARKET" -> Icons.Default.Store
        else -> Icons.Default.Lightbulb
    }
}