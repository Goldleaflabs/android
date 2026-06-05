package com.goldleaf.feature.farmermanagement.domain

import com.goldleaf.core.data.dto.farm.ContactInfo
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmInfo
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.dto.farm.FarmingType
import com.goldleaf.core.data.dto.farm.LandUnit
import com.goldleaf.core.data.dto.farm.OwnershipType
import com.goldleaf.core.data.dto.farm.PersonalInfo
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.FarmerEntity
import com.google.gson.Gson


// =====================================================
// Extension Functions for Data Mapping
// =====================================================

// Farmer Entity Mapping
fun Farmer.toEntity(): FarmerEntity {
    return FarmerEntity(
        id = id,
        name = personalInfo.fullName,
        phone = contactInfo.primaryPhone,
        email = contactInfo.email,
        location = contactInfo.address?.fullAddress,
        district = contactInfo.address?.district,
        region = contactInfo.address?.region,
        street = contactInfo.address?.street,
        country = contactInfo.address?.country,
        latitude = contactInfo.address?.latitude,
        longitude = contactInfo.address?.longitude,
        lastSyncTime = lastSyncTime,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userRole = userRole.name
    )
}


fun FarmerEntity.toDomain(): Farmer {
    val parts = name.trim().split(" ")
    return Farmer(
        id = id,
        personalInfo = PersonalInfo(
            firstName = parts.getOrNull(0) ?: "",
            lastName = parts.getOrNull(1) ?: "",
        ),
        contactInfo = ContactInfo(
            primaryPhone = phone,
            email = email,
            address = com.goldleaf.core.data.dto.farm.Address(
                street = street,
                village = null,
                district = district ?: "",
                region = region ?: "",
                country = country ?: "Kenya",
                postalCode = null,
                latitude = latitude,
                longitude = longitude
            )
        ),
        farmInfo = FarmInfo(
            totalLandSize = 0.0,
            landUnit = LandUnit. ACRES,
            farmingType = FarmingType.HORTICULTURE,
            ownershipType = OwnershipType.OWNER,
            farmingExperienceYears = 0,
            mainCrops =  emptyList()
        ),
        // Would be loaded separately
        achievements = emptyList(),
    )
}

// Farm Entity Mapping
fun Farm.toEntity(): FarmEntity {
    return FarmEntity(
        id = id,
        name = name,
        sizeUnit = sizeUnit.name,
        location = Gson().toJson(location),
        farmerId = farmerId,
        size = totalSize,
        latitude = location.centerLatitude,
        longitude = location.centerLongitude,
        boundaries = if (boundaries.isNotEmpty()) Gson().toJson(boundaries) else null
    )
}

