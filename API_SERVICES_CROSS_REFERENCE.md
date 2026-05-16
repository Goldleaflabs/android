# API Services Cross-Reference Matrix

## Service → Files Usage Matrix

This matrix shows which files use which API services.

### Core Module (API Definitions)

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI | OpenAIService | WeatherService | MarketService | PlantIdService |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| core/di/CoreApiModule.kt | **Provides** | **Provides** | **Provides** | Provides | - | - | - | **Provides** | **Provides** | - | - | - | - |
| core/data/api/OpenAIService.kt | - | - | - | - | ✅ Uses | - | - | - | - | ✅ Wrapper | - | - | - |
| core/data/api/WeatherMarketServices.kt | - | - | - | - | - | - | - | ✅ Uses | ✅ Uses | - | ✅ Wrapper | ✅ Wrapper | - |
| core/data/api/PlantIdService.kt | - | - | - | ✅ Uses | - | - | - | - | - | - | - | - | ✅ Wrapper |

### Feature Module: Farmer Management

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| feature-farmer-management/data/repository/FarmerRepositoryImpl.kt | ✅ Uses 40+ methods | - | - | - | - | - | - | - | - |
| feature-farmer-management/ui/FarmSetupViewModel.kt | ✅ Uses | - | - | - | - | - | - | - | - |
| feature-farmer-management/ui/auth/login/LoginViewModel.kt | ✅ Uses | - | - | - | - | - | - | - | - |
| feature-farmer-management/ui/auth/recovery/RecoveryViewModel.kt | ✅ Uses | - | - | - | - | - | - | - | - |
| feature-farmer-management/ui/viewmodels/FarmFencingViewModel.kt | ✅ Uses | - | - | - | - | - | - | - | - |
| feature-farmer-management/di/FarmerManagementModule.kt | **Provides** to Repos | - | - | - | - | - | - | - | - |

### Feature Module: Crop Management

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| feature-crop-management/data/repository/CropRepositoryImpl.kt | ✅ Uses 10+ methods | - | - | - | - | - | - | - | - |
| feature-crop-management/data/repository/TaskRepositoryImpl.kt | ✅ Uses 8 methods | - | - | - | - | - | - | - | - |
| feature-crop-management/data/repository/GrowthStageRepositoryImpl.kt | ✅ Uses 3 methods | - | - | - | - | - | - | - | - |
| feature-crop-management/ui/selection/OfficerRepository.kt | ✅ Uses | - | - | - | - | - | - | - | - |
| feature-crop-management/di/CropManagementModule.kt | **Provides** to Repos | - | - | - | - | - | - | - | - |

### Feature Module: Training Extension

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| feature-training-extension/data/repository/VideoRepositoryImpl.kt | ✅ Uses 4 methods | - | - | - | - | - | - | - | - |
| feature-training-extension/ui/TrainingDetailsViewModel.kt | - | ❓ Injected not used | - | - | - | - | - | - | - |
| feature-training-extension/di/TrainingModule.kt | **Provides** | - | - | - | - | - | - | - | - |

### Feature Module: Weather Climate

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| feature-weather-climate/data/repository/WeatherRepositoryImpl.kt | ✅ Uses 3 methods | - | - | - | - | - | - | - | - |
| feature-weather-climate/ui/WeatherViewModel.kt | Uses via Repo | - | - | - | - | - | - | - | - |

### Feature Module: Advisory Services

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI | PlantIdService |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| feature-advisory-services/ui/AdvisoryViewModel.kt | - | - | - | - | - | - | - | - | - | ✅ Uses |
| feature-advisory-services/ui/PestDetectionViewModel.kt | - | - | - | - | ✅ Uses | - | - | - | - | - |
| feature-advisory-services/ui/SoilAnalysisViewModel.kt | - | - | - | - | ✅ Uses | - | - | - | - | - |
| feature-advisory-services/ui/DiseaseDetectionViewModel.kt | - | - | - | - | ✅ Uses | - | - | - | - | - |
| feature-advisory-services/data/AIServiceRepository.kt | - | - | - | - | ✅ Uses | - | - | ✅ Uses | ✅ Uses | - |
| feature-advisory-services/data/AgriculturalIntelligenceService.kt | - | - | - | - | - | - | - | ✅ Uses | ✅ Uses | - |
| feature-advisory-services/data/GeminiAIService.kt | - | - | - | - | - | ✅ Uses | - | - | - | - |
| feature-advisory-services/data/AdvisoryRepository.kt | ✅ Uses | - | - | - | - | - | - | - | - | - |
| feature-advisory-services/di/AdvisoryModule.kt | - | - | - | - | - | **Provides** | - | **Provides** | **Provides** | - |

