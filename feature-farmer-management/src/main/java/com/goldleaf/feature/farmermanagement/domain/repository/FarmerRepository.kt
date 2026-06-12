package com.goldleaf.feature.farmermanagement.domain.repository


import com.goldleaf.core.data.dto.FarmerUpdateDto
import com.goldleaf.core.data.dto.auth.OTPRequest
import com.goldleaf.core.data.dto.auth.OTPVerificationRequest
import com.goldleaf.core.data.dto.crop.CropDto
import com.goldleaf.core.data.dto.crop.CropMasterDto
import com.goldleaf.core.data.dto.farm.Certification
import com.goldleaf.core.data.dto.farm.CropCategory
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.dto.farm.FarmerDashboardData
import com.goldleaf.core.data.dto.farm.FarmerPreferences
import com.goldleaf.core.data.dto.farm.FarmerRegistrationRequest
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.dto.farm.WaterRequirement
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.util.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for farmer-related operations
 * Handles authentication, profile management, farm management, and related features
 */
interface FarmerRepository {

    /**
     * Send OTP to the provided phone number
     * @param request OTP request containing phone number and action type
     * @return Result indicating success or failure
     */
    suspend fun sendOTP(request: OTPRequest): Result<Unit>

    /**
     * Verify the OTP code sent to the phone number
     * @param request OTP verification request with phone number and code
     * @return Result indicating verification success or failure
     */
    suspend fun verifyOTP(request: OTPVerificationRequest): Result<Unit>

    /**
     * Register a new farmer account
     * @param request Farmer registration details
     * @return Result containing the registered Farmer or error
     */
    suspend fun registerFarmer(request: FarmerRegistrationRequest): Result<Farmer>

    /**
     * Login farmer with phone number and password
     * @param phone Farmer's phone number
     * @param password Farmer's password
     * @return Result containing the authenticated Farmer or error
     */
    suspend fun loginFarmer(phone: String, password: String): Result<Farmer>

    /**
     * Get the currently logged in farmer as a Flow
     * @return Flow emitting the current Farmer or null if not logged in
     */
     fun getCurrentFarmer(farmerId: String): Flow<Farmer?>

    /**
     * Logout the current farmer
     * @return Result indicating logout success or failure
     */
    suspend fun logoutFarmer(): Result<Unit>

    // =====================================================
    // Profile Management
    // =====================================================

    /**
     * Update farmer profile information
     * @param farmerId Unique identifier of the farmer to update
     * @param request Update request with new profile data
     * @return Result containing updated Farmer or error
     */
    suspend fun updateFarmerProfile(farmerId: String, request: FarmerUpdateDto): Result<Farmer>

    /**
     * Upload and update farmer's profile image
     * @param imageUri Local URI of the image to upload
     * @return Result containing the uploaded image URL or error
     */
    suspend fun updateProfileImage(imageUri: String): Result<String>

    /**
     * Delete the farmer's account permanently
     * @return Result indicating deletion success or failure
     */

    // =====================================================
    // Dashboard Data
    // =====================================================

    /**
     * Get comprehensive dashboard data for the farmer
     * @return Result containing FarmerDashboardData or error
     */
    suspend fun getFarmerDashboard(farmerId: String): Result<FarmerDashboardData>

    /**
     * Get farmer details by ID
     * @param farmerId The farmer's unique identifier
     * @return Result containing the Farmer or error
     */
    suspend fun getFarmerById(farmerId: String): Result<Farmer>

    // =====================================================
    // Farm Management (Multiple farms support)
    // =====================================================

    /**
     * Add a new farm to the farmer's account
     * @param farm Farm details to add
     * @return Result containing the created Farm with ID or error
     */
    suspend fun addFarm(farm: Farm): Result<Farm>

    /**
     * Update existing farm details
     * @param farm Updated farm information
     * @return Result containing the updated Farm or error
     */
    suspend fun updateFarm(farm: Farm): Result<Farm>

    /**
     * Delete a farm from the farmer's account
     * @param farmId ID of the farm to delete
     * @return Result indicating deletion success or failure
     */
    suspend fun deleteFarm(farm: FarmEntity): Result<Unit>

    /**
     * Get all farms belonging to the current farmer
     * @return Flow emitting list of farms
     */
     fun getFarmerFarms(farmerId: String): Flow<List<Farm>>


    /**
     * Get specific farm details by ID
     * @param farmId The farm's unique identifier
     * @return Result containing the Farm or error
     */
    suspend fun getFarmById(farmId: String): Result<Farm>

    /**
     * Update farm boundaries (GPS coordinates)
     * @param farmId ID of the farm
     * @param boundaries List of GPS coordinates defining the farm perimeter
     * @return Result indicating success or failure
     */
    suspend fun setFarmBoundaries(farmId: String, boundaries: List<GeoPoint>): Result<Unit>

    // =====================================================
    // Preferences
    // =====================================================

    /**
     * Update farmer's preferences
     * @param preferences Updated preference settings
     * @return Result indicating success or failure
     */
    suspend fun updatePreferences(preferences: FarmerPreferences): Result<Unit>

    /**
     * Get farmer's current preferences
     * @return Flow emitting farmer preferences
     */
    suspend fun getPreferences(): Flow<FarmerPreferences>

    // =====================================================
    // Certifications
    // =====================================================

    /**
     * Add a new certification to farmer's profile
     * @param certification Certification details to add
     * @return Result indicating success or failure
     */
    suspend fun addCertification(certification: Certification): Result<Unit>

    /**
     * Get all certifications for the farmer
     * @return Flow emitting list of certifications
     */
    suspend fun getCertifications(): Flow<List<Certification>>

    /**
     * Remove a certification from farmer's profile
     * @param certificationId ID of certification to remove
     * @return Result indicating success or failure
     */
    suspend fun removeCertification(certificationId: String): Result<Unit>

