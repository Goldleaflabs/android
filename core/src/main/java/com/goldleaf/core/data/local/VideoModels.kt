package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID
import android.content.Context
import android.os.Environment
import java.io.File


// =====================================================
// CORE ENTITIES
// =====================================================

@Entity(tableName = "training_videos")
@Parcelize
data class TrainingVideo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val shortDescription: String = "",
    val instructor: String,
    val instructorAvatar: String? = null,
    val instructorBio: String? = null,
    val instructorExpertise: List<String> = emptyList(),
    val thumbnailUrl: String,
    val videoUrl: String,
    val previewUrl: String? = null,
    val subtitleUrl: String? = null,
    val hasSubtitles: Boolean = false,
    val duration: Long,
    val fileSize: Long = 0,
    val downloadSize: Long = 0,
    val aspectRatio: String = "16:9",
    val category: VideoCategory,
    val subCategory: String? = null,
    val tags: List<String> = emptyList(),
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val language: String = "en",
    val region: String? = null,
    val prerequisites: List<String> = emptyList(),
    val learningObjectives: List<String> = emptyList(),
    val keyTopics: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val season: FarmingSeason? = null,
    val videoQualityOptions: List<VideoQuality> = listOf(VideoQuality.MEDIUM),
    val isDownloadable: Boolean = true,
    val isOfflineAvailable: Boolean = false,
    val maxOfflineDays: Int = 30,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val shareCount: Int = 0,
    val downloadCount: Int = 0,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isLive: Boolean = false,
    val isPremium: Boolean = false,
    val isNew: Boolean = false,
    val uploadedAt: Date = Date(),
    val updatedAt: Date = Date(),
    val publishedAt: Date = Date(),
    val scheduledAt: Date? = null,
    val archivedAt: Date? = null,
    val resources: List<VideoResource> = emptyList(),
    val relatedVideoIds: List<String> = emptyList(),
    val contentWarnings: List<String> = emptyList(),
    val notes: String = "",
    val videoCodec: String = "H.264",
    val audioCodec: String = "AAC",
    val frameRate: String = "30fps",
    val hasCaptions: Boolean = false,
    val hasTranscript: Boolean = false,
    val farmerId: String? = null,
    val farmId: String? = null
) : Parcelable {
    val isShort: Boolean get() = duration < 300
    val isLong: Boolean get() = duration > 3600
    val durationFormatted: String get() = formatDuration(duration)
    val fileSizeFormatted: String get() = formatFileSize(fileSize)
    val isHighRated: Boolean get() = rating >= 4.0f && reviewCount >= 5
    val isPopular: Boolean get() = viewCount > 1000
    val ageInDays: Int get() = ((System.currentTimeMillis() - uploadedAt.time) / (1000 * 60 * 60 * 24)).toInt()
}

@Entity(tableName = "video_watch_progress", primaryKeys = ["videoId", "userId"])
@Parcelize
data class VideoWatchProgress(
    val videoId: String,
    val userId: String,
    val watchedDuration: Long = 0,
    val totalDuration: Long,
    val lastWatchedAt: Date = Date(),
    val resumePosition: Long = 0,
    val watchedPercentage: Float = 0f,
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val watchSessions: Int = 1,
    val totalWatchTime: Long = 0,
    val bookmarks: List<VideoBookmark> = emptyList(),
    val lastBookmarkAt: Long? = null,
    val playbackSpeed: Float = 1.0f,
    val quality: VideoQuality = VideoQuality.MEDIUM,
    val completionThreshold: Float = 95f,
    val farmerId: String? = null,
    val farmId: String? = null
) : Parcelable {
    val isStarted: Boolean get() = watchedDuration > 0
    val isInProgress: Boolean get() = isStarted && !isCompleted
    val canResume: Boolean get() = resumePosition > 10 && !isCompleted
    val progressPercentage: Int get() = (watchedPercentage.coerceIn(0f, 100f)).toInt()
    val remainingDuration: Long get() = (totalDuration - watchedDuration).coerceAtLeast(0)
    val estimatedTimeToComplete: Long get() = if (isCompleted) 0 else remainingDuration
}

