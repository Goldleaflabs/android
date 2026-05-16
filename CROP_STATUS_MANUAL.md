# Crop Status Lifecycle Manual
## GoldLeaf Labs - Crop Management Workflow

---

## 📋 Overview

This manual documents all crop statuses in the GoldLeaf system, their meanings, transitions, and what actions are available at each status.

**Total Statuses:** 6  
**Progression Flow:** PLANNED → PLANTED → GROWING → HARVESTED → COMPLETED  
**Exception Path:** Any status → FAILED (if crop fails)

---

## 🌱 Status Definitions & Actions

### 1. **PLANNED** (Status: Initial)
**Display Name:** "Planned"  
**Color Code:** Gray `#9E9E9E`  
**Progress:** 10%

#### What It Means:
- Farmer has created a crop record but has NOT yet planted it
- This is the planning/preparation stage
- Crop details are set but seeds are not in the ground

#### ✅ Allowed Actions at PLANNED Status:

| Action | Description | Triggers Transition |
|--------|-------------|-------------------|
| **Edit Crop Details** | Modify crop type, variety, location, area, expected yield | No |
| **Add Tasks** | Create preparation tasks (soil prep, fertilizer prep, etc.) | No |
| **View Crop Info** | View all crop details and metadata | No |
| **Upload Images** | Add photos of the field/farm | No |
| **Monitor (View Only)** | Check crop records but cannot add monitoring data yet | No |
| **Record Farm Activity** | Log activities like soil preparation, land clearing | No |
| **Mark as PLANTED** | Transition to next status when ready to plant | **YES** → PLANTED |
| **Mark as FAILED** | If crop cannot be planted (land issues, etc.) | **YES** → FAILED |

#### ❌ NOT Available at PLANTED Status:
- Create harvest batches (must be GROWING/HARVESTED)
- Request certification labels
- Record yield data
- Mark as harvested or completed

#### Associated Entities:
- **CropEntity** (Room Database)
- **TaskEntity** (for preparation tasks)
- **CropActivity** (for farm activities)

---

### 2. **PLANTED** (Status: Active)
**Display Name:** "Planted"  
**Color Code:** Blue `#2196F3`  
**Progress:** 25%

#### What It Means:
- Seeds have been planted in the ground
- Crop is in early growth stage (germination/early seedling)
- Active management period begins

#### ✅ Allowed Actions at PLANTED Status:

| Action | Description | Triggers Transition |
|--------|-------------|-------------------|
| **Add Tasks** | Create management tasks (irrigation, fertilization, pest control) | No |
| **Create Monitoring Records** | Record soil moisture, temperature, observations | No |
| **Record Activities** | Log irrigation, fertilization, weeding, etc. | No |
| **Upload Images** | Document crop progress with photos | No |
| **Add Health Assessments** | Rate crop health (Excellent/Good/Fair/Poor/Critical) | No |
| **View Growth Stages** | Monitor expected vs actual growth timeline | No |
| **Mark as GROWING** | Transition when crop shows active growth | **YES** → GROWING |
| **Mark as FAILED** | If planting failed or crop doesn't germinate | **YES** → FAILED |

#### ❌ NOT Available at PLANTED Status:
- Create harvest batches
- Request certification
- Record yield data
- Mark for harvesting yet

#### Database Impact:
- Crops queryable by: `getCropsByStatusAndFarmer([PLANTED], farmerId)`
- Used in: "Active Crops" dashboard
- Status color: Blue

---

### 3. **GROWING** (Status: Active)
**Display Name:** "Growing"  
**Color Code:** Green `#4CAF50`  
**Progress:** 60%

#### What It Means:
- Crop is actively growing and developing
- Most critical management period
- Vegetative to flowering/fruiting stages
- Eligible for harvest preparation

#### ✅ Allowed Actions at GROWING Status:

| Action | Description | Triggers Transition |
|--------|-------------|-------------------|
| **Add Tasks** | Create all management tasks (irrigation, pest control, fertilization, etc.) | No |
| **Create Monitoring Records** | Record detailed monitoring data | No |
| **Record Farm Activities** | Log all farm operations | No |
| **Upload Images** | Track crop progress visually | No |
| **Add Health Assessments** | Monitor and assess crop health | No |
| **View Growth Stages Timeline** | Track through germination → vegetative → flowering → fruiting | No |
| **Create Harvest Batch** ⭐ | **Create certification batch for harvest!** | No |
| **Request Labels** ⭐ | Request printed certification labels from county officer | No |
| **Mark as HARVESTED** | Transition when ready to harvest | **YES** → HARVESTED |
| **Mark as FAILED** | If crop fails before harvest (disease, pests, etc.) | **YES** → FAILED |

