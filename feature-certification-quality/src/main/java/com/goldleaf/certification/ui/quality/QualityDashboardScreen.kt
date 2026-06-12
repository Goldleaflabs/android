package com.goldleaf.certification.ui.quality

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.goldleaf.certification.presentation.quality.LabTestWithBatch
import com.goldleaf.certification.presentation.quality.QualityViewModel
import com.goldleaf.certification.presentation.quality.TestParameter
import com.goldleaf.core.data.local.Teststatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityDashboardScreen(
    navController: NavHostController,
    viewModel: QualityViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lab Test Reports", color = MaterialTheme.colorScheme.surface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.surface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = MaterialTheme.colorScheme.surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        }
    ) { padding ->
        when {
            ui.loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ui.items.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No lab test reports", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Outsourced lab results from the admin portal will appear here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Laboratory Test Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("${ui.items.size} test(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    items(ui.items, key = { it.testId }) { item ->
                        TestReportCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun TestReportCard(item: LabTestWithBatch) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dateStr = remember(item.testDate) {
        try { dateFormat.format(Date(item.testDate.toLongOrNull() ?: 0L)) } catch (_: Exception) { item.testDate }
    }
    val overallColor = when (item.status) {
        Teststatus.PASSED -> Color(0xFF4CAF50)
        Teststatus.FAILED -> Color(0xFFF44336)
        Teststatus.WARNING -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Batch: ${item.batchNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(item.productType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = overallColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (item.isPassed) "PASSED" else "FAILED",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = overallColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Test info
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("Test Type", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.testType, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Lab", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.labName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateStr, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Parameters table
            if (item.parameters.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Test Results", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ParamCell("Parameter", Modifier.weight(1.5f), bold = true)
                    ParamCell("Value", Modifier.weight(1f), bold = true, align = TextAlign.End)
                    ParamCell("Unit", Modifier.weight(0.8f), bold = true)
                    ParamCell("Standard", Modifier.weight(1.2f), bold = true)
                    ParamCell("Status", Modifier.weight(0.8f), bold = true, align = TextAlign.End)
                }

                // Parameter rows
                item.parameters.forEach { param ->
                    val pColor = when (param.status.uppercase()) {
                        "PASS" -> Color(0xFF4CAF50)
                        "FAIL" -> Color(0xFFF44336)
                        else -> Color(0xFFFF9800)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ParamCell(param.name, Modifier.weight(1.5f))
                        ParamCell(param.value, Modifier.weight(1f), align = TextAlign.End)
                        ParamCell(param.unit, Modifier.weight(0.8f))
                        ParamCell(param.standard, Modifier.weight(1.2f))
                        ParamCell(param.status, Modifier.weight(0.8f), color = pColor, bold = true, align = TextAlign.End)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun ParamCell(
    text: String,
    modifier: Modifier,
    bold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface,
    align: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        textAlign = align,
        maxLines = 1
    )
}
