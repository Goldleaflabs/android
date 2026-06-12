package com.goldleaf.feature.farmermanagement.data.repository

import android.util.Log
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.api.AddCertificationRequest
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.FarmerUpdateDto
import com.goldleaf.core.data.dto.auth.OTPRequest
import com.goldleaf.core.data.dto.auth.OTPVerificationRequest
import com.goldleaf.core.data.dto.crop.AddCropToFarmRequest
import com.goldleaf.core.data.dto.crop.CropMasterDto
import com.goldleaf.core.data.dto.crop.UpdateCropStatusRequest
import com.goldleaf.core.data.dto.crop.UpdateFarmCropsRequest
import com.goldleaf.core.data.dto.farm.Certification
import com.goldleaf.core.data.dto.farm.CropCategory
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmFacility
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.dto.farm.FarmerDashboardData
import com.goldleaf.core.data.dto.farm.FarmerPreferences
import com.goldleaf.core.data.dto.farm.FarmerRegistrationRequest
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.data.dto.farm.WaterRequirement
import com.goldleaf.core.data.dto.farm.WaterSource
import com.goldleaf.core.data.dto.farm.toDomain
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.PlotEntity
import com.goldleaf.core.data.local.dao.CertificationDao
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.core.data.local.dao.FarmerDao
import com.goldleaf.core.data.local.dao.PlotDao
import com.goldleaf.core.data.local.dao.toDomain
import com.goldleaf.core.util.Result
import com.goldleaf.feature.farmermanagement.data.mapper.toDomainModel
import com.goldleaf.feature.farmermanagement.domain.repository.FarmCrop
import com.goldleaf.feature.farmermanagement.domain.repository.FarmCropStats
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import com.goldleaf.feature.farmermanagement.domain.toDomain
import com.goldleaf.feature.farmermanagement.domain.toEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val farmerDao: FarmerDao,
    private val farmDao: FarmDao,
    private val cropDao: CropDao,
    private val certificationDao: CertificationDao,
    private val plotDao: PlotDao,
    private val sessionManager: UserSessionManager
) : FarmerRepository {

    override suspend fun sendOTP(request: OTPRequest): Result<Unit> {
        return try {
            val response = apiService.sendOTP(request)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to send OTP: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Failed to send OTP: ${e.localizedMessage}")
        }
    }

    override suspend fun verifyOTP(request: OTPVerificationRequest): Result<Unit> {
        return try {
            val response = apiService.verifyOTP(request)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Invalid OTP code")
            }
        } catch (e: Exception) {
            Result.Error("OTP verification failed: ${e.localizedMessage}")
        }
    }

    override suspend fun registerFarmer(request: FarmerRegistrationRequest): Result<Farmer> {
        return try {
            // Call API to register
            val response = apiService.registerFarmer(request)

            if (response.isSuccessful && response.body() != null) {
                val farmer = response.body()!!
                val authToken = response.headers()["Authorization"] ?: ""

                // Start session
                sessionManager.startSession(farmer.id, authToken)

                // Save farmer to local database (only ONE record)
                farmerDao.insertFarmer(farmer.toEntity())

                Result.Success(farmer)
            } else {
                Result.Error("Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Registration failed: ${e.localizedMessage}")
        }
    }

    override suspend fun loginFarmer(phone: String, password: String): Result<Farmer> {
        return try {
            // Call API to login
            val response = apiService.loginFarmer(phone, password)

            if (response.isSuccessful && response.body() != null) {
                val farmer = response.body()!!
                val authToken = response.headers()["Authorization"] ?: ""

                // Start new session
                sessionManager.startSession(farmer.id, authToken)

                // Save THIS user's data to local database
                farmerDao.insertFarmer(farmer.toEntity())

                Result.Success(farmer)
            } else {
                Result.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Login failed: ${e.localizedMessage}")
        }
    }

    override  fun getCurrentFarmer(farmerId: String): Flow<Farmer?> {
        // Simply get the farmer from local database (there's only ONE)
        return farmerDao.getCurrentFarmer().map { it?.toDomain() }
    }

    override suspend fun logoutFarmer(): Result<Unit> {
        return try {
            // Call API to invalidate token
            suspend fun logout() {
                try {
                    // Get token from UserSessionManager
                    val token = sessionManager.getBearerToken()

                    if (token.isNotEmpty()) {
                        apiService.logout(token)
                    }
                } catch (e: Exception) {
                    // Ignore API errors, still logout locally
                    Log.e("Logout", "Failed to logout from server", e)
                } finally {
                    // Always clear local session
                    sessionManager.clearSession()
                }
            }

            // Clear session only - keep local data for offline access
            sessionManager.clearSession()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Logout failed: ${e.localizedMessage}")
        }
    }

    override suspend fun updateFarmerProfile(farmerId: String, request: FarmerUpdateDto): Result<Farmer> {
        return try {
            val response = apiService.updateFarmerProfile(farmerId, request)
            if (response.isSuccessful && response.body() != null) {
                val farmer = response.body()!!
                farmerDao.updateFarmer(farmer.toEntity())
                Result.Success(farmer)
            } else {
                Result.Error("Update failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Update failed: ${e.localizedMessage}")
        }
    }

    override suspend fun updateProfileImage(imageUri: String): Result<String> {
        return try {
            val response = apiService.uploadProfileImage(imageUri)
            if (response.isSuccessful && response.body() != null) {
                val imageUrl = response.body()!!
                Result.Success(imageUrl)
            } else {
                Result.Error("Image upload failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Image upload failed: ${e.localizedMessage}")
        }
    }

    // =====================================================
    // Dashboard Data
    // =====================================================

    override suspend fun getFarmerDashboard(farmerId: String): Result<FarmerDashboardData> {

        return try {
            val response = apiService.getFarmerDashboard(farmerId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to load dashboard: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Dashboard loading failed: ${e.localizedMessage}")
        }
    }


    override suspend fun getFarmerById(farmerId: String): Result<Farmer> {
        return try {
            val response = apiService.getFarmerById(farmerId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val localFarmer = farmerDao.getFarmerByIdSingle(farmerId)
                if (localFarmer != null) {
                    Result.Success(localFarmer.toDomain())
                } else {
                    Result.Error("Farmer not found")
                }
            }
        } catch (e: Exception) {
            Result.Error("Failed to get farmer: ${e.localizedMessage}")
        }
    }

    // =====================================================
    // Farm Management (Multiple farms support)
    // =====================================================

    override suspend fun addFarm(farm: Farm): Result<Farm> {
        return try {
            // 1. Save locally FIRST
            val farmWithId = if (farm.id.isEmpty()) farm.copy(id = generateLocalId()) else farm
            farmDao.insertFarm(farmWithId.toEntity())

            // 2. Try to sync to server
            try {
                val response = apiService.addFarm(farmWithId)
                if (response.isSuccessful && response.body() != null) {
                    val savedFarm = response.body()!!
                    farmDao.insertFarm(savedFarm.toEntity())
                    Result.Success(savedFarm)
                } else {
                    // API failed but data is safe locally
                    Result.Success(farmWithId)
                }
            } catch (e: Exception) {
                // Network error - data is safe locally
                Result.Success(farmWithId)
            }
        } catch (e: Exception) {
            Result.Error("Failed to add farm: ${e.localizedMessage}")
        }
    }

    override suspend fun updateFarm(farm: Farm): Result<Farm> {
        return try {
            // 1. Update locally FIRST
            farmDao.updateFarm(farm.toEntity())

            // 2. Try to sync to server
            try {
                val response = apiService.updateFarm(farm)
                if (response.isSuccessful && response.body() != null) {
                    val updatedFarm = response.body()!!
                    farmDao.updateFarm(updatedFarm.toEntity())
                }
            } catch (e: Exception) {
                // Network error - data is safe locally
            }

            Result.Success(farm)
        } catch (e: Exception) {
            Result.Error("Failed to update farm: ${e.localizedMessage}")
        }
    }

    override suspend fun deleteFarm(farm: FarmEntity): Result<Unit> {
        return try {
            // 1. Delete locally FIRST
            farmDao.deleteFarm(farm)

            // 2. Try to sync deletion to server
            try {
                apiService.deleteFarm(farm)
            } catch (e: Exception) {
                // Network error - deletion is safe locally
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete farm: ${e.localizedMessage}")
        }
    }







    override fun getFarmerFarms(farmerId: String): Flow<List<Farm>> {
        Log.d("FarmRepository", "🌾 getFarmerFarms called with farmerId: '$farmerId'")
        
        return flow {
            Log.d("FarmRepository", "🌾 Flow started: Refreshing farms from server for farmerId: '$farmerId'")
            // Refresh from server FIRST
            refreshFarmsFromServer(farmerId)
            
            // Then emit the local data and keep emitting updates
            farmDao.getFarmsByFarmerId(farmerId).collect { farmEntities ->
                Log.d("FarmRepository", "🌾 Emitting ${farmEntities.size} farms from local DB")
                emit(farmEntities.map(FarmEntity::toDomain))
            }
        }
    }

    private suspend fun refreshFarmsFromServer(farmerId: String) {
        try {
            Log.d("FarmRepository", "🔄 CALLING REMOTE API: getFarmsByFarmerId for farmerId='$farmerId'")
            val response = apiService.getFarmsByFarmerId(farmerId)
            Log.d("FarmRepository", "🔄 API Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            if (response.isSuccessful) {
                val farmDtos = response.body()
                Log.d("FarmRepository", "🔄 API returned ${farmDtos?.size ?: 0} farms")
                
                if (!farmDtos.isNullOrEmpty()) {
                    // Use IO Dispatcher specifically for Room writes
                    withContext(Dispatchers.IO) {
                        val farmEntities = farmDtos.map { dto ->
                            dto.toEntity(farmerId = farmerId)
                        }

                        Log.d("FarmRepository", "💾 Saving ${farmEntities.size} farms to Room database")
                        farmDao.insertAll(farmEntities)
                        Log.d("FarmRepository", "✅ Successfully saved farms to Room")
                    }
                } else {
                    Log.w("FarmRepository", "⚠️ API returned empty list of farms")
                }
            } else {
                Log.e("FarmRepository", "❌ Server Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("FarmRepository", "❌ Exception during sync: ${e.message}", e)
        }
    }



    override suspend fun getFarmById(farmId: String): Result<Farm> {
        return try {
            // 1. Try local database
            val localFarm: FarmEntity? = farmDao.getFarmById(farmId)

            if (localFarm != null) {
                // Success: Map Entity -> Domain Farm
                Result.Success(localFarm.toDomain())
            } else {
                // 2. Try API if not in DB
                val response = apiService.getFarmById(farmId)
                val dto = response.body()

                if (response.isSuccessful && dto != null) {
                    // Save to DB (Map DTO -> Entity)
                    farmDao.insertFarm(dto.toEntity(farmId))
                    // Return to UI (Map DTO -> Domain Farm)
                    Result.Success(dto.toDomain())
                } else {
                    Result.Error("Farm not found on server")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun setFarmBoundaries(farmId: String, boundaries: List<GeoPoint>): Result<Unit> {
        return try {
            // 1. Update locally FIRST
            val farm = farmDao.getFarmById(farmId)
            farm?.let {
                farmDao.updateFarm(it.copy(boundaries = serializeBoundaries(boundaries)))
            }

            // 2. Try to sync to server
            try {
                apiService.setFarmBoundaries(farmId, boundaries)
            } catch (e: Exception) {
                // Network error - boundaries are safe locally
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to set farm boundaries: ${e.localizedMessage}")
        }
    }

    // =====================================================
    // Preferences
    // =====================================================

    override suspend fun updatePreferences(preferences: FarmerPreferences): Result<Unit> {
        return try {
            // Convert FarmerPreferences to JSON string
            val gson = Gson()
            val preferencesJson = gson.toJson(preferences)
            val farmerId = sessionManager.getCurrentUserId() // or however you get the current farmer ID

            // Pass farmerId and JSON string to DAO
            farmerDao.updateFarmerPreferences(
                farmerId = farmerId.toString(), // You need the farmer ID here
                preferencesJson = preferencesJson
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update preferences: ${e.localizedMessage}")
        }
    }



    override suspend fun getPreferences(): Flow<FarmerPreferences> {
        return farmerDao.getCurrentFarmer().map {
            it?.preferences ?: FarmerPreferences()
        }
    }

    // =====================================================
    // Certifications
    // =====================================================

    override suspend fun addCertification(certification: Certification): Result<Unit> {
        return try {
            val request = AddCertificationRequest(
                name = certification.name,
                issuingBody = certification.issuingOrganization,
                dateIssued = certification.issueDate.toString(),
                expiryDate = certification.expiryDate?.toString(),
                certificateNumber = null,
                documentUrl = certification.certificateUrl
            )

            val response = apiService.addCertification(request)

            if (response.isSuccessful) {
                Result.Success(Unit)  // Don't save locally, just use API
            } else {
                Result.Error("Failed to add certification: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Error adding certification: ${e.localizedMessage}")
        }
    }

    override suspend fun getCertifications(): Flow<List<Certification>> {
        // Get current farmer ID
        val farmerId = sessionManager.getCurrentUserId() // or however you get the current farmer ID

        return certificationDao.getCertificationsByFarmer(farmerId.toString()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun removeCertification(certificationId: String): Result<Unit> {
        return try {
            certificationDao.deleteCertificationById(certificationId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to remove certification: ${e.localizedMessage}")
        }
    }



    // =====================================================
    // Sync
    // =====================================================

    override suspend fun syncFarmerData(): Result<Unit> {
        return try {
            // Fetch latest farmer data from server
            val response = apiService.getCurrentFarmer()

            if (response.isSuccessful && response.body() != null) {
                val farmer = response.body()!!

                // Update local database
                farmerDao.insertFarmer(farmer.toEntity())

                // Update last sync time
                val currentTime = System.currentTimeMillis()
                val currentFarmer = farmerDao.getCurrentFarmerSync()
                currentFarmer?.let {
                    farmerDao.updateFarmer(it.copy(lastSyncTime = currentTime))
                }

                Result.Success(Unit)
            } else {
                Result.Error("Sync failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Sync failed: ${e.localizedMessage}")
        }
    }

    override suspend fun isDataStale(): Boolean {
        // Check if local data is older than 1 hour
        val lastSync = farmerDao.getLastSyncTime()
        val now = System.currentTimeMillis()
        return (now - lastSync!!) > (60 * 60 * 1000) // 1 hour
    }

    override suspend fun syncAllFromServer(farmerId: String): Result<Unit> {
        return try {
            val lastSync = farmerDao.getLastSyncTime() ?: 0L
            val response = apiService.syncDownload(farmerId, lastSync)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                data.plots?.let { plotDao.insertPlots(it) }
                data.crops?.let { cropDao.insertallCrop(it) }

                Result.Success(Unit)
            } else {
                Result.Error("Bulk sync failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Bulk sync failed: ${e.localizedMessage}")
        }
    }

    // Implement remaining methods similarly...
    override suspend fun getAvailableCrops(): Result<List<CropMasterDto>> {
        return try {
            val response = apiService.getAllCrops()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.Success(body)
            } else {
                Result.Error("Failed to fetch master crop list: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Error fetching farm crops: ${e.message}")
        }
    }

    override suspend fun getFarmCrops(farmId: String): Result<List<CropEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFarmCrops(farmId)

                if (response.isSuccessful && response.body() != null) {
                    val cropsFromServer = response.body()!!
                    cropDao.insertallCrop(cropsFromServer)

                    Result.Success(cropsFromServer)
                } else {
                    // Fallback to Local Data if Server Fails
                    val localData = cropDao.getCropsByFarmId(farmId)
                    Result.Success(localData)
                }
            } catch (e: Exception) {
                // Fallback to Local Data if Offline
                val localData = cropDao.getCropsByFarmId(farmId)
                if (localData.isNotEmpty()) {
                    Result.Success(localData)
                } else {
                    Result.Error("Network error: ${e.message}")
                }
            }
        }
    }

    override fun getFarmCropsFlow(farmId: String): Flow<List<CropEntity>> {
        return flow {
            // 1. FIRST: Emit local data immediately
            cropDao.getCropsByFarmIdFlow(farmId)
                .collect { localCrops ->
                    emit(localCrops)
                }
            
            // 2. THEN: Refresh from remote source in background
            refreshCropsFromServer(farmId)
        }
    }

    private suspend fun refreshCropsFromServer(farmId: String) {
        try {
            val response = apiService.getFarmCrops(farmId)

            if (response.isSuccessful) {
                val cropDtos = response.body()
                if (!cropDtos.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) {
                        Log.d("FarmRepository", "Saving ${cropDtos.size} crops to Room")
                        cropDao.insertallCrop(cropDtos)
                    }
                }
            } else {
                Log.e("FarmRepository", "Crop Sync Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("FarmRepository", "Crop Sync Failed", e)
        }
    }

    override suspend fun updateFarmCrops(
        farmId: String,
        crops: List<CropMasterDto>
    ): Result<List<FarmCrop>> {
        return withContext(Dispatchers.IO) {
            try {
                val cropIds = crops.map { it.id }
                val request = UpdateFarmCropsRequest(farmId, cropIds)
                val response = apiService.updateFarmCrops(farmId, request)

                if (response.isSuccessful && response.body() != null) {
                    val farmCrops = response.body()!!.map { it.toDomainModel() }
                    Result.Success(farmCrops)
                } else {
                    Result.Error("Failed to update farm crops: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error updating farm crops: ${e.message}")
            }
        }
    }


    override suspend fun getCropsByCategory(category: String): Result<List<CropEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCropsByCategory(category)

                if (response.isSuccessful && response.body() != null) {
                    val crops = response.body()!!.map { it.toDomainModel() }
                    Result.Success(crops)
                } else {
                    Result.Error("Failed to fetch crops by category: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error fetching crops by category: ${e.message}")
            }
        }
    }

    override suspend fun searchCrops(query: String): Result<List<CropEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchCrops(query)

                if (response.isSuccessful && response.body() != null) {
                    val crops = response.body()!!.map { it.toDomainModel() }
                    Result.Success(crops)
                } else {
                    Result.Error("Failed to search crops: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error searching crops: ${e.message}")
            }
        }
    }
    override suspend fun getCropById(cropId: String): Result<CropEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCropById(cropId)

                if (response.isSuccessful && response.body() != null) {
                    val crop = response.body()!!.toDomainModel()
                    Result.Success(crop)
                } else {
                    Result.Error("Failed to fetch crop: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error fetching crop: ${e.message}")
            }
        }
    }
    override suspend fun getCropCategories(): Result<List<CropCategory>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCropCategories()

                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map { it.toDomainModel() }
                    Result.Success(categories)
                } else {
                    Result.Error("Failed to fetch categories: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error fetching categories: ${e.message}")
            }
        }
    }

    override suspend fun addCropToFarm(
        farmId: String,
        cropId: String,
        plantingDate: java.time.LocalDateTime?,
        areaAllocated: Double?,
        notes: String?
    ): Result<FarmCrop> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AddCropToFarmRequest(
                    cropId = cropId,
                    plantingDate = plantingDate?.toString(),
                    areaAllocated = areaAllocated,
                    notes = notes
                )

                val response = apiService.addCropToFarm(farmId, request)

                if (response.isSuccessful && response.body() != null) {
                    val farmCrop = response.body()!!.toDomainModel()
                    Result.Success(farmCrop)
                } else {
                    Result.Error("Failed to add crop to farm: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error adding crop to farm: ${e.message}")
            }
        }
    }

    override suspend fun removeCropFromFarm(
        farmId: String,
        farmCropId: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeCropFromFarm(farmId, farmCropId)

                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error("Failed to remove crop from farm: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error removing crop from farm: ${e.message}")
            }
        }
    }

    override suspend fun updateCropStatus(
        farmCropId: String,
        status: CropStatus,
        notes: String?
    ): Result<FarmCrop> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateCropStatusRequest(
                    status = status.name,
                    notes = notes
                )

                val response = apiService.updateCropStatus(farmCropId, request)

                if (response.isSuccessful && response.body() != null) {
                    val farmCrop = response.body()!!.toDomainModel()
                    Result.Success(farmCrop)
                } else {
                    Result.Error("Failed to update crop status: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error updating crop status: ${e.message}")
            }
        }
    }

    override suspend fun getFarmCropStats(farmId: String): Result<FarmCropStats> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFarmCropStats(farmId)

                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!.toDomainModel()
                    Result.Success(stats)
                } else {
                    Result.Error("Failed to fetch farm crop stats: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error fetching farm crop stats: ${e.message}")
            }
        }
    }



    override suspend fun getRecommendedCrops(
        farmId: String,
        soilType: String?,
        waterAvailability: WaterRequirement?
    ): Result<List<CropEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRecommendedCrops(
                    farmId = farmId,
                    soilType = soilType,
                    waterAvailability = waterAvailability?.name
                )

                if (response.isSuccessful && response.body() != null) {
                    val crops = response.body()!!.map { it.toDomainModel() }
                    Result.Success(crops)
                } else {
                    Result.Error("Failed to fetch recommended crops: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Error fetching recommended crops: ${e.message}")
            }
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
    // =====================================================
    // Private Helper Methods
    // =====================================================

    private fun generateLocalId(): String {
        return "local_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}



// ✅ JSON Serialization Helper Functions (using Gson)
private val gson = Gson()

private fun serializeBoundaries(boundaries: List<GeoPoint>): String {
    return try {
        gson.toJson(boundaries)
    } catch (e: Exception) {
        "[]"
    }
}

private fun deserializeBoundaries(json: String): List<GeoPoint> {
    if (json.isBlank()) return emptyList()
    return try {
        gson.fromJson(json, Array<GeoPoint>::class.java)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun serializeFacilities(facilities: List<FarmFacility>): String {
    return try {
        gson.toJson(facilities)
    } catch (e: Exception) {
        "[]"
    }
}

private fun deserializeFacilities(json: String): List<FarmFacility> {
    if (json.isBlank()) return emptyList()
    return try {
        gson.fromJson(json, Array<FarmFacility>::class.java)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun serializeWaterSources(waterSources: List<WaterSource>): String {
    return try {
        gson.toJson(waterSources)
    } catch (e: Exception) {
        "[]"
    }
}

private fun deserializeWaterSources(json: String): List<WaterSource> {
    if (json.isBlank()) return emptyList()
    return try {
        gson.fromJson(json, Array<WaterSource>::class.java)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
