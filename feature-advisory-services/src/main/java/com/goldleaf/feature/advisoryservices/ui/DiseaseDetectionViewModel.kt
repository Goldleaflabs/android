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
class DiseaseDetectionViewModel @Inject constructor(
    private val openAIService: OpenAIService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiseaseDetectionUiState())
    val uiState: StateFlow<DiseaseDetectionUiState> = _uiState.asStateFlow()

    fun detectDisease(
        cropType: String,
        symptoms: String,
        affectedArea: String,
        duration: String,
        location: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val prompt = buildDiseaseDetectionPrompt(
                    cropType = cropType,
                    symptoms = symptoms,
                    affectedArea = affectedArea,
                    duration = duration,
                    location = location
                )

                val result = openAIService.createCompletion(
                    prompt = prompt,
                    maxTokens = 1000,
                    temperature = 0.7f
                )

                result.onSuccess { analysis ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        detectionResult = analysis,
                        error = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to detect disease: ${error.message}"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Disease detection failed: ${e.message}"
                )
            }
        }
    }

    private fun buildDiseaseDetectionPrompt(
        cropType: String,
        symptoms: String,
        affectedArea: String,
        duration: String,
        location: String
    ): String {
        return buildString {
            append("I need help identifying a disease on my $cropType crop.\n\n")
            append("Symptoms observed:\n$symptoms\n\n")
            if (affectedArea.isNotBlank()) {
                append("Affected plant parts: $affectedArea\n")
            }
            if (duration.isNotBlank()) {
                append("Duration: $duration\n")
            }
            if (location.isNotBlank()) {
                append("Location: $location\n")
            }
            append("\nPlease provide:\n")
            append("1. Most likely disease identification\n")
            append("2. Detailed explanation of the disease\n")
            append("3. Treatment recommendations (organic and chemical options)\n")
            append("4. Prevention measures for future\n")
            append("5. Expected timeline for recovery\n")
            append("6. Warning signs to watch for\n")
        }
    }
}

data class DiseaseDetectionUiState(
    val isLoading: Boolean = false,
    val detectionResult: String? = null,
    val error: String? = null
)