package com.goldleaf.core.data.models
// =====================================================
// File: ApiModels.kt
// Location: core/src/main/kotlin/com/goldleaf/core/data/models/ApiModels.kt
// =====================================================
import com.google.gson.annotations.SerializedName
// =====================================================
// VIDEO DTOs
// =====================================================

/**
 * Video Data Transfer Object from API
 */
data class VideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("instructor") val instructor: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("category") val category: String,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("review_count") val reviewCount: Int,
    @SerializedName("view_count") val viewCount: Int,
    @SerializedName("is_featured") val isFeatured: Boolean,
    @SerializedName("is_offline_available") val isOfflineAvailable: Boolean,
    @SerializedName("download_size") val downloadSize: Long,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

/**
 * Video view tracking request
 */
data class VideoViewRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("device_info") val deviceInfo: DeviceInfo? = null
)

/**
 * Download URL response
 */
data class DownloadUrlResponse(
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("expires_at") val expiresAt: Long,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("quality") val quality: String
)

// =====================================================
// AUTHENTICATION DTOs
// =====================================================

/**
 * Login request
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("device_id") val deviceId: String? = null
)

/**
 * Register request
 */
data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String? = null
)

/**
 * Authentication response
 */
data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("user") val user: UserDto
)

/**
 * Refresh token request
 */
data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

/**
 * Verify email request
 */
data class VerifyEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)

/**
 * Forgot password request
 */
data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

/**
 * Reset password request
 */
data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
    @SerializedName("new_password") val newPassword: String
)

// =====================================================
// USER DTOs
// =====================================================

/**
 * User Data Transfer Object
 */
data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("role") val role: String,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("created_at") val createdAt: String
)

/**
 * User profile DTO
 */
data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("preferences") val preferences: UserPreferences?,
    @SerializedName("stats") val stats: UserStats?
)

/**
 * Update profile request
 */
data class UpdateProfileRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)

/**
 * User preferences
 */
data class UserPreferences(
    @SerializedName("language") val language: String,
    @SerializedName("notifications_enabled") val notificationsEnabled: Boolean,
    @SerializedName("auto_download") val autoDownload: Boolean,
    @SerializedName("video_quality") val videoQuality: String
)

/**
 * User statistics
 */
data class UserStats(
    @SerializedName("total_watch_time") val totalWatchTime: Long,
    @SerializedName("videos_watched") val videosWatched: Int,
    @SerializedName("videos_completed") val videosCompleted: Int,
    @SerializedName("learning_streak") val learningStreak: Int,
    @SerializedName("certificates_earned") val certificatesEarned: Int
)

// =====================================================
// PROGRESS DTOs
// =====================================================

/**
 * Video progress DTO
 */
data class VideoProgressDto(
    @SerializedName("video_id") val videoId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("watched_duration") val watchedDuration: Int,
    @SerializedName("total_duration") val totalDuration: Int,
    @SerializedName("watched_percentage") val watchedPercentage: Float,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("last_position") val lastPosition: Int,
    @SerializedName("updated_at") val updatedAt: String
)

/**
 * Update progress request
 */
data class UpdateProgressRequest(
    @SerializedName("watched_duration") val watchedDuration: Int,
    @SerializedName("last_position") val lastPosition: Int,
    @SerializedName("is_completed") val isCompleted: Boolean
)

// =====================================================
// ANALYTICS DTOs
// =====================================================

/**
 * Analytics event
 */
data class AnalyticsEvent(
    @SerializedName("event_type") val eventType: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("video_id") val videoId: String?,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("properties") val properties: Map<String, Any>
)

/**
 * Video analytics DTO
 */
data class VideoAnalyticsDto(
    @SerializedName("video_id") val videoId: String,
    @SerializedName("total_views") val totalViews: Int,
    @SerializedName("unique_viewers") val uniqueViewers: Int,
    @SerializedName("average_watch_time") val averageWatchTime: Long,
    @SerializedName("completion_rate") val completionRate: Float,
    @SerializedName("retention_curve") val retentionCurve: List<Float>,
    @SerializedName("top_exit_points") val topExitPoints: List<Long>,
    @SerializedName("rating_breakdown") val ratingBreakdown: Map<Int, Int>
)

/**
 * User analytics DTO
 */
data class UserAnalyticsDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("total_watch_time") val totalWatchTime: Long,
    @SerializedName("videos_watched") val videosWatched: Int,
    @SerializedName("videos_completed") val videosCompleted: Int,
    @SerializedName("favorite_categories") val favoriteCategories: List<String>,
    @SerializedName("average_session_length") val averageSessionLength: Long,
    @SerializedName("learning_streak") val learningStreak: Int,
    @SerializedName("weekly_activity") val weeklyActivity: List<DailyActivity>
)

