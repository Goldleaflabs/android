# API Services Quick Reference Table

## All Services at a Glance

| # | Service Name | Type | Location | Backend | Status | Key Methods | Files Using |
|---|---|---|---|---|---|---|---|
| 1 | **ApiService** | Interface | `core/.../api/ApiService.kt` | GOLDLEAFAPI | ✅ ACTIVE | login, registerFarmer, createFarm, getTrainingVideos, createBatch (50+) | FarmerRepositoryImpl, VideoRepositoryImpl, CropRepositoryImpl, TaskRepositoryImpl, WeatherRepositoryImpl, etc. |
| 2 | **VideoApiService** | Interface | `core/.../api/VideoApiService.kt` | GOLDLEAFAPI | ❌ UNUSED | getAllVideos, getVideoById, getFeaturedVideos, getVideosByCategory, searchVideos, trackVideoView | TrainingDetailsViewModel (injected but not called) |
| 3 | **AuthApiService** | Interface | `core/.../api/AuthApiService.kt` | GOLDLEAFAPI | ⚠️ MINIMAL | login, refreshToken, getFarmerById | UserSessionManager (minimal use) |
| 4 | **PlantIdentificationApi** | Interface | `core/.../api/PlantIdService.kt` | GOLDLEAFAPI | ✅ ACTIVE | analyzeHealth, identifyPlant | PlantIdService.kt |
| 5 | **AIBackendService** | Interface | `core/.../api/OpenAIService.kt` | GOLDLEAFAPI | ✅ ACTIVE | createCompletion, getFarmingAdvice, analyzePest, getMarketAdvice, optimizeIrrigation, identifyPlant | OpenAIService.kt wrapper |
| 6 | **GeminiApi** | Interface | `feature-advisory/.../GeminiAIService.kt` | GOLDLEAFAPI | ✅ ACTIVE | getRecommendations, askQuestion | GeminiAIService.kt wrapper |
| 7 | **CertificationApiService** | Interface | `feature-certification/.../CertificationApiService.kt` | GOLDLEAFAPI | ✅ ACTIVE | createBatch, getFarmerBatches, getLabTests, getBlockchainRecord, verifyProduct | BatchRepositoryImpl, VerificationRepositoryImpl |
| 8 | **WeatherAPIInterface** | Interface | `core/.../api/WeatherMarketServices.kt` | GOLDLEAFAPI | ✅ ACTIVE | getCurrentWeather, getForecast | WeatherAPIService.kt wrapper |
| 9 | **MarketDataAPIInterface** | Interface | `core/.../api/WeatherMarketServices.kt` | GOLDLEAFAPI | ✅ ACTIVE | getCropPrices, getMarketInsights | MarketDataService.kt wrapper |
| 10 | **PlantIdApiService** | Interface | `feature-advisory/.../PlantIdApiService.kt` | EXTERNAL | ❌ UNUSED | identifyPlant | None |
| 11 | **OpenAIService** | Wrapper | `core/.../api/OpenAIService.kt` | GOLDLEAFAPI | ✅ ACTIVE | createCompletion, getFarmingAdvice, analyzePest, getMarketAdvice | PestDetectionViewModel, SoilAnalysisViewModel, DiseaseDetectionViewModel, AIServiceRepository |
| 12 | **WeatherAPIService** | Wrapper | `core/.../api/WeatherMarketServices.kt` | GOLDLEAFAPI | ✅ ACTIVE | getCurrentWeather, getWeatherForecast | AIServiceRepository, AgriculturalIntelligenceService |
| 13 | **MarketDataService** | Wrapper | `core/.../api/WeatherMarketServices.kt` | GOLDLEAFAPI | ✅ ACTIVE | getMarketPrices, getMarketInsights | AIServiceRepository, AgriculturalIntelligenceService |
| 14 | **PlantIdService** | Wrapper | `core/.../api/PlantIdService.kt` | GOLDLEAFAPI | ✅ ACTIVE | identifyPlantHealthIssues, identifyPlant | AdvisoryViewModel |
| 15 | **GeminiAIService** | Wrapper | `feature-advisory/.../GeminiAIService.kt` | GOLDLEAFAPI | ✅ ACTIVE | generateFarmingRecommendations, askFarmingQuestion | (Internal service) |

---

## Service Usage Breakdown

### ✅ ACTIVELY USED SERVICES (Primary Backend Calls)

#### 1. ApiService - 50+ Endpoints
```
POST   auth/login
POST   auth/send-otp
POST   auth/verify-otp
POST   auth/reset-password
POST   farmers/register
POST   farmers/farms
POST   training/videos
GET    farmers/{id}
GET    crops
GET    farms/{farmId}/crops
POST   api/farms/{farmId}/crops
GET    weather/current
GET    weather/forecast
... (40+ more endpoints)
```

**Files Using:** 12+ repositories and ViewModels across all feature modules

#### 2. PlantIdentificationApi (2 Endpoints)
```
POST   api/ai/plant/health-assessment     (multipart image upload)
POST   api/ai/plant/identify              (multipart image upload)
```

