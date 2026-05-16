# API Services Analysis - GoldLeaf Labs Android Project

## Summary
This document maps all API services in the GoldLeaf Labs Android project to identify which endpoints are actually being called and which services communicate with the main GOLDLEAFAPI backend vs external services.

---

## 1. ApiService (Core Backend Integration)
**Location:** `core/src/main/java/com/goldleaf/core/data/api/ApiService.kt`  
**Type:** Main backend interface calling GOLDLEAFAPI  
**Base URL:** goldleaflabs.co.ke

### Methods Called (In Use):

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `login()` | POST | `auth/login` | LoginViewModel.kt, FarmerRepositoryImpl.kt |
| `loginFarmer()` | POST | `auth/login` | FarmerRepositoryImpl.kt |
| `registerFarmer()` | POST | `farmers/register` | FarmerRepositoryImpl.kt, RegistrationViewModel.kt |
| `sendOTP()` | POST | `auth/send-otp` | FarmerRepositoryImpl.kt |
| `verifyOTP()` | POST | `auth/verify-otp` | FarmerRepositoryImpl.kt |
| `resetPassword()` | POST | `auth/reset-password` | RecoveryViewModel.kt, FarmerRepositoryImpl.kt |
| `getCurrentWeather()` | GET | `weather/current` | WeatherRepositoryImpl.kt |
| `getWeatherForecast()` | GET | `weather/forecast` | WeatherRepositoryImpl.kt |
| `updateFarmerProfile()` | POST | `farmers/updateFarmerProfile/{id}` | FarmerRepositoryImpl.kt |
| `getFarmerDashboard()` | GET | `farmers/{id}/dashboard` | FarmerRepositoryImpl.kt |
| `getFarmerById()` | GET | `farmers/{id}` | FarmerRepositoryImpl.kt, FarmFencingViewModel.kt |
| `getFarmsByFarmerId()` | GET | `farmers/{farmerId}/farms` | FarmerRepositoryImpl.kt |
| `addFarm()` | POST | `farmers/farms` | FarmerRepositoryImpl.kt |
| `updateFarm()` | POST | `farmers/{id}/farms/{farmId}` | FarmerRepositoryImpl.kt |
| `getFarmById()` | GET | `api/farms/{farmId}` | FarmFencingViewModel.kt, FarmerRepositoryImpl.kt |
| `setFarmBoundaries()` | PUT | `api/farms/{farmId}/boundaries` | FarmerRepositoryImpl.kt |
| `uploadProfileImage()` | POST | `farmers/profile-image` | FarmerRepositoryImpl.kt |
| `getTrainingVideos()` | GET | `training/videos` | VideoRepositoryImpl.kt |
| `getCategories()` | GET | `training/categories` | VideoRepositoryImpl.kt |
| `updateProgress()` | POST | `training/progress` | VideoRepositoryImpl.kt |
| `getUserProgress()` | GET | `training/progress/{userId}` | VideoRepositoryImpl.kt |
| `getAllCrops()` | GET | `crops` | FarmerRepositoryImpl.kt, CropRepositoryImpl.kt |
| `getFarmCrops()` | GET | `api/farms/{farmId}/crops` | FarmerRepositoryImpl.kt |
| `updateFarmCrops()` | PUT | `api/farms/{farmId}/crops` | FarmerRepositoryImpl.kt |
| `addCropToFarm()` | POST | `api/farms/{farmId}/crops` | FarmerRepositoryImpl.kt |
| `removeCropFromFarm()` | DELETE | `api/farms/{farmId}/crops/{farmCropId}` | FarmerRepositoryImpl.kt |
| `updateCropStatus()` | PATCH | `api/farm-crops/{farmCropId}/status` | FarmerRepositoryImpl.kt |
| `getFarmCropStats()` | GET | `api/farms/{farmId}/crops/stats` | FarmerRepositoryImpl.kt |
| `getRecommendedCrops()` | GET | `api/farms/{farmId}/crops/recommendations` | FarmerRepositoryImpl.kt |
| `getCropsByCategory()` | GET | `api/crops/category/{category}` | FarmerRepositoryImpl.kt |
| `searchCrops()` | GET | `api/crops/search` | FarmerRepositoryImpl.kt |
| `getCropById()` | GET | `api/crops/{cropId}` | FarmerRepositoryImpl.kt |
| `getCropCategories()` | GET | `api/crops/categories` | FarmerRepositoryImpl.kt |
| `createCrop()` | POST | `crops` | CropRepositoryImpl.kt |
| `updateCrop()` | PUT | `crops/{id}` | CropRepositoryImpl.kt |
| `deleteCrop()` | DELETE | `crops/{id}` | CropRepositoryImpl.kt |
| `getGrowthStagesByCropId()` | GET | `growth-stages/{cropId}` | GrowthStageRepositoryImpl.kt |
| `updateGrowthStage()` | PUT | `growth-stages/{id}` | GrowthStageRepositoryImpl.kt |
| `createGrowthStage()` | POST | `growth-stages` | GrowthStageRepositoryImpl.kt |
| `getTasksByCropId()` | GET | `crops/{cropId}/tasks` | TaskRepositoryImpl.kt |
| `getTasksByFarmId()` | GET | `farms/{farmId}/tasks` | TaskRepositoryImpl.kt |
| `getTasksByFarmAndCrop()` | GET | `farms/{farmId}/crops/{cropId}/tasks` | TaskRepositoryImpl.kt |
| `createTask()` | POST | `crops/{taskId}/tasks` | TaskRepositoryImpl.kt |
| `updateTaskStatus()` | PATCH | `tasks/{id}/status` | TaskRepositoryImpl.kt |
| `deleteTask()` | DELETE | `tasks/{taskId}` | TaskRepositoryImpl.kt |
| `addCertification()` | POST | `api/farmer/certifications` | FarmerRepositoryImpl.kt |
| `verifyProduct()` | GET | `public/verify/{batchNumber}` | VerificationRepositoryImpl.kt |
| `getCurrentFarmer()` | GET | `api/farmer/current` | FarmerRepositoryImpl.kt |
| `logout()` | POST | `auth/logout` | FarmerRepositoryImpl.kt |
| `getMarketPrices()` | GET | `market/prices` | ApiService.kt |

