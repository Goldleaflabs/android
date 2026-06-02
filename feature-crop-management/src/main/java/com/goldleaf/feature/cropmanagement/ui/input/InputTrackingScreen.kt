package com.goldleaf.feature.cropmanagement.ui.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.core.data.local.InputEntity
import com.goldleaf.core.data.local.InputType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTrackingScreen(
    cropId: String,
    farmId: String,
    farmerId: String,
    onNavigateBack: () -> Unit,
    viewModel: InputTrackingViewModel = hiltViewModel()
) {
    val inputs by viewModel.inputs.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cropId) { viewModel.loadInputs(cropId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inputs & Applications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add input")
                    }
                }
            )
        }
    ) { padding ->
        if (inputs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Spa, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("No inputs recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showDialog = true }) { Text("Add your first input") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(inputs, key = { it.id }) { input ->
                    InputCard(input = input, onDelete = { viewModel.deleteInput(input) })
                }
            }
        }
    }

    if (showDialog) {
        AddInputDialog(
            onDismiss = { showDialog = false },
            onConfirm = { type, name, qty, unit, date, cost, supplier, notes ->
                viewModel.addInput(cropId, farmId, farmerId, type, name, qty, unit, date, cost, supplier, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun InputCard(input: InputEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (input.type) {
                    InputType.SEED -> Icons.Default.Grass
                    InputType.FERTILIZER -> Icons.Default.Science
                    InputType.PESTICIDE, InputType.HERBICIDE, InputType.FUNGICIDE -> Icons.Default.BugReport
                    InputType.OTHER -> Icons.Default.Build
                },
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(input.name, fontWeight = FontWeight.Medium)
                Text("${input.type.name} · ${input.quantity} ${input.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(input.applicationDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (input.cost > 0) Text("KES ${input.cost}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (InputType, String, Double, String, String, Double, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(InputType.FERTILIZER) }
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var date by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Input") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    InputType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.take(4), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost (KES)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: return@TextButton
                    if (name.isNotBlank() && date.isNotBlank()) {
                        onConfirm(selectedType, name, qty, unit, date, cost.toDoubleOrNull() ?: 0.0, supplier, notes)
                    }
                },
                enabled = name.isNotBlank() && date.isNotBlank() && quantity.toDoubleOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
