package com.goldleaf.feature.advisoryservices.data

import com.goldleaf.core.data.api.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

data class AIRecommendation(
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val priorityColor: Color,
    val confidence: Int,
    val steps: List<String>,
    val source: String,
    val icon: ImageVector
)

data class MarketInsight(
    val commodity: String,
    val currentPrice: Double,
    val trend: String,
    val recommendation: String
)

@Singleton
class AIServiceRepository @Inject constructor(
    private val openAIService: OpenAIService,
    private val weatherAPIService: WeatherAPIService,
    private val marketDataService: MarketDataService
) {

    suspend fun generateWeatherBasedRecommendations(
        farmLocation: String,
        crops: List<String>,
        latitude: Double,
        longitude: Double
    ): Result<List<AIRecommendation>> = coroutineScope {
        try {
            val weatherResult = weatherAPIService.getCurrentWeather(latitude, longitude)

            if (weatherResult.isFailure) {
                return@coroutineScope Result.failure(
                    Exception("Weather data unavailable. Cannot generate recommendations.")
                )
            }

            val weatherData = weatherResult.getOrThrow()

            val prompt = buildString {
                append("Based on the following farm and weather data, provide agricultural recommendations:\n")
                append("Farm Location: $farmLocation\n")
                append("Crops: ${crops.joinToString()}\n")
                append("Current Weather: Temperature ${weatherData.current.temperature}°C, ")
                append("Humidity ${weatherData.current.humidity}%, ")
                append("Wind Speed: ${weatherData.current.wind_speed} m/s\n\n")
                append("Provide specific, actionable recommendations for irrigation, pest management, and crop care.")
            }

            val responseResult = openAIService.createCompletion(
                prompt = prompt,
                maxTokens = 800,
                temperature = 0.7f
            )
            val response = responseResult.getOrThrow()  // Now response is String
            Result.success(parseAIResponseToRecommendations(response, "Weather",Icons.Default.Info))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generatePestDiseaseRecommendations(
        farmLocation: String,
        crops: List<String>,
        latitude: Double,
        longitude: Double
    ): Result<List<AIRecommendation>> = coroutineScope {
        try {
            val weatherResult = weatherAPIService.getCurrentWeather(latitude, longitude)

            if (weatherResult.isFailure) {
                return@coroutineScope Result.failure(
                    Exception("Weather data required for pest analysis is unavailable.")
                )
            }

            val weatherData = weatherResult.getOrThrow()

            val prompt = buildString {
                append("As an agricultural AI expert, analyze pest and disease risks:\n")
                append("Location: $farmLocation\n")
                append("Crops: ${crops.joinToString()}\n")
                append("Season: ${getCurrentSeason()}\n")
                append("Weather conditions: ${weatherData.current.temperature}°C, ")
                append("${weatherData.current.humidity}% humidity\n")
                append("Precipitation: ${weatherData.current.precipitation}mm\n\n")
                append("Identify potential pest and disease threats and provide prevention strategies.")
            }

            val responseResult = openAIService.createCompletion(
                prompt = prompt,
                maxTokens = 600,
                temperature = 0.6f
            )

            val response = responseResult.getOrThrow()  // Now response is String
            Result.success(parseAIResponseToRecommendations(response, "Pest & Disease",Icons.Default.Warning ))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateMarketRecommendations(
        crops: List<String>,
        location: String
    ): Result<List<AIRecommendation>> = coroutineScope {
        try {
            val marketResult = marketDataService.getMarketPrices(crops, location)

            if (marketResult.isFailure) {
                return@coroutineScope Result.failure(
                    Exception("Market data unavailable. Cannot generate market recommendations.")
                )
            }

            val marketData = marketResult.getOrThrow()

            val prompt = buildString {
                append("Analyze market conditions and provide selling recommendations:\n")
                append("Crops: ${crops.joinToString()}\n")
                append("Current market prices:\n")
                marketData.forEach { data ->
                    append("- ${data.cropName}: ${data.currentPrice} per kg\n")
                }
                append("Location: $location\n\n")
                append("Provide timing recommendations for selling and market strategy.")
            }

            val responseResult = openAIService.createCompletion(
                prompt = prompt,
                maxTokens = 500,
                temperature = 0.8f
            )
            val response = responseResult.getOrThrow()  // Now response is String

            Result.success(parseAIResponseToRecommendations(response, "Market",Icons.Default.Info  ))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMarketInsights(
        crops: List<String>,
        location: String
    ): Result<List<MarketInsight>> {
        return try {
            val marketResult = marketDataService.getMarketPrices(crops, location)

            if (marketResult.isFailure) {
                return Result.failure(
                    Exception("Market data service unavailable")
                )
            }

            val marketData = marketResult.getOrThrow()

            val insights = marketData.map { data ->
                MarketInsight(
                    commodity = data.cropName,
                    currentPrice = data.currentPrice,
                    trend = determineTrend(data.priceHistory),
                    recommendation = generateMarketRecommendation(data)
                )
            }

            Result.success(insights)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAIResponseToRecommendations(
        response: String,
        category: String,
        icon: ImageVector
    ): List<AIRecommendation> {
        val recommendations = mutableListOf<AIRecommendation>()
        val sections = response.split("\n\n").filter { it.isNotBlank() }

        sections.forEachIndexed { _ , section ->
            val lines = section.split("\n").filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                val title = lines.first().removePrefix("- ").removePrefix("* ").take(100)
                val description = if (lines.size > 1) lines[1].take(200) else "AI-generated recommendation"
                val steps = lines.drop(2).map { it.removePrefix("- ").removePrefix("* ").take(150) }

                recommendations.add(
                    AIRecommendation(
                        title = title,
                        description = description,
                        category = category,
                        priority = determinePriority(title, description),
                        priorityColor = getPriorityColor(determinePriority(title, description)),
                        icon = icon,
                        steps = steps,
                        confidence = 85, // Based on AI model confidence
                        source = "AI Agricultural Assistant",
                    )
                )
            }
        }

        return recommendations
    }

    private fun determinePriority(title: String, description: String): String {
        val highKeywords = listOf("urgent", "critical", "immediate", "alert", "risk", "danger", "disease", "pest")
        val mediumKeywords = listOf("recommend", "suggest", "optimal", "should", "consider")

        val text = "$title $description".lowercase()

        return when {
            highKeywords.any { it in text } -> "High"
            mediumKeywords.any { it in text } -> "Medium"
            else -> "Low"
        }
    }

    private fun getPriorityColor(priority: String): Color {
        return when (priority) {
            "High" -> Color(0xFFD32F2F)
            "Medium" -> Color(0xFF1976D2)
            else -> Color(0xFF388E3C)
        }
    }

    private fun generateEstimatedImpact(category: String): String {
        return when (category) {
            "Weather" -> "Optimize water and resource usage"
            "Pest & Disease" -> "Prevent potential yield loss"
            "Soil Health" -> "Improve soil fertility and crop yield"
            "Crop Care" -> "Enhance crop quality and growth"
            "Market" -> "Optimize selling timing for better prices"
            else -> "Improve farming efficiency"
        }
    }

    private fun getCurrentSeason(): String {
        val month = LocalDateTime.now().monthValue
        return when (month) {
            in 3..5 -> "Long Rains Season"
            in 6..9 -> "Dry Season"
            in 10..12 -> "Short Rains Season"
            else -> "Harvest Season"
        }
    }

    private fun determineTrend(priceHistory: List<Double>): String {
        if (priceHistory.size < 2) return "Insufficient data"

        val recent = priceHistory.takeLast(3).average()
        val previous = priceHistory.dropLast(3).takeLast(3).average()

        return when {
            recent > previous * 1.05 -> "Rising"
            recent < previous * 0.95 -> "Falling"
            else -> "Stable"
        }
    }

    private fun generateMarketRecommendation(marketData: MarketPriceData): String {
        val trend = determineTrend(marketData.priceHistory)
        val priceChange = ((marketData.currentPrice - marketData.previousPrice) / marketData.previousPrice * 100).toInt()

        return when {
            trend == "Rising" && priceChange > 5 -> "Good time to sell - prices trending upward by $priceChange%"
            trend == "Falling" && priceChange < -5 -> "Consider selling soon - prices declining by ${-priceChange}%"
            trend == "Stable" -> "Monitor market - prices stable at ${marketData.currentPrice}/kg"
            else -> "Track price movements for optimal selling timing"
        }
    }
}