@Entity(tableName = "video_downloads")
@Parcelize
data class VideoDownload(
    @PrimaryKey val videoId: String,
    val localPath: String,
    val fileName: String = "",
    val downloadedAt: Date = Date(),
    val fileSize: Long = 0,
    val quality: VideoQuality = VideoQuality.MEDIUM,
    val isComplete: Boolean = true,
    val downloadProgress: Float = 100f,
    val downloadSpeed: Long = 0,
    val estimatedTimeRemaining: Long = 0,
    val expiresAt: Date? = null,
    val lastAccessedAt: Date = Date(),
    val accessCount: Int = 0,
    val subtitlePath: String? = null,
    val thumbnailPath: String? = null,
    val resourcePaths: List<String> = emptyList(),
    val downloadSource: String = "wifi",
    val downloadQueuePosition: Int = 0,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val checksumVerified: Boolean = false,
    val canDelete: Boolean = true,
    val isProtected: Boolean = false,
    val storageLocation: StorageLocation = StorageLocation.INTERNAL,
    val farmerId: String? = null,
    val farmId: String? = null
) : Parcelable {
    val fileSizeFormatted: String get() = formatFileSize(fileSize)
    val isExpired: Boolean get() = expiresAt?.let { it.before(Date()) } ?: false
    val isDownloading: Boolean get() = !isComplete && downloadProgress > 0
    val isPending: Boolean get() = !isComplete && downloadProgress == 0f
    val hasError: Boolean get() = !errorMessage.isNullOrBlank()
    val daysSinceDownload: Int get() = ((System.currentTimeMillis() - downloadedAt.time) / (1000 * 60 * 60 * 24)).toInt()
}

@Entity(tableName = "video_reviews")
@Parcelize
data class VideoReview(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val videoId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val rating: Float,
    val title: String = "",
    val comment: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isEdited: Boolean = false,
    val helpfulCount: Int = 0,
    val notHelpfulCount: Int = 0,
    val replyCount: Int = 0,
    val isHelpful: Boolean = false,
    val isVerified: Boolean = false,
    val reviewerType: ReviewerType = ReviewerType.REGULAR,
    val tags: List<String> = emptyList(),
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val wouldRecommend: Boolean = true,
    val experienceLevel: DifficultyLevel = DifficultyLevel.BEGINNER,
    val isReported: Boolean = false,
    val isHidden: Boolean = false,
    val moderatedAt: Date? = null,
    val moderatorNote: String? = null,
    val farmerId: String? = null,
    val farmId: String? = null
) : Parcelable {
    val isPositive: Boolean get() = rating >= 4.0f
    val isNegative: Boolean get() = rating <= 2.0f
    val hasComment: Boolean get() = comment.isNotBlank()
    val ageInDays: Int get() = ((System.currentTimeMillis() - createdAt.time) / (1000 * 60 * 60 * 24)).toInt()
    val helpfulnessRatio: Float get() = if (helpfulCount + notHelpfulCount > 0) helpfulCount.toFloat() / (helpfulCount + notHelpfulCount) else 0f
}

@Entity(tableName = "video_statistics")
@Parcelize
data class VideoStatistics(
    @PrimaryKey val videoId: String,
    val viewCount: Int = 0,
    val uniqueViewers: Int = 0,
    val totalWatchTime: Long = 0,
    val averageWatchTime: Long = 0,
    val completionRate: Float = 0.0f,
    val likeCount: Int = 0,
    val shareCount: Int = 0,
    val downloadCount: Int = 0,
    val bookmarkCount: Int = 0,
    val averageRating: Float = 0.0f,
    val ratingCount: Int = 0,
    val fiveStarCount: Int = 0,
    val fourStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val twoStarCount: Int = 0,
    val oneStarCount: Int = 0,
    val lastViewedAt: Long = 0L,
    val popularityScore: Float = 0.0f,
    val trendingScore: Float = 0.0f,
    val dailyViews: Int = 0,
    val weeklyViews: Int = 0,
    val monthlyViews: Int = 0,
    val averageQualityUsed: String = "MEDIUM",
    val mobileViews: Int = 0,
    val desktopViews: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val farmerId: String? = null,
    val farmId: String? = null
) : Parcelable {
    val hasViews: Boolean get() = viewCount > 0
    val isPopular: Boolean get() = popularityScore > 70f
    val isTrending: Boolean get() = trendingScore > 80f
    val hasHighRating: Boolean get() = averageRating >= 4.0f && ratingCount >= 10
    val engagementRate: Float get() = if (viewCount > 0) {
        ((likeCount + shareCount + downloadCount).toFloat() / viewCount) * 100f
    } else 0f
}

