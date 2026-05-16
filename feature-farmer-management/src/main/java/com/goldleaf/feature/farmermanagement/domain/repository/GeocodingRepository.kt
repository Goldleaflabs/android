package com.goldleaf.feature.farmermanagement.domain.repository

import android.util.Log
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.util.Result
import com.goldleaf.feature.farmermanagement.ui.viewmodels.AddressData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepository @Inject constructor() {

    // 1. REVERSE GEOCODING: Lat/Lng -> Address
    // Add this temporary logging to see what keys Nominatim returns
    suspend fun getAddress(lat: Double, lng: Double): AddressData = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "GoldLeafFarmerPortal/1.0 (judy@goldleaflabs.co.ke)")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val address = json.optJSONObject("address")

            // DEBUG: Log all available keys
            Log.d("GeocodingDebug", "Full response: $response")
            Log.d("GeocodingDebug", "Address keys: ${address?.keys()?.asSequence()?.toList()}")

            // Log each field attempt
            val county = address?.optString("county").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("state").takeIf { !it.isNullOrBlank() }
                ?: "Kenya"
            Log.d("GeocodingDebug", "County result: $county")

            val subCounty = address?.optString("state_district").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("suburb").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("city_district").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("municipality").takeIf { !it.isNullOrBlank() }
                ?: "Sub-County Unknown"
            Log.d("GeocodingDebug", "SubCounty result: $subCounty")

            val ward = address?.optString("locality").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("town").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("suburb").takeIf { !it.isNullOrBlank() }
                ?: "Ward Unknown"

            val village = address?.optString("village").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("hamlet").takeIf { !it.isNullOrBlank() }
                ?: address?.optString("neighbourhood").takeIf { !it.isNullOrBlank() }
                ?: "Village Unknown"

            val fullAddress = json.optString("display_name").takeIf { !it.isNullOrBlank() }
                ?: "Lat: $lat, Lon: $lng"
            Log.d("GeocodingDebug", "Full address: $fullAddress")

            AddressData(
                county = county,
                subCounty = subCounty,
                ward = ward,
                village = village,
                fullAddress = fullAddress
            )
        } catch (e: Exception) {
            Log.e("GeocodingDebug", "Error getting address", e)
            AddressData("Kenya", "Unknown", "Unknown", "Unknown", "Lat: $lat, Lon: $lng")
        }
    }

    // 2. SEARCH GEOCODING: Text -> Lat/Lng (Fixes your RED error)
    suspend fun getCoordinates(query: String): Result<GeoPoint> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            // Limit 1 ensures we get the most relevant result quickly
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "GoldLeafFarmerPortal/1.0 (judy@goldleaflabs.co.ke)")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)

            if (jsonArray.length() > 0) {
                val firstResult = jsonArray.getJSONObject(0)
                val lat = firstResult.getDouble("lat")
                val lon = firstResult.getDouble("lon")
                Result.Success(GeoPoint(lat, lon))
            } else {
                Result.Error("No location found for '$query'")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error occurred during search")
        }
    }
}