# ManageCropStatusSheet - User & Developer Manual

## 📖 Table of Contents
1. [Overview](#overview)
2. [Visual Interface](#visual-interface)
3. [Components](#components)
4. [How It Works](#how-it-works)
5. [User Workflows](#user-workflows)
6. [Developer Guide](#developer-guide)
7. [Validation Logic](#validation-logic)
8. [FAQ](#faq)

---

## 🎯 Overview

**ManageCropStatusSheet** is a Material3 bottom sheet component that allows farmers to **safely manage and transition their crop's lifecycle status**. It provides a visual, intuitive interface that:

- ✅ Shows crop lifecycle progression visually with a slider
- ✅ Prevents backwards transitions (forward-only movement)
- ✅ Displays valid next statuses as clickable options
- ✅ Warns about irreversible "FAILED" status
- ✅ Locks terminal states (COMPLETED, FAILED)
- ✅ Provides real-time validation

**Key Feature:** Farmers can ONLY advance their crop forward through the lifecycle, never backwards.

```
PLANNED → PLANTED → GROWING → HARVESTED → COMPLETED
   ↓        ↓         ↓          ↓
   └──────→ FAILED (from any status)
```

---

## 🎨 Visual Interface

### When the Sheet Opens
```
┌─────────────────────────────────────────┐
│  Manage Crop Status                     │  ← Header Title
├─────────────────────────────────────────┤
│                                         │
│  Current Status: [GROWING]              │  ← Current status badge
│                                         │
│  📊 Crop Lifecycle Progression          │
│  ████████░░ (60%)                       │  ← Progress bar
│  Current: Growing    3/5                │  ← Progress text
│                                         │
│  1️⃣ ✓ ✓ ✓ → → 5️⃣                        │  ← Status milestones
│  P  P  G  H  C                          │    (only future ones clickable)
│                                         │
│  Next possible status:                  │
│  ┌──────────┬──────────┐                │
│  │ Harvested│  Failed  │                │  ← Clickable chips
│  └──────────┴──────────┘                │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ ⚠️ Mark as Failed                    ││
│  │ If this crop has failed or cannot   ││
│  │ proceed, mark as failed. This is    ││
│  │ irreversible.                       ││
│  │                       [MARK AS FAILED]││  ← Red button
│  └─────────────────────────────────────┘│
│                                         │
│ (User swipes down to close)             │
└─────────────────────────────────────────┘
```

### Key Visual Elements

| Element | What It Shows | User Interaction |
|---------|--------------|------------------|
| **Title** | "Manage Crop Status" | Read-only |
| **Current Status Badge** | Colored badge with current status name | Read-only |
| **Progress Bar** | Horizontal bar showing % completion | Read-only |
| **Numbered Circles** | 5 circles (1-5) representing each status | Tap circle 4 or 5 to advance |
| **Checkmarks** | ✓ on completed circles | Visual feedback |
| **Connecting Lines** | Lines between circles | Color changes as you progress |
| **Next Valid Statuses** | Clickable chips showing what's next | Click to transition |
| **Failed Warning Card** | Red card warning about marking as failed | Click button to mark failed |

---

## 🧩 Components

### 1. **ManageCropStatusSheet** (Container)

```kotlin
@Composable
fun ManageCropStatusSheet(
    crop: CropEntity,                    // The crop being managed
    onStatusUpdated: (CropStatus) -> Unit, // Callback when user chooses new status
    onDismiss: () -> Unit                // Called when user closes sheet
)
```

**Responsibility:** Container component that wraps the UI in a `ModalBottomSheet`

**Props:**
- `crop` - Current crop data containing status, name, etc.
- `onStatusUpdated` - Triggered when farmer selects a new status
- `onDismiss` - Called when sheet is dismissed (user swipes down)

**How it works:**
1. Displays crop information at top
2. Renders `CropStatusProgressSlider` component
3. Passes callbacks to slider
4. Handles sheet opening/closing

---

### 2. **CropStatusProgressSlider** (Visual Slider)

```kotlin
@Composable
fun CropStatusProgressSlider(
    currentStatus: CropStatus,           // What status crop is at NOW
    onStatusSelected: (CropStatus) -> Unit, // Fired when user clicks a future status
    modifier: Modifier = Modifier,       // Layout modifier
    enabled: Boolean = true              // Whether interactions are enabled
)
```

**Responsibility:** Renders the visual slider with milestones and lifecycle info

**Key Parts:**

#### A. Progress Bar Section
```
📊 Crop Lifecycle Progression
████████░░ (60%)
Current: Growing    3/5
```
- Shows % of lifecycle completed
- Displays current status name
- Shows position (3 out of 5 statuses)

#### B. Status Milestones Section
```
1️⃣ ✓ ✓ ✓ → → 5️⃣
P  P  G  H  C
```
- 5 numbered circles for each status
- Completed stages show checkmarks ✓
- Current stage is larger/highlighted
- Connecting lines show progression
- **Future circles are clickable** (highlighted as pressable)
- **Past circles are disabled** (greyed out)

#### C. Next Possible Status Card
```
Next possible status:
┌──────────┬──────────┐
│ Harvested│  Failed  │
└──────────┴──────────┘
```
- Shows all valid transitions from current status
- Displayed as clickable chips
- Each can be tapped to transition

#### D. Failed Status Warning
```
⚠️ Mark as Failed
If this crop has failed or cannot proceed,
mark as failed. This is irreversible.
                    [MARK AS FAILED]
```
- Only shows if FAILED is a valid transition from current status
- Red background (error color)
- Red button for confirmation
- Warning text about irreversibility

---

## ⚙️ How It Works

### Step-by-Step Flow

**1. User Opens Crop Details Screen**
```
Farmer taps on a crop from the farm dashboard
↓
CropDetailsScreen loads
```

**2. User Taps "Manage Status" Button**
```kotlin
// In CropDetailsScreen bottom bar:
BottomActionButton(
    label = "Manage Status",
    onClick = { showStatusSheet = true }
)

// This triggers:
var showStatusSheet by remember { mutableStateOf(false) }
```

**3. Sheet Opens with ManageCropStatusSheet**
```kotlin
if (showStatusSheet && uiState.crop != null) {
    ManageCropStatusSheet(
        crop = uiState.crop!!,
        onStatusUpdated = { newStatus ->
            viewModel.updateCropStatus(newStatus)
        },
        onDismiss = { showStatusSheet = false }
    )
}
```

**4. CropStatusProgressSlider Renders**
- Calculates current position based on `currentStatus`
- Determines which circles are clickable
- Renders all visual elements

**5. User Selects a New Status**

**Option A: Click a Future Milestone Circle**
```
User sees: 1️⃣ ✓ ✓ ✓ [→] 5️⃣
User taps circle 5 (HARVESTED)
↓
onStatusSelected(CropStatus.HARVESTED) called
```

**Option B: Click a Valid Status Chip**
```
User sees: [Harvested] [Failed]
User taps "Harvested" chip
↓
onStatusSelected(CropStatus.HARVESTED) called
```

**Option C: Click "Mark as Failed" Button**
```
User reads warning
User taps red "MARK AS FAILED" button
↓
onStatusSelected(CropStatus.FAILED) called
↓
Sheet closes
↓
Permanent warning appears (cannot undo)
```

**6. ViewModel Updates Status**
```kotlin
onStatusUpdated = { newStatus ->
    viewModel.updateCropStatus(newStatus)  // ← Called
}

// In ViewModel:
fun updateCropStatus(newStatus: CropStatus) {
    // ✅ Validates transition is allowed
    if (!currentCrop.status.canTransitionTo(newStatus)) {
        showError("Invalid transition")
        return
    }
    
    // ✅ Updates in database
    // ✅ Updates UI state
    // ✅ Shows success message
}
```

**7. Sheet Closes**
```kotlin
onDismiss = { showStatusSheet = false }

// Result:
- Crop status updated in database
- UI reflects new status
- Progress bar updated
- Farmer can see changes in crop details
```

---

## 👥 User Workflows

### Workflow 1: Normal Progression (Farmer Advancing Crop)

```
Farmer plants seeds
↓
Opens Crop Details → Taps "Manage Status"
↓
Sees: Current Status: PLANTED, circles 1-2 completed
↓
Taps circle 3 (GROWING)
↓
Confirms transition
↓
Status changes to GROWING ✓
↓
Later: When crop is ready to harvest
↓
Opens Crop Details → "Manage Status"
↓
Sees: Current Status: GROWING, circles 1-3 completed
↓
Taps circle 4 (HARVESTED)
↓
Status changes to HARVESTED ✓
```

### Workflow 2: Immediate Failure (Disease/Pest)

```
Farmer notices crop has disease
↓
Opens Crop Details → "Manage Status"
↓
Sees red warning card: "Mark as Failed"
↓
Reads: "If this crop has failed, this is irreversible"
↓
Taps red button: "MARK AS FAILED"
↓
Crop status → FAILED (terminal state)
↓
Cannot change status anymore
↓
Button is now disabled
```

### Workflow 3: Trying Invalid Action (Backwards)

```
Farmer accidentally tries to click past status
↓
Appears greyed out/disabled
↓
System prevents any action
↓
No error shown (already prevented)
↓
Only future statuses are clickable
```

### Workflow 4: Terminal State (Crop Complete)

```
Farmer finishes entire harvest
↓
Final transition: HARVESTED → COMPLETED
↓
Opens "Manage Status" again
↓
Sees: "This crop has reached a final state"
↓
Progress bar is 100% full
↓
All buttons disabled
↓
Cannot make changes anymore
↓
Crop is read-only
```

---

## 👨‍💻 Developer Guide

### Integration in CropDetailsScreen

```kotlin
@Composable
fun CropDetailsScreen(
    cropId: String,
    onNavigateBack: () -> Unit,
    viewModel: CropDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 1. State for managing sheet visibility
    var showStatusSheet by remember { mutableStateOf(false) }

    // 2. Show the sheet if requested
    if (showStatusSheet && uiState.crop != null) {
        ManageCropStatusSheet(
            crop = uiState.crop!!,
            onStatusUpdated = { newStatus ->
                // 3. ViewModel handles the update
                viewModel.updateCropStatus(newStatus)
            },
            onDismiss = { 
                // 4. Close sheet
                showStatusSheet = false 
            }
        )
    }

    // 5. Bottom bar with button
    Scaffold(
        bottomBar = {
            BottomActionButton(
                label = "Manage Status",
                onClick = { showStatusSheet = true }
            )
        }
    ) {
        // Screen content
    }
}
```

### Validation in ViewModel

```kotlin
fun updateCropStatus(newStatus: CropStatus) {
    viewModelScope.launch {
        val currentCrop = _uiState.value.crop
        
        if (currentCrop != null) {
            // ✅ VALIDATION: Check if transition is allowed
            if (!currentCrop.status.canTransitionTo(newStatus)) {
                _uiState.value = _uiState.value.copy(
                    error = "Cannot transition from ${currentCrop.status.getDisplayName()} " +
                            "to ${newStatus.getDisplayName()}"
                )
                return@launch
            }

            // ✅ Create updated crop object
            val updatedCrop = currentCrop.copy(
                status = newStatus,
                updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(Date())
            )

            // ✅ Save to database using use case
            val result = updateCropUseCase(updatedCrop)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    crop = updatedCrop,
                    message = "Crop status updated to ${newStatus.getDisplayName()}"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Failed to update crop status"
                )
            }
        }
    }
}
```

### Status Validation (CropStatus.kt)

```kotlin
enum class CropStatus {
    PLANNED, PLANTED, GROWING, HARVESTED, COMPLETED, FAILED;

    // Get order for validation
    fun getOrder(): Int = when (this) {
        PLANNED -> 0
        PLANTED -> 1
        GROWING -> 2
        HARVESTED -> 3
        COMPLETED -> 4
        FAILED -> -1
    }

    // Check if transition is valid
    fun canTransitionTo(nextStatus: CropStatus): Boolean {
        // FAILED can be reached from any status
        if (nextStatus == FAILED) return true
        
        // Cannot transition FROM terminal states
        if (this == COMPLETED || this == FAILED) return false
        
        // Otherwise, only allow forward progression
        return nextStatus.getOrder() > this.getOrder()
    }

    // List valid next statuses
    fun getValidNextStatuses(): List<CropStatus> {
        return when (this) {
            PLANNED -> listOf(PLANTED, FAILED)
            PLANTED -> listOf(GROWING, FAILED)
            GROWING -> listOf(HARVESTED, FAILED)
            HARVESTED -> listOf(COMPLETED, FAILED)
            COMPLETED -> emptyList()
            FAILED -> emptyList()
        }
    }
}
```

---

## 🛡️ Validation Logic

### Status Transition Rules

```
From PLANNED:
  ✅ Can go to: PLANTED, FAILED
  ❌ Cannot go to: GROWING, HARVESTED, COMPLETED

From PLANTED:
  ✅ Can go to: GROWING, FAILED
  ❌ Cannot go to: HARVESTED, COMPLETED (must go through GROWING)

From GROWING:
  ✅ Can go to: HARVESTED, FAILED
  ❌ Cannot go to: PLANTED (cannot go backwards)

From HARVESTED:
  ✅ Can go to: COMPLETED, FAILED
  ❌ Cannot go to: Any other status (must go forward)

From COMPLETED:
  ✅ Can go to: NOTHING (terminal state)
  ❌ All transitions disabled

From FAILED:
  ✅ Can go to: NOTHING (terminal state)
  ❌ All transitions disabled

SPECIAL: FAILED Status
  - Can be reached from: PLANNED, PLANTED, GROWING, HARVESTED
  - Cannot be undone
  - When FAILED: crop.status becomes immutable
  - Red warning displayed to user
```

### Validation Layers

```
1. UI LAYER (CropStatusProgressSlider)
   - Only shows future circles as clickable
   - Disables past circles
   - Disables buttons on terminal states
   
2. COMPONENT LAYER (ManageCropStatusSheet)
   - Passes selected status to ViewModel
   
3. VIEWMODEL LAYER (CropDetailsViewModel)
   - Calls canTransitionTo() to validate
   - Returns error if invalid
   - Prevents save if invalid
   
4. DATA LAYER (CropStatus.kt)
   - Validates programmatically
   - Returns true/false for transitions
   - Lists valid next statuses
```

---

## ❓ FAQ

### Q1: Can a farmer go backwards (e.g., HARVESTED → GROWING)?
**A:** No. The system prevents backwards transitions at multiple levels:
- UI: Past circles are disabled and not clickable
- ViewModel: Calls `canTransitionTo()` which returns false
- Result: User cannot change status backwards

### Q2: What happens if a farmer marks a crop as FAILED?
**A:** 
- Status becomes FAILED (immutable)
- All buttons disable
- Cannot change status anymore
- Recorded in database for history
- Cannot undo or recover

### Q3: Can a farmer skip statuses (e.g., PLANTED → HARVESTED)?
**A:** No. The system enforces linear progression:
- PLANNED → PLANTED → GROWING → HARVESTED → COMPLETED
- Each step must be completed
- Skipping is not allowed

### Q4: What if the ViewModel fails to update status?
**A:**
1. Error message shown to user
2. Status does NOT change in UI
3. Sheet remains open for retry
4. User can try again or dismiss

### Q5: Can a farmer transition from COMPLETED to something else?
**A:** No. COMPLETED is a terminal state:
- All buttons become disabled
- Sheet shows "This crop has reached a final state"
- Status is locked permanently

### Q6: Is there a confirmation dialog before marking as FAILED?
**A:** The red button serves as confirmation:
- Big, visible warning card
- Text explains it's irreversible
- Red color indicates danger
- User must intentionally click button

### Q7: What happens to batches if status changes?
**A:** 
- Batch status stays PENDING or CONFIRMED
- Crop lifecycle is separate from certification
- Status change doesn't affect certification

### Q8: Can admin/officer override statuses?
**A:** Not through this interface. Admin capabilities would need:
- Separate admin screen
- Additional roles/permissions check
- Audit logging
- (Not currently implemented)

---

## 🔗 Related Files

- **CropStatusProgressSlider.kt** - UI component with visual slider
- **CropDetailsScreen.kt** - Container screen that uses the sheet
- **CropDetailsViewModel.kt** - Handles status updates
- **CropStatus.kt** - Validation logic
- **CropEntity.kt** - Database model

---

## 📝 Summary

**ManageCropStatusSheet** is a safe, visual interface for managing crop lifecycle transitions that:

1. ✅ Prevents invalid transitions (backwards movement)
2. ✅ Shows next valid options clearly
3. ✅ Warns about irreversible actions (FAILED)
4. ✅ Validates at multiple layers (UI, ViewModel, Data)
5. ✅ Provides clear visual feedback
6. ✅ Handles terminal states gracefully
7. ✅ Integrates seamlessly with CropDetailsScreen

Farmers can confidently manage their crop lifecycle knowing the system prevents mistakes while providing clear guidance on valid next steps.

---

**Last Updated:** March 16, 2026  
**Version:** 1.0
