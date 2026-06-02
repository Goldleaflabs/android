package com.goldleaf.core.data.local.dao

import com.goldleaf.core.data.dto.auth.FarmDto
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmLocation
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ===== EXTENSION FUNCTIONS FOR ENTITY/DTO CONVERSION =====

// Extension: FarmEntity → Farm (Domain Model)
fun FarmEntity.toDomain(): Farm {
    val farmLocation: FarmLocation = try {
        Gson().fromJson(location, FarmLocation::class.java)
    } catch (e: Exception) {
        FarmLocation(
            centerLatitude = latitude ?: 0.0,
            centerLongitude = longitude ?: 0.0
        )
    }

    val parsedBoundaries: List<GeoPoint> = try {
        val listType = object : TypeToken<List<GeoPoint>>() {}.type
        boundaries?.let { Gson().fromJson(it, listType) } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    return Farm(
        id = id,
        farmerId = farmerId,
        name = name,
        totalSize = size,
        location = farmLocation,
        boundaries = parsedBoundaries
    )
}


