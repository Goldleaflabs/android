# API Endpoints Comparison Report
**Generated:** March 2, 2026  
**Purpose:** Compare Android app endpoints vs PHP backend implementation

---

## EXECUTIVE SUMMARY

- **Total Android Endpoints:** 63 (excluding duplicates)
- **Total PHP Endpoints:** 37
- **Exact Matches:** 35 endpoints
- **Android Calling Missing PHP Endpoints:** 23 endpoints
- **PHP Endpoints Not Called by Android:** 2 endpoints
- **Critical Gaps:** Authentication endpoints completely missing from PHP backend

---

## 1. ENDPOINTS IN ANDROID THAT DON'T EXIST IN PHP (23 endpoints)

### 🔴 CRITICAL - Authentication Endpoints (MISSING ENTIRELY)
These are fundamental and MUST be implemented in PHP:
- `POST auth/login` - User login
- `POST auth/send-otp` - Send OTP for verification
- `POST auth/verify-otp` - Verify OTP code
- `POST auth/reset-password` - Password reset functionality

**Impact:** HIGH - App cannot authenticate users without these

---

### 🟠 IMPORTANT - Farmer Management Endpoints

| Android Endpoint | Closest PHP Match | Status |
|---|---|---|
| `POST farmers/updateFarmerProfile/{id}` | `PUT /farmers/{id}` | DUPLICATE - App has two ways to update |
| `POST farmers/farms` | `POST /farmers/{id}/farms` | DIFFERENT PATH - uses inconsistent routes |

---

### 🟠 IMPORTANT - Farm Boundary Endpoints

| Android Endpoint | PHP Endpoint | Issue |
|---|---|---|
| `PUT api/farms/{farmId}/boundaries` | `POST /api/farms/{id}/boundaries` | MISSING PUT method |
| `POST api/farms/{id}/boundaries` | ✅ Exists | Match |

**Issue:** Android expects PUT (update) but PHP only has POST (create)

---

### 🟠 IMPORTANT - Growth Stages Endpoints

| Android Endpoint | Status |
|---|---|
| `POST growth-stages` | ❌ MISSING FROM PHP |
| `PUT growth-stages/{id}` | ❌ MISSING FROM PHP |

**Note:** PHP only has `GET /growth-stages/{id}` - cannot create or update

---

### 🟠 IMPORTANT - Crop Management Endpoints

| Android Endpoint | PHP Equivalent | Status |
|---|---|---|
| `PUT crops/{id}` | NONE | ❌ MISSING - PHP only has GET, POST, DELETE |
| `GET api/crops` | `GET /crops` | Different path prefix |
| `GET api/crops/category/{category}` | NONE | ❌ MISSING - No filtering by category |
| `GET api/crops/search` | NONE | ❌ MISSING - No search functionality |
| `GET api/crops/{cropId}` | `GET /crops/{id}` | Different path prefix (api/crops vs crops) |
| `GET api/crops/categories` | NONE | ❌ MISSING - No category list endpoint |
| `GET growth-stages` | NONE | ❌ MISSING - Not in either list |

---

### 🟠 IMPORTANT - Farm-Crop Management Endpoints (NEW RESOURCE)

These appear to be farm-specific crop associations:
- `GET api/farms/{farmId}/crops` | ✅ Exists in PHP
- `POST api/farms/{farmId}/crops` | ❌ MISSING - Can't add crops to farm
- `PUT api/farms/{farmId}/crops` | ❌ MISSING - Can't update farm crops
- `DELETE api/farms/{farmId}/crops/{farmCropId}` | ❌ MISSING - Can't remove crops from farm
- `PATCH api/farm-crops/{farmCropId}/status` | ❌ MISSING - Can't update crop status
- `GET api/farms/{farmId}/crops/stats` | ❌ MISSING - No statistics endpoint
- `GET api/farms/{farmId}/crops/recommendations` | ❌ MISSING - No recommendations endpoint

**Impact:** CRITICAL - Android app is managing farm-specific crops but PHP backend lacks these endpoints

---

### 🟡 MODERATE - Task Management Endpoints