// =====================================================
// ENUMS
// =====================================================

@Parcelize
enum class VideoCategory(
    val displayName: String,
    val icon: String,
    val description: String,
    val color: String,
    val sortOrder: Int
) : Parcelable {
    CROP_MANAGEMENT("Crop Management", "🌾", "Planting, growing, and managing crops", "#4CAF50", 1),
    SOIL_HEALTH("Soil Health", "🌱", "Soil testing, fertilization, and improvement", "#8BC34A", 2),
    PEST_CONTROL("Pest Control", "🐛", "Identifying and managing pests and diseases", "#FF9800", 3),
    IRRIGATION("Irrigation", "💧", "Water management and irrigation systems", "#2196F3", 4),
    HARVESTING("Harvesting", "🚜", "Harvesting techniques and equipment", "#FF5722", 5),
    LIVESTOCK("Livestock", "🐄", "Animal husbandry and livestock care", "#795548", 6),
    ORGANIC_FARMING("Organic Farming", "🌿", "Organic and sustainable farming practices", "#4CAF50", 7),
    TECHNOLOGY("Farm Technology", "📱", "Modern farming tools and technology", "#9C27B0", 8),
    BUSINESS("Farm Business", "💼", "Farm management, finance, and marketing", "#607D8B", 9),
    SUSTAINABILITY("Sustainability", "♻️", "Environmental and sustainable practices", "#4CAF50", 10),
    WEATHER("Weather & Climate", "🌤️", "Weather patterns and climate adaptation", "#03A9F4", 11),
    EQUIPMENT("Equipment", "🔧", "Farm machinery and equipment maintenance", "#757575", 12),
    SEEDS("Seeds & Varieties", "🌰", "Seed selection and plant varieties", "#8BC34A", 13),
    FERTILIZERS("Fertilizers", "🧪", "Fertilizer types and application methods", "#FFC107", 14),
    POST_HARVEST("Post-Harvest", "📦", "Storage, processing, and preservation", "#795548", 15),
    MARKET_ACCESS("Market Access", "🏪", "Selling products and market strategies", "#009688", 16),
    SAFETY("Farm Safety", "🦺", "Agricultural safety and best practices", "#F44336", 17),
    GENERAL("General", "📚", "General farming knowledge and tips", "#9E9E9E", 99);

    companion object {
        fun getByDisplayName(displayName: String): VideoCategory? =
            values().find { it.displayName.equals(displayName, ignoreCase = true) }
        fun getSortedCategories(): List<VideoCategory> = values().sortedBy { it.sortOrder }
        fun getPopularCategories(): List<VideoCategory> =
            listOf(CROP_MANAGEMENT, SOIL_HEALTH, PEST_CONTROL, IRRIGATION, HARVESTING)
    }
}

@Parcelize
enum class DifficultyLevel(
    val displayName: String,
    val shortName: String,
    val color: String,
    val description: String,
    val targetAudience: String,
    val icon: String
) : Parcelable {
    BEGINNER("Beginner", "Basic", "#4CAF50", "Basic concepts and fundamental techniques", "New farmers or those learning new topics", "🌱"),
    INTERMEDIATE("Intermediate", "Standard", "#FF9800", "More complex techniques requiring some experience", "Farmers with 1-2 years of experience", "🌿"),
    ADVANCED("Advanced", "Expert", "#F44336", "Complex techniques for experienced farmers", "Farmers with 3+ years of experience", "🌳"),
    PROFESSIONAL("Professional", "Pro", "#9C27B0", "Professional-level advanced techniques and business strategies", "Commercial farmers and agricultural professionals", "👨‍🌾");

    companion object {
        fun getByDisplayName(displayName: String): DifficultyLevel? =
            values().find { it.displayName.equals(displayName, ignoreCase = true) }
    }
}

