package com.goldleaf.feature.trainingextension.domain.repository

import com.goldleaf.core.data.local.VideoCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for video training data
 */
interface VideoRepository {

    // Local database operations
    fun getAllVideos(): Flow<List<Video>>
    fun getVideosByCategory(category: String): Flow<List<Video>>
    suspend fun getVideoById(videoId: String): Video?
    fun getFavoriteVideos(): Flow<List<Video>>
    fun getRecentlyWatchedVideos(): Flow<List<Video>>
    suspend fun searchVideos(query: String): List<Video>
    suspend fun toggleFavorite(videoId: String)

    // Server sync operations
    suspend fun syncVideosFromServer(
        category: VideoCategory? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<Unit>

    suspend fun getCategories(): Result<List<Category>>

    // Progress tracking
    suspend fun updateVideoProgress(
        userId: String,
        videoId: String,
        watchedDuration: Int,
        completed: Boolean
    ): Result<Unit>

    suspend fun getUserProgress(userId: String): Result<List<VideoProgress>>

    suspend fun downloadVideoFile(video: Video): Result<Unit>
    suspend fun markVideoAsOfflineAvailable(videoId: String, isAvailable: Boolean): Result<Unit>
    suspend fun deleteDownloadedVideo(videoId: String): Result<Unit>
}

// Domain models
data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: Int,
    val category: VideoCategory,
    val instructor: String,
    val rating: Float,
    val views: Int,
    val isFavorite: Boolean = false,
    val lastWatched: String? = null,
    val watchProgress: Int = 0,
    val isOfflineAvailable: Boolean = false
)

data class Category(
    val id: String,
    val name: String,
    val description: String,
    val videoCount: Int,
    val icon: String
)

data class VideoProgress(
    val videoId: String,
    val watchedDuration: Int,
    val totalDuration: Int,
    val completed: Boolean,
    val lastWatched: String
)