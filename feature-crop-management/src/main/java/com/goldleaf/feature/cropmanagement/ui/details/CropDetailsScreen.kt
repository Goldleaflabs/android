package com.goldleaf.feature.cropmanagement.ui.details


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.core.data.local.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailsScreen(
    cropId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddTask: (String) -> Unit,
    viewModel: CropDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    // State for sheets
    var showMigrationSheet by remember { mutableStateOf(false) }
    var showMonitoringSheet by remember { mutableStateOf(false) }
    var showStatusSheet by remember { mutableStateOf(false) }

    LaunchedEffect(cropId) {
        viewModel.loadCropDetails(cropId)
    }

    // --- KANBAN MIGRATION SHEET ---
    if (showMigrationSheet) {
        StageMigrationSheet(
            currentStage = uiState.cropDetails?.growthStage ?: "Unknown",
            onDismiss = { showMigrationSheet = false },
            onMigrate = { nextStageName ->
                viewModel.transitionGrowthStage(nextStageName)
                showMigrationSheet = false
            }
        )
    }

    // --- MONITORING RECORD SHEET ---
    if (showMonitoringSheet) {
        AddMonitoringRecordSheet(
            cropId = cropId,
            onDismiss = { showMonitoringSheet = false },
            onSave = { record ->
                android.util.Log.d("🌾 MONITORING", "📥 Sheet callback: Passing record to viewModel")
                viewModel.addMonitoringRecord(record)
                showMonitoringSheet = false
            }
        )
    }

    // --- STATUS MANAGEMENT SHEET ---
    if (showStatusSheet && uiState.crop != null) {
        ManageCropStatusSheet(
            crop = uiState.crop!!,
            onStatusUpdated = { newStatus ->
                viewModel.updateCropStatus(newStatus)
            },
            onDismiss = { showStatusSheet = false },
            pipelineStages = uiState.pipelineStages
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.crop?.name ?: "Crop Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.onPrimaryContainer,
                    navigationIconContentColor = colorScheme.onPrimaryContainer,
                    actionIconContentColor = colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Show different actions based on selected tab
                    when (selectedTab) {
                        DetailTab.MONITORING -> {
                            BottomActionButton(
                                label = "Add Record",
                                icon = Icons.Default.Add,
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary,
                                modifier = Modifier.weight(1f),
                                onClick = { showMonitoringSheet = true }
                            )
                        }
                        DetailTab.TASKS -> {
                            BottomActionButton(
                                label = "New Task",
                                icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                containerColor = colorScheme.secondaryContainer,
                                contentColor = colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToAddTask(cropId) }
                            )
                        }
                        else -> {
                            BottomActionButton(
                                label = "New Task",
                                icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                containerColor = colorScheme.secondaryContainer,
                                contentColor = colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToAddTask(cropId) }
                            )
                        }
                    }

                    BottomActionButton(
                        label = "Manage Status",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { showStatusSheet = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.cropDetails == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { StatusBanner(uiState.cropDetails) }
                    item { QuickStats(uiState.cropDetails) }

                    // Tabs
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab.ordinal,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            DetailTab.entries.forEach { tab ->
                                Tab(
                                    selected = selectedTab == tab,
                                    onClick = { viewModel.selectTab(tab) },
                                    text = { Text(tab.displayName, style = MaterialTheme.typography.labelLarge) }
                                )
                            }
                        }
                    }

                    // Dynamic Tab Content
                    when (selectedTab) {
                        DetailTab.OVERVIEW -> {
                            item { ProgressSection(uiState.cropDetails) }
                            item { InfoCard(uiState.cropDetails) }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    HealthCard(uiState.cropDetails?.healthScore ?: "N/A", Modifier.weight(1f))
                                    MarketCard(uiState.cropDetails?.marketPrice ?: 0, Modifier.weight(1f))
                                }
                            }
                        }
                        DetailTab.TASKS -> {
                            if (uiState.upcomingTasks.isEmpty()) {
                                item { EmptyStateText("No tasks pending.") }
                            } else {
                                items(uiState.upcomingTasks) { task ->
                                    TaskCard(
                                        task = task,
                                        onMarkAsDone = {
                                            val taskEntity = uiState.tasks.find { it.title == task.name }
                                            taskEntity?.let {
                                                viewModel.updateTaskStatus(it.id, isCompleted = true)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        DetailTab.MONITORING -> {
                            if (uiState.recentActivities.isEmpty()) {
                                item { EmptyStateText("No recent activity.") }
                            } else {
                                items(uiState.recentActivities) { ActivityCard(it) }
                            }
                        }
                        DetailTab.ANALYTICS -> {
                            item { AnalyticsCard(uiState.analytics) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMonitoringRecordSheet(
    cropId: String,
    onDismiss: () -> Unit,
    onSave: (CropMonitoringRecord) -> Unit
) {
    var selectedHealthStatus by remember { mutableStateOf(HealthStatus.GOOD) }
    var notes by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var soilMoisture by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                "Add Monitoring Record",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Health Status Selection
            Text("Health Status", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthStatus.entries.take(5).forEach { status ->
                    FilterChip(
                        selected = selectedHealthStatus == status,
                        onClick = { selectedHealthStatus = status },
                        label = { Text(status.name, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Environmental Conditions
            OutlinedTextField(
                value = temperature,
                onValueChange = { temperature = it },
                label = { Text("Temperature (°C)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = humidity,
                onValueChange = { humidity = it },
                label = { Text("Humidity (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = soilMoisture,
                onValueChange = { soilMoisture = it },
                label = { Text("Soil Moisture (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val weatherInfo = buildString {
                            if (temperature.isNotBlank()) append("Temp: ${temperature}°C ")
                            if (humidity.isNotBlank()) append("Humidity: ${humidity}% ")
                        }.trim().ifBlank { null }

                        val record = CropMonitoringRecord(
                            id = UUID.randomUUID().toString(),
                            cropId = cropId,
                            recordDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                            healthStatus = selectedHealthStatus,
                            moistureLevel = soilMoisture.toDoubleOrNull(),
                            notes = notes.ifBlank { null },
                            photos = emptyList(),
                            weatherConditions = weatherInfo,
                            diseaseObservations = null,
                            pestObservations = null,
                            recordedBy = "User"
                        )
                        android.util.Log.d("🌾 MONITORING", "Saving record: temp=$temperature, humidity=$humidity, moisture=$soilMoisture, notes=$notes")
                        onSave(record)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageMigrationSheet(
    currentStage: String,
    onDismiss: () -> Unit,
    onMigrate: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Migrate Growth Stage", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Current: $currentStage", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            val stages = listOf("Seed Preparation","Planting","Germination", "Vegetative", "Flowering", "Fruit Development", "Maturation", "Harvesting")
            val currentIndex = stages.indexOf(currentStage)
            val nextStages = stages.drop(currentIndex + 1)

            if (nextStages.isEmpty()) {
                Text("Crop is at its final stage.", Modifier.padding(16.dp))
            } else {
                nextStages.forEach { stage ->
                    OutlinedButton(
                        onClick = { onMigrate(stage) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Move to $stage")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BottomActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusBanner(crop: CropDetails?) {
    val colorScheme = MaterialTheme.colorScheme
    val bannerColor = crop?.statusColor ?: colorScheme.primaryContainer
    val textColor = colorScheme.onPrimaryContainer
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(crop?.status ?: "Unknown", style = MaterialTheme.typography.headlineSmall, color = textColor, fontWeight = FontWeight.Bold)
                Text(crop?.growthStage ?: "N/A", style = MaterialTheme.typography.bodyLarge, color = textColor.copy(alpha = 0.8f))
            }
            Icon(Icons.Default.CheckCircle, null, tint = textColor, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun AnalyticsCard(analytics: CropAnalytics?) {
    if (analytics == null) return
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text("Crop Health Analysis", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Task Completion: ${analytics.completedTasksPercentage.toInt()}%", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { (analytics.completedTasksPercentage / 100).toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: UpcomingTask,
    onMarkAsDone: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(task.color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(task.icon, null, tint = task.color)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(task.name, fontWeight = FontWeight.Bold)
                Text(task.dueDate, color = task.color, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onMarkAsDone) {
                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Mark done", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: RecentActivity) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(activity.icon, null, tint = activity.color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(activity.name, fontWeight = FontWeight.Bold)
                Text(activity.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(activity.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun EmptyStateText(text: String) {
    Text(text, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun QuickStats(crop: CropDetails?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val colorScheme = MaterialTheme.colorScheme
        
        StatBox(
            icon = Icons.Default.Landscape,
            value = "${crop?.area ?: 0} acres",
            label = "Area",
            color = colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        StatBox(
            icon = Icons.Default.CalendarMonth,
            value = "${crop?.daysToHarvest ?: 0} days",
            label = "To Harvest",
            color = colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        StatBox(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            value = "${crop?.expectedYield ?: 0} kg",
            label = "Expected Yield",
            color = colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatBox( icon: ImageVector, value: String, label: String, color: Color,  modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressSection(crop: CropDetails?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Growth Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${crop?.progressPercentage ?: 0}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (crop?.progressPercentage ?: 0) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(crop: CropDetails?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoRow(
                icon = Icons.Default.Agriculture,
                label = "Variety",
                value = crop?.variety ?: "Unknown"
            )
            HorizontalDivider()
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Farm",
                value = crop?.farmName ?: "Unknown"
            )
            HorizontalDivider()
            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Planting Date",
                value = crop?.plantingDate ?: "Unknown"
            )
            HorizontalDivider()
            InfoRow(
                icon = Icons.Default.Event,
                label = "Expected Harvest",
                value = crop?.expectedHarvestDate ?: "Unknown"
            )
            HorizontalDivider()
            InfoRow(
                icon = Icons.Default.Terrain,
                label = "Soil Type",
                value = crop?.soilType ?: "Unknown"
            )
        }
    }
}

@Composable
private fun HealthCard(healthScore: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = healthScore,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Health Score",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MarketCard(price: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "KES $price/kg",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Market Price",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun InfoRow(  icon: ImageVector, label: String,  value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data classes
data class CropDetails(
    val id: String,
    val name: String,
    val variety: String?,
    val status: String,
    val statusColor: Color,
    val growthStage: String,
    val progressPercentage: Int,
    val area: Double?,
    val daysToHarvest: Int,
    val farmName: String?,
    val plantingDate: String,
    val expectedHarvestDate: String,
    val soilType: String,
    val expectedYield: Int,
    val healthScore: String,
    val marketPrice: Int
)

data class UpcomingTask(
    val name: String,
    val description: String,
    val dueDate: String,
    val icon: ImageVector,
    val color: Color
)

data class RecentActivity(
    val name: String,
    val description: String,
    val date: String,
    val icon: ImageVector,
    val color: Color
)