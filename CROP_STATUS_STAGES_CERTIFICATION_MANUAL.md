# Crop Status/Stages vs Certification - Complete Manual

## 📖 Table of Contents
1. [Overview](#overview)
2. [Key Concepts](#key-concepts)
3. [Crop Status Lifecycle](#crop-status-lifecycle)
4. [Growth Stages](#growth-stages)
5. [Certification Entities](#certification-entities)
6. [Certification Eligibility](#certification-eligibility)
7. [Certification Workflow](#certification-workflow)
8. [What Certification Monitors](#what-certification-monitors)
9. [Status Relationships](#status-relationships)
10. [Data Models](#data-models)

---

## 🎯 Overview

**Crop Status** and **Growth Stages** are **TWO SEPARATE CONCEPTS**:

- **Crop Status** — High-level lifecycle state (PLANNED → GROWING → COMPLETED)
- **Growth Stages** — Botanical stage within growth (GERMINATION → VEGETATIVE → FLOWERING → MATURATION)

**Certification** only cares about **Crop Status**, not growth stages. It monitors product batches created during the **GROWING** crop status.

```
CROP LIFECYCLE                    CERTIFICATION LIFECYCLE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PLANNED                           (No batches)
   ↓
PLANTED                           (No batches)
   ↓
GROWING ⭐ ——————————————————————→ CREATE BATCH → PENDING
   ↓                                      ↓
HARVESTED ←————————————————————— Status updated → CONFIRMED
   ↓                                      ↓
COMPLETED                         QR Code verified → DONE
```

---

## 🔑 Key Concepts

### 1. **Crop Status** (6 Statuses)
**What It Is:** Farmer-controlled lifecycle state of the crop  
**When It Changes:** Farmer transitions crop through growth phases  
**Examples:** PLANTED, GROWING, HARVESTED  
**Importance for Certification:** Only GROWING status allows batch creation

### 2. **Growth Stages** (Within Crop Status)
**What It Is:** Automatic/biological stage of crop development  
**When It Changes:** Time-based or observation-based (independent of status)  
**Examples:** GERMINATION (days 0-7), VEGETATIVE (days 8-30), FLOWERING (days 31-60)  
**Importance for Certification:** Purely informational, doesn't affect certification workflow

### 3. **Certification Status** (Batch Level)
**What It Is:** Blockchain/approval status of product batch  
**When It Changes:** Officer approves, tests complete, blockchain confirms  
**Values:** PENDING → CONFIRMED → FAILED  
**Importance:** Determines if product is certified authentic

---

## 🌱 Crop Status Lifecycle

### Complete Status Progression (For Certification Context)

```
┌─────────────────────────────────────────────────────────────────┐
│                    CROP LIFECYCLE STATES                        │
└─────────────────────────────────────────────────────────────────┘

STATUS          | DESCRIPTION                | CERTIFICATION ALLOWED?
────────────────┼────────────────────────────┼─────────────────────
PLANNED         | Farmer plans planting      | ❌ NO
                | (before seeds in ground)   | (Use later when GROWING)
────────────────┼────────────────────────────┼─────────────────────
PLANTED         | Seeds/seedlings in ground  | ❌ NO
                | (germination phase)        | (Too early, not ready)
────────────────┼────────────────────────────┼─────────────────────
GROWING ⭐      | Crop actively growing      | ✅ YES!
                | (vegetative/flowering)    | (Create batch here)
                |                            | (Request labels)
────────────────┼────────────────────────────┼─────────────────────
HARVESTED       | Crop harvested, yield      | ❌ NO MORE
                | recorded (batch created)   | (Batch already created)
────────────────┼────────────────────────────┼─────────────────────
COMPLETED       | Cycle finished,            | ❌ NO
                | certification done         | (Locked, final state)
────────────────┼────────────────────────────┼─────────────────────
FAILED          | Crop failed (disease,      | ❌ NO
                | pests, weather)            | (Cannot certify failed crop)
└─────────────────────────────────────────────────────────────────┘
```

### Why Only GROWING?

```
PLANNED
  ↓ (Crop doesn't exist yet, no product to certify)

PLANTED
  ↓ (Crop too young, too much can happen)

GROWING ⭐ ← ✅ PERFECT TIMING
  ✓ Crop is identifiable
  ✓ Farmer knows quantity to harvest
  ✓ Batch can be created
  ✓ Certification process can start
  ✓ Labels can be requested
  ↓

HARVESTED ← (Too late, batch already created)
  ✓ Batch exists, cannot create new one
  ✓ Crop transitions recorded
  ✓ Officer should have approved by now
  ↓

COMPLETED ← (Final state, immutable)

FAILED ← (Special case, no certification)
```

---

## 🌾 Growth Stages

**Independent from Crop Status** — Growth stages are calculated/tracked separately

### Growth Stage Values

```
SEED_PREPARATION → PLANTING → GERMINATION → VEGETATIVE → 
FLOWERING → FRUIT_DEVELOPMENT → MATURATION → HARVEST
```

### How Growth Stages Work

```kotlin
// Calculated automatically based on days since planting
val daysSincePlanting = getDaysSincePlanting()
val totalGrowingDays = 90  // e.g., maize = 90 days

val stage = when {
    daysSincePlanting < 7       → "Germination"      (0-10%)
    daysSincePlanting < 30      → "Vegetative"       (10-30%)
    daysSincePlanting < 60      → "Flowering"        (30-70%)
    daysSincePlanting < 90      → "Maturation"       (70-100%)
    else                        → "Ready for Harvest" (100%)
}
```

### Growth Stage Example Timeline

```
Crop: MAIZE (Typical 90-day cycle)

Days 0-7:     GERMINATION
              └─ Seed sprouting, roots developing
              └─ Crop Status: PLANTED
              └─ Certification: ❌ Not ready

Days 8-30:    VEGETATIVE
              └─ Leaves growing, no ears yet
              └─ Crop Status: GROWING
              └─ Certification: ✅ Can create batch now

Days 31-60:   FLOWERING
              └─ Tasseling, silking begins
              └─ Crop Status: GROWING
              └─ Certification: ✅ Still in GROWING

Days 61-90:   MATURATION
              └─ Grain filling, cob hardening
              └─ Crop Status: GROWING (or starting to transition)
              └─ Certification: ✅ Can still create batch

Day 91+:      HARVEST READY
              └─ Farmer marks → HARVESTED
              └─ Certification: ❌ Should've created batch earlier
```

### Key Point: Growth Stages Don't Control Certification

Growth stage is **informational only**. Certification doesn't check growth stage:

```kotlin
// Certification checks ONLY this:
val canCreateBatch = cropStatus == CropStatus.GROWING

// Certification does NOT check this:
// val stage = crop.growthStage  ← Ignored for certification timing
```

---

## 📦 Certification Entities

### 1. **ProductBatchEntity** — The Main Container

```kotlin
@Entity(tableName = "product_batches")
data class ProductBatchEntity(
    // Product Info
    val id: String,                    // Unique batch ID
    val batchNumber: String,           // e.g., "GL-HVST-FARM-2603-1234"
    val cropId: String,                // Reference to source crop
    val farmId: String,                // Reference to source farm
    
    // Batch Details
    val quantity: Double,              // e.g., 500.0
    val unit: String,                  // e.g., "kg"
    val productType: String,           // e.g., "Maize", "Beans"
    val harvestDate: Long,             // When harvested
    val qualityGrade: String?,         // e.g., "Grade A"
    
    // Farmer Info (snapshot)
    val farmerName: String,            // e.g., "John Doe"
    val farmerId: String,              // e.g., "farmer_123"
    
    // Batch Status (Supply Chain)
    val status: ProductStatus,         // HARVESTED → PROCESSING → PACKAGED → SHIPPED
    
    // Certification Status ⭐
    val blockchainStatus: BlockchainStatus,  // PENDING → CONFIRMED → FAILED
    val blockchainRecordId: String?,        // Link to blockchain record
    val blockchainHash: String?,            // Immutable hash on blockchain
    
    // QR Code
    val qrCode: String?,               // QR data for consumer verification
    
    // Timestamps
    val createdAt: Long,               // When batch created
    val lastSyncTime: Long             // Last sync with server
)

// Status Values
enum class ProductStatus {
    HARVESTED,      // Batch created, raw product
    PROCESSING,     // Being processed/packaged
    PACKAGED,       // Final packaging done
    SHIPPED,        // In transit
    DELIVERED       // Received by buyer
}

enum class BlockchainStatus {
    PENDING,        // Awaiting officer approval
    CONFIRMED,      // ✓ Certified and on blockchain
    FAILED          // ✗ Failed certification
}
```

### 2. **BlockchainRecord** — Certification Proof

```kotlin
@Entity(tableName = "blockchain_records")
data class BlockchainRecordEntity(
    // Record identity
    val id: String,                    // Unique record ID
    val transactionHash: String,       // Blockchain tx hash (immutable)
    
    // What it's for
    val batchId: String,               // Which batch this certifies
    val recordType: BlockchainRecordType,  // HARVEST, QUALITY_CHECK, SHIPMENT
    
    // Blockchain info
    val blockNumber: Long?,            // Which block on blockchain
    val timestamp: Long,               // When recorded on blockchain
    val status: BlockchainStatus,      // PENDING, CONFIRMED, FAILED
    
    // Reference info
    val entityId: String,              // Related batch/crop/farm ID
    val farmerId: String?,             // Farmer who owns it
    val farmId: String?,               // Farm it came from
    
    // Data
    val data: String,                  // JSON string of full record
    val lastSyncTime: Long             // Last updated
)

enum class BlockchainRecordType {
    HARVEST,           // Initial harvest record
    QUALITY_CHECK,     // Lab testing completed
    SHIPMENT           // Product in supply chain
}

enum class BlockchainStatus {
    PENDING,    // Waiting for confirmation (⏳)
    CONFIRMED,  // ✓ Verified on blockchain (🔒)
    FAILED      // ✗ Rejected (❌)
}
```

### 3. **LabTest** — Quality Testing

```kotlin
data class LabTest(
    val id: String,           // Unique test ID
    val batchId: String,      // Which batch tested
    
    // Test info
    val testType: String,     // "Pesticide", "Contamination", "GMO", "Heavy Metals"
    val testDate: String,     // When test performed
    val labName: String,      // Which lab conducted it
    
    // Results
    val status: String,       // "PASSED", "FAILED", "PENDING"
    val isPassed: Boolean?,   // true = passed, false = failed
    val resultUrl: String?,   // Link to test certificate
    
    // Notes
    val notes: String?        // Any remarks from lab
)
```

---

## ✅ Certification Eligibility

### What CAN Be Certified

```
REQUIREMENT                           | STATUS
──────────────────────────────────────┼──────────────────────
Crop Status = GROWING                 | ✅ MUST be GROWING
Not PLANNED, PLANTED, etc.            
──────────────────────────────────────┼──────────────────────
Crop exists in database               | ✅ Must have CropEntity
──────────────────────────────────────┼──────────────────────
Farmer is logged in                   | ✅ Must have FarmerEntity
──────────────────────────────────────┼──────────────────────
Quantity > 0                          | ✅ Must specify amount
──────────────────────────────────────┼──────────────────────
Batch didn't already exist            | ✅ One batch per harvest
──────────────────────────────────────┼──────────────────────
Officer phone available               | ✅ For label requests
```

### What CANNOT Be Certified

```
STATUS              | WHY NOT?                           | CAN FIX?
────────────────────┼────────────────────────────────────┼──────────
PLANNED             | Crop not planted yet              | Yes → PLANT
PLANTED             | Too early, uncertain outcome      | Yes → WAIT for GROWING
HARVESTED           | Already created batch, too late   | No → Start new crop
COMPLETED           | Cycle finished, locked            | No
FAILED              | Crop failed, not usable          | No
(No CropEntity)     | Doesn't exist                     | No
(No FarmerEntity)   | Not logged in                     | Yes → Login
(Quantity = 0)      | No product to certify             | Yes → Enter quantity
```

---

## 🔄 Certification Workflow

### Complete Workflow Overview

```
STEP 1: CROP GROWING
┌──────────────────────────────────────┐
│ Farmer has crop in GROWING status   │
│ • Ready to harvest soon              │
│ • Knows approximate quantity         │
└──────────────────────────────────────┘
                ↓
STEP 2: FARMER OPENS CERTIFICATION DASHBOARD
┌──────────────────────────────────────┐
│ Dashboard shows:                     │
│ • readyCrops = [crops with          │
│   status GROWING/PLANNED]            │
│ • Dropdown lists available crops    │
└──────────────────────────────────────┘
                ↓
STEP 3: FARMER CREATES HARVEST BATCH
┌──────────────────────────────────────┐
│ Farmer:                              │
│ 1. Selects crop (must be GROWING)   │
│ 2. Enters quantity (bags/boxes)     │
│ 3. Taps "Create Batch"              │
│                                      │
│ System:                              │
│ 1. Creates ProductBatchEntity       │
│ 2. Sets blockchainStatus = PENDING  │
│ 3. Generates unique batchNumber    │
│ 4. Updates crop → HARVESTED         │
│ 5. Creates HarvestRecord            │
└──────────────────────────────────────┘
                ↓
STEP 4: FARMER REQUESTS LABELS
┌──────────────────────────────────────┐
│ Farmer taps "Request Labels"        │
│                                      │
│ System:                              │
│ 1. Looks up county officer phone    │
│ 2. Creates WhatsApp message with:  │
│    - Batch ID                       │
│    - Quantity                       │
│    - Print link                     │
│    - Verification link              │
│ 3. Sends via WhatsApp               │
│ 4. Records LabelPrintRequest        │
└──────────────────────────────────────┘
                ↓
STEP 5: OFFICER RECEIVES & APPROVES
┌──────────────────────────────────────┐
│ County Officer:                      │
│ 1. Receives WhatsApp message        │
│ 2. Clicks print link                │
│ 3. Generates labels                 │
│ 4. Delivers labels to farmer        │
│ 5. Marks approved (backend)         │
│                                      │
│ System:                              │
│ 1. blockchainStatus → CONFIRMED     │
│ 2. Creates BlockchainRecord         │
│ 3. Generates QR code                │
└──────────────────────────────────────┘
                ↓
STEP 6: LAB TESTING (Optional)
┌──────────────────────────────────────┐
│ If required:                         │
│ 1. Sample sent to lab               │
│ 2. Tests conducted (pesticides, etc)│
│ 3. Results recorded as LabTest      │
│ 4. BlockchainRecord updated         │
└──────────────────────────────────────┘
                ↓
STEP 7: PRODUCT VERIFIED
┌──────────────────────────────────────┐
│ Consumer/Buyer:                      │
│ 1. Scans QR code on label           │
│ 2. Verifies product authenticity    │
│ 3. Sees blockchain proof            │
│ 4. Sees lab test results (if any)   │
│ 5. ✓ CERTIFIED                      │
└──────────────────────────────────────┘
```

---

## 🔍 What Certification Monitors

### 1. **ProductBatchEntity Status Tracking**

```
ProductBatchEntity Monitors:
├── Batch Identity
│   ├── batchNumber (e.g., GL-HVST-FARM-2603-1234)
│   ├── productType (Maize, Beans, Coffee)
│   └── harvestDate (when harvested)
│
├── Quantity Tracking
│   ├── quantity (500.0)
│   └── unit (kg, bags, boxes)
│
├── Blockchain Status ⭐
│   ├── blockchainStatus (PENDING → CONFIRMED)
│   ├── blockchainHash (immutable proof)
│   └── blockchainTimestamp (when approved)
│
└── Quality Information
    ├── qualityGrade (Grade A, Grade B)
    └── qrCode (consumer verification)
```

### 2. **BlockchainRecord Verification**

```
BlockchainRecord Monitors:
├── Transaction Details
│   ├── transactionHash (immutable on blockchain)
│   ├── blockNumber (which block)
│   └── timestamp (when recorded)
│
├── Record Type
│   ├── HARVEST (initial creation)
│   ├── QUALITY_CHECK (lab testing)
│   └── SHIPMENT (supply chain)
│
└── Status
    ├── PENDING (awaiting confirmation)
    ├── CONFIRMED (✓ verified)
    └── FAILED (✗ rejected)
```

### 3. **LabTest Records**

```
LabTest Monitors:
├── Test Information
│   ├── testType (Pesticide, GMO, Heavy Metals)
│   ├── testDate (when tested)
│   └── labName (which laboratory)
│
├── Test Results
│   ├── status (PASSED, FAILED, PENDING)
│   ├── isPassed (boolean)
│   └── resultUrl (certificate link)
│
└── Notes
    └── notes (any remarks from lab)
```

### 4. **Supply Chain Status**

```
ProductStatus Progression:
HARVESTED → PROCESSING → PACKAGED → SHIPPED → DELIVERED

Each stage can be:
- Monitored in real-time
- Tracked in batch details
- Linked to transactions
```

---

## 🔗 Status Relationships

### Relationship Diagram

```
┌─────────────────────────────────────────────────────────┐
│             CROP LIFECYCLE                              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  PLANNED → PLANTED → GROWING ⭐ → HARVESTED → COMPLETED│
│                        │                                │
│                        └──────→ FAILED                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
                          ↓
                          │ (Only from GROWING)
                          ↓
┌─────────────────────────────────────────────────────────┐
│       CERTIFICATION LIFECYCLE                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ProductBatchEntity Creation                           │
│  ├── blockchainStatus: PENDING                         │
│  ├── status: HARVESTED                                 │
│  └── qrCode: null                                      │
│          ↓                                              │
│  Officer Approves                                      │
│  ├── blockchainStatus: CONFIRMED                       │
│  ├── BlockchainRecord created                          │
│  └── qrCode: generated                                 │
│          ↓                                              │
│  Lab Tests (Optional)                                  │
│  ├── LabTest records created                           │
│  ├── Results added to blockchain                       │
│  └── BlockchainRecord type: QUALITY_CHECK              │
│          ↓                                              │
│  Supply Chain Tracking                                 │
│  ├── status: PROCESSING → PACKAGED → SHIPPED          │
│  └── BlockchainRecord updated                          │
│          ↓                                              │
│  Consumer Verification                                 │
│  ├── Scans QR code                                     │
│  ├── Blockchain verified                               │
│  ├── Lab tests visible                                 │
│  └── ✓ CERTIFIED AUTHENTIC                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### When Values Change

```
EVENT                           | CROP STATUS | BATCH STATUS | BLOCKCHAIN
────────────────────────────────┼─────────────┼──────────────┼────────────
Crop created                    | PLANNED     | —            | —
Farmer plants                   | PLANTED     | —            | —
Farmer marks growing            | GROWING     | —            | —
   ↓
Farmer creates batch            | GROWING→    | HARVESTED    | PENDING
                                | HARVESTED   |              |
   ↓
Officer receives WhatsApp       | HARVESTED   | HARVESTED    | PENDING→
                                |             |              | CONFIRMED
   ↓
Officer adds batch to           | HARVESTED   | HARVESTED    | CONFIRMED
blockchain                      |             |              |
   ↓
Lab tests completed             | HARVESTED   | HARVESTED    | CONFIRMED
                                |             |              | (+ QC record)
   ↓
Product shipped                 | HARVESTED   | SHIPPED      | CONFIRMED
   ↓
Consumer scans QR               | HARVESTED   | SHIPPED      | CONFIRMED
                                |             |              | (verified)
```

---

## 📊 Data Models

### Entity Relationships

```
CropEntity (Crop Management)
  ↓
  ├─ status: CropStatus [PLANNED, PLANTED, GROWING, HARVESTED, COMPLETED, FAILED]
  ├─ growthStage: String [GERMINATION, VEGETATIVE, FLOWERING, MATURATION]
  └─ (When GROWING, can create batch)
          ↓
          └──→ ProductBatchEntity (Certification Dashboard)
               ├─ batchNumber: String
               ├─ quantity: Double
               ├─ unit: String
               ├─ status: ProductStatus [HARVESTED, PROCESSING, PACKAGED, SHIPPED]
               ├─ blockchainStatus: BlockchainStatus [PENDING, CONFIRMED, FAILED]
               ├─ qrCode: String
               └─ blockchainRecordId: String
                     ↓
                     ├──→ BlockchainRecordEntity
                     │    ├─ transactionHash: String (immutable)
                     │    ├─ blockNumber: Long
                     │    ├─ recordType: BlockchainRecordType [HARVEST, QUALITY_CHECK]
                     │    ├─ status: BlockchainStatus [PENDING, CONFIRMED]
                     │    └─ (stored on blockchain for verification)
                     │
                     └──→ LabTestEntity (Optional, multiple)
                          ├─ testType: String [Pesticide, GMO, Heavy Metals]
                          ├─ testDate: String
                          ├─ status: String [PASSED, FAILED, PENDING]
                          └─ resultUrl: String (certificate)
```

---

## 📋 Quick Reference

### Certification Checklist

```
Before Creating Batch:
☑ Crop status = GROWING (not PLANNED, PLANTED, etc.)
☑ Farmer logged in (FarmerEntity exists)
☑ Quantity entered > 0
☑ Officer contact available
☑ No existing batch for this harvest

When Creating Batch:
☑ ProductBatchEntity created
☑ blockchainStatus = PENDING
☑ batchNumber generated
☑ Crop status → HARVESTED
☑ HarvestRecord created

When Officer Approves:
☑ blockchainStatus = CONFIRMED
☑ BlockchainRecord created
☑ QR code generated
☑ transactionHash stored

Optional Lab Testing:
☑ LabTest records created
☑ Test results linked to batch
☑ BlockchainRecord type = QUALITY_CHECK
```

---

## 🎯 Summary

| Concept | What It Is | Controls Certification |
|---------|-----------|----------------------|
| **Crop Status** | Farmer's lifecycle stage | ✅ YES - Only GROWING allowed |
| **Growth Stage** | Biological phase | ❌ NO - Informational only |
| **BlockchainStatus** | Certification approval | ✅ YES - PENDING→CONFIRMED |
| **ProductStatus** | Supply chain stage | ⚠️ TRACKING - Not blocking |
| **LabTest** | Quality verification | ✅ YES - Optional requirement |
| **QR Code** | Consumer verification | ✅ YES - Final proof |

---

**Last Updated:** March 16, 2026  
**Version:** 1.0