@Parcelize
enum class FarmingSeason(val displayName: String, val months: List<String>, val description: String, val icon: String) : Parcelable {
    SPRING("Spring", listOf("March", "April", "May"), "Planting and preparation season", "🌸"),
    SUMMER("Summer", listOf("June", "July", "August"), "Growing and maintenance season", "☀️"),
    FALL("Fall/Autumn", listOf("September", "October", "November"), "Harvesting season", "🍂"),
    WINTER("Winter", listOf("December", "January", "February"), "Planning and equipment maintenance", "❄️"),
    YEAR_ROUND("Year Round", emptyList(), "Applicable throughout the year", "🔄");
}

@Parcelize
enum class VideoQuality(val displayName: String, val resolution: String, val bitrate: Int, val fileSizePerHour: String, val description: String, val sortOrder: Int) : Parcelable {
    LOW("Low Quality (360p)", "640x360", 800, "~300MB/hr", "Good for slow connections, basic viewing", 1),
    MEDIUM("Medium Quality (720p)", "1280x720", 2500, "~1GB/hr", "Best balance of quality and file size", 2),
    HIGH("High Quality (1080p)", "1920x1080", 5000, "~2GB/hr", "Crystal clear details, requires good connection", 3),
    AUTO("Auto Quality", "Adaptive", 0, "Adaptive", "Automatically adjusts based on connection speed", 0);

    companion object {
        fun getDefaultQuality(): VideoQuality = MEDIUM
        fun getBestQualityForConnection(connectionSpeed: Int): VideoQuality = when {
            connectionSpeed < 1000 -> LOW
            connectionSpeed < 5000 -> MEDIUM
            else -> HIGH
        }
    }
}

@Parcelize
enum class ResourceType(val displayName: String, val icon: String, val extensions: List<String>, val description: String) : Parcelable {
    PDF("PDF Document", "📄", listOf("pdf"), "Downloadable guide or reference"),
    LINK("Web Link", "🔗", emptyList(), "External website or article"),
    IMAGE("Image", "🖼️", listOf("jpg", "jpeg", "png", "gif"), "Reference image or diagram"),
    DOCUMENT("Document", "📝", listOf("doc", "docx", "txt"), "Text document or guide"),
    SPREADSHEET("Spreadsheet", "📊", listOf("xls", "xlsx", "csv"), "Data sheet or calculator"),
    PRESENTATION("Presentation", "📑", listOf("ppt", "pptx"), "Slide presentation"),
    AUDIO("Audio File", "🎵", listOf("mp3", "wav", "aac"), "Audio guide or recording"),
    TOOL("Calculator/Tool", "🧮", emptyList(), "Interactive tool or calculator"),
    VIDEO("Video Clip", "🎥", listOf("mp4", "avi", "mov"), "Additional video content");

    companion object {
        fun fromExtension(extension: String): ResourceType =
            values().find { type -> type.extensions.any { it.equals(extension, ignoreCase = true) } } ?: DOCUMENT
    }
}

@Parcelize
enum class StorageLocation(val displayName: String, val isRemovable: Boolean) : Parcelable {
    INTERNAL("Internal Storage", false),
    EXTERNAL("SD Card", true),
    CACHE("Cache", false);

    fun resolvePath(context: Context): String {
        return when (this) {
            INTERNAL -> File(context.filesDir, "videos").path
            EXTERNAL -> File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "GoldLeaf/videos").path
            CACHE -> File(context.cacheDir, "videos").path
        }
    }
}


@Parcelize
enum class ReviewerType(val displayName: String, val badge: String, val color: String) : Parcelable {
    REGULAR("Regular User", "", "#757575"),
    VERIFIED("Verified Farmer", "✓", "#4CAF50"),
    EXPERT("Agricultural Expert", "★", "#FF9800"),
    INSTRUCTOR("Instructor", "🎓", "#2196F3"),
    MODERATOR("Moderator", "🛡️", "#9C27B0");
}