| Android Endpoint | PHP Match | Status |
|---|---|---|
| `GET farms/{farmId}/crops/{cropId}/tasks` | NONE | ❌ MISSING - No nested task retrieval |
| `DELETE tasks/{taskId}` | NONE | ❌ MISSING - Can only update status |
| `PATCH tasks/{id}/status` | `PUT /tasks/{id}/status` | Different HTTP method |

**Issue:** Android uses PATCH for status, PHP uses PUT

---

### 🟡 MODERATE - Weather Endpoints

| Android Endpoint | Status |
|---|---|
| `GET weather/forecast` | ❌ MISSING - Only current weather available |
| `GET weather/alerts` | ❌ MISSING - No alert system in PHP |

**Note:** `GET /weather/current` exists but `GET /api/weather/current` is a variant not called by Android

---

### 🟡 MODERATE - Certification & Public Endpoints

| Android Endpoint | Status |
|---|---|
| `POST api/farmer/certifications` | ❌ MISSING - Can't submit certifications |
| `GET public/verify/{batchNumber}` | ❌ MISSING - Public verification not implemented |

---

## 2. ENDPOINTS IN PHP THAT MIGHT NOT BE CALLED BY ANDROID (2 endpoints)

| PHP Endpoint | Android Calls | Status |
|---|---|---|
| `GET /api/weather/current` | `GET weather/current` | ⚠️ VARIANT - Different path, may be unused |
| `GET /api/farms/{farmId}` | ✅ Calls this | Match |

**Analysis:** 
- `/api/weather/current` appears to be a duplicate variant
- Could be deprecated or legacy code
- Should verify if Android actually calls this or the `weather/current` version

---

## 3. ENDPOINTS THAT MATCH 100% (35 endpoints)

### ✅ Farmer Management (5 endpoints)
- `POST /farmers/register`
- `GET /farmers/{id}`
- `PUT /farmers/{id}`
- `GET /farmers/{id}/dashboard`
- `POST /farmers/profile-image`

### ✅ Farm Management (6 endpoints)
- `GET /api/farmer/current`
- `GET /api/farms/{farmId}`
- `GET /farmers/{id}/farms`
- `POST /farmers/{id}/farms`
- `DELETE /farmers/{id}/farms/{farmId}`
- `POST /farmers/{id}/farms/{farmId}`

### ✅ Crop Management (5 endpoints)
- `GET /crops`
- `GET /crops/{id}`
- `POST /crops`
- `DELETE /crops/{id}`
- `GET /growth-stages/{id}`
- `GET /crops/{id}/monitoring`

### ✅ Farm Crop Management (1 endpoint)
- `GET /api/farms/{farmId}/crops`

### ✅ Task Management (4 endpoints)
- `POST /crops/{id}/tasks`
- `GET /farms/{id}/tasks`
- `GET /crops/{id}/tasks`
- `PUT /tasks/{id}/status`

### ✅ Quality & Certification (2 endpoints)
- `GET /quality/batches/farmer/{id}`
- `GET /quality/batches/{id}/lab-tests`

### ✅ Blockchain & Journey (2 endpoints)
- `POST /blockchain/verify`
- `GET /journey/batch/{id}`

### ✅ Training (3 endpoints)
- `GET /training/videos`
- `GET /training/categories`
- `POST /training/enroll`

### ✅ Advisory (2 endpoints)
- `GET /advisory/recommendations`
- `GET /advisory/experts`

### ✅ Officers (1 endpoint)
- `GET /api/officers`

### ✅ Weather (1 endpoint)
- `GET /weather/current`

### ✅ Sync (2 endpoints)
- `POST /sync/upload`
- `GET /sync/download`

### ✅ Farm Boundaries (1 endpoint)
- `POST /api/farms/{id}/boundaries`

---

## 4. MISSING AUTHENTICATION ENDPOINTS IN PHP

| Endpoint | Status | Priority |
|---|---|---|
| `POST /auth/login` | ❌ MISSING | 🔴 CRITICAL |
| `POST /auth/send-otp` | ❌ MISSING | 🔴 CRITICAL |
| `POST /auth/verify-otp` | ❌ MISSING | 🔴 CRITICAL |
| `POST /auth/reset-password` | ❌ MISSING | 🔴 CRITICAL |

