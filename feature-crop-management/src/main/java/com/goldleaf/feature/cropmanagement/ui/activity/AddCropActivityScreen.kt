package com.goldleaf.feature.cropmanagement.ui.activity


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.core.data.local.CropStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropTasksScreen(
    cropId: String,
    onNavigateBack: () -> Unit = {},
    onActivitySaved: () -> Unit = {},
    viewModel: AddCropActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    // Form States
    var selectedActivityType by remember { mutableStateOf<ActivityType?>(null) }
    var activityDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Validation State
    var attemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCropInfo(cropId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onActivitySaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Activity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.error?.let { message ->
                ErrorBanner(message = message)
            }

            // 1. Crop Info Card
            uiState.cropInfo?.let { crop ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Spa, null, tint = colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(crop.name, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant)
                            Text("${crop.variety} • ${crop.farmName}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // 2. Activity Type Selection (Mandatory)
            Text(
                text = "Activity Type *",
                fontWeight = FontWeight.SemiBold,
                color = if (attemptedSave && selectedActivityType == null) colorScheme.error else colorScheme.onBackground
            )

            ActivityType.values().forEach { type ->
                ActivityTypeCard(
                    activityType = type,
                    isSelected = selectedActivityType == type,
                    onClick = { selectedActivityType = type }
                )
            }

            // 3. Date Input (Mandatory)
            var showDatePicker by remember { mutableStateOf(false) }
            val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Today, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (activityDate.isNotBlank()) activityDate else "Select date *")
            }
            if (attemptedSave && activityDate.isBlank()) {
                Text("Date is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = if (activityDate.isNotBlank()) {
                        try { dateFormat.parse(activityDate)?.time } catch (_: Exception) { null }
                    } else null
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                activityDate = dateFormat.format(Date(millis))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // 4. Description (Mandatory)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                isError = attemptedSave && description.isBlank(),
                supportingText = {
                    if (attemptedSave && description.isBlank()) Text("Description is required")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // 5. Quantity & Unit (Optional)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f)
                )
            }

            // 6. Cost & Notes (Optional)
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost (KES)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // 7. Save Button
            Button(
                onClick = {
                    attemptedSave = true
                    val isValid = selectedActivityType != null && activityDate.isNotBlank() && description.isNotBlank()

                    if (isValid) {
                        viewModel.saveActivity(
                            cropId = cropId,
                            activityType = selectedActivityType!!,
                            date = activityDate,
                            description = description,
                            quantity = quantity.toDoubleOrNull(),
                            unit = unit.takeIf { it.isNotBlank() },
                            cost = cost.toDoubleOrNull(),
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Activity")
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = colors.onErrorContainer)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Unable to save activity", fontWeight = FontWeight.SemiBold)
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { /* no-op: stateless banner */ }) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}
@Composable
private fun ActivityTypeCard(
    activityType: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorScheme.primaryContainer else colorScheme.surface
        ),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                activityType.icon,
                null,
                tint = if (isSelected) colorScheme.primary else activityType.color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(activityType.displayName, fontWeight = FontWeight.Bold)
                Text(activityType.description, style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = colorScheme.primary)
        }
    }
}



enum class ActivityType(
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
) {
    WATERING("Watering", "Irrigation or manual watering", Icons.Default.WaterDrop, Color(0xFF2196F3)),
    FERTILIZING("Fertilizing", "Applying fertilizer or manure", Icons.Default.Grass, Color(0xFF4CAF50)),
    WEEDING("Weeding", "Removing weeds from farm", Icons.Default.RemoveCircle, Color(0xFFFF9800)),
    PEST_CONTROL("Pest Control", "Spraying pesticides", Icons.Default.BugReport, Color(0xFFE91E63)),
    PRUNING("Pruning", "Trimming and pruning plants", Icons.Default.ContentCut, Color(0xFF9C27B0)),
    HARVESTING("Harvesting", "Collecting mature crops", Icons.Default.Agriculture, Color(0xFF4CAF50)),
    PLANTING("Planting", "Sowing seeds or transplanting", Icons.Default.Yard, Color(0xFF8BC34A)),
    SOIL_PREP("Soil Preparation", "Tilling, ploughing, etc.", Icons.Default.Landscape, Color(0xFF795548)),
    OTHER("Other", "Any other farm activity", Icons.Default.MoreHoriz, Color(0xFF607D8B))
}

data class CropInfo(
    val id: String,
    val name: String,
    val variety: String?,
    val farmName: String? = "Unknown",
    val status: CropStatus?
)
