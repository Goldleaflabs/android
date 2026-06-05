package com.goldleaf.feature.cropmanagement.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.core.data.local.PlotEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSelectionScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    viewModel: SelectionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) onNavigateBack()
    }
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Select Crop", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.catalogCrops.isEmpty()) {
                Text(
                    text = "All crop types are already selected for this farm.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.catalogCrops, key = { it.id }) { crop ->
                        val isSaving = uiState.savingCropIds.contains(crop.id)
                        Card(
                            onClick = { viewModel.onCropSelected(crop) },
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .graphicsLayer { alpha = if (isSaving) 0.5f else 1f },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = crop.name.take(1).uppercase(),
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(crop.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        text = if (isSaving) "Saving..." else crop.variety ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Plot selection dialog
    if (uiState.showPlotDialog && uiState.selectedCrop != null) {
        AddCropPlotDialog(
            cropName = uiState.selectedCrop!!.name,
            plots = uiState.plots,
            onDismiss = { viewModel.dismissPlotDialog() },
            onConfirm = { plotId -> viewModel.addCropWithPlot(uiState.selectedCrop!!, plotId) }
        )
    }
}

@Composable
fun AddCropPlotDialog(
    cropName: String,
    plots: List<PlotEntity>,
    onDismiss: () -> Unit,
    onConfirm: (plotId: String?) -> Unit
) {
    var selectedPlotId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add $cropName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (plots.isNotEmpty()) {
                    Text("Assign to a plot (optional):", style = MaterialTheme.typography.bodyMedium)
                    plots.forEach { plot ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPlotId == plot.id,
                                onClick = { selectedPlotId = plot.id }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${plot.name} (${plot.size} ${plot.sizeUnit})", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Crop rotation will be checked against the selected plot.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("No plots defined yet. You can create them later from the Dashboard.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedPlotId) }) { Text("Add to Farm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
