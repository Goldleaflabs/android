package com.goldleaf.feature.advisoryservices.ui


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.OpenAIService
import com.goldleaf.feature.advisoryservices.util.PestImageValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PestDetectionViewModel @Inject constructor(
    private val openAIService: OpenAIService,
    private val pestImageValidator: PestImageValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(PestDetectionUiState())
    val uiState: StateFlow<PestDetectionUiState> = _uiState.asStateFlow()

    fun analyzePest(
        symptoms: String,
        cropType: String,
        location: String,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Validate symptoms and crop type
                if (symptoms.isBlank() || cropType.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Please provide symptoms and crop type"
                    )
                    return@launch
                }

                // Validate image if provided
                if (imageUri != null && !pestImageValidator.isValidImage(imageUri)) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Image is too large or invalid (max 10MB)"
                    )
                    return@launch
                }

                // Call OpenAI service for analysis
                val result = openAIService.analyzePest(
                    symptoms = symptoms,
                    cropType = cropType,
                    location = location,
                    imageUri = imageUri
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
                        error = "Failed to analyze: ${error.message ?: "Unknown error"}"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Analysis failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    /**
     * Clear the analysis result
     */
    fun clearAnalysis() {
        _uiState.value = _uiState.value.copy(
            analysisResult = null,
            error = null
        )
    }
}

data class PestDetectionUiState(
    val isLoading: Boolean = false,
    val analysisResult: String? = null,
    val error: String? = null
)