### Files Using ApiService:
- feature-farmer-management/data/repository/FarmerRepositoryImpl.kt
- feature-farmer-management/ui/FarmSetupViewModel.kt
- feature-farmer-management/ui/auth/login/LoginViewModel.kt
- feature-farmer-management/ui/auth/recovery/RecoveryViewModel.kt
- feature-farmer-management/ui/viewmodels/FarmFencingViewModel.kt
- feature-training-extension/data/repository/VideoRepositoryImpl.kt
- feature-crop-management/data/repository/CropRepositoryImpl.kt
- feature-crop-management/data/repository/TaskRepositoryImpl.kt
- feature-crop-management/data/repository/GrowthStageRepositoryImpl.kt
- feature-crop-management/ui/selection/OfficerRepository.kt
- feature-weather-climate/data/repository/WeatherRepositoryImpl.kt
- feature-advisory-services/data/AdvisoryRepository.kt

---

## 2. AuthApiService
**Location:** `core/src/main/java/com/goldleaf/core/data/api/AuthApiService.kt`  
**Type:** Authentication interface (alternate auth service)

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `login()` | POST | `auth/login` | Potentially unused (wrapped by ApiService) |
| `refreshToken()` | POST | `auth/refresh` | Potentially unused (wrapped by ApiService) |
| `getFarmerById()` | POST | `auth/getFarmerById` | Potentially unused |

### Files Using AuthApiService:
- core/auth/UserSessionManager.kt

---

