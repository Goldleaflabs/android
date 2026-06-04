package com.goldleaf.core.data.dto.auth

import com.goldleaf.core.data.dto.crop.CropDto
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmLocation
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.core.data.local.AdvisoryEntity
import com.goldleaf.core.data.local.AuditRecordEntity
import com.goldleaf.core.data.local.BlockchainRecordEntity
import com.goldleaf.core.data.local.CertificationEntity
import com.goldleaf.core.data.local.CertificationRequirementEntity
import com.goldleaf.core.data.local.CropActivity
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropMasterEntity
import com.goldleaf.core.data.local.CropMonitoringRecordEntity
import com.goldleaf.core.data.local.CropTaskEntity
import com.goldleaf.core.data.local.CropVarietyEntity
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.FarmerEntity
import com.goldleaf.core.data.local.GrowthStageEntity
import com.goldleaf.core.data.local.HarvestRecordEntity
import com.goldleaf.core.data.local.JourneyEventEntity
import com.goldleaf.core.data.local.LabTestEntity
import com.goldleaf.core.data.local.MarketPriceEntity
import com.goldleaf.core.data.local.MonitoringRecordEntity
import com.goldleaf.core.data.local.Officer
import com.goldleaf.core.data.local.ProductBatchEntity
import com.goldleaf.core.data.local.ProductJourneyEntity
import com.goldleaf.core.data.local.QualityParameterEntity
import com.goldleaf.core.data.local.ComplianceChecklistEntity
import com.goldleaf.core.data.local.PlotEntity
import com.goldleaf.core.data.local.PaymentEntity
import com.goldleaf.core.data.local.HarvestDeliveryEntity
import com.goldleaf.core.data.local.BatchSalesEntity
import com.goldleaf.core.data.local.DeductionEntity
import com.goldleaf.core.data.local.FarmerPayoutInfoEntity
import com.goldleaf.core.data.local.SeasonalPlanEntity
import com.goldleaf.core.data.local.SoilTestEntity
import com.goldleaf.core.data.local.TaskEntity
import com.goldleaf.core.data.local.WeatherEntity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// =====================================================
// LOGIN
// =====================================================

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("farmer")
    val farmer: FarmerDto?,

    @SerializedName("token")
    val token: String?
)

// =====================================================
// REGISTER
// =====================================================

data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("farmName")
    val farmName: String,

    @SerializedName("role")
    val role: String
)

data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("farmer")
    val farmer: FarmerDto?,

    @SerializedName("token")
    val token: String?
)

// =====================================================
// FORGOT PASSWORD
// =====================================================

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class ForgotPasswordResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

// =====================================================
// VERIFY OTP
// =====================================================

data class VerifyOTPRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("otp")
    val otp: String
)



data class VerifyOTPResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("resetToken")
    val resetToken: String?
)

// =====================================================
// RESET PASSWORD
// =====================================================

data class ResetPasswordRequest(
    @SerializedName("resetToken")
    val resetToken: String,

    @SerializedName("newPassword")
    val newPassword: String
)

data class ResetPasswordResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

// =====================================================
// VERIFY PHONE
// =====================================================

data class VerifyPhoneRequest(
    @SerializedName("phone")
    val phone: String
)

data class VerifyPhoneResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("otp")
    val otp: String? // Present in development, remove in production
)

// =====================================================
// FARMER DTO (Used in responses)
// =====================================================


data class FarmerDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    // from first DTO
    @SerializedName("farmName")
    val farmName: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("district")
    val district: String? = null,

    @SerializedName("region")
    val region: String? = null,

    @SerializedName("idNumber")
    val idNumber: String? = null,

    // from second DTO (make optional because older / other endpoint might not include them)
    @SerializedName("totalFarmSize")
    val totalFarmSize: Double? = null,

    @SerializedName("activeCrops")
    val activeCrops: Int? = null,

    @SerializedName("profileImage")
    val profileImage: String? = null,

    @SerializedName("farms")
    val farms: List<FarmDto>? = null
)

// =====================================================
// COMMON RESPONSE (For endpoints that don't return data)
// =====================================================

data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)


data class OTPRequest(
    val phoneNumber: String,
    val action: OTPAction = OTPAction.REGISTRATION
)

data class OTPVerificationRequest(
    val phoneNumber: String,
    val otpCode: String,
    val action: OTPAction = OTPAction.REGISTRATION
)

enum class OTPAction {
    REGISTRATION, LOGIN, PASSWORD_RESET, PHONE_UPDATE
}
//////////////////////////////////////////////////////////////

// ==================== EXISTING DATA MODELS ====================
// Authentication

