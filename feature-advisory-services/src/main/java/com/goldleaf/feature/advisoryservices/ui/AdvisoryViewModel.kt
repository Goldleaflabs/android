package com.goldleaf.feature.advisoryservices.ui


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.PlantAnalysisResult
import com.goldleaf.feature.advisoryservices.data.AIRecommendation
import com.goldleaf.feature.advisoryservices.data.AdvisoryRepository
import com.goldleaf.feature.advisoryservices.data.GeminiAIService
import com.goldleaf.core.data.api.PlantIdService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class AdvisoryViewModel @Inject constructor(
    private val advisoryRepository: AdvisoryRepository,
    private val geminiAIService: GeminiAIService,
    private val plantIdService: PlantIdService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisoryUiState())
    val uiState: StateFlow<AdvisoryUiState> = _uiState.asStateFlow()

    fun loadAdvisoryData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val weatherData = advisoryRepository.getCurrentWeatherData()
                val cropData = advisoryRepository.getCurrentCropData()

                // Generate AI status
                val aiStatus = generateAIStatus()

                // Load urgent alerts
                val urgentAlerts = advisoryRepository.getUrgentAlerts()

                // Generate weather-based advice
                val weatherAdvice = generateWeatherBasedAdvice(weatherData)

                // Get crop health insights
                val cropHealthInsights = generateCropHealthInsights(cropData)


                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    aiStatus = aiStatus,
                    urgentAlerts = urgentAlerts,
                    weatherAdvice = weatherAdvice,
                    cropHealthInsights = cropHealthInsights,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load advisory data: ${e.message}"
                )
            }
        }
    }

    fun generatePersonalizedRecommendations() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingRecommendations = true)

                val farmerData = advisoryRepository.getFarmerData()
                val cropData = advisoryRepository.getCurrentCropData()
                val weatherData = advisoryRepository.getCurrentWeatherData()
                val soilData = advisoryRepository.getSoilData()

                // Create context for AI
                val farmingContext = buildFarmingContext(farmerData, cropData, weatherData, soilData)

                // Generate recommendations using Gemini AI
                val aiRecommendations = geminiAIService.generateFarmingRecommendations(farmingContext)

                _uiState.value = _uiState.value.copy(
                    isGeneratingRecommendations = false,
                    recommendations = aiRecommendations
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingRecommendations = false,
                    error = "Failed to generate AI recommendations: ${e.message}"
                )
            }
        }
    }

    fun refreshRecommendations() {
        generatePersonalizedRecommendations()
    }

    fun analyzePlantImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            try {
                val analysisResult = plantIdService.identifyPlantHealthIssues(imageBytes)

                // Convert Plant.id result to recommendations
                val recommendations = convertPlantAnalysisToRecommendations(analysisResult)

                _uiState.value = _uiState.value.copy(
                    recommendations = _uiState.value.recommendations + recommendations
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to analyze plant image: ${e.message}"
                )
            }
        }
    }

    private fun generateAIStatus(): AIStatus {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timeString = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"

        return AIStatus(
            statusText = "Online & Learning",
            description = "AI assistant is actively monitoring your farm conditions",
            statusColor = Color(0xFF4CAF50),
            icon = Icons.Default.Psychology,
            lastUpdated = timeString
        )
    }

    private fun buildFarmingContext(
        farmerData: FarmerData,
        cropData: List<CropData>,
        weatherData: WeatherContextData,
        soilData: SoilData
    ): FarmingContext {
        return FarmingContext(
            location = farmerData.location,
            farmSize = farmerData.farmSize,
            soilType = soilData.type,
            soilPH = soilData.ph,
            crops = cropData.map { "${it.name} (${it.variety})" },
            currentWeather = "${weatherData.temperature}°C, ${weatherData.condition}",
            humidity = weatherData.humidity,
            rainfall = weatherData.rainfall,
            season = getCurrentSeason(),
            challenges = farmerData.challenges
        )
    }

    private fun generateWeatherBasedAdvice(weatherData: WeatherContextData): List<WeatherAdvice> {
        val advice = mutableListOf<WeatherAdvice>()

        // High temperature advice
        if (weatherData.temperature > 30) {
            advice.add(
                WeatherAdvice(
                    title = "High Temperature Alert",
                    advice = "Increase irrigation frequency. Provide shade for young plants. Avoid midday field work.",
                    weatherCondition = "Hot",
                    weatherIcon = Icons.Default.WbSunny
                )
            )
        }

        // Rain advice
        if (weatherData.rainChance > 70) {
            advice.add(
                WeatherAdvice(
                    title = "Heavy Rain Expected",
                    advice = "Ensure proper drainage. Delay fertilizer application. Harvest ready crops if possible.",
                    weatherCondition = "Rainy",
                    weatherIcon = Icons.Default.WaterDrop
                )
            )
        }

        // High humidity advice
        if (weatherData.humidity > 85) {
            advice.add(
                WeatherAdvice(
                    title = "High Humidity Warning",
                    advice = "Monitor for fungal diseases. Improve air circulation around plants. Consider fungicide application.",
                    weatherCondition = "Humid",
                    weatherIcon = Icons.Default.Cloud
                )
            )
        }

        return advice
    }

    private fun generateCropHealthInsights(cropData: List<CropData>): List<CropHealthInsight> {
        return cropData.map { crop ->
            val healthColor = when (crop.healthScore) {
                in 80..100 -> Color(0xFF4CAF50)
                in 60..79 -> Color(0xFFFF9800)
                else -> Color(0xFFE91E63)
            }

            val healthStatus = when (crop.healthScore) {
                in 80..100 -> "Excellent"
                in 60..79 -> "Good"
                in 40..59 -> "Fair"
                else -> "Poor"
            }

            val insight = when {
                crop.healthScore >= 80 -> "Your ${crop.name} is thriving! Continue current care routine."
                crop.healthScore >= 60 -> "Your ${crop.name} shows good growth. Monitor for any changes."
                else -> "Your ${crop.name} needs attention. Check for pests, diseases, or nutrient deficiencies."
            }

            CropHealthInsight(
                cropName = crop.name,
                healthStatus = healthStatus,
                healthColor = healthColor,
                insight = insight,
                healthIcon = when (crop.healthScore) {
                    in 80..100 -> Icons.Default.CheckCircle
                    in 60..79 -> Icons.Default.Warning
                    else -> Icons.Default.Error
                }
            )
        }
    }

    private fun convertPlantAnalysisToRecommendations(analysisResult: PlantAnalysisResult): List<AIRecommendation> {
        return analysisResult.suggestions.map { suggestion ->
            AIRecommendation(
                title = suggestion.name,
                description = suggestion.description,
                category = "PEST_DISEASE",
                priority = when (suggestion.probability) {
                    in 0.8..1.0 -> "HIGH"
                    in 0.5..0.8 -> "MEDIUM"
                    else -> "LOW"
                },
                priorityColor = when (suggestion.probability) {
                    in 0.8..1.0 -> Color(0xFFE91E63)
                    in 0.5..0.8 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                confidence = (suggestion.probability * 100).toInt(),
                steps = suggestion.treatment.split(". ").take(3),
                source = "Plant.id AI",
                icon = Icons.Default.BugReport
            )
        }
    }

    private fun getCurrentSeason(): String {
        val month = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber
        return when (month) {
            12, 1, 2 -> "Dry Season"
            3, 4, 5 -> "Long Rains"
            6, 7, 8 -> "Cool Season"
            9, 10, 11 -> "Short Rains"
            else -> "Unknown"
        }
    }
}

