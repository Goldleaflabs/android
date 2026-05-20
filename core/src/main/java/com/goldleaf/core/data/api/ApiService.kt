package com.goldleaf.core.data.api

import com.goldleaf.core.data.dto.FarmerUpdateDto
import com.goldleaf.core.data.dto.PipelineResponse
import retrofit2.Response
import retrofit2.http.*
import com.goldleaf.core.data.dto.crop.*
import com.goldleaf.core.data.dto.auth.*
import com.goldleaf.core.data.dto.crop.CropDto
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropGrowthStage
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.TaskEntity
import com.goldleaf.core.data.local.VideoCategory
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.dto.farm.FarmerDashboardData
import com.goldleaf.core.data.dto.farm.FarmerPreferences
import com.goldleaf.core.data.dto.farm.FarmerRegistrationRequest
import com.goldleaf.core.data.dto.farm.FarmerUpdateRequest
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.data.local.GrowthStageEntity
import com.goldleaf.core.data.local.Officer
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part

interface ApiService {
    // ==================== AUTHENTICATION ====================
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/login")
    suspend fun loginFarmer(@Field("phone") phone: String,@Field("password") password: String): Response<Farmer>

    @POST("auth/password-change")
    suspend fun changePassword( @Body request: PasswordChangeRequest): Unit

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<BaseResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse>

// ==================== OTP AUTHENTICATION ====================

    @POST("auth/send-otp")
    suspend fun sendOTP(@Body request: OTPRequest): Response<BaseResponse>


    @POST("auth/verify-otp")
    suspend fun verifyOTP(@Body request: OTPVerificationRequest): Response<VerifyOTPResponse>

    /**
     * Reset password with reset token
     * POST /auth/reset-password
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    /**
     * Verify phone number (sends OTP via SMS)
     * POST /auth/verify-phone
     */
    @POST("auth/verify-phone")
    suspend fun verifyPhone(@Body request: VerifyPhoneRequest): Response<VerifyPhoneResponse>


// ==================== FARMER MANAGEMENT ====================

    @POST("farmers/register")
    suspend fun registerFarmer(@Body request: FarmerRegistrationRequest): Response<Farmer>

    @POST("farmers/updateFarmerProfile/{farmerId}")
    suspend fun updateFarmerProfile(@Path("farmerId") farmerId: String, @Body request: FarmerUpdateDto): Response<Farmer>

    @GET("farmers/{id}")
    suspend fun getFarmer(@Path("id") farmerId: String): Response<FarmerDto>


    @GET("farmers/{id}")
    suspend fun getFarmerById(@Path("id") farmerId: String): Response<Farmer>

    @PUT("farmers/{id}")
    suspend fun updateFarmer( @Path("id") farmerId: String, @Body farmer: FarmerUpdateRequest): Response<FarmerDto>

    @GET("farmers/{farmerId}/farms")
    suspend fun getFarmsByFarmerId(@Path("farmerId") farmerId: String ): Response<List<FarmDto>>


    @GET("farmers/{id}/dashboard")
    suspend fun getFarmerDashboard(@Path("id") farmerId: String): Response<FarmerDashboardData>

    @POST("farmers/{id}/farms")
    suspend fun createFarm( @Path("id") farmerId: String, @Body farm: FarmCreateRequest ): Response<FarmDto>

    @GET("farmers/{id}/farms")
    suspend fun getFarms(@Path("id") farmerId: String): Response<List<FarmDto>>

    @DELETE("farmers/{id}/farms/{farmId}")
    suspend fun deleteFarm(farm: FarmEntity): Response<BaseResponse>

    @POST("farmers/profile-image")
    suspend fun uploadProfileImage( @Part image: String): Response<String>

    @POST("farmers/farms")
    suspend fun addFarm(farm: Farm): Response<Farm>

    @POST("farmers/{id}/farms/{farmId}")
    suspend fun updateFarm(farm: Farm):Response<Farm>

    // Farm methods
    @GET("api/farms/{farmId}")
    suspend fun getFarmById(@Path("farmId") farmId: String): Response<FarmDto>

    @PUT("api/farms/{farmId}/boundaries")
    suspend fun updateFarmBoundaries( @Path("farmId") farmId: String,  @Body request: UpdateFarmBoundariesRequest ): Response<Unit>

    // Farmer methods
    @GET("api/farmer/current")
    fun getCurrentFarmer(): Response<Farmer>


