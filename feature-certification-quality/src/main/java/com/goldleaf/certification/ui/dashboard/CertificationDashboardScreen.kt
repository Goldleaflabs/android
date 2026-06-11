package com.goldleaf.certification.ui.dashboard

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.goldleaf.certification.navigation.CertificationRoutes
import com.goldleaf.certification.presentation.batch.BatchViewModel
import com.goldleaf.certification.presentation.batch.PrintStatus
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.ProductBatchEntity
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationDashboardScreen(
    navController: NavHostController,
    viewModel: BatchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Certification") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncBatches() }) {
                        Icon(Icons.Default.Refresh, "Sync")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            if (ui.farmer != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Create Batch")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                ui.farmer == null -> {
                    Text("Please login", modifier = Modifier.align(Alignment.Center))
                }

                ui.loading && ui.batches.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                ui.error != null && ui.batches.isEmpty() -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ui.error ?: "Error")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBatches() }) { Text("Retry") }
                    }
                }

                ui.batches.isEmpty() -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No batches yet")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Create First Batch")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ui.batches) { batch ->
                            BatchCard(
                                batch = batch,
                                onClick = {
                                    navController.navigate(CertificationRoutes.BatchDetails.createRoute(batch.id))
                                },
                                onPrintLabels = {
                                    viewModel.printLabelsNow(
                                        context = context,
                                        batchId = batch.batchNumber,
                                        qty = batch.quantity,
                                        unit = batch.unit
                                    )
                                },
                                onRequestLabels = {
                                    viewModel.requestLabelsFromOfficer(
                                        batchId = batch.batchNumber,
                                        qty = batch.quantity,
                                        unit = batch.unit,
                                        county = "Kenya"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Print Status Snackbar
            ui.printStatus.let { status ->
                when (status) {
                    is PrintStatus.Printing -> {
                        Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Printing labels...")
                            }
                        }
                    }
                    is PrintStatus.Success -> {
                        LaunchedEffect(status) {
                            viewModel.resetPrintStatus()
                            // Optional: show toast
                        }
                    }
                    is PrintStatus.Error -> {
                        LaunchedEffect(status) {
                            viewModel.resetPrintStatus()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateBatchDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun BatchCard(
    batch: ProductBatchEntity,
    onClick: () -> Unit,
    onPrintLabels: () -> Unit,
    onRequestLabels: () -> Unit,
    isLocationSet: Boolean = true,
    officerPhone: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(batch.batchNumber, fontWeight = FontWeight.Bold)
                    Text(batch.productType, style = MaterialTheme.typography.bodyLarge)
                    Text("${batch.quantity} ${batch.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Harvest: ${batch.harvestDate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BatchStatusChip(status = batch.blockchainStatus.name)
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPrintLabels, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Print")
                }
                Button(
                    onClick = onRequestLabels,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Request")
                }
            }
        }
    }
}

@Composable
fun BatchStatusChip(status: String?) {
    val (containerColor, contentColor) = when (status?.uppercase()) {
        "CONFIRMED" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        "PENDING" -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = containerColor, shape = MaterialTheme.shapes.small) {
        Text(
            text = status?.uppercase() ?: "LOCAL",
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBatchDialog(
    viewModel: BatchViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val readyCrops = ui.readyCrops
    val currentFarmer = ui.farmer
    val isCreating = ui.loading && ui.selectedBatch == null // or use your isCreatingBatch flag

    var selectedCrop by remember { mutableStateOf<CropEntity?>(null) }
    var quantityText by remember { mutableStateOf("") } // Changed from bagsText
    var expanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf<Pair<String, Double>?>(null) }

    // Available packaging units by crop type
    val getAvailableUnits = { cropName: String ->
        when (cropName.lowercase()) {
            "maize", "beans" -> listOf(
                "90kg Bag" to 90.0,
                "50kg Bag" to 50.0,
                "25kg Bag" to 25.0,
                "Loose (kg)" to 1.0
            )
            "coffee", "tea" -> listOf(
                "50kg Bag" to 50.0,
                "25kg Bag" to 25.0,
                "1kg Package" to 1.0
            )
            "tomatoes", "onions" -> listOf(
                "Crate (25kg)" to 25.0,
                "Crate (15kg)" to 15.0,
                "Loose (kg)" to 1.0
            )
            "avocado", "mango" -> listOf(
                "4kg Box" to 4.0,
                "2kg Box" to 2.0,
                "Loose (kg)" to 1.0
            )
            else -> listOf("Unit (kg)" to 1.0)
        }
    }

    val availableUnits = remember(selectedCrop) {
        selectedCrop?.let { getAvailableUnits(it.name) } ?: emptyList()
    }

    // Auto-select first unit when crop changes
    LaunchedEffect(selectedCrop) {
        selectedCrop?.let {
            selectedUnit = getAvailableUnits(it.name).firstOrNull()
        } ?: run {
            selectedUnit = null
        }
    }

    val unitInfo = selectedUnit ?: ("Unit (kg)" to 1.0)

    val quantityCount = quantityText.toIntOrNull() ?: 0
    val totalWeightKg = quantityCount * unitInfo.second
    val isValid = selectedCrop != null && quantityCount > 0 && currentFarmer != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Harvest & Create Batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (readyCrops.isEmpty()) {
                    Text("No crops ready for harvest", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCrop?.let { "${it.name} ${it.variety} • ${it.area}ha" }
                                ?: "Select Crop",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable,true)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            readyCrops.forEach { crop ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${crop.name} ${crop.variety}", fontWeight = FontWeight.Medium)
                                            Text(
                                                "${crop.area}ha • Planted ${crop.plantingDate}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCrop = crop
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedCrop != null) {
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit?.first ?: "Select Packaging Unit",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Packaging Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            availableUnits.forEach { (unitName, weightMultiplier) ->
                                DropdownMenuItem(
                                    text = { Text(unitName) },
                                    onClick = {
                                        selectedUnit = unitName to weightMultiplier
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            quantityText = input
                        }
                    },
                    // Label now changes based on crop!
                    label = { Text("Number of ${unitInfo.first}s") },
                    placeholder = { Text("Enter quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedCrop != null && currentFarmer != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Batch Preview", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            PreviewRow("Product", "${selectedCrop!!.name} (${selectedCrop!!.variety})")
                            PreviewRow("Unit Type", unitInfo.first)
                            PreviewRow("Total Weight", "${String.format("%.1f", totalWeightKg)} kg")
                            currentFarmer.location?.let { PreviewRow("Origin", it) }
                        }
                    }
                }

                // Show sync errors from ViewModel inside the dialog
                ui.error?.let { err ->
                    Spacer(Modifier.height(8.dp))
                    Text(text = err, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCrop?.let { crop ->
                        viewModel.createHarvestBatch(
                            crop = crop,
                            bags = quantityCount,
                            farmName = crop.name,
                            onSuccess = onDismiss
                        )
                    }
                },
                enabled = isValid && !isCreating
            ) {
                Text("Confirm Harvest")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
