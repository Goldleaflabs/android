package com.goldleaf.core.data.dto.farm

import android.os.Parcelable
import com.goldleaf.core.auth.UserRole
import com.goldleaf.core.data.local.CertificationEntity
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime


@Parcelize
data class Farmer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    val location: String? = null,
    val lastSyncTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val farms: List<Farm> = emptyList(),
    val personalInfo: PersonalInfo,
    val contactInfo: ContactInfo,
    val farmInfo: FarmInfo,
    val preferences: FarmerPreferences = FarmerPreferences(),
    val registrationDate: Long = System.currentTimeMillis(),
    val lastLoginDate: LocalDateTime? = null,
    val status: FarmerStatus = FarmerStatus.ACTIVE,
    val profileImageUrl: String? = null,
    val totalFarms: Int = 0,
    val totalCrops: Int = 0,
    val experienceYears: Int = 0,
    val certifications: List<Certification> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val userRole:  UserRole = UserRole.FARMER
) : Parcelable

@Parcelize
data class PersonalInfo(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDateTime? = null,
    val gender: Gender? = null,
    val nationalId: String? = null,
    val preferredLanguage: String = "en"
) : Parcelable {
    val fullName: String get() = "$firstName $lastName"
}

@Parcelize
data class ContactInfo(
    val primaryPhone: String,
    val secondaryPhone: String? = null,
    val email: String? = null,
    val address: Address?,
    val emergencyContact: EmergencyContact? = null
) : Parcelable

@Parcelize
data class Address(
    val street: String? = null,
    val village: String? = null,
    val district: String,
    val region: String,
    val country: String,
    val postalCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable {
    val fullAddress: String get() = listOfNotNull(
        street, village, district, region, country
    ).joinToString(", ")
}

@Parcelize
data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phone: String,
    val address: String? = null
) : Parcelable

@Parcelize
data class FarmInfo(
    val primaryFarmId: String? = null,
    val totalLandSize: Double = 0.0, // in acres
    val landUnit: LandUnit = LandUnit.ACRES,
    val farmingType: FarmingType = FarmingType.MIXED,
    val mainCrops: List<String> = emptyList(),
    val ownershipType: OwnershipType = OwnershipType.OWNER,
    val farmingExperienceYears: Int = 0,
    val farmName: String? = null,
    val idNumber: String? = null

) : Parcelable

@Parcelize
data class FarmerPreferences(
    val language: String = "en",
    val currency: String = "KES",
    val units: UnitPreferences = UnitPreferences(),
    val notifications: NotificationPreferences = NotificationPreferences(),
    val privacy: PrivacyPreferences = PrivacyPreferences(),
    val theme: String = "system" // light, dark, system
) : Parcelable

@Parcelize
data class UnitPreferences(
    val temperature: String = "celsius", // celsius, fahrenheit
    val distance: String = "metric", // metric, imperial
    val area: String = "acres", // acres, hectares
    val weight: String = "kg" // kg, lbs
) : Parcelable

@Parcelize
data class NotificationPreferences(
    val weather: Boolean = true,
    val crops: Boolean = true,
    val market: Boolean = true,
    val training: Boolean = true,
    val reminders: Boolean = true,
    val marketing: Boolean = false
) : Parcelable

@Parcelize
data class PrivacyPreferences(
    val shareLocation: Boolean = true,
    val shareContactInfo: Boolean = false,
    val shareYieldData: Boolean = false,
    val allowAnalytics: Boolean = true
) : Parcelable

@Parcelize
data class Certification(
    val id: String,
    val name: String,
    val issuingOrganization: String,
    val issueDate: LocalDateTime,
    val expiryDate: LocalDateTime? = null,
    val certificateUrl: String? = null,
    val status: CertificationStatus = CertificationStatus.ACTIVE
) : Parcelable





@Parcelize
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val earnedDate: LocalDateTime,
    val iconUrl: String? = null,
    val points: Int = 0
) : Parcelable

// Farm Models
@Parcelize
data class Farm(
    val id: String = "",
    val name: String,
    val description: String? = null,
    val farmerId: String = "",
    val location: FarmLocation,
    val totalSize: Double,
    val sizeUnit: LandUnit = LandUnit.ACRES,
    val farmType: FarmingType = FarmingType.MIXED,
    val crops: List<String> = emptyList(),
    val livestock: List<String> = emptyList(),
    val boundaries: List<GeoPoint> = emptyList(), // Farm fence coordinates
    val facilities: List<FarmFacility> = emptyList(),
    val soilType: String? = null,
    val waterSources: List<WaterSource> = emptyList(),
    val registrationDate: Long = System.currentTimeMillis(),
    val status: FarmStatus = FarmStatus.ACTIVE,
    val imageUrls: List<String> = emptyList()
) : Parcelable

