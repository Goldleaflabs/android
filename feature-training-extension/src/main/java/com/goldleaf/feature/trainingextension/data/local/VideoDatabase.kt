package com.goldleaf.feature.trainingextension.data.local

import androidx.room.*
import com.goldleaf.core.data.local.DifficultyLevel
import com.goldleaf.core.data.local.TrainingConverters
import com.goldleaf.core.data.local.TrainingVideo
import com.goldleaf.core.data.local.VideoBookmark
import com.goldleaf.core.data.local.VideoCategory
import com.goldleaf.core.data.local.VideoDownload
import com.goldleaf.core.data.local.VideoQuality
import com.goldleaf.core.data.local.VideoResource
import com.goldleaf.core.data.local.VideoReview
import com.goldleaf.core.data.local.VideoStatistics
import com.goldleaf.core.data.local.VideoWatchProgress
import java.util.Date
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [
        TrainingVideo::class,
        VideoWatchProgress::class,
        VideoDownload::class,
        VideoReview::class,
        VideoStatistics::class
    ],
    version = 2,
    exportSchema = false
)

@TypeConverters(
    VideoTypeConverters::class,
    DateConverters::class,
    ListConverters::class,
    EnumConverters::class,
    TrainingConverters::class
)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    companion object {
        const val DATABASE_NAME = "video_training_database"
    }
}

// TYPE CONVERTERS
class VideoTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromVideoCategory(category: VideoCategory?): String? =
        category?.name

    @TypeConverter
    fun toVideoCategory(value: String?): VideoCategory? =
        value?.let { runCatching { VideoCategory.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromDifficultyLevel(level: DifficultyLevel?): String? =
        level?.name

    @TypeConverter
    fun toDifficultyLevel(value: String?): DifficultyLevel? =
        value?.let { runCatching { DifficultyLevel.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromVideoQuality(quality: VideoQuality?): String? =
        quality?.name

    @TypeConverter
    fun toVideoQuality(value: String?): VideoQuality? =
        value?.let { runCatching { VideoQuality.valueOf(it) }.getOrNull() ?: VideoQuality.MEDIUM }


    private inline fun <reified T> fromList(list: List<T>?): String =
        gson.toJson(list ?: emptyList<T>())

    private inline fun <reified T> toList(json: String?): List<T> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromVideoQualityList(value: List<VideoQuality>?): String =
        fromList(value)

    @TypeConverter
    fun toVideoQualityList(json: String?): List<VideoQuality> =
        toList(json)

    @TypeConverter
    fun fromVideoBookmarkList(value: List<VideoBookmark>?): String =
        fromList(value)

    @TypeConverter
    fun toVideoBookmarkList(json: String?): List<VideoBookmark> =
        toList(json)


}

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

class ListConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = value?.joinToString("|~|") ?: ""

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("|~|").filter { it.isNotBlank() }

    @TypeConverter
    fun fromVideoResourceList(resources: List<VideoResource>?): String =
        if (resources.isNullOrEmpty()) "[]" else gson.toJson(resources)

    @TypeConverter
    fun toVideoResourceList(resources: String): List<VideoResource> {
        return if (resources.isBlank() || resources == "[]") emptyList()
        else try {
            val type = object : TypeToken<List<VideoResource>>() {}.type
            gson.fromJson(resources, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
}

class EnumConverters {
    @TypeConverter
    fun fromSortOption(option: SortOption?): String? = option?.name

    @TypeConverter
    fun toSortOption(option: String?): SortOption? =
        option?.let { try { SortOption.valueOf(it) } catch (e: IllegalArgumentException) { SortOption.RELEVANCE } }
}

object VideoDatabaseBuilder {
    fun buildDatabase(
        context: android.content.Context,
        enableLogging: Boolean = false
    ): VideoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            VideoDatabase::class.java,
            VideoDatabase.DATABASE_NAME
        )
            .apply {
                if (enableLogging) {
                    setQueryCallback({ sqlQuery, bindArgs ->
                        android.util.Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
                    }, java.util.concurrent.Executors.newSingleThreadExecutor())
                }
            }
            .fallbackToDestructiveMigration()  // Optional: Handle migrations
            .build()
    }
}