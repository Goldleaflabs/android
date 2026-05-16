package com.goldleaf.core.network

// ==================== AUTH DTOs ====================
data class LoginRequest(
    val email: String,
    val password: String
)


data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val role: String
)



data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val isVerified: Boolean
)

// ==================== FARMER DTOs ====================
data class FarmerResponse(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val location: String?,
    val totalFarmSize: Double,
    val activeCrops: Int,
    val farms: List<FarmResponse>
)

data class FarmResponse(
    val id: String,
    val name: String,
    val size: Double,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val soilType: String?,
    val crops: List<String>
)

data class FarmCreateRequest(
    val name: String,
    val size: Double,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val soilType: String?
)

// ==================== CERTIFICATION DTOs ====================
data class CertificationResponse(
    val id: String,
    val farmerId: String,
    val farmId: String,
    val certificationType: String,
    val status: String,
    val applicationDate: String,
    val expiryDate: String?,
    val certificateUrl: String?
)

data class CertificationRequest(
    val farmerId: String,
    val farmId: String,
    val certificationType: String
)

data class RequirementResponse(
    val id: String,
    val certificationId: String,
    val requirement: String,
    val isCompliant: Boolean,
    val evidenceUrl: String?,
    val notes: String?
)

data class RequirementUpdateRequest(
    val isCompliant: Boolean,
    val evidenceUrl: String?,
    val notes: String?
)

data class EvidenceRequest(
    val requirementId: String,
    val evidenceUrl: String,
    val notes: String?
)

data class EvidenceResponse(
    val id: String,
    val requirementId: String,
    val evidenceUrl: String,
    val uploadedAt: String
)

data class AuditResponse(
    val id: String,
    val certificationId: String,
    val auditDate: String,
    val auditorName: String,
    val findings: List<String>,
    val passed: Boolean
)

data class AuditRequest(
    val certificationId: String,
    val auditDate: String,
    val auditorName: String,
    val findings: List<String>,
    val passed: Boolean
)

// ==================== BATCH/QUALITY DTOs ====================
data class BatchResponse(
    val id: String,
    val batchNumber: String,
    val farmerId: String,
    val farmId: String,
    val cropType: String,
    val variety: String,
    val harvestDate: String,
    val quantity: Double,
    val unit: String,
    val qualityGrade: String?,
    val status: String,
    val qrCode: String?,
    val blockchainHash: String?
)

data class BatchCreateRequest(
    val farmerId: String,
    val farmId: String,
    val cropType: String,
    val variety: String,
    val harvestDate: String,
    val quantity: Double,
    val unit: String
)

data class QualityParameterResponse(
    val id: String,
    val batchId: String,
    val parameterName: String,
    val value: String,
    val unit: String,
    val isCompliant: Boolean
)

data class QualityCheckRequest(
    val batchId: String,
    val parameters: Map<String, String>
)

data class QualityCheckResponse(
    val batchId: String,
    val qualityGrade: String,
    val parameters: List<QualityParameterResponse>,
    val passed: Boolean
)

data class LabTestResponse(
    val id: String,
    val batchId: String,
    val labName: String,
    val testType: String,
    val testDate: String,
    val result: String,
    val certificateUrl: String?
)

data class LabTestRequest(
    val batchId: String,
    val labName: String,
    val testType: String,
    val testDate: String
)

data class GradeUpdateRequest(
    val qualityGrade: String,
    val qualityScore: Double
)

// ==================== JOURNEY DTOs ====================
data class JourneyResponse(
    val batchId: String,
    val events: List<JourneyEventResponse>
)

data class JourneyEventResponse(
    val id: String,
    val batchId: String,
    val stage: String,
    val location: String,
    val timestamp: String,
    val actorName: String,
    val activity: String,
    val verified: Boolean
)

data class JourneyEventRequest(
    val batchId: String,
    val stage: String,
    val location: String,
    val actorName: String,
    val activity: String
)

// ==================== BLOCKCHAIN DTOs ====================
data class BlockchainRegisterRequest(
    val batchId: String,
    val farmerId: String,
    val productType: String,
    val harvestDate: String,
    val quantity: Double
)

data class BlockchainRegisterResponse(
    val success: Boolean,
    val transactionHash: String,
    val blockNumber: Long,
    val timestamp: String
)

data class BlockchainVerifyResponse(
    val verified: Boolean,
    val batchId: String,
    val transactionHash: String,
    val timestamp: String
)

data class BlockchainRecordResponse(
    val batchId: String,
    val transactionHash: String,
    val blockNumber: Long,
    val farmerId: String,
    val productType: String,
    val harvestDate: String,
    val quantity: Double,
    val timestamp: String,
    val verified: Boolean
)

// ==================== QR CODE DTOs ====================
data class QRGenerateRequest(
    val batchId: String
)

data class QRGenerateResponse(
    val qrCodeUrl: String,
    val batchNumber: String,
    val verificationUrl: String
)

data class ProductVerificationResponse(
    val verified: Boolean,
    val batchNumber: String,
    val productName: String,
    val farmerName: String,
    val farmLocation: String,
    val harvestDate: String,
    val certifications: List<String>,
    val blockchainVerified: Boolean,
    val transactionHash: String?
)