/**
 * Daily activity data
 */
data class DailyActivity(
    @SerializedName("date") val date: String,
    @SerializedName("watch_time") val watchTime: Long,
    @SerializedName("videos_watched") val videosWatched: Int
)

/**
 * Watch time tracking request
 */
data class WatchTimeRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("watch_time") val watchTime: Long,
    @SerializedName("current_position") val currentPosition: Int,
    @SerializedName("session_id") val sessionId: String
)

// =====================================================
// REVIEW DTOs
// =====================================================

/**
 * Video review DTO
 */
data class VideoReviewDto(
    @SerializedName("id") val id: String,
    @SerializedName("video_id") val videoId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_avatar") val userAvatar: String?,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("helpful_count") val helpfulCount: Int
)

/**
 * Add review request
 */
data class AddReviewRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String
)

/**
 * Update review request
 */
data class UpdateReviewRequest(
    @SerializedName("rating") val rating: Int?,
    @SerializedName("comment") val comment: String?
)

// =====================================================
// DOWNLOAD DTOs
// =====================================================

/**
 * Video download DTO
 */
data class VideoDownloadDto(
    @SerializedName("video_id") val videoId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("expires_at") val expiresAt: Long,
    @SerializedName("downloaded_at") val downloadedAt: String
)

/**
 * Download request
 */
data class DownloadRequest(
    @SerializedName("video_id") val videoId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("quality") val quality: String
)

// =====================================================
// RECOMMENDATION DTOs
// =====================================================

/**
 * Video recommendation DTO
 */
data class VideoRecommendationDto(
    @SerializedName("video") val video: VideoDto,
    @SerializedName("score") val score: Float,
    @SerializedName("reasons") val reasons: List<String>,
    @SerializedName("recommendation_type") val recommendationType: String
)

/**
 * Recommendation request
 */
data class RecommendationRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("categories") val categories: List<String>? = null
)

// =====================================================
// SEARCH DTOs
// =====================================================

/**
 * Search response
 */
data class SearchResponse(
    @SerializedName("results") val results: List<VideoDto>,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("has_more") val hasMore: Boolean
)

/**
 * Search filters
 */
data class SearchFilters(
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("min_rating") val minRating: Float? = null,
    @SerializedName("duration_min") val durationMin: Int? = null,
    @SerializedName("duration_max") val durationMax: Int? = null
)

// =====================================================
// CATEGORY DTOs
// =====================================================

/**
 * Category DTO
 */
data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("video_count") val videoCount: Int,
    @SerializedName("order") val order: Int
)

// =====================================================
// PLAYLIST DTOs
// =====================================================

/**
 * Playlist DTO
 */
data class PlaylistDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("videos") val videos: List<VideoDto>,
    @SerializedName("video_count") val videoCount: Int,
    @SerializedName("total_duration") val totalDuration: Int,
    @SerializedName("created_at") val createdAt: String
)

/**
 * Create playlist request
 */
data class CreatePlaylistRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("video_ids") val videoIds: List<String>
)

// =====================================================
// NOTIFICATION DTOs
// =====================================================

/**
 * Notification DTO
 */
data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: Map<String, Any>?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)

/**
 * Update notification request
 */
data class UpdateNotificationRequest(
    @SerializedName("is_read") val isRead: Boolean
)

// =====================================================
// CERTIFICATE DTOs
// =====================================================

/**
 * Certificate DTO
 */
data class CertificateDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("video_id") val videoId: String,
    @SerializedName("video_title") val videoTitle: String,
    @SerializedName("certificate_url") val certificateUrl: String,
    @SerializedName("issued_at") val issuedAt: String,
    @SerializedName("verification_code") val verificationCode: String
)

// =====================================================
// COMMON DTOs
// =====================================================

/**
 * Device information
 */
data class DeviceInfo(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("os_version") val osVersion: String,
    @SerializedName("app_version") val appVersion: String
)

/**
 * Pagination metadata
 */
data class PaginationMetadata(
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_previous") val hasPrevious: Boolean
)

/**
 * Generic paginated response
 */
data class PaginatedResponse<T>(
    @SerializedName("data") val data: List<T>,
    @SerializedName("metadata") val metadata: PaginationMetadata
)

/**
 * Error response from API
 */
data class ErrorResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null,
    @SerializedName("timestamp") val timestamp: Long
)