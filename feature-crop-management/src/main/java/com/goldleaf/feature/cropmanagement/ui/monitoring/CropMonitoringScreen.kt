package com.goldleaf.feature.cropmanagement.ui.monitoring

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.YieldAnalytics


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropMonitoringScreen(
    viewModel: CropMonitoringViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCropDetails: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var showStatusSheet by remember { mutableStateOf(false) }
    var selectedCrop by remember { mutableStateOf<CropEntity?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Monitoring") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, "Filter", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            CropFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.displayName) },
                                    onClick = {
                                        viewModel.updateFilter(filter)
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (filter == CropFilter.ALL) Icons.Default.AllInclusive
                                            else Icons.Default.Circle,
                                            null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
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
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    item { OverviewMetricsCard(crops = uiState.crops, yieldAnalytics = uiState.yieldAnalytics) }
                    item { GrowthProgressChart(crops = uiState.crops) }
                    item { CropHealthAnalytics(crops = uiState.crops) }
                    item {
                        ActiveCropsSummary(
                            crops = uiState.crops,
                            onCropClick = { crop ->
                                selectedCrop = crop
                                showStatusSheet = true },
                            onNavigateToCropDetails = onNavigateToCropDetails
                        )
                    }
                    uiState.yieldAnalytics?.let { item { YieldAnalyticsCard(it) } }
                }
            }
        }
    }

    if (showStatusSheet && selectedCrop != null) {

        // Capture the current crop in a non-null local variable
        val crop = selectedCrop!!

        ModalBottomSheet(onDismissRequest = { showStatusSheet = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().navigationBarsPadding()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Manage ${crop.name}", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = {
                        showStatusSheet = false
                        onNavigateToCropDetails(crop.id)
                    }) {
                        Text("Full Details")
                    }
                }
                Spacer(Modifier.height(16.dp))
                CropStatus.entries.forEach { status ->
                    val isCurrent = crop.status == status
                    OutlinedButton(
                        onClick = { viewModel.updateCropStatus(crop.id, status); showStatusSheet = false },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            contentColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (isCurrent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text(status.name.replace("_", " "))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


// ALL YOUR MISSING COMPOSABLES — NOW INCLUDED & PERFECT
@Composable
private fun OverviewMetricsCard(  crops: List<CropEntity>,  yieldAnalytics: YieldAnalytics?) {
    val totalArea = crops.sumOf { it.area ?: 0.0 }.toDouble()
    val activeCrops = crops.count { it.status in listOf(CropStatus.PLANTED, CropStatus.GROWING) }
    val avgHealth = if (crops.isNotEmpty())
        (crops.count { it.status == CropStatus.GROWING }.toFloat() / crops.size * 100) else 0f
    val estimatedYield = yieldAnalytics?.totalExpectedYield ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Farm Overview", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem("Total Area", String.format("%.1f ha", totalArea), Icons.Default.Landscape, Color(0xFF4CAF50))
                MetricItem("Active Crops", activeCrops.toString(), Icons.Default.Agriculture, Color(0xFF2196F3))
                MetricItem("Avg Health", "${avgHealth.toInt()}%", Icons.Default.Favorite, Color(0xFFE91E63))
                MetricItem("Est. Yield", String.format("%.1fT", estimatedYield / 1000), Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun GrowthProgressChart(crops: List<CropEntity>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Growth Progress", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            if (crops.isEmpty()) {
                Text("No active crops", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            } else {
                crops.take(5).forEach { crop ->
                    CropProgressItem(crop)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CropHealthAnalytics(crops: List<CropEntity>) {
    val total = crops.size
    val excellent = crops.count { it.status == CropStatus.GROWING }
    val good = crops.count { it.status == CropStatus.PLANTED }
    val fair = crops.count { it.status == CropStatus.HARVESTED }
    val poor = crops.count { it.status == CropStatus.PLANNED }
    val critical = crops.count { it.status == CropStatus.FAILED }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Crop Health Status", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HealthMetric("Excellent", excellent, Color(0xFF4CAF50), if (total > 0) excellent * 100 / total else 0)
                HealthMetric("Good", good, Color(0xFF8BC34A), if (total > 0) good * 100 / total else 0)
                HealthMetric("Fair", fair, Color(0xFFFF9800), if (total > 0) fair * 100 / total else 0)
                HealthMetric("Poor", poor, Color(0xFFFF5722), if (total > 0) poor * 100 / total else 0)
                HealthMetric("Critical", critical, Color(0xFFF44336), if (total > 0) critical * 100 / total else 0)
            }
        }
    }
}


@Composable
private fun ActiveCropsSummary(
    crops: List<CropEntity>,
    onCropClick: (CropEntity) -> Unit,
    onNavigateToCropDetails: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Active Crops", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            crops.forEach { crop ->
                ActiveCropItem(
                    crop = crop,
                    onManageStatus = { onCropClick(crop) },
                    onViewDetails = { onNavigateToCropDetails(crop.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActiveCropItem(crop: CropEntity, onManageStatus: () -> Unit, onViewDetails: () -> Unit) {
    val statusColor = getStatusColor(crop.status ?: CropStatus.PLANNED)
    OutlinedCard(onClick = onManageStatus, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(crop.name, fontWeight = FontWeight.SemiBold)
                Text("${crop.area} ha", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onViewDetails) {
                Icon(Icons.Default.Info, "Details", tint = MaterialTheme.colorScheme.primary)
            }
            Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                Text(crop.status?.name ?: "PLANNED", color = MaterialTheme.colorScheme.surface, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun YieldAnalyticsCard(analytics: YieldAnalytics) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Yield Analytics", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Expected Yield", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(String.format("%.1f kg/ha", analytics.averageYieldPerHectare), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Efficiency", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.1f%%", analytics.yieldEfficiency), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (analytics.yieldEfficiency!! >= 80) MaterialTheme.colorScheme.primary else Color(0xFFFF9800))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Total Expected", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(String.format("%.1f kg", analytics.totalExpectedYield), fontSize = 14.sp) }
                Column(horizontalAlignment = Alignment.End) { Text("Total Actual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(String.format("%.1f kg", analytics.totalActualYield), fontSize = 14.sp) }
            }
        }
    }
}

// Helper Composables
@Composable
private fun MetricItem(title: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = color)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CropProgressItem(crop: CropEntity) {
    val progress = calculateCropProgress(crop)
    val color = getStatusColor(crop.status  ?: CropStatus.PLANNED)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(crop.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text((crop.status ?: CropStatus.PLANNED).name.replace("_", " "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("$progress%", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.width(80.dp), color = color)
        }
    }
}

@Composable
private fun HealthMetric(title: String, count: Int, color: Color, percentage: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Text(count.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Text("$percentage%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



// Helper Functions — KEPT 100% INTACT
private fun calculateCropProgress(crop: CropEntity): Int = when (crop.status) {
    CropStatus.PLANNED -> 10
    CropStatus.PLANTED -> 25
    CropStatus.GROWING -> 60
    CropStatus.HARVESTED -> 90
    CropStatus.COMPLETED -> 100
    CropStatus.FAILED -> 0
    null -> 0
}

private fun getStatusColor(status: CropStatus): Color = when (status) {
    CropStatus.GROWING -> Color(0xFF4CAF50)
    CropStatus.PLANTED -> Color(0xFF2196F3)
    CropStatus.HARVESTED, CropStatus.COMPLETED -> Color(0xFFFF9800)
    CropStatus.FAILED -> Color(0xFFF44336)
    else -> Color(0xFF9E9E9E)
}