data class RefreshTokenRequest(val refreshToken: String)

data class AuthResponse(val user: UserDto, val accessToken: String, val refreshToken: String)

data class TokenResponse(val accessToken: String, val refreshToken: String)


data class MarketSummary(
    val location: String,
    val marketStatus: String,
    val activePrices: Int,
    val buyersOnline: Int,
    val lastUpdate: String
)

// User/Farmer
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileImage: String?
)



data class DashboardDto(
    val totalFarms: Int,
    val totalCrops: Int,
    val activeTasks: Int,
    val upcomingEvents: List<EventDto>,
    val recommendations: List<RecommendationDto>,
    val weatherAlert: WeatherAlertDto?
)


data class FarmDto(
    val id: String,
    val name: String,
    val totalSize: Double,
    val location: LocationDto,
    val boundaries: List<LocationDto>,
    val crops: List<CropDto>,
    val farmerId: String
) {

    // 1. Convert API Data -> Database Table (Entity)
    fun toEntity(farmerId: String): FarmEntity {
        return FarmEntity(
            id = this.id,
            name = this.name,
            size = this.totalSize,
            latitude = this.location.latitude,
            longitude = this.location.longitude,
            boundaries = Gson().toJson(this.boundaries),
            farmerId = farmerId,
            location = Gson().toJson(this.location)
        )
    }

    // 2. Convert API Data -> UI Model (Domain)
    fun toDomain(): Farm {
        return Farm(
            id = this.id,
            name = this.name,
            totalSize = this.totalSize,
            // No need for JSON here; use the object directly from the DTO
            location = FarmLocation(
                centerLatitude = this.location.latitude,
                centerLongitude = this.location.longitude
            ),
            // Map the List<LocationDto> to List<GeoPoint>
            boundaries = this.boundaries.map {
                GeoPoint(it.latitude, it.longitude)
            }
        )
    }


}

data class FarmCreateRequest(
    val name: String,
    val size: Double,
    val locationName: String? = null,
    val region: String? = null,
    val documentReference: String? = null,
    val location: LocationDto,
    val boundaries: List<LocationDto>
)

data class LocationDto(val latitude: Double, val longitude: Double)


data class CropCreateRequest(
    val name: String,
    val variety: String?,
    val plantingDate: String,
    val expectedHarvestDate: String,
    val area: Double,
    val farmId: String
)

data class CropUpdateRequest(
    val status: String?,
    val expectedHarvestDate: String?,
    val area: Double?
)

data class CropMonitoringDto(
    val cropId: String,
    val growthStage: String,
    val healthStatus: String,
    val soilMoisture: Double?,
    val temperature: Double?,
    val lastUpdated: String
)

data class CropActivityRequest(
    val type: String,
    val description: String,
    val date: String,
    val cost: Double?
)

data class CropActivityDto(
    val id: String,
    val type: String,
    val description: String,
    val date: String,
    val cost: Double?
)

// Weather
data class WeatherDto(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val rainfall: Double,
    val condition: String,
    val icon: String,
    val timestamp: String,
    val feelsLike: Double,
    val precipitation: Double,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Int,
    val cloudCover: Int,
    val sunrise: String,
    val sunset: String,
)

data class WeatherForecastDto(
    val location: LocationDto,
    val forecast: List<DailyForecastDto>,
    val hourly: List<HourlyForecastDto> = emptyList()  // Add this
)
data class HourlyForecastDto(
    val time: String,
    val temperature: Double,
    val condition: String,
    val precipitation: Double
)

data class DailyForecastDto(
    val date: String,
    val tempMax: Double,
    val tempMin: Double,
    val condition: String,
    val icon: String,
    val rainfall: Double?
)




data class WeatherAlertDto(
    val id: String,
    val type: String,
    val severity: String,
    val title: String,
    val description: String,
    val validFrom: String,
    val validTo: String
)

// Market
data class MarketPriceDto(
    val commodity: String,
    val price: Double,
    val unit: String,
    val currency: String,
    val market: String,
    val date: String,
    val trend: String
)

data class BuyerDto(
    val id: String,
    val name: String,
    val commodities: List<String>,
    val location: String,
    val phone: String,
    val rating: Float,
    val verified: Boolean
)

data class BuyerConnectionRequest(
    val farmerId: String,
    val buyerId: String,
    val commodity: String,
    val quantity: Double,
    val message: String?
)

data class ConnectionDto(
    val id: String,
    val status: String,
    val createdAt: String
)

data class MarketTrendsDto(
    val commodity: String,
    val priceHistory: List<PricePointDto>,
    val analysis: String
)

data class PricePointDto(val date: String, val price: Double)

