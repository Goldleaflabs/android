package com.goldleaf.feature.advisoryservices.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.OpenAIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoilAnalysisViewModel @Inject constructor(
    private val openAIService: OpenAIService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilAnalysisUiState())
    val uiState: StateFlow<SoilAnalysisUiState> = _uiState.asStateFlow()

    fun analyzeSoil(
        soilType: String,
        ph: Double?,
        nitrogen: Double?,
        phosphorus: Double?,
        potassium: Double?,
        cropType: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val prompt = buildSoilAnalysisPrompt(soilType, ph, nitrogen, phosphorus, potassium, cropType)

                val result = openAIService.createCompletion(
                    prompt = prompt,
                    maxTokens = 800,
                    temperature = 0.7f
                )

                result.onSuccess { analysis ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        analysisResult = analysis,
                        error = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to analyze soil: ${error.message}"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Analysis failed: ${e.message}"
                )
            }
        }
    }

    private fun buildSoilAnalysisPrompt(
        soilType: String,
        ph: Double?,
        nitrogen: Double?,
        phosphorus: Double?,
        potassium: Double?,
        cropType: String
    ): String {
        return buildString {
            append("Analyze this soil for growing $cropType:\n")
            append("Soil Type: $soilType\n")
            if (ph != null) append("pH Level: $ph\n")
            if (nitrogen != null) append("Nitrogen: $nitrogen\n")
            if (phosphorus != null) append("Phosphorus: $phosphorus\n")
            if (potassium != null) append("Potassium: $potassium\n")
            append("\nProvide recommendations for:\n")
            append("1. Soil amendments needed\n")
            append("2. Fertilization strategy\n")
            append("3. Best practices for this crop\n")
            append("4. Expected challenges and solutions")
        }
    }
}

data class SoilAnalysisUiState(
    val isLoading: Boolean = false,
    val analysisResult: String? = null,
    val error: String? = null
)