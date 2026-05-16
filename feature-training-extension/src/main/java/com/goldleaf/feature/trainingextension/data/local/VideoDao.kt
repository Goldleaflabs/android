package com.goldleaf.feature.trainingextension.data.local


import androidx.room.*
import com.goldleaf.core.data.local.DifficultyLevel
import com.goldleaf.core.data.local.FarmingSeason
import com.goldleaf.core.data.local.StorageLocation
import com.goldleaf.core.data.local.TrainingVideo
import com.goldleaf.core.data.local.VideoCategory
import com.goldleaf.core.data.local.VideoDownload
import com.goldleaf.core.data.local.VideoReview
import com.goldleaf.core.data.local.VideoStatistics
import com.goldleaf.core.data.local.VideoWatchProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first


/**
 * Data Access Object for video-related operations
 *
 * This comprehensive DAO handles all database operations for the video training system,
 * including videos, progress tracking, downloads, reviews, and analytics.
 */
@Dao
interface VideoDao {

    // ==================== TRAINING VIDEO OPERATIONS ====================
    @Query("UPDATE training_videos SET isOfflineAvailable = :isAvailable WHERE id = :videoId")
    suspend fun updateOfflineStatus(videoId: String, isAvailable: Boolean)
    /**
     * Get all videos with reactive updates
     */
    @Query("SELECT * FROM training_videos WHERE isActive = 1 ORDER BY uploadedAt DESC")
    fun getAllVideos(): Flow<List<TrainingVideo>>

    /**
     * Get video by ID
     */
    @Query("SELECT * FROM training_videos WHERE id = :id AND isActive = 1")
    suspend fun getVideoById(id: String): TrainingVideo?

    /**
     * Get videos by category with sorting options
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE category = :category AND isActive = 1 
        ORDER BY 
        CASE WHEN :sortBy = 'RATING' THEN rating END DESC,
        CASE WHEN :sortBy = 'POPULARITY' THEN viewCount END DESC,
        CASE WHEN :sortBy = 'NEWEST' THEN uploadedAt END DESC,
        CASE WHEN :sortBy = 'TITLE' THEN title END ASC
    """)
    fun getVideosByCategory(
        category: VideoCategory,
        sortBy: String = "RATING"
    ): Flow<List<TrainingVideo>>

    /**
     * Get featured videos with limit
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isFeatured = 1 AND isActive = 1 
        ORDER BY rating DESC, viewCount DESC 
        LIMIT :limit
    """)
    fun getFeaturedVideos(limit: Int = 10): Flow<List<TrainingVideo>>

    /**
     * Get downloaded videos
     */
    @Query("""
        SELECT v.* FROM training_videos v
        INNER JOIN video_downloads d ON v.id = d.videoId
        WHERE d.isComplete = 1 AND v.isActive = 1
        ORDER BY d.downloadedAt DESC
    """)
    fun getDownloadedVideos(): Flow<List<TrainingVideo>>

    /**
     * Advanced video search with full-text search
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isActive = 1 AND (
            title LIKE '%' || :query || '%' OR
            description LIKE '%' || :query || '%' OR
            instructor LIKE '%' || :query || '%' OR
            shortDescription LIKE '%' || :query || '%' OR
            notes LIKE '%' || :query || '%'
        )
        ORDER BY 
        CASE 
            WHEN title LIKE :query || '%' THEN 1
            WHEN title LIKE '%' || :query || '%' THEN 2
            WHEN instructor LIKE :query || '%' THEN 3
            ELSE 4
        END,
        rating DESC, viewCount DESC
    """)
    fun searchVideos(query: String): Flow<List<TrainingVideo>>

    /**
     * Get videos by difficulty level
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE difficulty = :difficulty AND isActive = 1 
        ORDER BY rating DESC, viewCount DESC
    """)
    fun getVideosByDifficulty(difficulty: DifficultyLevel): Flow<List<TrainingVideo>>

    /**
     * Get videos by instructor
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE instructor = :instructor AND isActive = 1 
        ORDER BY uploadedAt DESC
    """)
    fun getVideosByInstructor(instructor: String): Flow<List<TrainingVideo>>

    /**
     * Get videos by multiple criteria (complex filtering)
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isActive = 1 
        AND (:category IS NULL OR category = :category)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:minDuration IS NULL OR duration >= :minDuration)
        AND (:maxDuration IS NULL OR duration <= :maxDuration)
        AND (:minRating IS NULL OR rating >= :minRating)
        AND (:instructor IS NULL OR instructor = :instructor)
        AND (:language IS NULL OR language = :language)
        AND (:isPremium IS NULL OR isPremium = :isPremium)
        ORDER BY 
        CASE WHEN :sortBy = 'RATING' THEN rating END DESC,
        CASE WHEN :sortBy = 'POPULARITY' THEN viewCount END DESC,
        CASE WHEN :sortBy = 'NEWEST' THEN uploadedAt END DESC,
        CASE WHEN :sortBy = 'DURATION_SHORT' THEN duration END ASC,
        CASE WHEN :sortBy = 'DURATION_LONG' THEN duration END DESC,
        CASE WHEN :sortBy = 'TITLE' THEN title END ASC
    """)
    fun getVideosWithFilters(
        category: VideoCategory? = null,
        difficulty: DifficultyLevel? = null,
        minDuration: Long? = null,
        maxDuration: Long? = null,
        minRating: Float? = null,
        instructor: String? = null,
        language: String? = null,
        isPremium: Boolean? = null,
        sortBy: String = "RATING"
    ): Flow<List<TrainingVideo>>

    /**
     * Get all unique instructors
     */
    @Query("""
        SELECT DISTINCT instructor FROM training_videos 
        WHERE isActive = 1 AND instructor IS NOT NULL AND instructor != ''
        ORDER BY instructor
    """)
    suspend fun getAllInstructors(): List<String>

