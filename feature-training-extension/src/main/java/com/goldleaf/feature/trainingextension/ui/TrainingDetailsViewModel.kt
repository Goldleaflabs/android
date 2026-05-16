package com.goldleaf.feature.trainingextension.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.VideoApiService
import com.goldleaf.core.data.network.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingDetailsViewModel @Inject constructor(
    private val videoApiService: VideoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingDetailsUiState())
    val uiState: StateFlow<TrainingDetailsUiState> = _uiState.asStateFlow()

    fun loadTrainingDetails(trainingId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val response = videoApiService.getVideoById(trainingId)

                when (response) {
                    is ApiResponse.Success -> {
                        val video = response.data
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            training = TrainingDetail(
                                id = video.id,
                                title = video.title,
                                description = video.description,
                                videoUrl = video.videoUrl,
                                duration = video.duration.toString(),
                                category = video.category,
                                instructor = video.instructor
                            ),
                            error = null
                        )
                    }
                    is ApiResponse.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }

                    ApiResponse.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load training: ${e.message}"
                )
            }
        }
    }
}

data class TrainingDetailsUiState(
    val isLoading: Boolean = false,
    val training: TrainingDetail? = null,
    val error: String? = null
)

data class TrainingDetail(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val duration: String,
    val category: String,
    val instructor: String
)