data class AdvisoryUiState(
    val isLoading: Boolean = false,
    val isGeneratingRecommendations: Boolean = false,
    val aiStatus: AIStatus = AIStatus(
        statusText = "Initializing...",
        description = "Starting AI advisory services",
        statusColor = Color.Gray,
        icon = Icons.Default.Psychology,
        lastUpdated = "00:00"
    ),
    val recommendations: List<AIRecommendation> = emptyList(),
    val urgentAlerts: List<UrgentAlert> = emptyList(),
    val weatherAdvice: List<WeatherAdvice> = emptyList(),
    val cropHealthInsights: List<CropHealthInsight> = emptyList(),
    val marketInsights: List<MarketInsight> = emptyList(),
    val error: String? = null
)

// Data classes for API integration
data class FarmingContext(
    val location: String,
    val farmSize: Double,
    val soilType: String,
    val soilPH: Double,
    val crops: List<String>,
    val currentWeather: String,
    val humidity: Int,
    val rainfall: Double,
    val season: String,
    val challenges: List<String>
)

data class FarmerData(
    val location: String,
    val farmSize: Double,
    val challenges: List<String>
)

data class CropData(
    val name: String,
    val variety: String,
    val plantingDate: String,
    val healthScore: Int
)

data class WeatherContextData(
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val rainChance: Int,
    val rainfall: Double
)

data class SoilData(
    val type: String,
    val ph: Double,
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double
)
