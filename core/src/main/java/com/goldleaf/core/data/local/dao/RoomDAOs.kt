package com.goldleaf.core.data.local.dao

import com.goldleaf.core.data.dto.auth.FarmDto
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmLocation
import com.google.gson.Gson

// ===== EXTENSION FUNCTIONS FOR ENTITY/DTO CONVERSION =====

// Extension: FarmEntity → Farm (Domain Model)
fun FarmEntity.toDomain(): Farm {
    val (lat, lon) = location.split(",").map { it.toDoubleOrNull() ?: 0.0 }

    return Farm(
        id = id,
        farmerId = farmerId,
        name = name,
        totalSize = size,
        location = FarmLocation(
            centerLatitude = lat,
            centerLongitude = lon,
            address = "$name Farm",
            village = null,
            district = "$name District",
            region = "$name region",
            country = "Kenya"
        )
    )
}


