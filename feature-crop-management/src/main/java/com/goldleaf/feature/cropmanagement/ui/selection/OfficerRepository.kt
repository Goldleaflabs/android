package com.goldleaf.feature.cropmanagement.ui.selection

import android.util.Log
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.dao.CropDao
import javax.inject.Inject

class OfficerRepository  @Inject constructor(
    private val api: ApiService,
    private val dao: CropDao
) {

    suspend fun getOfficerPhoneByCounty(county: String): String? {
        Log.d("OfficerRepository", "🔍 getOfficerPhoneByCounty() called for county: '$county'")
        
        return try {
            // 1. Call server
            Log.d("OfficerRepository", "📡 Calling API for county: $county")
            val response = api.getOfficerByCounty(county)
            Log.d("OfficerRepository", "📡 API Response received - success: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful) {
                val officers = response.body()
                Log.d("OfficerRepository", "✅ API returned ${officers?.size ?: 0} officers")
                
                // If server returned data
                officers?.let {
                    // 2. Save to Room
                    Log.d("OfficerRepository", "💾 Saving ${it.size} officers to Room database")
                    dao.insertAllOfficers(it)
                    
                    // 3. Return phone from server data
                    val phoneFromServer = it.firstOrNull()?.phone
                    Log.d("OfficerRepository", "📞 Returning phone from server: $phoneFromServer")
                    return phoneFromServer
                }
            }

            // 4. If API failed → fallback to Room
            Log.d("OfficerRepository", "⚠️ API unsuccessful or returned null, falling back to Room database")
            val phoneFromRoom = dao.getOfficerPhoneByCounty(county)
            Log.d("OfficerRepository", "📞 Phone from Room: $phoneFromRoom")
            phoneFromRoom

        } catch (e: Exception) {
            // API error → return from local store
            Log.e("OfficerRepository", "❌ API call failed: ${e.message}", e)
            Log.d("OfficerRepository", "🔄 Falling back to Room database")
            val phoneFromRoom = dao.getOfficerPhoneByCounty(county)
            Log.d("OfficerRepository", "📞 Phone from Room (fallback): $phoneFromRoom")
            phoneFromRoom
        }
    }
}