#### 🎯 Key Actions (Certification):
```ruby
# ⭐ AT THIS POINT:
✓ readyCrops includes GROWING status crops
✓ Can create ProductBatchEntity for certification
✓ Can trigger requestLabelsFromOfficer()
✓ WhatsApp message sent to county officer
```

#### ❌ NOT Available at GROWING Status:
- Transition directly to COMPLETED (must go through HARVESTED first)

#### Database Impact:
- **Special Query:** `getCropsByStatusAndFarmer([GROWING, PLANNED], farmerId)` = **readyCrops**
- Used in: Certification Dashboard "Create Batch" dropdown
- Status color: Green
- Progress: 60%

---

### 4. **HARVESTED** (Status: Post-Harvest Processing)
**Display Name:** "Ready to Harvest"  
**Color Code:** Orange `#FF9800`  
**Progress:** 90%

#### What It Means:
- Crop has been physically harvested from the field
- Actual yield is recorded
- Product batches have been created
- Certification process is ongoing or complete

#### ✅ Allowed Actions at HARVESTED Status:

| Action | Description | Triggers Transition |
|--------|-------------|-------------------|
| **Record Actual Yield** | Document final harvest weight/quantity | No |
| **View Harvest Records** | Check harvest date and yield data | No |
| **Monitor Batch Status** | Track certification batch status (PENDING/CONFIRMED) | No |
| **View Certification Details** | Check blockchain certification status | No |
| **Add Harvest Activity Records** | Log harvest completion details | No |
| **Update Notes** | Add notes about harvest quality, issues, etc. | No |
| **Calculate Yield Analytics** | View yield per hectare, efficiency metrics | No |
| **Mark as COMPLETED** | Finish the crop cycle after certification | **YES** → COMPLETED |
| **Mark as FAILED** | If harvest damaged/failed | **YES** → FAILED |

#### 📊 Yield Recording:
```kotlin
// Captured at this status:
updateCropStatusAndYield(
    cropId = String,
    newStatus = CropStatus.HARVESTED,
    actualYieldKg = Double  // Physical harvest weight
)
```

#### ❌ NOT Available at HARVESTED Status:
- Cannot create new batches (already created when GROWING)
- Cannot request labels (should be done when GROWING)
- Cannot go back to GROWING

#### Associated Entities:
- **HarvestRecordEntity** (harvest details)
- **ProductBatchEntity** (certification batch created earlier)
- **BlockchainRecord** (certification status)

---

### 5. **COMPLETED** (Status: End of Cycle)
**Display Name:** "Completed"  
**Color Code:** Green `#4CAF50` (same as GROWING)  
**Progress:** 100%

#### What It Means:
- Entire crop lifecycle is complete
- Certification is finalized (CONFIRMED status)
- Crop cycle ended successfully
- Record kept for analytics and history

#### ✅ Allowed Actions at COMPLETED Status:

| Action | Description | Triggers Transition |
|--------|-------------|-------------------|
| **View Complete Records** | Access all historical data, activities, monitoring | No |
| **View Yield Analytics** | Analyze final yield and performance | No |
| **Generate Reports** | Create summary reports for the crop cycle | No |
| **Export Data** | Export crop data and records | No |
| **View Certification** | Check final blockchain certification (CONFIRMED) | No |
| **View Activities Log** | Complete history of all farm activities | No |
| **Archive Crop** | Move to archive for historical reference | No |

#### ❌ NOT Available at COMPLETED Status:
- Cannot create new batches
- Cannot perform any management tasks
- Cannot add monitoring records
- Cannot request labels
- Cannot modify crop details
- Status is final (read-only mode)

#### Use Cases:
- Historical record keeping
- Yield comparison year-over-year
- Performance analytics
- Compliance/audit documentation

---

### 6. **FAILED** (Status: Exception State)
**Display Name:** "Failed"  
**Color Code:** Red `#F44336`  
**Progress:** 0%