// Training
data class VideoDto(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: Int,
    val category: String,
    val instructor: String,
    val rating: Float,
    val views: Int
)

data class VideoListResponse(
    val videos: List<VideoDto>,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int
)

data class CategoryDto(
    val id: String,
    val name: String,
    val description: String,
    val videoCount: Int,
    val icon: String
)

data class ProgressUpdateRequest(
    val userId: String,
    val videoId: String,
    val watchedDuration: Int,
    val completed: Boolean
)

data class ProgressDto(
    val userId: String,
    val videoId: String,
    val watchedDuration: Int,
    val totalDuration: Int,
    val completed: Boolean,
    val lastWatched: String
)

data class CertificateDto(
    val id: String,
    val courseTitle: String,
    val completionDate: String,
    val certificateUrl: String
)

data class EnrollmentRequest(
    val userId: String,
    val courseId: String
)

data class EnrollmentDto(
    val id: String,
    val userId: String,
    val courseId: String,
    val enrolledAt: String,
    val status: String
)

// Advisory
data class RecommendationDto(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val priority: String,
    val actionItems: List<String>,
    val deadline: String?
)

data class PestIdentificationRequest(
    val image: String,
    val cropType: String?,
    val location: LocationDto?
)

data class PestIdentificationDto(
    val pestName: String,
    val confidence: Float,
    val description: String,
    val treatment: List<String>,
    val prevention: List<String>
)

data class SoilAnalysisRequest(
    val farmId: String,
    val sampleData: Map<String, Double>
)

data class SoilAnalysisDto(
    val ph: Double,
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val recommendations: List<String>
)

data class ExpertDto(
    val id: String,
    val name: String,
    val specialization: String,
    val rating: Float,
    val available: Boolean,
    val hourlyRate: Double?
)

data class ConsultationRequest(
    val farmerId: String,
    val expertId: String,
    val topic: String,
    val preferredDate: String,
    val notes: String?
)

data class ConsultationDto(
    val id: String,
    val status: String,
    val scheduledAt: String
)

// Notifications
data class NotificationDto(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String,
    val actionUrl: String?
)

// Basic Certification (Legacy)
data class CertificationRequirementsDto(
    val type: String,
    val requirements: List<String>,
    val documents: List<String>,
    val inspectionRequired: Boolean,
    val cost: Double,
    val duration: String
)

data class CertificationApplicationRequest(
    val farmerId: String,
    val type: String,
    val documents: List<String>
)

data class CertificationDto(
    val id: String,
    val farmerId: String,
    val type: String,
    val status: String,
    val appliedDate: String,
    val approvedDate: String?,
    val certificateUrl: String?
)