## 3. VideoApiService
**Location:** `core/src/main/java/com/goldleaf/core/data/api/VideoApiService.kt`  
**Type:** Dedicated video API (appears to be unused - ApiService used instead)

### Methods in Interface (Not Called):

| Method Name | HTTP Verb | Endpoint | Status |
|---|---|---|---|
| `getAllVideos()` | GET | `training/videos` | ❌ Not used - ApiService used instead |
| `getVideoById()` | GET | `training/videos/{id}` | ❌ Not used - ApiService used instead |
| `getFeaturedVideos()` | GET | `training/videos/featured` | ❌ Not used - ApiService used instead |
| `getVideosByCategory()` | GET | `training/videos/category/{category}` | ❌ Not used - ApiService used instead |
| `searchVideos()` | GET | `training/videos/search` | ❌ Not used - ApiService used instead |
| `trackVideoView()` | POST | `training/videos/{id}/view` | ❌ Not used - ApiService used instead |

### Files Using VideoApiService:
- feature-training-extension/ui/TrainingDetailsViewModel.kt (injected but unclear if actually used)

---

## 4. PlantIdentificationApi (PlantIdService)
**Location:** `core/src/main/java/com/goldleaf/core/data/api/PlantIdService.kt`  
**Type:** Plant identification backend API

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `analyzeHealth()` | POST (Multipart) | `api/ai/plant/health-assessment` | PlantIdService.kt |
| `identifyPlant()` | POST (Multipart) | `api/ai/plant/identify` | PlantIdService.kt, AdvisoryViewModel.kt |

### Wrapper Service Methods:
- `identifyPlantHealthIssues()` - wraps `analyzeHealth()`
- `identifyPlant()` - wraps `identifyPlant()`

### Files Using PlantIdService:
- feature-advisory-services/ui/AdvisoryViewModel.kt
- core/data/api/PlantIdService.kt (implementation)

---

## 5. AIBackendService (OpenAIService)
**Location:** `core/src/main/java/com/goldleaf/core/data/api/OpenAIService.kt`  
**Type:** AI completion and farming advice service

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `createCompletion()` | POST | `api/ai/completion` | OpenAIService.kt, AIServiceRepository.kt, AgriculturalIntelligenceService.kt, DiseaseDetectionViewModel.kt, SoilAnalysisViewModel.kt |
| `getFarmingAdvice()` | POST | `api/ai/farming-advice` | OpenAIService.kt |
| `analyzePest()` | POST | `api/ai/analyze-pest` | OpenAIService.kt, PestDetectionViewModel.kt |
| `getMarketAdvice()` | POST | `api/ai/market-advice` | OpenAIService.kt (in AIServiceRepository) |
| `optimizeIrrigation()` | POST | `api/ai/optimize-irrigation` | OpenAIService.kt (potential) |
| `identifyPlant()` | POST | `api/ai/identify-plant` | OpenAIService.kt (potential) |

### Wrapper Service Methods:
- `createCompletion()` - wraps `createCompletion()`
- `getFarmingAdvice()` - wraps `getFarmingAdvice()`
- `analyzePest()` - wraps `analyzePest()`
- `getMarketAdvice()` - wraps `getMarketAdvice()`

### Files Using OpenAIService:
- feature-advisory-services/ui/PestDetectionViewModel.kt
- feature-advisory-services/ui/SoilAnalysisViewModel.kt
- feature-advisory-services/ui/DiseaseDetectionViewModel.kt
- feature-advisory-services/data/AIServiceRepository.kt
- feature-advisory-services/data/AgriculturalIntelligenceService.kt

---

## 6. GeminiApi
**Location:** `feature-advisory-services/src/main/java/com/goldleaf/feature/advisoryservices/data/GeminiAIService.kt`  
**Type:** Gemini AI recommendations service (backend integration)

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `getRecommendations()` | POST | `api/ai/gemini/recommendations` | GeminiAIService.kt |
| `askQuestion()` | POST | `api/ai/gemini/question` | GeminiAIService.kt |

