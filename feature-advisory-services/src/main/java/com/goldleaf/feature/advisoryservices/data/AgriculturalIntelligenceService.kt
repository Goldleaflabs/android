package com.goldleaf.feature.advisoryservices.data


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.goldleaf.core.data.api.MarketDataService
import com.goldleaf.core.data.api.OpenAIService
import com.goldleaf.core.data.api.WeatherAPIService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgriculturalIntelligenceService @Inject constructor(
    private val openAIService: OpenAIService,
    private val weatherAPIService: WeatherAPIService,
    private val marketDataService: MarketDataService
) {

    suspend fun generateComprehensiveRecommendations(
        farmLocation: String,
        crops: List<String>,
        latitude: Double,
        longitude: Double
    ): Result<List<AIRecommendation>> = coroutineScope {
        try {
            val weatherDeferred = async {
                weatherAPIService.getCurrentWeather(latitude, longitude)
            }

            val weather = weatherDeferred.await().getOrNull()

            val recommendations = mutableListOf<AIRecommendation>()

            // Weather-based recommendations
            if (weather != null) {
                val weatherPrompt = buildString {
                    append("Based on current weather conditions ")
                    append("(Temperature: ${weather.current.temperature}°C, ")
                    append("Humidity: ${weather.current.humidity}%, ")
                    append("Forecast: ${weather.current.weather_description}), ")
                    append("provide agricultural recommendations for ${crops.joinToString()} in $farmLocation. ")
                    append("Be specific and actionable.")
                }

                val weatherResult = openAIService.createCompletion(
                    prompt = weatherPrompt,
                    maxTokens = 500,
                    temperature = 0.7f
                )

                if (weatherResult.isSuccess) {
                    recommendations.addAll(
                        parseRecommendations(
                            weatherResult.getOrThrow(),
                            "Weather Advisory",
                            Icons.Default.Cloud
                        )
                    )
                }
            }

            Result.success(recommendations)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseRecommendations(
        response: String,
        category: String,
        icon: ImageVector
    ): List<AIRecommendation> {
        val recommendations = mutableListOf<AIRecommendation>()
        val sections = response.split("\n\n").filter { it.isNotBlank() }

        sections.forEachIndexed { _, section ->
            val lines = section.split("\n").filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                val title = lines.first().take(100)
                val description = if (lines.size > 1) lines[1].take(200) else ""

                recommendations.add(
                    AIRecommendation(
                        title = title,
                        description = description,
                        category = category,
                        priority = "Medium",
                        priorityColor = Color(0xFF1976D2),
                        icon = icon,
                        steps = lines.drop(2).take(3),
                        confidence = 85,
                        source = "AI Agricultural Intelligence"
                    )
                )
            }
        }

        return recommendations
    }
}