    // =====================================================
    // Achievements
    // =====================================================


    // =====================================================
    // Data Synchronization
    // =====================================================

    /**
     * Synchronize local farmer data with remote server
     * @return Result indicating sync success or failure
     */
    suspend fun syncFarmerData(): Result<Unit>

    /**
     * Check if local data is stale and needs syncing
     * @return true if data is stale, false otherwise
     */
    suspend fun isDataStale(): Boolean

    // =====================================================
    // Crop Management (Optional - consider moving to CropRepository)
    // =====================================================

    /**
     * Get list of all available crops in the system
     * @return Result containing list of available Crops or error
     */
    suspend fun getAvailableCrops(): Result<List<CropMasterDto>>

    /**
     * Get crops filtered by category
     * @param category Category name to filter by
     * @return Result containing filtered list of Crops or error
     */
    suspend fun getCropsByCategory(category: String): Result<List<CropEntity>>

    /**
     * Search for crops by name or description
     * @param query Search query string
     * @return Result containing matching Crops or error
     */
    suspend fun searchCrops(query: String): Result<List<CropEntity>>

    /**
     * Get detailed information about a specific crop
     * @param cropId Crop's unique identifier
     * @return Result containing the Crop or error
     */
    suspend fun getCropById(cropId: String): Result<CropEntity>

    /**
     * Get all crop categories
     * @return Result containing list of CropCategory or error
     */
    suspend fun getCropCategories(): Result<List<CropCategory>>

    // =====================================================
    // Farm-Crop Association
    // =====================================================

    /**
     * Get all crops associated with a specific farm
     * @param farmId Farm's unique identifier
     * @return Result containing list of FarmCrop associations or error
     */
    suspend fun getFarmCrops(farmId: String): Result<List<CropEntity>>

    /**
     * Get farm crops as a reactive Flow (local-first)
     * @param farmId Farm's unique identifier
     * @return Flow emitting list of crops, updates when data changes
     */
    fun getFarmCropsFlow(farmId: String): Flow<List<CropEntity>>

    /**
     * Update the crops grown on a farm
     * @param farmId Farm's unique identifier
     * @param crops List of crops to associate with the farm
     * @return Result containing updated list of FarmCrop associations or error
     */
    suspend fun updateFarmCrops(farmId: String, crops: List<CropMasterDto>): Result<List<FarmCrop>>

    /**
     * Add a single crop to a farm with planting details
     * @param farmId Farm's unique identifier
     * @param cropId Crop's unique identifier
     * @param plantingDate Date when crop was planted
     * @param areaAllocated Area allocated for this crop in acres
     * @param notes Additional notes about the planting
     * @return Result containing the created FarmCrop association or error
     */
    suspend fun addCropToFarm(
        farmId: String,
        cropId: String,
        plantingDate: java.time.LocalDateTime? = null,
        areaAllocated: Double? = null,
        notes: String? = null
    ): Result<FarmCrop>

    /**
     * Remove a crop from a farm
     * @param farmId Farm's unique identifier
     * @param farmCropId Farm-Crop association ID
     * @return Result indicating success or failure
     */
    suspend fun removeCropFromFarm(farmId: String, farmCropId: String): Result<Unit>

    /**
     * Update the status of a crop on a farm
     * @param farmCropId Farm-Crop association ID
     * @param status New crop status
     * @param notes Optional notes about the status update
     * @return Result containing updated FarmCrop or error
     */
    suspend fun updateCropStatus(
        farmCropId: String,
        status: CropStatus,
        notes: String? = null
    ): Result<FarmCrop>

    /**
     * Get statistics about crops on a farm
     * @param farmId Farm's unique identifier
     * @return Result containing FarmCropStats or error
     */
    suspend fun getFarmCropStats(farmId: String): Result<FarmCropStats>

    /**
     * Get crop recommendations based on farm conditions
     * @param farmId Farm's unique identifier
     * @param soilType Optional soil type filter
     * @param waterAvailability Optional water requirement filter
     * @return Result containing list of recommended Crops or error
     */

    suspend fun getRecommendedCrops( farmId: String,  soilType: String?,  waterAvailability: WaterRequirement?  ): Result<List<CropEntity>>

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /**
     * Bulk sync all farmer data (crops, plots, tasks) from server
     */
    suspend fun syncAllFromServer(farmerId: String): Result<Unit>

}

// =====================================================
// Additional Data Classes for Crop Management
// =====================================================




/**
 * Represents a crop category grouping
 */
/**
 * Represents the association between a farm and a crop
 */
data class FarmCrop(
    val id: String,
    val farmId: String,
    val cropId: String,
    val crop: CropEntity,
    val plantingDate: LocalDateTime? = null,
    val expectedHarvestDate: LocalDateTime? = null,
    val actualHarvestDate: LocalDateTime? = null,
    val areaAllocated: Double? = null, // in acres
    val status :CropStatus,   //CropStatus.PLANNED ,
    val yieldAmount: Double? = null, // actual yield in kg
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Statistics about crops on a specific farm
 */
data class FarmCropStats(
    val totalCrops: Int,
    val activeCrops: Int,
    val plannedCrops: Int,
    val harvestedCrops: Int,
    val totalAreaAllocated: Double,
    val cropsByCategory: Map<String, Int>,
    val totalExpectedYield: Double? = null,
    val totalActualYield: Double? = null
)

/**
 * Price range for a crop
 */
data class PriceRange(
    val min: Double,
    val max: Double,
    val currency: String = "KES"
)



/**
 * Water requirement levels for crops
 */
enum class WaterRequirement {
    LOW,      // Minimal water needed
    MODERATE, // Average water needs
    HIGH      // High water requirement
}