// Sync
data class SyncDataDto(
    val farmerId: String? = null,
    val farmId: String? = null,
    val farmers: List<FarmerEntity>? = null,
    val farms: List<FarmEntity>? = null,
    val crops: List<CropEntity>? = null,
    val tasks: List<TaskEntity>? = null,
    val advisories: List<AdvisoryEntity>? = null,
    val marketPrices: List<MarketPriceEntity>? = null,
    val weather: List<WeatherEntity>? = null,
    val certifications: List<CertificationEntity>? = null,
    val certificationRequirements: List<CertificationRequirementEntity>? = null,
    val auditRecords: List<AuditRecordEntity>? = null,
    val productBatches: List<ProductBatchEntity>? = null,
    val qualityParameters: List<QualityParameterEntity>? = null,
    val labTests: List<LabTestEntity>? = null,
    val blockchainRecords: List<BlockchainRecordEntity>? = null,
    val productJourneys: List<ProductJourneyEntity>? = null,
    val journeyEvents: List<JourneyEventEntity>? = null,
    val soilTests: List<SoilTestEntity>? = null,
    val monitoringRecords: List<MonitoringRecordEntity>? = null,
    val growthStages: List<GrowthStageEntity>? = null,
    val cropVarieties: List<CropVarietyEntity>? = null,
    val cropTasks: List<CropTaskEntity>? = null,
    val cropMonitoringRecords: List<CropMonitoringRecordEntity>? = null,
    val cropActivities: List<CropActivity>? = null,
    val activities: List<CropActivity>? = null, // legacy alias
    val officers: List<Officer>? = null,
    val harvestRecords: List<HarvestRecordEntity>? = null,
    val cropMaster: List<CropMasterEntity>? = null,
    @SerializedName("seasonal_plans")
    val seasonalPlans: List<SeasonalPlanEntity>? = null,
    @SerializedName("compliance_checklist")
    val complianceChecklist: List<ComplianceChecklistEntity>? = null,
    @SerializedName("plots")
    val plots: List<PlotEntity>? = null,
    val payments: List<PaymentEntity>? = null,
    val harvestDeliveries: List<HarvestDeliveryEntity>? = null,
    val batchSales: List<BatchSalesEntity>? = null,
    val farmerDeductions: List<DeductionEntity>? = null,
    val farmerPayoutInfo: List<FarmerPayoutInfoEntity>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncResponseDto(
    val success: Boolean,
    val message: String,
    val syncedItems: Int,
    val lastSyncTimestamp: Long
)

data class EventDto(
    val id: String,
    val title: String,
    val date: String,
    val type: String
)

// ==================== NEW CERTIFICATION & QUALITY MODELS ====================

// Certification Models
data class CertificationResponseDto(
    val id: String,
    val farmerId: String,
    val farmId: String,
    val certificationType: String,
    val certificationBody: String,
    val status: String,
    val applicationDate: String,
    val inspectionDate: String?,
    val issueDate: String?,
    val expiryDate: String?,
    val certificateNumber: String?,
    val certificateUrl: String?,
    val inspectorName: String?,
    val inspectorContact: String?,
    val score: Float?,
    val nonCompliances: List<String>,
    val correctiveActions: List<String>,
    val nextAuditDate: String?,
    val notes: String
)

data class CertificationCreateRequest(
    val farmerId: String,
    val farmId: String,
    val certificationType: String,
    val certificationBody: String
)

data class CertificationUpdateRequest(
    val status: String?,
    val inspectionDate: String?,
    val issueDate: String?,
    val expiryDate: String?,
    val certificateNumber: String?,
    val score: Float?,
    val notes: String?
)

data class RequirementDto(
    val id: String,
    val certificationId: String,
    val category: String,
    val requirementCode: String,
    val requirement: String,
    val isCompliant: Boolean,
    val evidenceType: String,
    val evidenceUrl: String?,
    val notes: String,
    val verifiedBy: String?,
    val verifiedDate: String?,
    val priority: String
)

data class RequirementUpdateRequest(
    val isCompliant: Boolean,
    val evidenceUrl: String?,
    val notes: String?
)

data class EvidenceUploadResponse(
    val evidenceUrl: String,
    val uploadedAt: String
)

data class AuditRecordDto(
    val id: String,
    val certificationId: String,
    val auditType: String,
    val auditorName: String,
    val auditorOrganization: String,
    val auditDate: String,
    val findings: List<AuditFindingDto>,
    val overallScore: Float,
    val passed: Boolean,
    val reportUrl: String?,
    val followUpDate: String?
)

data class AuditFindingDto(
    val findingType: String,
    val description: String,
    val correctiveAction: String,
    val deadline: String
)

data class AuditRecordRequest(
    val certificationId: String,
    val auditType: String,
    val auditorName: String,
    val auditorOrganization: String,
    val auditDate: String,
    val overallScore: Float,
    val passed: Boolean,
    val findings: List<AuditFindingDto>
)

data class InspectionScheduleRequest(
    val inspectionDate: String,
    val inspectorName: String,
    val inspectorContact: String
)

data class InspectionDto(
    val id: String,
    val certificationId: String,
    val inspectionDate: String,
    val status: String
)

// Quality Models
data class ProductBatchDto(
    val id: String,
    val batchNumber: String,
    val farmerId: String,
    val farmId: String,
    val cropType: String,
    val variety: String,
    val harvestDate: String,
    val quantity: Float,
    val unit: String,
    val qualityGrade: String,
    val qualityScore: Float,
    val qrCode: String,
    val blockchainHash: String?,
    val status: String,
    val storageLocation: String?,
    val certifications: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class BatchCreateRequest(
    val farmerId: String,
    val farmId: String,
    val cropType: String,
    val variety: String,
    val harvestDate: String,
    val quantity: Float,
    val unit: String,
    val storageLocation: String?
)

data class BatchUpdateRequest(
    val qualityGrade: String?,
    val qualityScore: Float?,
    val status: String?,
    val storageLocation: String?
)

data class QualityParameterDto(
    val id: String,
    val batchId: String,
    val parameterName: String,
    val value: String,
    val unit: String,
    val standardMin: Float?,
    val standardMax: Float?,
    val isCompliant: Boolean,
    val testMethod: String?,
    val testedBy: String?,
    val testDate: String
)

data class QualityParameterRequest(
    val batchId: String,
    val parameterName: String,
    val value: String,
    val unit: String,
    val testMethod: String?,
    val testedBy: String?
)

data class LabTestDto(
    val id: String,
    val batchId: String,
    val labName: String,
    val testType: String,
    val testDate: String,
    val resultDate: String?,
    val result: String,
    val parameters: List<QualityParameterDto>,
    val certificateUrl: String?,
    val notes: String
)

data class LabTestRequest(
    val batchId: String,
    val labName: String,
    val testType: String,
    val testDate: String,
    val notes: String?
)

data class LabTestUpdateRequest(
    val resultDate: String?,
    val result: String?,
    val parameters: List<QualityParameterRequest>?,
    val notes: String?
)

// Blockchain Models
data class BlockchainRecordDto(
    val id: String,
    val batchId: String,
    val blockHash: String,
    val transactionHash: String,
    val timestamp: String,
    val data: Map<String, String>,
    val previousHash: String?,
    val verified: Boolean
)

data class BlockchainCreateRequest(
    val batchId: String,
    val data: Map<String, String>
)

data class BlockchainVerifyRequest(
    val blockHash: String
)

data class BlockchainVerificationDto(
    val verified: Boolean,
    val blockHash: String,
    val timestamp: String
)

// Journey Models
data class JourneyPointDto(
    val id: String,
    val batchId: String,
    val stage: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val actorName: String,
    val actorType: String,
    val activity: String,
    val photoUrl: String?,
    val notes: String,
    val temperature: Float?,
    val verified: Boolean
)

data class JourneyPointRequest(
    val batchId: String,
    val stage: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val actorName: String,
    val actorType: String,
    val activity: String,
    val photoUrl: String?,
    val notes: String?,
    val temperature: Float?
)

// QR Code Models
data class QRCodeGenerateRequest(
    val batchId: String
)

data class QRCodeResponse(
    val qrCodeUrl: String,
    val qrCodeData: String,
    val verificationUrl: String
)

data class QRCodeDataDto(
    val batchId: String,
    val batchNumber: String,
    val cropType: String,
    val farmerName: String,
    val harvestDate: String,
    val verificationUrl: String,
    val certifications: List<String>
)

// Consumer Verification Models
data class ConsumerVerificationDto(
    val batchNumber: String,
    val cropName: String,
    val variety: String,
    val farmerName: String,
    val farmLocation: String,
    val harvestDate: String,
    val qualityGrade: String,
    val certifications: List<CertificationBadgeDto>,
    val journey: List<JourneyPointDto>,
    val testResults: List<LabTestDto>,
    val farmerPhoto: String?,
    val farmPhotos: List<String>,
    val verified: Boolean,
    val blockchainVerified: Boolean
)

data class CertificationBadgeDto(
    val type: String,
    val certificateNumber: String,
    val issueDate: String,
    val expiryDate: String,
    val badgeUrl: String
)

data class PublicJourneyDto(
    val batchNumber: String,
    val cropType: String,
    val journey: List<JourneyPointDto>,
    val verificationUrl: String
)

// Dashboard Models
data class CertificationDashboardDto(
    val activeCertifications: List<CertificationResponseDto>,
    val expiringSoon: List<CertificationResponseDto>,
    val pendingInspections: List<CertificationResponseDto>,
    val complianceScore: Float,
    val totalBatches: Int,
    val qualityScore: Float,
    val recentTests: List<LabTestDto>
)

data class QualityDashboardDto(
    val totalBatches: Int,
    val averageQualityScore: Float,
    val gradeDistribution: Map<String, Int>,
    val recentBatches: List<ProductBatchDto>,
    val pendingTests: List<LabTestDto>,
    val blockchainRecords: Int
)

data class QualityAnalyticsDto(
    val period: String,
    val batchesByGrade: Map<String, Int>,
    val qualityTrend: List<QualityTrendPoint>,
    val topPerformingCrops: List<CropPerformanceDto>,
    val complianceRate: Float
)

data class QualityTrendPoint(
    val date: String,
    val averageScore: Float
)

data class CropPerformanceDto(
    val cropType: String,
    val batchCount: Int,
    val averageScore: Float,
    val averageGrade: String
)

// ==================== PAYMENT / REVENUE / NOTIFICATION DTOs ====================

data class FarmerRevenueResponse(
    val success: Boolean,
    val data: FarmerRevenueData?
)

data class FarmerRevenueData(
    val totalPaid: Double,
    val successfullyPaid: Double,
    val totalDeclaredKg: Double,
    val totalConfirmedKg: Double,
    val totalDeductions: Double,
    val recentPayments: List<PaymentEntity>
)

data class FarmerPayoutInfoResponse(
    val success: Boolean,
    val data: FarmerPayoutInfoEntity?
)

data class NotificationListResponse(
    val success: Boolean,
    val data: List<NotificationDto>? = null
)