    /**
     * Get top-rated videos above threshold
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE rating >= :minRating AND reviewCount >= :minReviews AND isActive = 1
        ORDER BY rating DESC, reviewCount DESC, viewCount DESC
        LIMIT :limit
    """)
    fun getTopRatedVideos(
        minRating: Float = 4.0f,
        minReviews: Int = 5,
        limit: Int = 20
    ): Flow<List<TrainingVideo>>

    /**
     * Get most popular videos by view count
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isActive = 1
        ORDER BY viewCount DESC, likeCount DESC
        LIMIT :limit
    """)
    fun getMostPopularVideos(limit: Int = 20): Flow<List<TrainingVideo>>

    /**
     * Get trending videos (high activity in recent period)
     */
    @Query("""
        SELECT v.* FROM training_videos v
        LEFT JOIN video_statistics s ON v.id = s.videoId
        WHERE v.isActive = 1
        ORDER BY 
        (COALESCE(s.weeklyViews, 0) * 0.4 + 
         COALESCE(s.monthlyViews, 0) * 0.3 + 
         v.rating * 0.2 + 
         v.likeCount * 0.1) DESC
        LIMIT :limit
    """)
    fun getTrendingVideos(limit: Int = 20): Flow<List<TrainingVideo>>

    /**
     * Get recently uploaded videos
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isActive = 1 AND uploadedAt >= :since
        ORDER BY uploadedAt DESC
        LIMIT :limit
    """)
    fun getRecentVideos(since: Long, limit: Int = 20): Flow<List<TrainingVideo>>

    /**
     * Get new videos (marked as new)
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isNew = 1 AND isActive = 1
        ORDER BY uploadedAt DESC
    """)
    fun getNewVideos(): Flow<List<TrainingVideo>>

    /**
     * Get videos by seasonal relevance
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE season = :season AND isActive = 1
        ORDER BY rating DESC, viewCount DESC
    """)
    fun getVideosBySeason(season: FarmingSeason): Flow<List<TrainingVideo>>

    // ==================== VIDEO CRUD OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: TrainingVideo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<TrainingVideo>)

    @Update
    suspend fun updateVideo(video: TrainingVideo)

    @Delete
    suspend fun deleteVideo(video: TrainingVideo)

    @Query("UPDATE training_videos SET isActive = 0 WHERE id = :videoId")
    suspend fun softDeleteVideo(videoId: String)

    /**
     * Increment view count atomically
     */
    @Query("""
        UPDATE training_videos 
        SET viewCount = viewCount + 1,
            updatedAt = :timestamp
        WHERE id = :videoId
    """)
    suspend fun incrementViewCount(videoId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update like count
     */
    @Query("UPDATE training_videos SET likeCount = :likeCount WHERE id = :videoId")
    suspend fun updateLikeCount(videoId: String, likeCount: Int)

    /**
     * Update share count
     */
    @Query("UPDATE training_videos SET shareCount = shareCount + 1 WHERE id = :videoId")
    suspend fun incrementShareCount(videoId: String)

    /**
     * Update video rating and review count
     */
    @Query("""
        UPDATE training_videos 
        SET rating = :rating, reviewCount = :reviewCount, updatedAt = :timestamp
        WHERE id = :videoId
    """)
    suspend fun updateVideoRating(
        videoId: String,
        rating: Float,
        reviewCount: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Mark video as featured/unfeatured
     */
    @Query("UPDATE training_videos SET isFeatured = :featured WHERE id = :videoId")
    suspend fun updateFeaturedStatus(videoId: String, featured: Boolean)

    // ==================== WATCH PROGRESS OPERATIONS ====================

    /**
     * Get user's watch progress for a specific video
     */
    @Query("""
        SELECT * FROM video_watch_progress 
        WHERE videoId = :videoId AND userId = :userId
    """)
    suspend fun getWatchProgress(videoId: String, userId: String): VideoWatchProgress?

    /**
     * Get all watch progress for a user
     */
    @Query("""
        SELECT * FROM video_watch_progress 
        WHERE userId = :userId 
        ORDER BY lastWatchedAt DESC
    """)
    fun getUserWatchProgress(userId: String): Flow<List<VideoWatchProgress>>

    /**
     * Get completed videos for a user
     */
    @Query("""
        SELECT * FROM video_watch_progress 
        WHERE userId = :userId AND isCompleted = 1 
        ORDER BY completedAt DESC
    """)
    fun getCompletedVideos(userId: String): Flow<List<VideoWatchProgress>>

    /**
     * Get videos in progress
     */
    @Query("""
        SELECT * FROM video_watch_progress 
        WHERE userId = :userId AND isCompleted = 0 AND watchedDuration > 0
        ORDER BY lastWatchedAt DESC
    """)
    fun getInProgressVideos(userId: String): Flow<List<VideoWatchProgress>>

    /**
     * Get videos to resume (with significant progress)
     */
    @Query("""
        SELECT * FROM video_watch_progress 
        WHERE userId = :userId AND isCompleted = 0 
        AND resumePosition > 30 AND watchedPercentage >= 10
        ORDER BY lastWatchedAt DESC
        LIMIT :limit
    """)
    fun getVideosToResume(userId: String, limit: Int = 10): Flow<List<VideoWatchProgress>>

    /**
     * Get learning statistics for user
     */
    @Query("""
        SELECT 
            COUNT(*) as totalVideos,
            COUNT(CASE WHEN isCompleted = 1 THEN 1 END) as completedVideos,
            CAST(SUM(watchedDuration) as INTEGER) as totalWatchTime,
            CAST(AVG(watchedPercentage) as REAL) as avgWatchPercentage,
            COUNT(CASE WHEN isCompleted = 1 AND completedAt >= :weekAgo THEN 1 END) as weeklyCompleted,
            COUNT(CASE WHEN isCompleted = 1 AND completedAt >= :monthAgo THEN 1 END) as monthlyCompleted
        FROM video_watch_progress 
        WHERE userId = :userId
    """)
    suspend fun getUserLearningStats(
        userId: String,
        weekAgo: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
        monthAgo: Long = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
    ): UserLearningStats?

    /**
     * Get user's favorite categories based on watch history
     */
    @Query("""
        SELECT v.category, COUNT(*) as watchCount
        FROM video_watch_progress p
        INNER JOIN training_videos v ON p.videoId = v.id
        WHERE p.userId = :userId AND p.watchedPercentage >= :minWatchPercentage
        GROUP BY v.category
        ORDER BY watchCount DESC, AVG(p.watchedPercentage) DESC
        LIMIT :limit
    """)
    suspend fun getUserFavoriteCategories(
        userId: String,
        minWatchPercentage: Float = 25f,
        limit: Int = 5
    ): List<CategoryStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchProgress(progress: VideoWatchProgress)

    @Update
    suspend fun updateWatchProgress(progress: VideoWatchProgress)

    /**
     * Mark video as completed with timestamp
     */
    @Query("""
        UPDATE video_watch_progress 
        SET isCompleted = 1, 
            completedAt = :completedAt,
            watchedPercentage = 100.0,
            lastWatchedAt = :completedAt
        WHERE videoId = :videoId AND userId = :userId
    """)
    suspend fun markVideoAsCompleted(
        videoId: String,
        userId: String,
        completedAt: Long = System.currentTimeMillis()
    )

    /**
     * Update resume position
     */
    @Query("""
        UPDATE video_watch_progress 
        SET resumePosition = :position, 
            lastWatchedAt = :timestamp,
            watchSessions = watchSessions + 1
        WHERE videoId = :videoId AND userId = :userId
    """)
    suspend fun updateResumePosition(
        videoId: String,
        userId: String,
        position: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteWatchProgress(progress: VideoWatchProgress)

    /**
     * Clear all progress for a user
     */
    @Query("DELETE FROM video_watch_progress WHERE userId = :userId")
    suspend fun clearUserProgress(userId: String)

    // ==================== DOWNLOAD OPERATIONS ====================

    /**
     * Get all downloads
     */
    @Query("SELECT * FROM video_downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<VideoDownload>>

    /**
     * Get download info for specific video
     */
    @Query("SELECT * FROM video_downloads WHERE videoId = :videoId")
    suspend fun getDownload(videoId: String): VideoDownload?

    /**
     * Get completed downloads
     */
    @Query("""
        SELECT * FROM video_downloads 
        WHERE isComplete = 1 
        ORDER BY downloadedAt DESC
    """)
    fun getCompletedDownloads(): Flow<List<VideoDownload>>

    /**
     * Get downloads in progress
     */
    @Query("""
        SELECT * FROM video_downloads 
        WHERE isComplete = 0 AND downloadProgress > 0
        ORDER BY downloadProgress DESC
    """)
    fun getIncompleteDownloads(): Flow<List<VideoDownload>>

    /**
     * Get pending downloads
     */
    @Query("""
        SELECT * FROM video_downloads 
        WHERE isComplete = 0 AND downloadProgress = 0
        ORDER BY downloadQueuePosition ASC
    """)
    fun getPendingDownloads(): Flow<List<VideoDownload>>

    /**
     * Get total download storage usage
     */
    @Query("""
        SELECT COALESCE(SUM(fileSize), 0) FROM video_downloads 
        WHERE isComplete = 1
    """)
    suspend fun getTotalDownloadSize(): Long

    /**
     * Get download count
     */
    @Query("SELECT COUNT(*) FROM video_downloads WHERE isComplete = 1")
    suspend fun getDownloadCount(): Int

    /**
     * Get downloads by storage location
     */
    @Query("""
        SELECT * FROM video_downloads 
        WHERE storageLocation = :location AND isComplete = 1
        ORDER BY downloadedAt DESC
    """)
    fun getDownloadsByLocation(location: StorageLocation): Flow<List<VideoDownload>>

    /**
     * Get expired downloads
     */
    @Query("""
        SELECT * FROM video_downloads 
        WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime
    """)
    suspend fun getExpiredDownloads(currentTime: Long = System.currentTimeMillis()): List<VideoDownload>

    /**
     * Get downloads that can be cleaned up (old, unused)
     */
    @Query("""
        SELECT * FROM video_downloads 
        WHERE isComplete = 1 AND canDelete = 1
        AND (
            (expiresAt IS NOT NULL AND expiresAt < :currentTime) OR
            (lastAccessedAt < :oldAccessThreshold AND accessCount = 0) OR
            (downloadedAt < :oldDownloadThreshold AND accessCount < 3)
        )
        ORDER BY lastAccessedAt ASC
    """)
    suspend fun getDownloadsForCleanup(
        currentTime: Long = System.currentTimeMillis(),
        oldAccessThreshold: Long = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L), // 30 days
        oldDownloadThreshold: Long = System.currentTimeMillis() - (90L * 24L * 60L * 60L * 1000L) // 90 days
    ): List<VideoDownload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: VideoDownload)

    @Update
    suspend fun updateDownload(download: VideoDownload)

    @Delete
    suspend fun deleteDownload(download: VideoDownload)

    @Query("DELETE FROM video_downloads WHERE videoId = :videoId")
    suspend fun deleteDownloadByVideoId(videoId: String)

    /**
     * Update download progress
     */
    @Query("""
        UPDATE video_downloads 
        SET downloadProgress = :progress,
            downloadSpeed = :speed,
            estimatedTimeRemaining = :timeRemaining
        WHERE videoId = :videoId
    """)
    suspend fun updateDownloadProgress(
        videoId: String,
        progress: Float,
        speed: Long = 0,
        timeRemaining: Long = 0
    )

    /**
     * Mark download as complete
     */
    @Query("""
        UPDATE video_downloads 
        SET isComplete = 1, 
            downloadProgress = 100.0,
            estimatedTimeRemaining = 0,
            checksumVerified = :verified
        WHERE videoId = :videoId
    """)
    suspend fun markDownloadComplete(videoId: String, verified: Boolean = true)

    /**
     * Update access tracking
     */
    @Query("""
        UPDATE video_downloads 
        SET lastAccessedAt = :accessTime,
            accessCount = accessCount + 1
        WHERE videoId = :videoId
    """)
    suspend fun updateDownloadAccess(videoId: String, accessTime: Long = System.currentTimeMillis())

    // ==================== REVIEW OPERATIONS ====================

    /**
     * Get reviews for a video
     */
    @Query("""
        SELECT * FROM video_reviews 
        WHERE videoId = :videoId AND isHidden = 0
        ORDER BY 
        CASE WHEN :sortBy = 'HELPFUL' THEN helpfulCount END DESC,
        CASE WHEN :sortBy = 'NEWEST' THEN createdAt END DESC,
        CASE WHEN :sortBy = 'OLDEST' THEN createdAt END ASC,
        CASE WHEN :sortBy = 'RATING_HIGH' THEN rating END DESC,
        CASE WHEN :sortBy = 'RATING_LOW' THEN rating END ASC,
        createdAt DESC
    """)
    fun getVideoReviews(videoId: String, sortBy: String = "HELPFUL"): Flow<List<VideoReview>>

    /**
     * Get reviews by user
     */
    @Query("""
        SELECT * FROM video_reviews 
        WHERE userId = :userId AND isHidden = 0
        ORDER BY createdAt DESC
    """)
    fun getUserReviews(userId: String): Flow<List<VideoReview>>

    /**
     * Get user's review for specific video
     */
    @Query("""
        SELECT * FROM video_reviews 
        WHERE videoId = :videoId AND userId = :userId AND isHidden = 0
    """)
    suspend fun getUserReviewForVideo(videoId: String, userId: String): VideoReview?

    /**
     * Get top reviews (highly rated and helpful)
     */
    @Query("""
        SELECT * FROM video_reviews 
        WHERE videoId = :videoId AND rating >= :minRating 
        AND helpfulCount >= :minHelpful AND isHidden = 0
        ORDER BY helpfulCount DESC, rating DESC, createdAt DESC
        LIMIT :limit
    """)
    fun getTopReviews(
        videoId: String,
        minRating: Float = 4.0f,
        minHelpful: Int = 3,
        limit: Int = 10
    ): Flow<List<VideoReview>>

    /**
     * Get verified reviews (from experts, instructors)
     */
    @Query("""
        SELECT * FROM video_reviews 
        WHERE videoId = :videoId AND reviewerType != 'REGULAR' AND isHidden = 0
        ORDER BY 
        CASE reviewerType
            WHEN 'EXPERT' THEN 1
            WHEN 'INSTRUCTOR' THEN 2
            WHEN 'VERIFIED' THEN 3
            ELSE 4
        END,
        helpfulCount DESC, createdAt DESC
    """)
    fun getVerifiedReviews(videoId: String): Flow<List<VideoReview>>

    /**
     * Get review statistics for a video
     */
    @Query("""
        SELECT 
            COUNT(*) as totalReviews,
            CAST(AVG(rating) as REAL) as averageRating,
            COUNT(CASE WHEN rating = 5 THEN 1 END) as fiveStars,
            COUNT(CASE WHEN rating = 4 THEN 1 END) as fourStars,
            COUNT(CASE WHEN rating = 3 THEN 1 END) as threeStars,
            COUNT(CASE WHEN rating = 2 THEN 1 END) as twoStars,
            COUNT(CASE WHEN rating = 1 THEN 1 END) as oneStar,
            COUNT(CASE WHEN wouldRecommend = 1 THEN 1 END) as recommendations
        FROM video_reviews 
        WHERE videoId = :videoId AND isHidden = 0
    """)
    suspend fun getVideoReviewStats(videoId: String): VideoReviewStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: VideoReview)

    @Update
    suspend fun updateReview(review: VideoReview)

    @Delete
    suspend fun deleteReview(review: VideoReview)

    /**
     * Mark review as helpful
     */
    @Query("""
        UPDATE video_reviews 
        SET helpfulCount = helpfulCount + 1 
        WHERE id = :reviewId
    """)
    suspend fun markReviewAsHelpful(reviewId: String)

    /**
     * Mark review as not helpful
     */
    @Query("""
        UPDATE video_reviews 
        SET notHelpfulCount = notHelpfulCount + 1 
        WHERE id = :reviewId
    """)
    suspend fun markReviewAsNotHelpful(reviewId: String)

    /**
     * Hide/show review (moderation)
     */
    @Query("""
        UPDATE video_reviews 
        SET isHidden = :hidden, 
            moderatedAt = :moderatedAt,
            moderatorNote = :note
        WHERE id = :reviewId
    """)
    suspend fun updateReviewVisibility(
        reviewId: String,
        hidden: Boolean,
        moderatedAt: Long = System.currentTimeMillis(),
        note: String? = null
    )

    // ==================== RECOMMENDATION OPERATIONS ====================

    /**
     * Get recommended videos based on user watch history
     */
    @Query("""
        SELECT DISTINCT v.* FROM training_videos v
        WHERE v.isActive = 1 AND v.id NOT IN (
            SELECT p.videoId FROM video_watch_progress p 
            WHERE p.userId = :userId AND p.watchedPercentage >= 25
        )
        AND v.category IN (
            SELECT DISTINCT tv.category FROM training_videos tv
            INNER JOIN video_watch_progress wp ON tv.id = wp.videoId
            WHERE wp.userId = :userId AND wp.isCompleted = 1
        )
        AND v.difficulty <= (
            SELECT COALESCE(MAX(
                CASE tv.difficulty
                    WHEN 'BEGINNER' THEN 1
                    WHEN 'INTERMEDIATE' THEN 2  
                    WHEN 'ADVANCED' THEN 3
                    WHEN 'PROFESSIONAL' THEN 4
                    ELSE 1
                END
            ), 1) FROM training_videos tv
            INNER JOIN video_watch_progress wp ON tv.id = wp.videoId
            WHERE wp.userId = :userId AND wp.isCompleted = 1
        )
        ORDER BY v.rating DESC, v.viewCount DESC
        LIMIT :limit
    """)
    fun getRecommendedVideos(userId: String, limit: Int = 20): Flow<List<TrainingVideo>>

    /**
     * Get similar videos based on category, difficulty, and tags
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE isActive = 1 AND id != :videoId
        AND (category = :category OR difficulty = :difficulty)
        ORDER BY 
        CASE WHEN category = :category THEN 2 ELSE 0 END +
        CASE WHEN difficulty = :difficulty THEN 1 ELSE 0 END DESC,
        rating DESC, viewCount DESC
        LIMIT :limit
    """)
    suspend fun getSimilarVideos(
        videoId: String,
        category: VideoCategory,
        difficulty: DifficultyLevel,
        limit: Int = 10
    ): List<TrainingVideo>

    /**
     * Get videos by same instructor (related content)
     */
    @Query("""
        SELECT * FROM training_videos 
        WHERE instructor = :instructor AND id != :excludeVideoId AND isActive = 1
        ORDER BY uploadedAt DESC, rating DESC
        LIMIT :limit
    """)
    suspend fun getVideosByInstructorExcluding(
        instructor: String,
        excludeVideoId: String,
        limit: Int = 5
    ): List<TrainingVideo>

    // ==================== ANALYTICS AND STATISTICS ====================

    /**
     * Get video statistics
     */
    @Query("SELECT * FROM video_statistics WHERE videoId = :videoId")
    suspend fun getVideoStatistics(videoId: String): VideoStatistics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoStatistics(statistics: VideoStatistics)

    @Update
    suspend fun updateVideoStatistics(statistics: VideoStatistics)

    /**
     * Get popular search terms (would need search history table)
     */
    @Query("""
        SELECT category, COUNT(*) as count FROM training_videos 
        WHERE isActive = 1 
        GROUP BY category 
        ORDER BY count DESC
    """)
    suspend fun getCategoryPopularity(): List<CategoryPopularity>

    /**
     * Get instructor rankings by engagement
     */
    @Query("""
        SELECT 
            instructor,
            COUNT(*) as videoCount,
            CAST(AVG(rating) as REAL) as averageRating,
            SUM(viewCount) as totalViews,
            SUM(likeCount) as totalLikes
        FROM training_videos 
        WHERE isActive = 1 AND instructor IS NOT NULL
        GROUP BY instructor
        HAVING videoCount >= :minVideos
        ORDER BY averageRating DESC, totalViews DESC
        LIMIT :limit
    """)
    suspend fun getTopInstructors(minVideos: Int = 3, limit: Int = 20): List<InstructorStats>

    // ==================== UTILITY AND MAINTENANCE OPERATIONS ====================

    /**
     * Clear all videos (for testing or reset)
     */
    @Query("DELETE FROM training_videos")
    suspend fun clearAllVideos()

    /**
     * Get database size information
     */
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM training_videos WHERE isActive = 1) as activeVideos,
            (SELECT COUNT(*) FROM video_watch_progress) as progressRecords,
            (SELECT COUNT(*) FROM video_downloads WHERE isComplete = 1) as completedDownloads,
            (SELECT COUNT(*) FROM video_reviews WHERE isHidden = 0) as reviews,
            (SELECT COALESCE(SUM(fileSize), 0) FROM video_downloads WHERE isComplete = 1) as totalDownloadSize
    """)
    suspend fun getDatabaseStats(): DatabaseStats?

    /**
     * Clean up old data
     */
    @Query("""
        DELETE FROM video_watch_progress 
        WHERE lastWatchedAt < :cutoffTime AND isCompleted = 0 AND watchedDuration < 30
    """)
    suspend fun cleanupOldIncompleteProgress(cutoffTime: Long)

    @Query("""
        DELETE FROM video_reviews 
        WHERE createdAt < :cutoffTime AND helpfulCount = 0 AND rating = 0
    """)
    suspend fun cleanupEmptyReviews(cutoffTime: Long)

    /**
     * Update video as downloaded/not downloaded
     */
    @Query("UPDATE training_videos SET isOfflineAvailable = :available WHERE id = :videoId")
    suspend fun updateOfflineAvailability(videoId: String, available: Boolean)

    /**
     * Batch update video metrics from analytics
     */
    @Query("""
        UPDATE training_videos 
        SET viewCount = :viewCount,
            rating = :rating,
            reviewCount = :reviewCount,
            likeCount = :likeCount,
            shareCount = :shareCount,
            updatedAt = :timestamp
        WHERE id = :videoId
    """)
    suspend fun updateVideoMetrics(
        videoId: String,
        viewCount: Int,
        rating: Float,
        reviewCount: Int,
        likeCount: Int,
        shareCount: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Get videos that need metric updates (stale data)
     */
    @Query("""
        SELECT id FROM training_videos 
        WHERE isActive = 1 AND updatedAt < :staleThreshold
        ORDER BY viewCount DESC
        LIMIT :limit
    """)
    suspend fun getVideosNeedingMetricUpdate(
        staleThreshold: Long = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // 24 hours
        limit: Int = 100
    ): List<String>

    // ==================== COMPLEX TRANSACTION OPERATIONS ====================

    /**
     * Transaction: Insert video with initial statistics
     */
    @Transaction
    suspend fun insertVideoWithStats(video: TrainingVideo, statistics: VideoStatistics) {
        insertVideo(video)
        insertVideoStatistics(statistics)
    }

    /**
     * Transaction: Complete video watch and update metrics
     */
    @Transaction
    suspend fun completeVideoWatch(
        userId: String,
        videoId: String,
        finalProgress: VideoWatchProgress,
        incrementView: Boolean = true
    ) {
        updateWatchProgress(finalProgress)
        markVideoAsCompleted(videoId, userId)
        if (incrementView) {
            incrementViewCount(videoId)
        }
    }

    /**
     * Transaction: Add review and update video rating
     */
    @Transaction
    suspend fun addReviewAndUpdateRating(
        review: VideoReview,
        newAverageRating: Float,
        newReviewCount: Int
    ) {
        insertReview(review)
        updateVideoRating(review.videoId, newAverageRating, newReviewCount)
    }

    /**
     * Transaction: Delete download and update video status
     */
    @Transaction
    suspend fun deleteDownloadAndUpdateVideo(videoId: String) {
        deleteDownloadByVideoId(videoId)
        updateOfflineAvailability(videoId, false)
    }

    /**
     * Transaction: Bulk cleanup old data
     */
    @Transaction
    suspend fun performMaintenanceCleanup(
        progressCutoff: Long = System.currentTimeMillis() - (90L * 24L * 60L * 60L * 1000L), // 90 days
        reviewCutoff: Long = System.currentTimeMillis() - (365L * 24L * 60L * 60L * 1000L) // 1 year
    ) {
        cleanupOldIncompleteProgress(progressCutoff)
        cleanupEmptyReviews(reviewCutoff)
    }
}

// =====================================================
// DATA CLASSES FOR QUERY RESULTS
// =====================================================

/**
 * Statistics result classes for complex queries
 */
data class UserLearningStats(
    val totalVideos: Int = 0,
    val completedVideos: Int = 0,
    val totalWatchTime: Long = 0, // seconds
    val avgWatchPercentage: Float = 0f,
    val weeklyCompleted: Int = 0,
    val monthlyCompleted: Int = 0
) {
    val completionRate: Float get() = if (totalVideos > 0) (completedVideos.toFloat() / totalVideos) * 100f else 0f
    val averageSessionTime: Long get() = if (completedVideos > 0) totalWatchTime / completedVideos else 0L
}

data class CategoryStats(
    val category: VideoCategory,
    val watchCount: Int
)

data class VideoReviewStats(
    val totalReviews: Int = 0,
    val averageRating: Float = 0f,
    val fiveStars: Int = 0,
    val fourStars: Int = 0,
    val threeStars: Int = 0,
    val twoStars: Int = 0,
    val oneStar: Int = 0,
    val recommendations: Int = 0
) {
    val recommendationPercentage: Float get() = if (totalReviews > 0) (recommendations.toFloat() / totalReviews) * 100f else 0f
    val positiveReviews: Int get() = fiveStars + fourStars
    val negativeReviews: Int get() = twoStars + oneStar
    val positivePercentage: Float get() = if (totalReviews > 0) (positiveReviews.toFloat() / totalReviews) * 100f else 0f
}

data class CategoryPopularity(
    val category: VideoCategory,
    val count: Int
)

data class InstructorStats(
    val instructor: String,
    val videoCount: Int,
    val averageRating: Float,
    val totalViews: Int,
    val totalLikes: Int
) {
    val engagementScore: Float get() = (averageRating * 0.4f) +
            (totalLikes.toFloat() / totalViews.coerceAtLeast(1) * 100 * 0.3f) +
            (videoCount * 0.3f)
}

data class DatabaseStats(
    val activeVideos: Int = 0,
    val progressRecords: Int = 0,
    val completedDownloads: Int = 0,
    val reviews: Int = 0,
    val totalDownloadSize: Long = 0
)

// =====================================================
// DAO EXTENSIONS AND HELPER FUNCTIONS
// =====================================================

suspend fun VideoDao.getVideosAdvanced(
    searchQuery: String? = null,
    categories: List<VideoCategory> = emptyList(),
    difficulties: List<DifficultyLevel> = emptyList(),
    minDuration: Long? = null,
    maxDuration: Long? = null,
    minRating: Float? = null,
    instructor: String? = null,
    language: String? = null,
    sortBy: SortOption = SortOption.RATING,
    offset: Int = 0,
    limit: Int = 20
): List<TrainingVideo> {
    return if (!searchQuery.isNullOrBlank()) {
        searchVideos(searchQuery).first().drop(offset).take(limit)
    } else {
        getVideosWithFilters(
            category = categories.firstOrNull(),
            difficulty = difficulties.firstOrNull(),
            minDuration = minDuration,
            maxDuration = maxDuration,
            minRating = minRating,
            instructor = instructor,
            language = language,
            sortBy = sortBy.name
        ).first().drop(offset).take(limit)
    }
}

/**
 * Get personalized learning dashboard data
 */
suspend fun VideoDao.getLearningDashboard(userId: String): LearningDashboard {
    val stats = getUserLearningStats(userId)
    val inProgress = getInProgressVideos(userId).first()
    val toResume = getVideosToResume(userId, 5).first()
    val favorites = getUserFavoriteCategories(userId)

    return LearningDashboard(
        stats = stats,
        inProgressVideos = inProgress,
        videosToResume = toResume,
        favoriteCategories = favorites,
        recommendedVideos = getRecommendedVideos(userId, 10).first()
    )
}

/**
 * Get comprehensive video details with all related data
 */
suspend fun VideoDao.getVideoDetails(videoId: String, userId: String? = null): VideoDetails? {
    val video = getVideoById(videoId) ?: return null
    val statistics = getVideoStatistics(videoId)
    val reviews = getVideoReviews(videoId).first()
    val similarVideos = getSimilarVideos(videoId, video.category, video.difficulty, 5)
    val instructorVideos = getVideosByInstructorExcluding(video.instructor, videoId, 3)
    val userProgress = userId?.let { getWatchProgress(videoId, it) }
    val userReview = userId?.let { getUserReviewForVideo(videoId, it) }
    val downloadInfo = getDownload(videoId)

    return VideoDetails(
        video = video,
        statistics = statistics,
        reviews = reviews.take(10), // Limit for performance
        similarVideos = similarVideos,
        instructorVideos = instructorVideos,
        userProgress = userProgress,
        userReview = userReview,
        downloadInfo = downloadInfo
    )
}

/**
 * Batch operations for better performance
 */
suspend fun VideoDao.batchUpdateViewCounts(videoViews: Map<String, Int>) {
    videoViews.forEach { (videoId, count) ->
        repeat(count) {
            incrementViewCount(videoId)
        }
    }
}

/**
 * Search with autocomplete suggestions
 */
suspend fun VideoDao.getSearchSuggestions(query: String, limit: Int = 10): List<String> {
    val instructors = getAllInstructors().filter {
        it.contains(query, ignoreCase = true)
    }.take(limit / 2)

    val categories = VideoCategory.values().filter {
        it.displayName.contains(query, ignoreCase = true)
    }.map { it.displayName }.take(limit / 2)

    return (instructors + categories).take(limit)
}

// =====================================================
// COMPLEX DATA STRUCTURES FOR DASHBOARD
// =====================================================

data class LearningDashboard(
    val stats: UserLearningStats?,
    val inProgressVideos: List<VideoWatchProgress>,
    val videosToResume: List<VideoWatchProgress>,
    val favoriteCategories: List<CategoryStats>,
    val recommendedVideos: List<TrainingVideo>
)

data class VideoDetails(
    val video: TrainingVideo,
    val statistics: VideoStatistics?,
    val reviews: List<VideoReview>,
    val similarVideos: List<TrainingVideo>,
    val instructorVideos: List<TrainingVideo>,
    val userProgress: VideoWatchProgress?,
    val userReview: VideoReview?,
    val downloadInfo: VideoDownload?
)

// =====================================================
// SORT OPTIONS ENUM FOR QUERIES
// =====================================================

enum class SortOption {
    RELEVANCE, RATING, POPULARITY, NEWEST, OLDEST,
    DURATION_SHORT, DURATION_LONG, TITLE, INSTRUCTOR, HELPFUL
}

// =====================================================
// QUERY BUILDERS FOR DYNAMIC FILTERING
// =====================================================

/**
 * Builder class for complex video queries
 */
class VideoQueryBuilder {
    private var searchQuery: String? = null
    private var categories: List<VideoCategory> = emptyList()
    private var difficulties: List<DifficultyLevel> = emptyList()
    private var instructors: List<String> = emptyList()
    private var minRating: Float? = null
    private var maxDuration: Long? = null
    private var minDuration: Long? = null
    private var sortBy: SortOption = SortOption.RELEVANCE
    private var limit: Int = 20
    private var offset: Int = 0

    fun search(query: String) = apply { this.searchQuery = query }
    fun categories(vararg categories: VideoCategory) = apply { this.categories = categories.toList() }
    fun difficulties(vararg difficulties: DifficultyLevel) = apply { this.difficulties = difficulties.toList() }
    fun instructors(vararg instructors: String) = apply { this.instructors = instructors.toList() }
    fun minRating(rating: Float) = apply { this.minRating = rating }
    fun duration(min: Long? = null, max: Long? = null) = apply {
        this.minDuration = min
        this.maxDuration = max
    }
    fun sortBy(sort: SortOption) = apply { this.sortBy = sort }
    fun limit(limit: Int) = apply { this.limit = limit }
    fun offset(offset: Int) = apply { this.offset = offset }

    fun buildSqlQuery(): String {
        val conditions = mutableListOf("v.isActive = 1")

        searchQuery?.let {
            conditions.add("(v.title LIKE '%$it%' OR v.description LIKE '%$it%' OR v.instructor LIKE '%$it%')")
        }

        if (categories.isNotEmpty()) {
            val categoryList = categories.joinToString("','", "'", "'") { it.name }
            conditions.add("v.category IN ($categoryList)")
        }

        if (difficulties.isNotEmpty()) {
            val difficultyList = difficulties.joinToString("','", "'", "'") { it.name }
            conditions.add("v.difficulty IN ($difficultyList)")
        }

        if (instructors.isNotEmpty()) {
            val instructorList = instructors.joinToString("','", "'", "'")
            conditions.add("v.instructor IN ($instructorList)")
        }

        minRating?.let { conditions.add("v.rating >= $it") }
        minDuration?.let { conditions.add("v.duration >= $it") }
        maxDuration?.let { conditions.add("v.duration <= $it") }

        val whereClause = "WHERE ${conditions.joinToString(" AND ")}"

        val orderClause = when (sortBy) {
            SortOption.RATING -> "ORDER BY v.rating DESC, v.reviewCount DESC"
            SortOption.POPULARITY -> "ORDER BY v.viewCount DESC, v.likeCount DESC"
            SortOption.NEWEST -> "ORDER BY v.uploadedAt DESC"
            SortOption.OLDEST -> "ORDER BY v.uploadedAt ASC"
            SortOption.DURATION_SHORT -> "ORDER BY v.duration ASC"
            SortOption.DURATION_LONG -> "ORDER BY v.duration DESC"
            SortOption.TITLE -> "ORDER BY v.title ASC"
            SortOption.INSTRUCTOR -> "ORDER BY v.instructor ASC, v.title ASC"
            else -> "ORDER BY v.rating DESC, v.viewCount DESC" // RELEVANCE default
        }

        return """
            SELECT v.* FROM training_videos v
            $whereClause
            $orderClause
            LIMIT $limit OFFSET $offset
        """.trimIndent()
    }
}


/*
fun VideoEntity.toDomainModel(): Video {
    return Video(
        id = id,
        title = title,
        description = description,
        instructor = instructor,
        category = VideoCategory.valueOf(category),
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoUrl,
        duration = duration,
        rating = rating,
        isOfflineAvailable = isOfflineAvailable, // Map this
        isFavorite = isFavorite
    )
}*/