@Parcelize
data class FarmLocation(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val address: String? = null,
    val village: String? = null,
    val district: String? = null,
    val region: String? = null,
    val country: String? = null,
    val elevation: Double? = null,
    val timezone: String? = null
) : Parcelable

@Parcelize
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null
) : Parcelable

@Parcelize
data class FarmFacility(
    val id: String,
    val type: FacilityType,
    val name: String,
    val location: GeoPoint,
    val description: String? = null,
    val capacity: String? = null,
    val status: FacilityStatus = FacilityStatus.ACTIVE
) : Parcelable

@Parcelize
data class WaterSource(
    val id: String,
    val type: WaterSourceType,
    val name: String,
    val location: GeoPoint,
    val capacity: Double? = null, // in liters/day
    val quality: WaterQuality? = null,
    val status: WaterSourceStatus = WaterSourceStatus.ACTIVE
) : Parcelable

// Enums
enum class FarmerStatus {
    ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
}

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

enum class LandUnit {
    ACRES, HECTARES, SQUARE_METERS
}

enum class OwnershipType {
    OWNER, TENANT, CARETAKER, COOPERATIVE_MEMBER
}

enum class CertificationStatus {
    ACTIVE, EXPIRED, SUSPENDED, PENDING
}

enum class AchievementCategory {
    FARMING_EXCELLENCE, TRAINING_COMPLETION, COMMUNITY_CONTRIBUTION, INNOVATION, SUSTAINABILITY
}

enum class FacilityType {
    STORAGE, GREENHOUSE, IRRIGATION_SYSTEM, LIVESTOCK_SHELTER,
    EQUIPMENT_STORAGE, PROCESSING_FACILITY, OFFICE, HOUSING
}

enum class FacilityStatus {
    ACTIVE, MAINTENANCE, INACTIVE, UNDER_CONSTRUCTION
}

enum class WaterSourceType {
    BOREHOLE, WELL, RIVER, LAKE, DAM, RAINWATER_HARVEST, MUNICIPAL
}

enum class WaterQuality {
    EXCELLENT, GOOD, FAIR, POOR, NEEDS_TREATMENT
}

enum class WaterSourceStatus {
    ACTIVE, SEASONAL, DRY, NEEDS_REPAIR
}

// Data Transfer Objects for API
data class FarmerRegistrationRequest(
    val personalInfo: PersonalInfo,
    val contactInfo: ContactInfo,
    val farmInfo: FarmInfo,
    val password: String
)


data class FarmerUpdateRequest(
    val personalInfo: PersonalInfo?,
    val contactInfo: ContactInfo?,
    val farmInfo: FarmInfo?,
    val preferences: FarmerPreferences?,
    val name: String?,
    val phone: String?,
    val location: String?,
    val profileImage: String?
)



@Parcelize
data class FarmSummary(
    val id: String,
    val name: String,
    val size: Double,
    val sizeUnit: String = "acres",
    val location: String,
    val farmType: FarmingType,
    val activeCrops: Int = 0,
    val status: FarmStatus,
    val healthScore: Float = 0.0f, // 0.0 to 1.0
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class Activity(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: LocalDateTime,
    val farmId: String? = null
) : Parcelable

@Parcelize
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: LocalDateTime,
    val priority: TaskPriority,
    val farmId: String? = null
) : Parcelable

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

@Parcelize
data class WeatherSummary(
    val currentTemp: Double,
    val condition: String,
    val humidity: Int,
    val location: String
) : Parcelable

@Parcelize
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
) : Parcelable

enum class NotificationType {
    WEATHER, CROP, MARKET, TRAINING, TASK, SYSTEM
}



/*No value passed for parameter 'farmerId'
No value passed for parameter 'status'
No value passed for parameter 'type'*/
// Extension function to convert Certification to CertificationEntity
private fun Certification.toEntity(): CertificationEntity {
    return CertificationEntity(
        id = id,
        name = name,
        issuingAuthority = issuingOrganization,
        validFrom = issueDate,
        validUntil = expiryDate,
        certificateUrl = certificateUrl,
        farmerId=null,
        status=null,
        type=null,
    )
}

 fun CertificationEntity.toDomain(): Certification {
    return Certification(
        id = id,
        name = name,
        issuingOrganization = issuingAuthority,
        issueDate = validFrom,
        expiryDate = validUntil,
        certificateUrl = certificateUrl
    )
}