    @PUT("api/farmer/preferences")
    suspend fun updateFarmerPreferences( @Body preferences: FarmerPreferences): Response<Unit>

    // Certification methods
    @POST("api/farmer/certifications")
    suspend fun addCertification( @Body request: AddCertificationRequest ): Response<CertificationDto>

    @GET("api/farmer/certifications")
    suspend fun getFarmerCertifications(): Response<List<CertificationDto>>




    // ==================== CROP MANAGEMENT ====================
    // HTTP calls to your backend
    @GET("growth-stages/{cropId}")
    suspend fun getGrowthStagesByCropId( @Path("cropId") cropId: String): Response<List<CropGrowthStage>>

    @POST("growth-stages")
    suspend fun createGrowthStage(@Body stage: GrowthStageEntity): Response<GrowthStageEntity>


    @PUT("growth-stages/{id}")
    suspend fun updateGrowthStage(@Path("id") id: String,@Body stage: CropGrowthStage): Response<CropGrowthStage>


    @GET("crops")
    suspend fun getAllCrops(): Response<List<CropMasterDto>>  // ✅ CropEntity instead of CropDto


    @GET("crops/{id}")
    suspend fun getCrop(@Path("id") cropId: String): Response<CropDto>

    @POST("crops")
    suspend fun createCrop(@Body crop: CropEntity): Response<CropEntity>

    @PUT("crops/{id}")
    suspend fun updateCrop(@Path("id") cropId: String,  @Body crop: CropEntity): Response<CropEntity>

    @DELETE("crops/{id}")
    suspend fun deleteCrop(@Path("id") cropId: String): Response<Unit>

    @GET("crops/{id}/monitoring")
    suspend fun getCropMonitoring(@Path("id") cropId: String): Response<CropMonitoringDto>

    @POST("crops/{id}/activities")
    suspend fun addCropActivity(@Path("id") cropId: String, @Body activity: CropActivityRequest ): Response<CropActivityDto>

    // ==================== WEATHER SERVICES ====================

    @GET("weather/current")
    suspend fun getCurrentWeather( @Query("lat") latitude: Double, @Query("lon") longitude: Double ): Response<WeatherDto>

    @GET("weather/forecast")
    suspend fun getWeatherForecast( @Query("lat") latitude: Double,  @Query("lon") longitude: Double,  @Query("days") days: Int = 7 ): Response<WeatherForecastDto>

    @GET("weather/alerts")
    suspend fun getWeatherAlerts( @Query("lat") latitude: Double, @Query("lon") longitude: Double ): Response<List<WeatherAlertDto>>

    // ==================== MARKET ACCESS ====================

    @GET("market/prices")
    suspend fun getMarketPrices( @Query("commodity") commodity: String? = null ): Response<List<MarketPriceDto>>

    @GET("market/prices/{commodity}")
    suspend fun getCommodityPrice(@Path("commodity") commodity: String): Response<MarketPriceDto>

    @GET("market/buyers")
    suspend fun getBuyers( @Query("commodity") commodity: String? = null, @Query("location") location: String? = null ): Response<List<BuyerDto>>

    @POST("market/connections")
    suspend fun connectWithBuyer(@Body request: BuyerConnectionRequest): Response<ConnectionDto>

    @GET("market/trends")
    suspend fun getMarketTrends(  @Query("commodity") commodity: String,  @Query("period") period: String = "30d" ): Response<MarketTrendsDto>

    // ==================== TRAINING & EXTENSION ====================

    @GET("training/videos")
    suspend fun getTrainingVideos(  @Query("category") category: VideoCategory? = null, @Query("page") page: Int = 1, @Query("limit") limit: Int = 20 ): Response<VideoListResponse>

    @GET("training/videos/{id}")
    suspend fun getVideo(@Path("id") videoId: String): Response<VideoDto>

