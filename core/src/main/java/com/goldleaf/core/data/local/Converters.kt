package com.goldleaf.core.data.local

import androidx.room.TypeConverter
import com.goldleaf.core.data.dto.farm.Achievement
import com.goldleaf.core.data.dto.farm.Certification
import com.goldleaf.core.data.dto.farm.ContactInfo
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.goldleaf.core.data.dto.farm.FarmerPreferences
import com.goldleaf.core.data.dto.farm.FarmerStatus
import com.goldleaf.core.data.dto.farm.PersonalInfo
import kotlinx.datetime.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class Converters {

    // Converts LocalDateTime ↔ Long (epoch seconds)
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? {
        return value?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }



    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
}

class CertificationStatusConverter {
    @TypeConverter
    fun fromCertificationStatus(status: CertificationStatus): String {
        return status.name
    }

    @TypeConverter
    fun toCertificationStatus(status: String): CertificationStatus {
        return try {
            CertificationStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            CertificationStatus.PENDING
        }
    }
}

class JourneyStatusConverter {
    @TypeConverter
    fun fromJourneyStatus(status: JourneyStatus): String {
        return status.name
    }

    @TypeConverter
    fun toJourneyStatus(status: String): JourneyStatus {
        return JourneyStatus.valueOf(status)
    }

}

class QualityStatusConverter {

    @TypeConverter
    fun fromQualityStatus(status: qualitystatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toQualityStatus(value: String?): qualitystatus? {
        return value?.let { qualitystatus.valueOf(it) }
    }
}

class TestStatusConverter {

    @TypeConverter
    fun fromTestStatus(status: Teststatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toTestStatus(value: String?): Teststatus? {
        return value?.let { Teststatus.valueOf(it) }
    }
}

class BlockchainStatusConverter {

    @TypeConverter
    fun fromBlockchainStatus(status: BlockchainStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toBlockchainStatus(value: String?): BlockchainStatus? {
        return value?.let { BlockchainStatus.valueOf(it) }
    }
}

class CropPerformanceConverters {
    private val gson = Gson()

    // ==================== CropPerformance List Converter ====================

    /**
     * Converts List<CropPerformance> to JSON string for database storage
     */
    @TypeConverter
    fun fromCropPerformanceList(value: List<CropPerformance>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    /**
     * Converts JSON string back to List<CropPerformance>
     */
    @TypeConverter
    fun toCropPerformanceList(value: String?): List<CropPerformance>? {
        if (value == null) return null
        val listType = object : TypeToken<List<CropPerformance>>() {}.type
        return gson.fromJson(value, listType)
    }


}

class FarmerPreferencesConverter {
    private val gson = Gson()
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // FarmerPreferences
    @TypeConverter
    fun fromPreferences(preferences: FarmerPreferences): String {
        return gson.toJson(preferences)
    }

    @TypeConverter
    fun toPreferences(json: String): FarmerPreferences {
        return gson.fromJson(json, FarmerPreferences::class.java)
    }

    // PersonalInfo
    @TypeConverter
    fun fromPersonalInfo(value: PersonalInfo): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPersonalInfo(json: String): PersonalInfo {
        return gson.fromJson(json, PersonalInfo::class.java)
    }

    // ContactInfo
    @TypeConverter
    fun fromContactInfo(value: ContactInfo): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toContactInfo(json: String): ContactInfo {
        return gson.fromJson(json, ContactInfo::class.java)
    }

    // FarmInfo
    @TypeConverter
    fun fromFarmInfo(value: FarmInfo): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFarmInfo(json: String): FarmInfo {
        return gson.fromJson(json, FarmInfo::class.java)
    }

    // List<Farm>
    @TypeConverter
    fun fromFarmList(value: List<Farm>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFarmList(json: String): List<Farm> {
        val type = object : TypeToken<List<Farm>>() {}.type
        return gson.fromJson(json, type)
    }

    // List<Certification>
    @TypeConverter
    fun fromCertificationList(value: List<Certification>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCertificationList(json: String): List<Certification> {
        val type = object : TypeToken<List<Certification>>() {}.type
        return gson.fromJson(json, type)
    }

    // List<Achievement>
    @TypeConverter
    fun fromAchievementList(value: List<Achievement>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAchievementList(json: String): List<Achievement> {
        val type = object : TypeToken<List<Achievement>>() {}.type
        return gson.fromJson(json, type)
    }

    // FarmerStatus enum
    @TypeConverter
    fun fromFarmerStatus(value: FarmerStatus): String {
        return value.name
    }

    @TypeConverter
    fun toFarmerStatus(json: String): FarmerStatus {
        return FarmerStatus.valueOf(json)
    }
}

class TrainingConverters {
    private val gson = Gson()

    // FarmingSeason enum (nullable)
    @TypeConverter
    fun fromFarmingSeason(value: FarmingSeason?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFarmingSeason(value: String?): FarmingSeason? {
        return value?.let { FarmingSeason.valueOf(it) }
    }

}