### Wrapper Service Methods:
- `generateFarmingRecommendations()` - wraps `getRecommendations()`
- `askFarmingQuestion()` - wraps `askQuestion()`

### Files Using GeminiApi:
- feature-advisory-services/data/GeminiAIService.kt (implementation)

---

## 7. CertificationApiService
**Location:** `feature-certification-quality/src/main/java/com/goldleaf/certification/data/remote/CertificationApiService.kt`  
**Type:** Product certification and batch management

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `createBatch()` | POST | `batches` | BatchRepositoryImpl.kt |
| `getFarmerBatches()` | GET | `batches/farmer/{farmerId}` | BatchRepositoryImpl.kt |
| `getLabTests()` | GET | `batches/{batchId}/lab-tests` | BatchRepositoryImpl.kt |
| `getBlockchainRecord()` | GET | `blockchain/{batchId}` | BatchRepositoryImpl.kt |
| `verifyProduct()` | GET | `verify/{batchNumber}` | VerificationRepositoryImpl.kt, BatchRepositoryImpl.kt |

### Files Using CertificationApiService:
- feature-certification-quality/data/repository/BatchRepositoryImpl.kt
- feature-certification-quality/data/repository/VerificationRepositoryImpl.kt

---

## 8. WeatherAPIInterface & WeatherAPIService
**Location:** `core/src/main/java/com/goldleaf/core/data/api/WeatherMarketServices.kt`  
**Type:** Backend weather service (calls GOLDLEAFAPI)

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `getCurrentWeather()` | GET | `api/weather/current` | WeatherAPIService.kt, AIServiceRepository.kt, AgriculturalIntelligenceService.kt |
| `getForecast()` | GET | `api/weather/forecast` | WeatherAPIService.kt |

### Wrapper Service (WeatherAPIService):
- `getCurrentWeather()` - wraps interface call
- `getWeatherForecast()` - wraps interface call

### Files Using WeatherAPIService:
- feature-advisory-services/data/AIServiceRepository.kt
- feature-advisory-services/data/AgriculturalIntelligenceService.kt

---

## 9. MarketDataAPIInterface & MarketDataService
**Location:** `core/src/main/java/com/goldleaf/core/data/api/WeatherMarketServices.kt`  
**Type:** Backend market data service (calls GOLDLEAFAPI)

### Methods Called:

| Method Name | HTTP Verb | Endpoint | Files Using It |
|---|---|---|---|
| `getCropPrices()` | GET | `api/market/prices` | MarketDataService.kt |
| `getMarketInsights()` | GET | `api/market/insights` | MarketDataService.kt |

### Wrapper Service (MarketDataService):
- `getMarketPrices()` - wraps `getCropPrices()`
- `getMarketInsights()` - wraps `getMarketInsights()`

### Files Using MarketDataService:
- feature-advisory-services/data/AIServiceRepository.kt
- feature-advisory-services/data/AgriculturalIntelligenceService.kt

---

## 10. PlantIdApiService
**Location:** `feature-advisory-services/src/main/java/com/goldleaf/feature/advisoryservices/data/PlantIdApiService.kt`  
**Type:** External plant identification API wrapper (appears unused)

### Methods in Interface:

| Method Name | HTTP Verb | Endpoint | Status |
|---|---|---|---|
| `identifyPlant()` | POST | `v2/identify` | ❌ Not used - uses PlantIdentificationApi instead |

### Note:
This appears to be a wrapper for an external plant.id API service but is not actively used. The core PlantIdentificationApi is used instead.

---

## 11. OpenAIService (Wrapper)
**Location:** `core/src/main/java/com/goldleaf/core/data/api/OpenAIService.kt`  
**Type:** Wrapper service for AIBackendService

**Status:** This is a wrapper/implementation service that calls AIBackendService interface

