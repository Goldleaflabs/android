package com.goldleaf.feature.trainingextension.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.local.VideoCategory
import com.goldleaf.feature.trainingextension.domain.repository.Category
import com.goldleaf.feature.trainingextension.domain.repository.Video
import com.goldleaf.feature.trainingextension.domain.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CORRECT TrainingViewModel - Uses proper repository and state management
 */
@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VideoCategory?>(null)
    val selectedCategory: StateFlow<VideoCategory?> = _selectedCategory.asStateFlow()

    init {
        loadInitialData()
    }

    fun onCategorySelected(category: VideoCategory?) {
        _selectedCategory.value = category
    }


    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load categories
            videoRepository.getCategories()
                .onSuccess { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }

            // Sync videos from server
            syncVideos()

            // Observe local videos
            observeVideos()
        }
    }

    private fun observeVideos() {
        viewModelScope.launch {
            combine(
                videoRepository.getAllVideos(),
                _selectedCategory,
                _uiState.map { it.searchQuery }
            ) { allVideos, categorys, searchQuery ->
                var filteredVideos = allVideos

                // Filter by category
                if (categorys != null) {
                    filteredVideos = filteredVideos.filter { it.category == categorys }

                }

                // Filter by search query
                if (searchQuery.isNotEmpty()) {
                    filteredVideos = filteredVideos.filter { video ->
                        video.title.contains(searchQuery, ignoreCase = true) ||
                                video.description.contains(searchQuery, ignoreCase = true) ||
                                video.instructor.contains(searchQuery, ignoreCase = true)
                    }
                }

                filteredVideos
            }.collect { videos ->
                _uiState.update {
                    it.copy(
                        videos = videos,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun selectCategory(category: VideoCategory?) {
        _selectedCategory.value = category
    }

    fun searchVideos(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun syncVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }

            videoRepository.syncVideosFromServer(
                category = _selectedCategory.value,
                page = 1,
                limit = 50
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = "Sync failed: ${error.message}"
                    )
                }
            }
        }
    }

    fun toggleFavorite(videoId: String) {
        viewModelScope.launch {
            videoRepository.toggleFavorite(videoId)
        }
    }

    fun updateVideoProgress(
        userId: String,
        videoId: String,
        watchedDuration: Int,
        totalDuration: Int
    ) {
        viewModelScope.launch {
            val completed = watchedDuration >= totalDuration

            videoRepository.updateVideoProgress(
                userId = userId,
                videoId = videoId,
                watchedDuration = watchedDuration,
                completed = completed
            ).onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun loadFavoriteVideos() {
        viewModelScope.launch {
            videoRepository.getFavoriteVideos().collect { favorites ->
                _uiState.update { it.copy(favoriteVideos = favorites) }
            }
        }
    }

    fun loadRecentlyWatched() {
        viewModelScope.launch {
            videoRepository.getRecentlyWatchedVideos().collect { recent ->
                _uiState.update { it.copy(recentlyWatched = recent) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshCategories() {
        viewModelScope.launch {
            videoRepository.getCategories()
                .onSuccess { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }


    fun onVideoClicked(video: Video) {
        viewModelScope.launch {
            try {
                // Track video click - you can add analytics here later if needed
                // For now, just update the selected video in state
                _uiState.update { it.copy(selectedVideo = video) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error: ${e.message}") }
            }
        }
    }

    fun downloadVideo(video: Video) {
        viewModelScope.launch {
            try {
                // Add video to downloading set
                _uiState.update { currentState ->
                    currentState.copy(
                        downloadingVideos = currentState.downloadingVideos + video.id
                    )
                }

                // Download the actual video file
                videoRepository.downloadVideoFile(video)

                // Mark video as offline available in the database
                videoRepository.markVideoAsOfflineAvailable(video.id, true)



            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        downloadingVideos = currentState.downloadingVideos - video.id,
                        error = "Failed to download video: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            try {

                // Delete the downloaded video file
                videoRepository.deleteDownloadedVideo(videoId)

                // Mark video as NOT offline available
                videoRepository.markVideoAsOfflineAvailable(videoId, false)
                // Implement delete download logic when ready
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete: ${e.message}") }
            }
        }
    }

}

// UI State
data class TrainingUiState(
    val videos: List<Video> = emptyList(),
    val favoriteVideos: List<Video> = emptyList(),
    val recentlyWatched: List<Video> = emptyList(),
    val featuredVideos: List<Video> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedVideo: Video? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val lastSyncTime: Long = 0L,
    val downloadingVideos: Set<String> = emptySet() // Add this line
)

