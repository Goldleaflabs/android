package com.goldleaf.feature.farmermanagement.data.mapper

import com.goldleaf.core.data.dto.auth.FarmerDto
import com.goldleaf.core.data.dto.farm.Address
import com.goldleaf.core.data.dto.farm.ContactInfo
import com.goldleaf.core.data.dto.farm.FarmInfo
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.dto.farm.PersonalInfo


fun FarmerDto.toDomain() = Farmer(
    id = id,
    personalInfo = PersonalInfo(
        firstName = name.split(" ").firstOrNull() ?: name,
        lastName = name.split(" ").drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: ""
    ),
    contactInfo = ContactInfo(
        primaryPhone = phone,
        email = email,
        address = Address(
                street = "",
                district = district ?: "",
                region = region ?: "",
                country = "Kenya"
        )
    ),
    farmInfo = FarmInfo(
        farmName = farmName
    ),
)


