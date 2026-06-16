package com.goldleaf.feature.cropmanagement.ui.compliance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.goldleaf.core.data.local.ComplianceChecklistEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceTrackingScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    viewModel: ComplianceTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(farmId) {
        viewModel.loadItems(farmId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compliance Checklist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add item")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            uiState.syncMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (msg.contains("failed", true)) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            CategoryFilterBar(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategoryFilter(it) }
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                EmptyComplianceState(onAddClick = { showAddDialog = true })
            } else {
                ComplianceList(
                    items = uiState.items,
                    onStatusChange = { item, status -> viewModel.updateStatus(item, status) },
                    onEvidenceCapture = { item, uri ->
                        viewModel.captureEvidence(item, uri)
                    },
                    onNotesChange = { item, notes -> viewModel.updateNotes(item, notes) },
                    onDelete = { viewModel.deleteItem(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddComplianceDialog(
            farmId = farmId,
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, name, desc, due ->
                viewModel.addItem(farmId = farmId, farmerId = null, category = cat, itemName = name, description = desc, dueDate = due)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val allCats = listOf("ALL") + categories
    ScrollableTabRow(
        selectedTabIndex = allCats.indexOf(selectedCategory).coerceAtLeast(0),
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 8.dp
    ) {
        allCats.forEach { cat ->
            Tab(
                selected = selectedCategory == cat,
                onClick = { onCategorySelected(cat) },
                text = { Text(if (cat == "ALL") "All" else cat.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun ComplianceList(
    items: List<ComplianceChecklistEntity>,
    onStatusChange: (ComplianceChecklistEntity, String) -> Unit,
    onEvidenceCapture: (ComplianceChecklistEntity, Uri) -> Unit,
    onNotesChange: (ComplianceChecklistEntity, String) -> Unit,
    onDelete: (ComplianceChecklistEntity) -> Unit
) {
    val context = LocalContext.current
    var pendingGalleryItem by remember { mutableStateOf<ComplianceChecklistEntity?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { pendingGalleryItem?.let { item -> onEvidenceCapture(item, it) } }
        pendingGalleryItem = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            val photoFile = remember(item.evidenceLocalPath) {
                item.evidenceLocalPath?.takeIf { it.isNotBlank() }?.let(::File)
            }
            val photoExists = photoFile?.exists() == true

            ComplianceItemCard(
                item = item,
                photoExists = photoExists,
                photoFile = if (photoExists) photoFile else null,
                onStatusChange = { s -> onStatusChange(item, s) },
                onPickGallery = {
                    pendingGalleryItem = item
                    galleryLauncher.launch("image/*")
                },
                onNotesChange = { notes -> onNotesChange(item, notes) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun ComplianceItemCard(
    item: ComplianceChecklistEntity,
    photoExists: Boolean = false,
    photoFile: File? = null,
    onStatusChange: (String) -> Unit,
    onPickGallery: () -> Unit,
    onNotesChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val statusColors = mapOf(
        "COMPLIANT" to Color(0xFF4CAF50),
        "NON_COMPLIANT" to Color(0xFFF44336),
        "PENDING" to Color(0xFFFF9800),
        "NOT_APPLICABLE" to Color(0xFF607D8B)
    )
    val statusIcons = mapOf(
        "COMPLIANT" to Icons.Default.CheckCircle,
        "NON_COMPLIANT" to Icons.Default.Cancel,
        "PENDING" to Icons.Default.Schedule,
        "NOT_APPLICABLE" to Icons.Default.RemoveCircle
    )
    val catIcons = mapOf(
        "CERTIFICATION" to Icons.Default.Verified,
        "SAFETY" to Icons.Default.Shield,
        "ENVIRONMENTAL" to Icons.Default.Eco,
        "QUALITY" to Icons.Default.Science,
        "LABOR" to Icons.Default.Groups,
        "OTHER" to Icons.Default.Task
    )

    val color = statusColors[item.status] ?: Color.Gray
    val icon = statusIcons[item.status] ?: Icons.Default.Help

    var showNotesDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = catIcons[item.category] ?: Icons.Default.Task,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.itemName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        item.category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Toggle details")
                }
            }

            if (item.description != null) {
                Text(item.description!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(item.status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = color)
                if (item.dueDate != null) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(2.dp))
                    Text(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.dueDate!!)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Photo preview
            if (photoExists && photoFile != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoFile),
                    contentDescription = "Evidence photo",
                    modifier = Modifier.fillMaxWidth().height(160.dp).clickable { /* expand */ },
                    contentScale = ContentScale.Crop
                )
            }
            if (item.evidenceUrl != null) {
                Text("Verified: ${item.evidenceUrl}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = item.status == "COMPLIANT",
                        onClick = { onStatusChange("COMPLIANT") },
                        label = { Text("Compliant") },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = item.status == "NON_COMPLIANT",
                        onClick = { onStatusChange("NON_COMPLIANT") },
                        label = { Text("Non-Compliant") },
                        leadingIcon = { Icon(Icons.Default.Cancel, null, Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = item.status == "PENDING",
                        onClick = { onStatusChange("PENDING") },
                        label = { Text("Pending") }
                    )
                    FilterChip(
                        selected = item.status == "NOT_APPLICABLE",
                        onClick = { onStatusChange("NOT_APPLICABLE") },
                        label = { Text("N/A") }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onPickGallery,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pick from gallery"
                        )
                    }
                    IconButton(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Add notes"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (item.notes != null) {
                    Text("Notes: ${item.notes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showNotesDialog) {
        NotesDialog(
            currentNotes = item.notes,
            onDismiss = { showNotesDialog = false },
            onSave = { notes ->
                onNotesChange(notes)
                showNotesDialog = false
            }
        )
    }
}

@Composable
private fun NotesDialog(currentNotes: String?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var notes by remember { mutableStateOf(currentNotes ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notes") },
        text = { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, maxLines = 5, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { onSave(notes) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EmptyComplianceState(onAddClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Checklist, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text("No compliance items", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Add your first compliance item", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onAddClick) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Item") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddComplianceDialog(
    farmId: String,
    onDismiss: () -> Unit,
    onConfirm: (category: String, name: String, description: String?, dueDate: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("CERTIFICATION") }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val categories = listOf("CERTIFICATION", "SAFETY", "ENVIRONMENTAL", "QUALITY", "LABOR", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Compliance Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth())

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (dueDateMillis != null) "Due: ${dateFmt.format(Date(dueDateMillis!!))}" else "Set Due Date (optional)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedCategory, name, description.ifBlank { null }, dueDateMillis) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { dueDateMillis = dpState.selectedDateMillis; showDatePicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dpState)
        }
    }
}