### Feature Module: Certification Quality

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| feature-certification-quality/data/repository/BatchRepositoryImpl.kt | - | - | - | - | - | - | ✅ Uses 4 methods | - | - |
| feature-certification-quality/data/repository/VerificationRepositoryImpl.kt | - | - | - | - | - | - | ✅ Uses | - | - |
| feature-certification-quality/presentation/batch/BatchViewModel.kt | Uses via Repo | - | - | - | - | - | - | - | - |
| feature-certification-quality/di/CertificationModule.kt | - | - | - | - | - | - | **Provides** | - | - |

### Authentication Module

| File | ApiService | VideoApiService | AuthApiService | PlantIdApi | AIBackendService | GeminiApi | CertifiApi | WeatherAPI | MarketAPI |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| core/auth/UserSessionManager.kt | - | - | ⚠️ Minimal | - | - | - | - | - | - |

---

## Service → Endpoints Matrix

### ApiService Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `auth/login` | POST | LoginViewModel, FarmerRepositoryImpl | Farmer login |
| `auth/send-otp` | POST | FarmerRepositoryImpl | Send OTP |
| `auth/verify-otp` | POST | FarmerRepositoryImpl | Verify OTP |
| `auth/reset-password` | POST | RecoveryViewModel, FarmerRepositoryImpl | Password reset |
| `farmers/register` | POST | FarmerRepositoryImpl | Farmer registration |
| `farmers/{id}` | GET | FarmerRepositoryImpl, FarmFencingViewModel | Get farmer details |
| `farmers/{id}/dashboard` | GET | FarmerRepositoryImpl | Get dashboard data |
| `farmers/{farmerId}/farms` | GET | FarmerRepositoryImpl | Get farmer's farms |
| `farmers/farms` | POST | FarmerRepositoryImpl | Add farm |
| `farmers/{id}/farms/{farmId}` | POST | FarmerRepositoryImpl | Update farm |
| `api/farms/{farmId}` | GET | FarmFencingViewModel, FarmerRepositoryImpl | Get farm details |
| `api/farms/{farmId}/boundaries` | PUT | FarmerRepositoryImpl | Set farm boundaries |
| `api/farms/{farmId}/crops` | GET/PUT/POST | FarmerRepositoryImpl | Manage farm crops |
| `api/farms/{farmId}/crops/stats` | GET | FarmerRepositoryImpl | Get crop stats |
| `api/farms/{farmId}/crops/recommendations` | GET | FarmerRepositoryImpl | Get crop recommendations |
| `crops` | GET | CropRepositoryImpl, FarmerRepositoryImpl | Get all crops |
| `crops/{id}` | GET/POST/PUT/DELETE | CropRepositoryImpl | Crop CRUD |
| `growth-stages/{cropId}` | GET | GrowthStageRepositoryImpl | Get growth stages |
| `growth-stages` | POST | GrowthStageRepositoryImpl | Create growth stage |
| `growth-stages/{id}` | PUT | GrowthStageRepositoryImpl | Update growth stage |
| `crops/{cropId}/tasks` | GET | TaskRepositoryImpl | Get tasks for crop |
| `farms/{farmId}/tasks` | GET | TaskRepositoryImpl | Get tasks for farm |
| `farms/{farmId}/crops/{cropId}/tasks` | GET | TaskRepositoryImpl | Get tasks for farm+crop |
| `crops/{taskId}/tasks` | POST | TaskRepositoryImpl | Create task |
| `tasks/{taskId}` | PUT | TaskRepositoryImpl | Update task |
| `tasks/{id}/status` | PATCH | TaskRepositoryImpl | Update task status |
| `tasks/{taskId}` | DELETE | TaskRepositoryImpl | Delete task |
| `training/videos` | GET | VideoRepositoryImpl | Get training videos |
| `training/categories` | GET | VideoRepositoryImpl | Get video categories |
| `training/progress` | POST | VideoRepositoryImpl | Update progress |
| `training/progress/{userId}` | GET | VideoRepositoryImpl | Get user progress |
| `weather/current` | GET | WeatherRepositoryImpl | Get current weather |
| `weather/forecast` | GET | WeatherRepositoryImpl | Get forecast |
| `public/verify/{batchNumber}` | GET | VerificationRepositoryImpl | Verify product |
| `api/farmer/current` | GET | FarmerRepositoryImpl | Get current farmer |
| `api/farmer/certifications` | POST | FarmerRepositoryImpl | Add certification |
| `api/officers` | GET | OfficerRepository | Get officers |