### Methods Provided:
- `createCompletion()` 
- `getFarmingAdvice()`
- `analyzePest()`
- `getMarketAdvice()`
- `optimizeIrrigation()`
- `identifyPlant()`

---

## Summary Table: All Services & Their Usage

| Service Name | Type | Backend/External | Methods Used | Primary Usage |
|---|---|---|---|---|
| **ApiService** | Interface | GOLDLEAFAPI | 50+ | Core farmer, farm, crop, weather, training, tasks management |
| **VideoApiService** | Interface | GOLDLEAFAPI | 0 (unused) | Training videos (overridden by ApiService) |
| **AuthApiService** | Interface | GOLDLEAFAPI | 0-1 | Authentication (wrapped by ApiService) |
| **PlantIdentificationApi** | Interface | GOLDLEAFAPI | 2 | Plant health & species identification |
| **AIBackendService** | Interface | GOLDLEAFAPI | 6 | AI completions, pest analysis, farming advice |
| **GeminiApi** | Interface | GOLDLEAFAPI | 2 | Gemini AI recommendations & Q&A |
| **CertificationApiService** | Interface | GOLDLEAFAPI | 5 | Batch management & certification |
| **WeatherAPIInterface** | Interface | GOLDLEAFAPI | 2 | Weather data & forecasts |
| **MarketDataAPIInterface** | Interface | GOLDLEAFAPI | 2 | Market prices & insights |
| **PlantIdApiService** | Interface | External | 0 (unused) | Plant ID API wrapper (unused) |
| **OpenAIService** | Wrapper Service | GOLDLEAFAPI | Multiple | Wraps AIBackendService |
| **WeatherAPIService** | Wrapper Service | GOLDLEAFAPI | Multiple | Wraps WeatherAPIInterface |
| **MarketDataService** | Wrapper Service | GOLDLEAFAPI | Multiple | Wraps MarketDataAPIInterface |
| **PlantIdService** | Wrapper Service | GOLDLEAFAPI | 2 | Wraps PlantIdentificationApi |
| **GeminiAIService** | Wrapper Service | GOLDLEAFAPI | 2 | Wraps GeminiApi |

---

## Key Findings

### ✅ **Active Backend Integration**
1. **ApiService** - Heavily used across all modules for core operations
2. **PlantIdentificationApi** - Used for plant analysis
3. **AIBackendService** - Used for AI-powered farming advice
4. **GeminiApi** - Used for Gemini AI recommendations
5. **CertificationApiService** - Used for batch certification management
6. **WeatherAPIInterface** - Used for weather data
7. **MarketDataAPIInterface** - Used for market data

### ⚠️ **Unused/Redundant Services**
1. **VideoApiService** - Defined but not used; functionality covered by ApiService
2. **PlantIdApiService** - External plant identification API wrapper (appears unused)
3. **AuthApiService** - Minimal use; authentication wrapped by ApiService

### 📊 **API Call Pattern**
- Wrapper Services pattern used extensively (OpenAIService, WeatherAPIService, etc.) for:
  - Error handling
  - Token management
  - Response transformation
  - Coroutine handling

### 🎯 **Primary Backend Server**
- **goldleaflabs.co.ke** handles the vast majority of API calls
- All main endpoints are centralized in ApiService
- Backend provides comprehensive functionality:
  - Authentication & Farmer Management
  - Farm & Crop Management
  - Weather & Market Data
  - Training Content
  - AI/ML Services
  - Certification & Quality Assurance
  - Task Management

---

## Recommendations

1. **Remove unused services**: VideoApiService and PlantIdApiService should be deprecated
2. **Consolidate authentication**: AuthApiService functionality is redundant with ApiService
3. **Document external APIs**: PlantIdApiService appears to be for future integration
4. **Consider API versioning**: Implement v1, v2 endpoints for backward compatibility
5. **Add comprehensive error handling**: Enhance wrapper service error messages