    @GET("training/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("training/progress")
    suspend fun updateProgress(@Body progress: ProgressUpdateRequest): Response<ProgressDto>

    @GET("training/progress/{userId}")
    suspend fun getUserProgress(@Path("userId") userId: String): Response<List<ProgressDto>>

    @GET("training/certificates/{userId}")
    suspend fun getUserCertificates(@Path("userId") userId: String): Response<List<CertificateDto>>

    @POST("training/enroll")
    suspend fun enrollInCourse(@Body request: EnrollmentRequest): Response<EnrollmentDto>

    // ==================== ADVISORY SERVICES ====================

    @GET("advisory/recommendations")
    suspend fun getRecommendations( @Query("farmerId") farmerId: String, @Query("category") category: String? = null): Response<List<RecommendationDto>>

    @POST("advisory/pest-identification")
    suspend fun identifyPest(@Body request: PestIdentificationRequest): Response<PestIdentificationDto>

    @POST("advisory/soil-analysis")
    suspend fun analyzeSoil(@Body request: SoilAnalysisRequest): Response<SoilAnalysisDto>

    @GET("advisory/experts")
    suspend fun getExperts( @Query("specialization") specialization: String? = null ): Response<List<ExpertDto>>


    @POST("advisory/consultations")
    suspend fun bookConsultation(@Body request: ConsultationRequest): Response<ConsultationDto>

    // ==================== NOTIFICATIONS ====================

    @GET("notifications")
    suspend fun getNotifications( @Query("userId") userId: String, @Query("unreadOnly") unreadOnly: Boolean = false): Response<List<NotificationDto>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") notificationId: String): Response<BaseResponse>

    @PUT("notifications/read-all")
    suspend fun markAllAsRead(@Query("userId") userId: String): Response<BaseResponse>

    // ==================== BASIC CERTIFICATION (LEGACY) ====================

    @GET("certification/requirements")
    suspend fun getCertificationRequirements( @Query("type") certificationType: String): Response<CertificationRequirementsDto>

    @POST("certification/applications")
    suspend fun applyCertification(@Body request: CertificationApplicationRequest): Response<CertificationDto>

    @GET("certification/applications/{farmerId}")
    suspend fun getCertificationStatus(@Path("farmerId") farmerId: String): Response<List<CertificationDto>>

    // ==================== CERTIFICATION & QUALITY MODULE ====================

    // Certification Management
    @GET("certification/certifications/farmer/{farmerId}")
    suspend fun getCertifications(@Path("farmerId") farmerId: String): Response<List<CertificationResponseDto>>

    @GET("certification/certifications/{id}")
    suspend fun getCertificationById(@Path("id") certificationId: String): Response<CertificationResponseDto>

    @POST("certification/certifications")
    suspend fun createCertification(@Body request: CertificationCreateRequest): Response<CertificationResponseDto>

    @PUT("certification/certifications/{id}")
    suspend fun updateCertification(
        @Path("id") certificationId: String,
        @Body request: CertificationUpdateRequest
    ): Response<CertificationResponseDto>

    @DELETE("certification/certifications/{id}")
    suspend fun deleteCertification(@Path("id") certificationId: String): Response<BaseResponse>

    // Requirements & Checklist
    @GET("certification/certifications/{id}/requirements")
    suspend fun getRequirements(@Path("id") certificationId: String): Response<List<RequirementDto>>

    @PUT("certification/requirements/{id}")
    suspend fun updateRequirement(
        @Path("id") requirementId: String,
        @Body request: RequirementUpdateRequest
    ): Response<RequirementDto>



    @Multipart
    @POST("certification/requirements/{id}/evidence")
    suspend fun uploadEvidence(
        @Path("id") requirementId: String,
        @Part file: MultipartBody.Part,
        @Part("notes") notes: RequestBody?
    ): Response<EvidenceUploadResponse>

    @GET("certification/templates/{type}")
    suspend fun getCertificationTemplate(@Path("type") certificationType: String): Response<List<RequirementDto>>

    // Audit Management
    @GET("certification/certifications/{id}/audits")
    suspend fun getAuditRecords(@Path("id") certificationId: String): Response<List<AuditRecordDto>>

    @POST("certification/audits")
    suspend fun createAuditRecord(@Body request: AuditRecordRequest): Response<AuditRecordDto>

    @POST("certification/audits/{id}/schedule")
    suspend fun scheduleInspection(
        @Path("id") certificationId: String,
        @Body request: InspectionScheduleRequest
    ): Response<InspectionDto>

    // Quality Batch Management
    @GET("quality/batches/farmer/{farmerId}")
    suspend fun getBatches(
        @Path("farmerId") farmerId: String,
        @Query("status") status: String? = null
    ): Response<List<ProductBatchDto>>

    @GET("quality/batches/{id}")
    suspend fun getBatchById(@Path("id") batchId: String): Response<ProductBatchDto>

    @POST("quality/batches")
    suspend fun createBatch(@Body request: BatchCreateRequest): Response<ProductBatchDto>

    @PUT("quality/batches/{id}")
    suspend fun updateBatch(
        @Path("id") batchId: String,
        @Body request: BatchUpdateRequest
    ): Response<ProductBatchDto>

    @DELETE("quality/batches/{id}")
    suspend fun deleteBatch(@Path("id") batchId: String): Response<BaseResponse>

    // Quality Parameters
    @GET("quality/batches/{id}/parameters")
    suspend fun getQualityParameters(@Path("id") batchId: String): Response<List<QualityParameterDto>>

    @POST("quality/parameters")
    suspend fun addQualityParameter(@Body request: QualityParameterRequest): Response<QualityParameterDto>

    @PUT("quality/parameters/{id}")
    suspend fun updateQualityParameter(
        @Path("id") parameterId: String,
        @Body request: QualityParameterRequest
    ): Response<QualityParameterDto>

    // Lab Tests
    @GET("quality/batches/{id}/lab-tests")
    suspend fun getLabTests(@Path("id") batchId: String): Response<List<LabTestDto>>

    @POST("quality/lab-tests")
    suspend fun submitLabTest(@Body request: LabTestRequest): Response<LabTestDto>

    @PUT("quality/lab-tests/{id}")
    suspend fun updateLabTest(  @Path("id") testId: String,  @Body request: LabTestUpdateRequest ): Response<LabTestDto>

    @Multipart
    @POST("quality/lab-tests/{id}/result")
    suspend fun uploadLabResult( @Path("id") testId: String,  @Part certificate: MultipartBody.Part ): Response<LabTestDto>

    // Blockchain & Authentication
    @POST("blockchain/create")
    suspend fun createBlockchainRecord(@Body request: BlockchainCreateRequest): Response<BlockchainRecordDto>

    @GET("blockchain/batch/{batchId}")
    suspend fun getBlockchainRecord(@Path("batchId") batchId: String): Response<BlockchainRecordDto>

    @POST("blockchain/verify")
    suspend fun verifyBlockchain(@Body request: BlockchainVerifyRequest): Response<BlockchainVerificationDto>

    // Product Journey
    @GET("journey/batch/{batchId}")
    suspend fun getProductJourney(@Path("batchId") batchId: String): Response<List<JourneyPointDto>>

    @POST("journey/points")
    suspend fun addJourneyPoint(@Body request: JourneyPointRequest): Response<JourneyPointDto>

    @PUT("journey/points/{id}/verify")
    suspend fun verifyJourneyPoint(@Path("id") pointId: String): Response<JourneyPointDto>

    // QR Code & Verification
    @POST("qr/generate")
    suspend fun generateQRCode(@Body request: QRCodeGenerateRequest): Response<QRCodeResponse>

    @GET("qr/batch/{batchNumber}")
    suspend fun getQRData(@Path("batchNumber") batchNumber: String): Response<QRCodeDataDto>

    // Consumer Verification (PUBLIC)
    @GET("public/verify/{batchNumber}")
    suspend fun verifyProduct(@Path("batchNumber") batchNumber: String): Response<ConsumerVerificationDto>

    @GET("public/journey/{batchNumber}")
    suspend fun getPublicJourney(@Path("batchNumber") batchNumber: String): Response<PublicJourneyDto>

    // Dashboards & Analytics
    @GET("certification/dashboard/{farmerId}")
    suspend fun getCertificationDashboard(@Path("farmerId") farmerId: String): Response<CertificationDashboardDto>

    @GET("quality/dashboard/{farmerId}")
    suspend fun getQualityDashboard(@Path("farmerId") farmerId: String): Response<QualityDashboardDto>

    @GET("quality/analytics/{farmerId}")
    suspend fun getQualityAnalytics(  @Path("farmerId") farmerId: String,  @Query("period") period: String = "30d" ): Response<QualityAnalyticsDto>

    // ==================== SYNC SERVICES ====================

    @GET("crops")
    suspend fun getAvailableCrops(): Response<List<CropMasterDto>>

    @GET("crops/category/{category}")
    suspend fun getCropsByCategory(@Path("category") category: String): Response<List<CropDto>>

    @GET("crops/search")
    suspend fun searchCrops(@Query("q") query: String): Response<List<CropDto>>

    @GET("crops/{cropId}")
    suspend fun getCropById(@Path("cropId") cropId: String): Response<CropDto>

    @GET("crops/categories")
    suspend fun getCropCategories(): Response<List<CropCategoryDto>>

    @GET("api/farms/{farmId}/crops")
    suspend fun getFarmCrops(@Path("farmId") farmId: String): Response<List<CropEntity>>

    @PUT("api/farms/{farmId}/crops")
    suspend fun updateFarmCrops(@Path("farmId") farmId: String,  @Body request: UpdateFarmCropsRequest ): Response<List<FarmCropDto>>

    @POST("api/farms/{farmId}/crops")
    suspend fun addCropToFarm(@Path("farmId") farmId: String,  @Body request: AddCropToFarmRequest ): Response<FarmCropDto>

    @DELETE("api/farms/{farmId}/crops/{farmCropId}")
    suspend fun removeCropFromFarm(@Path("farmId") farmId: String,  @Path("farmCropId") farmCropId: String  ): Response<Unit>

    @PATCH("api/farm-crops/{farmCropId}/status")
    suspend fun updateCropStatus(@Path("farmCropId") farmCropId: String,  @Body request: UpdateCropStatusRequest): Response<FarmCropDto>

    @GET("api/farms/{farmId}/crops/stats")
    suspend fun getFarmCropStats(@Path("farmId") farmId: String): Response<CropStatsDto>

    @GET("api/farms/{farmId}/crops/recommendations")
    suspend fun getRecommendedCrops(@Path("farmId") farmId: String,  @Query("soilType") soilType: String?,  @Query("waterAvailability") waterAvailability: String? ): Response<List<CropDto>>

    @POST("sync/upload")
    suspend fun syncUpload(@Body data: SyncDataDto): Response<SyncResponseDto>

    @GET("sync/download")
    suspend fun syncDownload( @Query("userId") userId: String, @Query("lastSync") lastSyncTimestamp: Long ): Response<SyncDataDto>


    @GET("api/v1/market/summary")
    suspend fun getMarketSummary( @Query("location") location: String ): Response<MarketSummary>
    // ==================== TASKS ====================

    @GET("crops/{cropId}/tasks")
    suspend fun getTasksByCropId(@Path("cropId") cropId: String ): Response<List<TaskEntity>>

    // ✅ Get tasks by farmId
    @GET("farms/{farmId}/tasks")
    suspend fun getTasksByFarmId(@Path("farmId") farmId: String): Response<List<TaskEntity>>

    // ✅ Get tasks by both farmId and cropId
    @GET("farms/{farmId}/crops/{cropId}/tasks")
    suspend fun getTasksByFarmAndCrop(@Path("farmId") farmId: String,@Path("cropId") cropId: String): Response<List<TaskEntity>>

    @POST("crops/{taskId}/tasks")
    suspend fun createTask(@Path("taskId") taskId: String,@Body task: TaskEntity): Response<TaskEntity>

    @PUT("tasks/{taskId}")
    suspend fun updateTask(@Path("taskId") taskId: String, @Body task: TaskEntity): Response<TaskEntity>

    @DELETE("tasks/{taskId}")
    suspend fun deleteTask( @Path("taskId") taskId: String ): Response<Unit>

    // ✅ ADD THIS METHOD
    @PATCH("tasks/{id}/status")
    suspend fun updateTaskStatus( @Path("id") taskId: String,@Query("isCompleted") isCompleted: Boolean ): Response<Unit>

    @PUT("api/farms/{farmId}/boundaries")
    suspend fun setFarmBoundaries(@Path("farmId") farmId: String, @Body boundaries: List<GeoPoint> ): Response<Unit>

    @GET("api/officers")
    suspend fun getOfficerByCounty(@Query("county") county: String ): Response<List<Officer>>

    // ==================== PIPELINE ====================
    @GET("pipeline")
    suspend fun getPipeline(): Response<PipelineResponse>
}



data class UploadImageResponse(
    @SerializedName("imageUrl") val imageUrl: String
)

// Add these to your DTOs package

data class UpdateFarmBoundariesRequest(
    val boundaries: List<GeoPoint>
)

data class UpdateFarmerPreferencesRequest(
    val language: String,
    val notificationsEnabled: Boolean,
    val measurementUnit: String,
    val currency: String
)

data class AddCertificationRequest(
    val name: String,
    val issuingBody: String,
    val dateIssued: String,
    val expiryDate: String?,
    val certificateNumber: String?,
    val documentUrl: String?
)

data class CertificationDto(
    val id: String,
    val name: String,
    val issuingBody: String,
    val dateIssued: String,
    val expiryDate: String?,
    val certificateNumber: String?,
    val documentUrl: String?
)

data class PasswordChangeRequest(
    val currentPassword: String,
   val newPassword: String
)