@Parcelize
enum class SortOption(val displayName: String) : Parcelable {
    RELEVANCE("Most Relevant"),
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    RATING("Highest Rated"),
    POPULARITY("Most Popular"),
    DURATION_SHORT("Shortest First"),
    DURATION_LONG("Longest First"),
    TITLE("Alphabetical"),
    INSTRUCTOR("By Instructor");
}

@Parcelize
enum class SortOrder : Parcelable { ASC, DESC }

@Parcelize
enum class ContentType : Parcelable { VIDEO, REVIEW, COMMENT }

@Parcelize
enum class FlagType(val displayName: String, val severity: Int) : Parcelable {
    SPAM("Spam", 1),
    INAPPROPRIATE("Inappropriate Content", 2),
    MISLEADING("Misleading Information", 3),
    COPYRIGHT("Copyright Violation", 4),
    OFFENSIVE("Offensive Language", 2),
    DANGEROUS("Dangerous Advice", 5);
}

@Parcelize
enum class ModerationStatus : Parcelable { PENDING, APPROVED, REJECTED, REMOVED }

@Parcelize
enum class LearningBadge(val displayName: String, val description: String, val icon: String, val color: String, val requirement: String) : Parcelable {
    FIRST_VIDEO("First Steps", "Completed your first video", "🎯", "#4CAF50", "Watch 1 video"),
    WEEK_STREAK("Weekly Warrior", "7 days learning streak", "🔥", "#FF9800", "7 day streak"),
    CATEGORY_EXPERT("Category Expert", "Mastered a category", "🏆", "#FFD700", "Complete 10 videos in one category"),
    PERFECT_RATING("5-Star Learner", "All completed videos rated 4+ stars", "⭐", "#FF9800", "Rate 10 videos 4+ stars"),
    EARLY_BIRD("Early Bird", "Learning before 8 AM", "🌅", "#2196F3", "Watch 5 videos before 8 AM"),
    NIGHT_OWL("Night Owl", "Learning after 10 PM", "🦉", "#9C27B0", "Watch 5 videos after 10 PM"),
    SPEED_LEARNER("Speed Learner", "Completed videos in 1.5x+ speed", "⚡", "#FF5722", "Watch 10 videos at 1.5x speed"),
    BOOKWORM("Bookworm", "Downloaded lots of resources", "📚", "#795548", "Download 25 resources"),
    SOCIAL_LEARNER("Social Learner", "Active in community", "👥", "#607D8B", "Write 10 helpful reviews"),
    SEASONAL_EXPERT("Seasonal Expert", "Learned all season content", "🌱", "#8BC34A", "Complete videos from all 4 seasons");
}

// =====================================================
// SUPPORTING DATA CLASSES
// =====================================================

@Parcelize
data class VideoResource(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val type: ResourceType,
    val url: String,
    val fileSize: Long = 0,
    val mimeType: String = "",
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val downloadedAt: Date? = null,
    val isRequired: Boolean = false,
    val sortOrder: Int = 0
) : Parcelable {
    val fileSizeFormatted: String get() = formatFileSize(fileSize)
    val isDownloadable: Boolean get() = type != ResourceType.LINK
}

@Parcelize
data class VideoBookmark(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val title: String,
    val note: String? = null,
    val createdAt: Date = Date(),
    val isImportant: Boolean = false,
    val category: String? = null
) : Parcelable {
    val timestampFormatted: String get() = formatDuration(timestamp)
}

@Parcelize
data class VideoSearchFilter(
    val query: String = "",
    val categories: List<VideoCategory> = emptyList(),
    val difficulties: List<DifficultyLevel> = emptyList(),
    val durations: DurationFilter? = null,
    val ratings: RatingFilter? = null,
    val instructor: String? = null,
    val tags: List<String> = emptyList(),
    val isDownloaded: Boolean? = null,
    val isFeatured: Boolean? = null,
    val language: String? = null,
    val season: FarmingSeason? = null,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val sortOrder: SortOrder = SortOrder.DESC
) : Parcelable