**Files Using:** PlantIdService.kt wrapper → AdvisoryViewModel

#### 3. AIBackendService (6 Endpoints)
```
POST   api/ai/completion
POST   api/ai/farming-advice
POST   api/ai/analyze-pest
POST   api/ai/market-advice
POST   api/ai/optimize-irrigation
POST   api/ai/identify-plant
```

**Files Using:** OpenAIService wrapper → Multiple ViewModels & Repositories

#### 4. GeminiApi (2 Endpoints)
```
POST   api/ai/gemini/recommendations
POST   api/ai/gemini/question
```

**Files Using:** GeminiAIService wrapper

#### 5. CertificationApiService (5 Endpoints)
```
POST   batches
GET    batches/farmer/{farmerId}
GET    batches/{batchId}/lab-tests
GET    blockchain/{batchId}
GET    verify/{batchNumber}
```

**Files Using:** BatchRepositoryImpl, VerificationRepositoryImpl

#### 6. WeatherAPIInterface (2 Endpoints)
```
GET    api/weather/current
GET    api/weather/forecast
```

**Files Using:** WeatherAPIService wrapper → Advisory services

#### 7. MarketDataAPIInterface (2 Endpoints)
```
GET    api/market/prices
GET    api/market/insights
```

**Files Using:** MarketDataService wrapper → Advisory services

---

### ⚠️ MINIMAL/UNUSED SERVICES

#### AuthApiService
- Minimal use - authentication handled primarily by ApiService
- Used only in: UserSessionManager.kt
- Note: Redundant with ApiService.login()

#### VideoApiService
- **Status:** Defined but NOT USED
- Methods for training videos exist but ApiService.getTrainingVideos() is used instead
- Injected in TrainingDetailsViewModel but not called

#### PlantIdApiService
- **Status:** Completely unused
- Appears to be placeholder for external plant.id API integration
- No files currently using it

---

## Key Usage Patterns

### 🔄 Wrapper Service Pattern (Used Extensively)

All services follow this pattern:
```
UI Layer (ViewModel)
    ↓
Repository/Service Layer
    ↓
Wrapper Service (OpenAIService, WeatherAPIService, etc.)
    ↓ (error handling, token management, response transformation)
    ↓
Retrofit Interface (AIBackendService, WeatherAPIInterface, etc.)
    ↓ (HTTP calls)
    ↓
GOLDLEAFAPI Backend (goldleaflabs.co.ke)
```

### 🎯 Endpoint Base URL
All services target: `https://goldleaflabs.co.ke` (inferred from module structure)

### 🔐 Authentication
Most endpoints require Bearer token in Authorization header

---

## Feature Module Distribution

| Feature Module | Services Used | Primary Endpoints |
|---|---|---|
| **feature-farmer-management** | ApiService | auth/*, farmers/*, farms/* |
| **feature-crop-management** | ApiService | crops/*, tasks/*, growth-stages/* |
| **feature-training-extension** | ApiService, VideoApiService (unused) | training/videos/* |
| **feature-weather-climate** | ApiService, WeatherAPIInterface | weather/* |
| **feature-advisory-services** | ApiService, AIBackendService, GeminiApi, WeatherAPIInterface, MarketDataAPIInterface, PlantIdService | ai/*, gemini/*, market/*, weather/* |
| **feature-certification-quality** | CertificationApiService | batches/*, certification/* |

---

## Endpoints Mapped to Backend

### All ~80 endpoints in ApiService point to: goldleaflabs.co.ke

**Categories:**
- **Authentication** (8 endpoints): login, register, OTP, password reset
- **Farmer Management** (10 endpoints): profile, dashboard, preferences
- **Farm Management** (8 endpoints): CRUD operations, boundaries, crops
- **Crop Management** (12 endpoints): CRUD, categories, stats, recommendations
- **Growth Stages** (3 endpoints): get, create, update
- **Training** (7 endpoints): videos, categories, progress, certificates
- **Tasks** (6 endpoints): CRUD, status updates
- **Weather** (3 endpoints): current, forecast, alerts
- **Market** (4 endpoints): prices, trends, buyers, connections
- **Certification** (15+ endpoints): batches, requirements, audits, quality
- **Advisory** (4 endpoints): recommendations, pest ID, soil analysis, experts
- **Notifications** (3 endpoints): get, mark read, mark all read
- **QR/Blockchain** (6 endpoints): generate, verify, create records

---

## Recommendations Summary

| Item | Priority | Action |
|---|---|---|
| Remove VideoApiService | HIGH | Consolidate with ApiService.getTrainingVideos() |
| Remove PlantIdApiService | MEDIUM | Delete if external API integration not planned |
| Deprecate AuthApiService | HIGH | Use ApiService.login() instead |
| Add API versioning | MEDIUM | Implement v1, v2 endpoint support |
| Enhance error handling | MEDIUM | Better error messages in wrapper services |
| Add request/response logging | LOW | Debug logging for API calls |
| Document external APIs | LOW | Note which services call GOLDLEAFAPI vs other services |

