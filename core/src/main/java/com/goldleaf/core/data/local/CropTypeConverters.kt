package com.goldleaf.core.data.local


import androidx.room.TypeConverter

class CropTypeConverters {

    // CropStatus Converter
    @TypeConverter
    fun fromCropStatus(value: CropStatus?): String? = value?.name

    @TypeConverter
    fun toCropStatus(value: String?): CropStatus? {
        return value?.let { CropStatus.valueOf(it) }
    }

    // TaskPriority Converter
    @TypeConverter
    fun fromTaskPriority(value: TaskPriority?): String? = value?.name

    @TypeConverter
    fun toTaskPriority(value: String?): TaskPriority? {
        return value?.let { TaskPriority.valueOf(it) }
    }

    // TaskCategory Converter
    @TypeConverter
    fun fromTaskCategory(value: TaskCategory?): String? = value?.name

    @TypeConverter
    fun toTaskCategory(value: String?): TaskCategory? {
        return value?.let { TaskCategory.valueOf(it) }
    }

    // HealthStatus Converter
    @TypeConverter
    fun fromHealthStatus(value: HealthStatus?): String? = value?.name

    @TypeConverter
    fun toHealthStatus(value: String?): HealthStatus? {
        return value?.let { HealthStatus.valueOf(it) }
    }

    // GrowthStage Converter
    @TypeConverter
    fun fromGrowthStage(value: GrowthStage?): String? = value?.name

    @TypeConverter
    fun toGrowthStage(value: String?): GrowthStage? {
        return value?.let { GrowthStage.valueOf(it) }
    }

    // EventType Converter
    @TypeConverter
    fun fromEventType(value: EventType?): String? = value?.name

    @TypeConverter
    fun toEventType(value: String?): EventType? {
        return value?.let { EventType.valueOf(it) }
    }
}