@Parcelize
enum class DurationFilter(val displayName: String, val minSeconds: Long, val maxSeconds: Long) : Parcelable {
    SHORT("Short (< 5 min)", 0, 300),
    MEDIUM("Medium (5-20 min)", 300, 1200),
    LONG("Long (20-60 min)", 1200, 3600),
    VERY_LONG("Very Long (> 1 hour)", 3600, Long.MAX_VALUE);
}

@Parcelize
enum class RatingFilter(val displayName: String, val minRating: Float) : Parcelable {
    ALL("All Ratings", 0f),
    GOOD("3+ Stars", 3f),
    VERY_GOOD("4+ Stars", 4f),
    EXCELLENT("4.5+ Stars", 4.5f);
}

@Parcelize
data class VideoRecommendation(
    val video: TrainingVideo,
    val score: Float,
    val reasons: List<RecommendationReason>,
    val priority: RecommendationPriority = RecommendationPriority.NORMAL
) : Parcelable

@Parcelize
enum class RecommendationReason(val displayName: String, val weight: Float) : Parcelable {
    SAME_CATEGORY("Similar topic", 0.3f),
    SAME_INSTRUCTOR("Same instructor", 0.2f),
    SAME_DIFFICULTY("Appropriate difficulty", 0.15f),
    HIGH_RATED("Highly rated", 0.25f),
    TRENDING("Trending now", 0.1f),
    SEASONAL("Seasonal content", 0.2f),
    PREREQUISITE("Next in series", 0.4f),
    USER_INTEREST("Based on your interests", 0.35f);
}

@Parcelize
enum class RecommendationPriority : Parcelable { LOW, NORMAL, HIGH, URGENT }

@Parcelize
data class VideoMetrics(
    val videoId: String,
    val totalViews: Int = 0,
    val uniqueViewers: Int = 0,
    val averageWatchTime: Long = 0,
    val completionRate: Float = 0f,
    val likeRatio: Float = 0f,
    val shareCount: Int = 0,
    val downloadCount: Int = 0,
    val averageRating: Float = 0f,
    val reviewCount: Int = 0,
    val bookmarkCount: Int = 0,
    val retentionCurve: List<Float> = emptyList(),
    val topExitPoints: List<Long> = emptyList(),
    val peakConcurrentViewers: Int = 0,
    val viewsByCountry: Map<String, Int> = emptyMap(),
    val viewsByDevice: Map<String, Int> = emptyMap(),
    val viewsByQuality: Map<VideoQuality, Int> = emptyMap(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

@Parcelize
data class UserLearningStats(
    val userId: String,
    val totalVideosWatched: Int = 0,
    val totalWatchTime: Long = 0,
    val completedVideos: Int = 0,
    val averageRating: Float = 0f,
    val favoriteCategories: List<VideoCategory> = emptyList(),
    val skillLevel: DifficultyLevel = DifficultyLevel.BEGINNER,
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val badges: List<LearningBadge> = emptyList(),
    val weeklyGoal: Int = 3,
    val weeklyProgress: Int = 0,
    val monthlyGoal: Int = 12,
    val monthlyProgress: Int = 0,
    val joinedAt: Date = Date(),
    val lastActiveAt: Date = Date()
) : Parcelable

@Parcelize
data class ContentModerationFlag(
    val id: String = UUID.randomUUID().toString(),
    val contentId: String,
    val contentType: ContentType,
    val flagType: FlagType,
    val reason: String = "",
    val reportedBy: String,
    val reportedAt: Date = Date(),
    val status: ModerationStatus = ModerationStatus.PENDING,
    val moderatorId: String? = null,
    val moderatorNotes: String? = null,
    val resolvedAt: Date? = null
) : Parcelable

// =====================================================
// UTILITY FUNCTIONS
// =====================================================

fun formatDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%d:%02d", minutes, seconds)
        else -> "0:${String.format("%02d", seconds)}"
    }
}

fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun TrainingVideo.isRecentlyUploaded(days: Int = 7): Boolean =
    (System.currentTimeMillis() - uploadedAt.time) / (1000 * 60 * 60 * 24) <= days

fun TrainingVideo.isRecentlyUpdated(days: Int = 7): Boolean =
    (System.currentTimeMillis() - updatedAt.time) / (1000 * 60 * 60 * 24) <= days

