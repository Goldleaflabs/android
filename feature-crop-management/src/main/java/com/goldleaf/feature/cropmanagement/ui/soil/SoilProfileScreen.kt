package com.goldleaf.feature.cropmanagement.ui.soil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goldleaf.core.data.local.SoilTestEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilProfileScreen(
    farmId: String,
    farmerId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: SoilProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(farmId) {
        viewModel.loadTests(farmId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soil Profile & History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Test")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.tests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No soil tests recorded yet", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.showAddDialog() }) {
                            Text("Add Soil Test")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tests) { test ->
                        SoilTestCard(test, onDelete = { viewModel.deleteTest(test) })
                    }
                }
            }

            uiState.syncMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    action = {
                        TextButton(onClick = { /* dismiss */ }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddSoilTestDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { soilType, ph, n, p, k, om, moisture, ec, labName, notes ->
                viewModel.addTest(
                    farmId = farmId,
                    farmerId = farmerId,
                    soilType = soilType,
                    ph = ph,
                    nitrogen = n,
                    phosphorus = p,
                    potassium = k,
                    organicMatter = om,
                    moisture = moisture,
                    ec = ec,
                    labName = labName,
                    notes = notes
                )
            }
        )
    }
}

@Composable
fun SoilTestCard(test: SoilTestEntity, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(test.testDate)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    test.labName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientChip("Soil", test.soilType)
                NutrientChip("pH", test.ph.toString())
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientChip("N", "${test.nitrogen} ppm")
                NutrientChip("P", "${test.phosphorus} ppm")
                NutrientChip("K", "${test.potassium} ppm")
            }

            test.organicMatter?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Organic Matter: ${it}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            test.moisture?.let {
                Text(
                    text = "Moisture: ${it}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            test.notes?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NutrientChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddSoilTestDialog(
    onDismiss: () -> Unit,
    onConfirm: (soilType: String, ph: Double, n: Double, p: Double, k: Double,
                  organicMatter: Double?, moisture: Double?, ec: Double?,
                  labName: String?, notes: String?) -> Unit
) {
    var soilType by remember { mutableStateOf("Loam") }
    var ph by remember { mutableStateOf("7.0") }
    var nitrogen by remember { mutableStateOf("") }
    var phosphorus by remember { mutableStateOf("") }
    var potassium by remember { mutableStateOf("") }
    var organicMatter by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var ec by remember { mutableStateOf("") }
    var labName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val soilTypes = listOf("Clay", "Sandy", "Loam", "Clay Loam", "Sandy Loam", "Silt Loam")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Soil Test") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = soilType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Soil Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        soilTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    soilType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ph,
                        onValueChange = { ph = it },
                        label = { Text("pH") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nitrogen,
                        onValueChange = { nitrogen = it },
                        label = { Text("N (ppm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phosphorus,
                        onValueChange = { phosphorus = it },
                        label = { Text("P (ppm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = potassium,
                        onValueChange = { potassium = it },
                        label = { Text("K (ppm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = organicMatter,
                        onValueChange = { organicMatter = it },
                        label = { Text("Organic Matter %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = moisture,
                        onValueChange = { moisture = it },
                        label = { Text("Moisture %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = ec,
                    onValueChange = { ec = it },
                    label = { Text("EC (dS/m)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = labName,
                    onValueChange = { labName = it },
                    label = { Text("Lab Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        soilType,
                        ph.toDoubleOrNull() ?: 7.0,
                        nitrogen.toDoubleOrNull() ?: 0.0,
                        phosphorus.toDoubleOrNull() ?: 0.0,
                        potassium.toDoubleOrNull() ?: 0.0,
                        organicMatter.toDoubleOrNull(),
                        moisture.toDoubleOrNull(),
                        ec.toDoubleOrNull(),
                        labName.ifBlank { null },
                        notes.ifBlank { null }
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