#### What It Means:
- Crop failed at any point in the lifecycle
- Reasons: disease, pest outbreak, weather, soil issues, poor germination, etc.
- Crop will not proceed to harvest
- Recorded for failure analysis

#### ✅ Allowed Actions at FAILED Status:

| Action | Description | Triggers Transition |
|--------|-------------|-------------------|
| **View Failure Records** | Check what led to crop failure | No |
| **Document Failure Reason** | Add notes about why crop failed | No |
| **View Activities/Monitoring** | Analyze what happened leading up to failure | No |
| **Record Lessons Learned** | Document findings for future planning | No |
| **Create New Crop** | Start over with new crop for same field | No (New crop) |

#### 🚫 Cannot:
- Create batches
- Request certification
- Record yield
- Proceed to any other status
- Perform management tasks
- Harvest the crop

#### Failure Points (Where FAILED can occur):
```kotlin
// Can transition to FAILED from:
- PLANNED (cannot plant)
- PLANTED (germination failure, early plant death)
- GROWING (disease outbreak, pest infestation, severe weather)
- HARVESTED (rare, if harvest damaged beyond use)
```

#### Database Query:
```kotlin
// Get failed crops:
GetFailedCropsUseCase → getCropsByStatus(listOf(CropStatus.FAILED))
```

---

## 📊 Status Transition Diagram

```
                    ┌─────────────┐
                    │   PLANNED   │ ◄──── CREATE NEW CROP
                    └──────┬──────┘
                           │ Plant
                           ▼
                    ┌─────────────┐
                    │   PLANTED   │
                    └──────┬──────┘
                           │ Growing
                           ▼
              ┌────────────────────────────┐
              │       GROWING ⭐            │ ◄── CERTIFICATION HERE
              │    (Create Batch)          │
              │   (Request Labels)         │
              └────────────────────────────┘
                           │
                    Growth Complete
                           │
                           ▼
                    ┌─────────────┐
                    │  HARVESTED  │
                    └──────┬──────┘
                           │ Certification
                           │ Complete
                           ▼
                    ┌─────────────┐
                    │  COMPLETED  │ ◄──── END OF CYCLE
                    └─────────────┘


EXCEPTION PATH (from ANY status):
    PLANNED ─┐
    PLANTED ─┤
    GROWING ─┼──► FAILED (✗ Crop dies, disease, pests, bad harvest)
    HARVESTED┤
    COMPLETED┘
```

---

## 🎯 Certification Workflow (Key for Your App)

### Creating Harvest Batches (When crop is GROWING):

```kotlin
// UI Layer (CertificationDashboardScreen.kt)
readyCrops = cropRepository.getCropsByStatusAndFarmer(
    listOf(CropStatus.GROWING, CropStatus.PLANNED)  // ← Gets available crops
)

// When user selects GROWING crop and clicks "Create Batch":
viewModel.createHarvestBatch(
    crop = selectedCrop,  // Must be GROWING or PLANNED
    bags = quantityCount,
    farmName = crop.name
)

// Result:
→ Creates ProductBatchEntity (stored in certification database)
→ Status: PENDING (waiting for officer approval)
```

### Requesting Labels (When crop is GROWING):

```kotlin
// User clicks "Request Labels" button on batch card:
viewModel.requestLabelsFromOfficer(
    batchId = batch.batchNumber,
    qty = batch.quantity,
    unit = batch.unit,
    county = extractFromFarmer.location
)

// Behind the scenes:
1. Retrieves officer phone from OfficerRepository (or fallback)
2. Creates WhatsApp message with batch details
3. Sends WhatsApp intent to officer
4. Officer receives: batch ID, quantity, verification link, print link
5. Officer can click print link to generate labels
```

---

## 📱 Room Database Entities

```kotlin
@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey val id: String,
    val farmerId: String,
    val farmId: String,
    val name: String,
    val variety: String?,
    val status: CropStatus,  // ← THIS FIELD
    val plantingDate: String,
    val harvestDate: String?,
    val area: Double,
    val expectedYield: Double?,
    val actualYield: Double?,
    val notes: String?,
    val updatedAt: String
)

// Query by status:
@Query("SELECT * FROM crops WHERE status IN (:statuses) AND farmerId = :farmerId")
suspend fun getCropsByStatusAndFarmer(
    statuses: List<CropStatus>,
    farmerId: String
): List<CropEntity>
```