**Observation:** Authentication is likely handled elsewhere in PHP app or middleware, but these specific endpoints don't appear in the v1/index.php route list.

---

## 5. RECOMMENDATIONS FOR ALIGNMENT

### 🔴 PHASE 1: CRITICAL (Must implement immediately)

1. **Authentication Endpoints** - PHP backend MUST implement:
   ```
   POST /auth/login
   POST /auth/send-otp
   POST /auth/verify-otp
   POST /auth/reset-password
   ```
   **Rationale:** App cannot function without authentication

2. **Update HTTP Methods for Consistency:**
   - Add `PUT /api/farms/{farmId}/boundaries` (currently only POST exists)
   - Add `PUT /crops/{id}` (currently can't update crops)
   - Change `PUT /tasks/{id}/status` method or add PATCH variant

### 🟠 PHASE 2: MAJOR (High priority - app features depend on these)

3. **Farm-Crop Management Endpoints** - Implement full CRUD:
   ```
   POST /api/farms/{farmId}/crops
   PUT /api/farms/{farmId}/crops
   DELETE /api/farms/{farmId}/crops/{farmCropId}
   PATCH /api/farm-crops/{farmCropId}/status
   GET /api/farms/{farmId}/crops/stats
   GET /api/farms/{farmId}/crops/recommendations
   ```

4. **Growth Stages Management:**
   ```
   POST /growth-stages
   PUT /growth-stages/{id}
   ```

5. **Crop Search & Filtering:**
   ```
   GET /crops/categories
   GET /crops/category/{category}
   GET /crops/search
   GET /api/crops/{cropId}
   ```

6. **Task Management Improvements:**
   ```
   GET /farms/{farmId}/crops/{cropId}/tasks
   DELETE /tasks/{taskId}
   ```

### 🟡 PHASE 3: IMPORTANT (Nice-to-have features)

7. **Weather Endpoints:**
   ```
   GET /weather/forecast
   GET /weather/alerts
   ```

8. **Farmer Certifications:**
   ```
   POST /api/farmer/certifications
   ```

9. **Public Verification:**
   ```
   GET /public/verify/{batchNumber}
   ```

### 🟢 PHASE 4: CLEANUP

10. **Review Path Consistency:**
    - Standardize between `/api/crops` and `/crops` prefixes
    - Choose one pattern: `/farmers/profile-image` vs `farmers/updateFarmerProfile/{id}`
    - Decide on HTTP method patterns (PUT vs PATCH for updates)

11. **Remove Duplicate Variants:**
    - Delete `/api/weather/current` if `/weather/current` is the standard
    - Consolidate farmer profile update endpoints

12. **Documentation:**
    - Create API documentation mapping Android calls to PHP endpoints
    - Ensure all endpoints have consistent authentication/authorization

---

## SUMMARY TABLE

| Category | Matched | Missing | Total |
|---|---|---|---|
| Authentication | 0 | 4 | 4 |
| Farmer Management | 5 | 1 | 6 |
| Farm Management | 6 | 0 | 6 |
| Crop Management | 5 | 6 | 11 |
| Farm-Crop Management | 1 | 6 | 7 |
| Growth Stages | 1 | 2 | 3 |
| Tasks | 4 | 3 | 7 |
| Quality/Blockchain | 2 | 0 | 2 |
| Training | 3 | 0 | 3 |
| Advisory | 2 | 0 | 2 |
| Weather | 1 | 2 | 3 |
| Other | 2 | 2 | 4 |
| **TOTALS** | **35** | **23** | **58** |

---

## TECHNICAL DEBT ASSESSMENT

**Risk Level:** 🔴 **HIGH**

- 4 critical authentication endpoints missing
- 6 farm-crop management endpoints missing (currently being called by app)
- 6 crop management features unavailable
- Potential runtime failures if Android app calls any of the 23 missing endpoints

**Estimated Implementation Effort:** 40-60 hours
- Auth endpoints: 10-15 hours
- Farm-crop management: 15-20 hours
- Remaining endpoints: 15-25 hours

