package com.goldleaf.feature.trainingextension.data.repository

import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.ProgressUpdateRequest

import com.goldleaf.core.data.local.TrainingVideo

import com.goldleaf.core.data.local.VideoCategory

import com.goldleaf.core.data.local.VideoWatchProgress
import com.goldleaf.feature.trainingextension.data.local.VideoDao
import com.goldleaf.feature.trainingextension.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.first


/**
 * CORRECT Implementation - Uses goldleaflabs.co.ke API endpoints
 */
class VideoRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val videoDao: VideoDao
) : VideoRepository {

    override fun getAllVideos(): Flow<List<Video>> {
        return videoDao.getAllVideos().map { entities ->
            entities.map { it.toVideo() }
        }
    }

    override fun getVideosByCategory(category: String): Flow<List<Video>> {
        return videoDao.getVideosByCategory(VideoCategory.valueOf(category)).map { entities ->
            entities.map { it.toVideo() }
        }
    }

    override suspend fun getVideoById(videoId: String): Video? {
        return videoDao.getVideoById(videoId)?.toVideo()
    }



    override suspend fun syncVideosFromServer(category: VideoCategory?, page: Int, limit: Int): Result<Unit> {
        return try {
            val response = apiService.getTrainingVideos(category, page, limit)

            if (response.isSuccessful) {
                val videos = response.body()?.videos ?: emptyList()
                val videoEntities = videos.map { dto ->
                    TrainingVideo(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        thumbnailUrl = dto.thumbnailUrl,
                        videoUrl = dto.videoUrl,
                        duration = dto.duration.toLong(),
                        category = VideoCategory.valueOf(dto.category),
                        instructor = dto.instructor,
                        rating = dto.rating,
                        viewCount = dto.views
                    )
                }
                videoDao.insertVideos(videoEntities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync videos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()

            if (response.isSuccessful) {
                val categories = response.body()?.map { dto ->
                    Category(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        videoCount = dto.videoCount,
                        icon = dto.icon
                    )
                } ?: emptyList()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVideoProgress(
        userId: String,
        videoId: String,
        watchedDuration: Int,
        completed: Boolean
    ): Result<Unit> {
        return try {
            val progress = VideoWatchProgress(
                videoId = videoId,
                userId = userId,
                watchedDuration = watchedDuration.toLong(),
                totalDuration = 0L,
                isCompleted = completed
            )
            videoDao.insertWatchProgress(progress)

            // Sync with server
            val response = apiService.updateProgress(
                ProgressUpdateRequest(
                    userId = userId,
                    videoId = videoId,
                    watchedDuration = watchedDuration,
                    completed = completed
                )
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server sync failed but progress saved locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProgress(userId: String): Result<List<VideoProgress>> {
        return try {
            val response = apiService.getUserProgress(userId)

            if (response.isSuccessful) {
                val progressList = response.body()?.map { dto ->
                    VideoProgress(
                        videoId = dto.videoId,
                        watchedDuration = dto.watchedDuration,
                        totalDuration = dto.totalDuration,
                        completed = dto.completed,
                        lastWatched = dto.lastWatched
                    )
                } ?: emptyList()
                Result.success(progressList)
            } else {
                Result.failure(Exception("Failed to fetch progress: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadVideoFile(video: Video): Result<Unit> {
        return try {
            // 1. Mark as downloaded locally first (local-first pattern)
            val videoEntity = videoDao.getVideoById(video.id)
            if (videoEntity != null) {
                val updated = videoEntity.copy(
                    isOfflineAvailable = true
                )
                videoDao.insertVideo(updated)
            }

            // 2. Try to download file in background (non-blocking)
            // TODO: If real download needed, use WorkManager for large files
            // For now: store URL locally so it can be played when online
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markVideoAsOfflineAvailable(videoId: String, isAvailable: Boolean): Result<Unit> {
        return try {
            videoDao.updateOfflineStatus(videoId, isAvailable)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDownloadedVideo(videoId: String): Result<Unit> {
        return try {
            // Use the existing transaction to update database status
            videoDao.deleteDownloadAndUpdateVideo(videoId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(videoId: String) {
        try {
            val video = videoDao.getVideoById(videoId)
            if (video != null) {
                videoDao.updateVideo(
                    video.copy(isFeatured = !video.isFeatured)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to toggle favorite: ${e.message}", e)
        }
    }


    override fun getFavoriteVideos(): Flow<List<Video>> {
        return videoDao.getAllVideos().map { entities ->
            entities.filter { it.isFeatured }.map { it.toVideo() }
        }
    }

    override suspend fun searchVideos(query: String): List<Video> {
        return videoDao.searchVideos(query).first().map { it.toVideo() }
    }

    override fun getRecentlyWatchedVideos(): Flow<List<Video>> {
        return videoDao.getAllVideos().map { entities ->
            entities.take(10).map { it.toVideo() }
        }
    }
}

// Extension function to map TrainingVideo to Video
private fun TrainingVideo.toVideo(): Video {
    return Video(
        id = this.id,
        title = this.title,
        description = this.description,
        thumbnailUrl = this.thumbnailUrl,
        videoUrl = this.videoUrl,
        duration = this.duration.toInt(),
        category = this.category,
        instructor = this.instructor,
        rating = this.rating,
        views = this.viewCount,
        isFavorite = this.isFeatured,
        lastWatched = null,
        watchProgress = 0
    )
}
