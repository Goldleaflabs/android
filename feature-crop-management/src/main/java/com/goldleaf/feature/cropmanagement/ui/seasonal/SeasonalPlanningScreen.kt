package com.goldleaf.feature.cropmanagement.ui.seasonal

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.core.data.local.SeasonalPlanEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonalPlanningScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    viewModel: SeasonalPlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(farmId) { viewModel.loadPlans(farmId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seasonal Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Icon(Icons.Default.Add, contentDescription = "Add plan")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SeasonFilterBar(
                selectedSeason = uiState.selectedSeason,
                onSeasonSelected = { viewModel.setSeasonFilter(it) }
            )

            uiState.syncMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (msg.contains("failed", ignoreCase = true))
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.plans.isEmpty()) {
                EmptyPlansState(onAddClick = { showAddDialog = true })
            } else {
                PlansList(
                    plans = uiState.plans,
                    onToggleComplete = { viewModel.toggleComplete(it) },
                    onDelete = { viewModel.deletePlan(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddPlanDialog(
            farmId = farmId,
            onDismiss = { showAddDialog = false },
            onConfirm = { plan ->
                viewModel.addPlan(
                    farmId = plan.farmId,
                    farmerId = plan.farmerId,
                    cropId = plan.cropId,
                    title = plan.title,
                    description = plan.description,
                    eventType = plan.eventType,
                    startDate = plan.startDate,
                    endDate = plan.endDate,
                    season = plan.season
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SeasonFilterBar(
    selectedSeason: String,
    onSeasonSelected: (String) -> Unit
) {
    val seasons = listOf("ALL", "SPRING", "SUMMER", "FALL", "WINTER", "YEAR_ROUND")
    val seasonLabels = mapOf(
        "ALL" to "All",
        "SPRING" to "Spring",
        "SUMMER" to "Summer",
        "FALL" to "Fall",
        "WINTER" to "Winter",
        "YEAR_ROUND" to "All Year"
    )

    ScrollableTabRow(
        selectedTabIndex = seasons.indexOf(selectedSeason).coerceAtLeast(0),
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 8.dp
    ) {
        seasons.forEach { season ->
            Tab(
                selected = selectedSeason == season,
                onClick = { onSeasonSelected(season) },
                text = {
                    Text(
                        text = seasonLabels[season] ?: season,
                        maxLines = 1
                    )
                }
            )
        }
    }
}

@Composable
private fun PlansList(
    plans: List<SeasonalPlanEntity>,
    onToggleComplete: (SeasonalPlanEntity) -> Unit,
    onDelete: (SeasonalPlanEntity) -> Unit
) {
    val grouped = plans.groupBy { plan ->
        val cal = Calendar.getInstance().apply { timeInMillis = plan.startDate }
        cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }.entries.sortedBy { (key, _) -> key.first * 100 + key.second }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (yearMonth, monthPlans) ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, yearMonth.first)
                set(Calendar.MONTH, yearMonth.second)
            }
            val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

            item {
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(monthPlans, key = { it.id }) { plan ->
                PlanCard(
                    plan = plan,
                    onToggleComplete = { onToggleComplete(plan) },
                    onDelete = { onDelete(plan) }
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SeasonalPlanEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val eventColors = mapOf(
        "PLANTING" to Color(0xFF4CAF50),
        "HARVESTING" to Color(0xFFFF9800),
        "FERTILIZING" to Color(0xFF2196F3),
        "IRRIGATION" to Color(0xFF00BCD4),
        "PEST_CONTROL" to Color(0xFFF44336),
        "PRUNING" to Color(0xFF9C27B0),
        "SOIL_PREP" to Color(0xFF795548)
    )

    val eventIcons = mapOf(
        "PLANTING" to Icons.Default.Eco,
        "HARVESTING" to Icons.Default.ShoppingCart,
        "FERTILIZING" to Icons.Default.Science,
        "IRRIGATION" to Icons.Default.WaterDrop,
        "PEST_CONTROL" to Icons.Default.BugReport,
        "PRUNING" to Icons.Default.ContentCut,
        "SOIL_PREP" to Icons.Default.Handyman
    )

    val color = eventColors[plan.eventType] ?: MaterialTheme.colorScheme.secondary
    val icon = eventIcons[plan.eventType] ?: Icons.Default.EventNote

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )

            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (plan.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(Date(plan.startDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val desc = plan.description
                if (desc != null) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (plan.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (plan.isCompleted) "Completed" else "Mark complete",
                    tint = if (plan.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyPlansState(onAddClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No plans yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first seasonal plan to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Plan")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlanDialog(
    farmId: String,
    onDismiss: () -> Unit,
    onConfirm: (SeasonalPlanEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PLANTING") }
    var selectedSeason by remember { mutableStateOf("SPRING") }
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val eventTypes = listOf(
        "PLANTING" to "Planting",
        "FERTILIZING" to "Fertilizing",
        "HARVESTING" to "Harvesting",
        "IRRIGATION" to "Irrigation",
        "PEST_CONTROL" to "Pest Control",
        "PRUNING" to "Pruning",
        "SOIL_PREP" to "Soil Prep",
        "OTHER" to "Other"
    )

    val seasons = listOf(
        "SPRING" to "Spring",
        "SUMMER" to "Summer",
        "FALL" to "Fall",
        "WINTER" to "Winter",
        "YEAR_ROUND" to "All Year"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Seasonal Plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Event Type", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    eventTypes.take(4).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedType == key,
                            onClick = { selectedType = key },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    eventTypes.drop(4).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedType == key,
                            onClick = { selectedType = key },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }

                Text("Season", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    seasons.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedSeason == key,
                            onClick = { selectedSeason = key },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: ${dateFormat.format(Date(startDateMillis))}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            SeasonalPlanEntity(
                                id = "",
                                farmId = farmId,
                                farmerId = null,
                                cropId = null,
                                title = title,
                                description = description.ifBlank { null },
                                eventType = selectedType,
                                startDate = startDateMillis,
                                endDate = null,
                                season = selectedSeason
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDateMillis = it }
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
}