### AIBackendService Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `api/ai/completion` | POST | OpenAIService, AIServiceRepository, DiseaseDetectionViewModel, SoilAnalysisViewModel | General AI completion |
| `api/ai/farming-advice` | POST | OpenAIService | Farming advice |
| `api/ai/analyze-pest` | POST | OpenAIService, PestDetectionViewModel | Pest analysis |
| `api/ai/market-advice` | POST | OpenAIService, AIServiceRepository | Market advice |
| `api/ai/optimize-irrigation` | POST | OpenAIService | Irrigation optimization |
| `api/ai/identify-plant` | POST | OpenAIService | Plant identification |

### PlantIdentificationApi Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `api/ai/plant/health-assessment` | POST (Multipart) | PlantIdService | Analyze plant health |
| `api/ai/plant/identify` | POST (Multipart) | PlantIdService, AdvisoryViewModel | Identify plant species |

### GeminiApi Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `api/ai/gemini/recommendations` | POST | GeminiAIService | Get farming recommendations |
| `api/ai/gemini/question` | POST | GeminiAIService | Ask farming question |

### CertificationApiService Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `batches` | POST | BatchRepositoryImpl | Create batch |
| `batches/farmer/{farmerId}` | GET | BatchRepositoryImpl | Get farmer's batches |
| `batches/{batchId}/lab-tests` | GET | BatchRepositoryImpl | Get lab tests |
| `blockchain/{batchId}` | GET | BatchRepositoryImpl | Get blockchain record |
| `verify/{batchNumber}` | GET | VerificationRepositoryImpl, BatchRepositoryImpl | Verify product |

### WeatherAPIInterface & WeatherAPIService Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `api/weather/current` | GET | WeatherAPIService, AIServiceRepository, AgriculturalIntelligenceService | Get current weather |
| `api/weather/forecast` | GET | WeatherAPIService | Get weather forecast |

### MarketDataAPIInterface & MarketDataService Endpoints Used

| Endpoint | HTTP | Used By | Purpose |
|---|---|---|---|
| `api/market/prices` | GET | MarketDataService, AIServiceRepository | Get crop prices |
| `api/market/insights` | GET | MarketDataService | Get market insights |

---

## Summary Statistics

### Total Services: 15
- **Interfaces (Retrofit):** 9
- **Wrapper Services:** 6

### Active Usage:
- **Actively Used:** 11 services (73%)
- **Minimal Use:** 2 services (13%)
- **Unused:** 2 services (13%)

### Total Endpoints:
- **ApiService:** 50+ endpoints
- **Other Services:** 20+ endpoints
- **Total:** 70+ endpoints

### Primary Backend:
- **GOLDLEAFAPI (goldleaflabs.co.ke):** ~65 endpoints (93%)
- **External Services:** ~5 endpoints (7%)

### Module Distribution:
- **Farmer Management:** 5 files using ApiService
- **Crop Management:** 4 files using ApiService
- **Training:** 3 files (mostly ApiService)
- **Weather:** 2 files using ApiService + WeatherAPI
- **Advisory:** 8 files using multiple services (AIBackendService, GeminiApi, etc.)
- **Certification:** 3 files using CertificationApiService

---

## Key Insights

### 🎯 Central Hub
**ApiService** is the absolute core - used by 12+ files across all modules

### 🔄 Wrapper Pattern
All external API calls follow a consistent pattern:
1. UI calls Repository/ViewModel
2. Repository calls Wrapper Service
3. Wrapper Service calls Retrofit Interface
4. Retrofit Interface calls Backend

### ⚙️ AI Integration
Multiple AI services handle different types of intelligence:
- **AIBackendService/OpenAIService:** General AI completion + pest analysis
- **GeminiApi/GeminiAIService:** Specific Gemini recommendations
- **PlantIdentificationApi/PlantIdService:** Plant-specific analysis

### 🏗️ Modular Design
Each feature module has its own DI configuration providing services to repositories

### ⚠️ Technical Debt
- VideoApiService unused - should be removed
- AuthApiService minimal - should be consolidated
- PlantIdApiService unused - appears to be placeholder for future integration