fun TrainingVideo.getEstimatedDownloadTime(connectionSpeedKbps: Int): Long {
    if (connectionSpeedKbps <= 0 || downloadSize <= 0) return 0
    return (downloadSize * 8) / (connectionSpeedKbps * 1000)
}

fun TrainingVideo.isCompatibleWith(userLevel: DifficultyLevel): Boolean = when (userLevel) {
    DifficultyLevel.BEGINNER -> difficulty == DifficultyLevel.BEGINNER
    DifficultyLevel.INTERMEDIATE -> difficulty <= DifficultyLevel.INTERMEDIATE
    DifficultyLevel.ADVANCED -> difficulty <= DifficultyLevel.ADVANCED
    DifficultyLevel.PROFESSIONAL -> true
}

fun VideoWatchProgress.updateProgress(currentPosition: Long, videoDuration: Long): VideoWatchProgress {
    val newWatchedDuration = maxOf(watchedDuration, currentPosition)
    val newPercentage = if (videoDuration > 0) (currentPosition.toFloat() / videoDuration.toFloat()) * 100f else 0f
    val newCompleted = newPercentage >= completionThreshold
    return copy(
        watchedDuration = newWatchedDuration,
        resumePosition = if (newCompleted) 0 else currentPosition,
        watchedPercentage = newPercentage,
        isCompleted = newCompleted,
        completedAt = if (newCompleted && !isCompleted) Date() else completedAt,
        lastWatchedAt = Date(),
        totalWatchTime = totalWatchTime + 1
    )
}

// =====================================================
// VALIDATION HELPERS
// =====================================================

object VideoValidation {

    fun validateTrainingVideo(video: TrainingVideo): List<String> {
        val errors = mutableListOf<String>()

        if (video.title.isBlank()) errors.add("Title cannot be empty")
        if (video.title.length > 200) errors.add("Title too long (max 200 characters)")
        if (video.description.isBlank()) errors.add("Description cannot be empty")
        if (video.description.length < 50) errors.add("Description too short (min 50 characters)")
        if (video.instructor.isBlank()) errors.add("Instructor name cannot be empty")
        if (video.duration <= 0) errors.add("Duration must be positive")
        if (video.duration > 14400) errors.add("Duration too long (max 4 hours)")
        if (video.videoUrl.isBlank()) errors.add("Video URL cannot be empty")
        if (video.thumbnailUrl.isBlank()) errors.add("Thumbnail URL cannot be empty")
        if (video.rating < 0 || video.rating > 5) errors.add("Rating must be between 0 and 5")

        return errors
    }

    fun validateVideoReview(review: VideoReview): List<String> {
        val errors = mutableListOf<String>()

        if (review.rating < 1 || review.rating > 5) errors.add("Rating must be between 1 and 5")
        if (review.comment.length > 2000) errors.add("Review comment too long (max 2000 characters)")
        if (review.title.length > 100) errors.add("Review title too long (max 100 characters)")

        return errors
    }
}

// =====================================================
// SEARCH HELPERS
// =====================================================

object VideoSearchHelper {

    fun createBasicFilter(query: String): VideoSearchFilter {
        return VideoSearchFilter(
            query = query,
            sortBy = SortOption.RELEVANCE
        )
    }

    fun createCategoryFilter(category: VideoCategory): VideoSearchFilter {
        return VideoSearchFilter(
            categories = listOf(category),
            sortBy = SortOption.RATING
        )
    }

    fun createBeginnerFilter(): VideoSearchFilter {
        return VideoSearchFilter(
            difficulties = listOf(DifficultyLevel.BEGINNER),
            durations = DurationFilter.SHORT,
            sortBy = SortOption.RATING
        )
    }

    fun createPopularFilter(): VideoSearchFilter {
        return VideoSearchFilter(
            ratings = RatingFilter.VERY_GOOD,
            sortBy = SortOption.POPULARITY
        )
    }

    fun createSeasonalFilter(season: FarmingSeason): VideoSearchFilter {
        return VideoSearchFilter(
            season = season,
            sortBy = SortOption.NEWEST
        )
    }
}