---

## 🔄 Common Workflows

### Workflow 1: Normal Crop Lifecycle (Success Path)
```
1. Farmer creates crop → PLANNED
   ↓ Sets planting date, area, expected yield
2. Farmer plants seeds → PLANTED
   ↓ Creates tasks (irrigation, fertilization)
3. Crop is growing → GROWING ⭐
   ↓ Creates harvest batch
   ↓ Requests certification labels from officer
4. Harvest time → HARVESTED
   ↓ Records actual yield
   ↓ Officer approves certification
5. Cycle ends → COMPLETED ✓
   ↓ Crop archived, data kept for analytics
```

### Workflow 2: Crop Failure Path
```
1. Farmer creates crop → PLANNED
2. Farmer plants → PLANTED
3. Disease outbreak → FAILED ✗
   ↓ No harvest batch created
   ↓ No labels requested
   ↓ Failure reason documented
4. Farmer starts new crop with lessons learned
```

### Workflow 3: Certification Request
```
When crop is GROWING:
1. Farmer clicks "Create Batch" in Certification Dashboard
2. System shows only GROWING/PLANNED crops in dropdown
3. Farmer selects quantity and unit
4. Creates ProductBatchEntity with status PENDING
5. Farmer clicks "Request Labels" button
6. WhatsApp opens with pre-filled message
7. Message sent to county officer
8. Officer prints labels
9. Farmer receives labeled products
```

---

## ⚠️ Important Notes

### Status Queries in Your App:
```kotlin
// These are the key queries:

// GET READY-TO-HARVEST CROPS (for Certification Dashboard)
val readyCrops = cropRepository.getCropsByStatusAndFarmer(
    listOf(CropStatus.GROWING, CropStatus.PLANNED)
)

// GET ACTIVE CROPS (for Farm Dashboard)
val activeCrops = repository.getCropsForHarvest()  
// Returns: PLANTED, GROWING

// GET FAILED CROPS (for Analytics)
val failedCrops = repository.getCropsByStatus(listOf(CropStatus.FAILED))

// GET COMPLETED CROPS (for Historical Records)
val completedCrops = repository.getCropsByStatus(listOf(CropStatus.COMPLETED))
```

### Status Values in Database:
- Stored as: `"PLANNED"`, `"PLANTED"`, `"GROWING"`, `"HARVESTED"`, `"COMPLETED"`, `"FAILED"`
- Case-sensitive: Use uppercase
- Enum conversion: `CropStatus.valueOf("GROWING")`

### No Direct Transitions:
```kotlin
// Invalid transitions:
PLANNED → HARVESTED  // ✗ Must go through PLANTED, GROWING
PLANTED → COMPLETED  // ✗ Must go through GROWING, HARVESTED
HARVESTED → GROWING  // ✗ Cannot go backwards
```

---

## 📋 Status Reference Table

| Status | Display | Color | Progress | Can Create Batch | Can Request Labels | For Harvest |
|--------|---------|-------|----------|------------------|-------------------|------------|
| PLANNED | Planned | Gray | 10% | ❌ | ❌ | ❌ |
| PLANTED | Planted | Blue | 25% | ❌ | ❌ | ❌ |
| GROWING | Growing | Green | 60% | ✅ | ✅ | ✅ |
| HARVESTED | Ready to Harvest | Orange | 90% | ❌ | ❌ | ❌ |
| COMPLETED | Completed | Green | 100% | ❌ | ❌ | ❌ |
| FAILED | Failed | Red | 0% | ❌ | ❌ | ❌ |

---

## 📞 Support

For questions about:
- **Crop Management Logic:** See [CropEntity.kt](crop/CropEntity.kt)
- **Status Transitions:** See [CropDetailsViewModel.kt](crop/CropDetailsViewModel.kt)
- **Batch Creation:** See [BatchViewModel.kt](certification/BatchViewModel.kt)
- **Certification Flow:** See [CertificationDashboardScreen.kt](certification/CertificationDashboardScreen.kt)

---

**Last Updated:** March 16, 2026  
**Version:** 1.0  
**Author:** GoldLeaf Labs Team
