package com.goldleaf.certification.ui.details

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.LabTest
import com.goldleaf.certification.presentation.batch.BatchViewModel
import com.goldleaf.core.data.local.ProductBatchEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailsScreen(
    batchId: String,
    navController: NavHostController,
    viewModel: BatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()

    LaunchedEffect(batchId) {
        viewModel.loadBatchDetails(batchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share functionality */ }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.selectedBatch == null -> {
                    Text(
                        text = "Batch not found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    BatchDetailsContent(
                        batch = uiState.selectedBatch!!,
                        qrCode = uiState.qrCode,
                        labTests = uiState.labTests,
                        blockchainRecord = uiState.blockchain
                    )
                }
            }
        }
    }
}

@Composable
fun BatchDetailsContent(
    batch: ProductBatchEntity,
    qrCode: Bitmap?,
    labTests: List<LabTest>,
    blockchainRecord: BlockchainRecord?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // QR Code Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verification QR Code",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                qrCode?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Scan to verify authenticity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Batch Information
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Product Information",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow("Batch Number", batch.batchNumber)
                InfoRow("Product Type", batch.productType)
                InfoRow("Quantity", "${batch.quantity} ${batch.unit}")
                InfoRow("Harvest Date", batch.harvestDate.toString())
                InfoRow("Farmer", batch.farmerName)
                // Add a "Total Weight" Row for clarity
                val totalWeight = calculateTotalWeight(batch.quantity, batch.unit)
                InfoRow("Certified Weight", "$totalWeight kg")
                batch.qualityGrade?.let {
                    InfoRow("Quality Grade", it)
                }
            }
        }

        // Blockchain Section
        blockchainRecord?.let { record ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Blockchain Verification",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Status", record.status.toString())
                    InfoRow("Transaction Hash", record.transactionHash.take(16) + "...")
                    InfoRow("Block Number", record.blockNumber.toString())
                    InfoRow("Network", record.network)
                    InfoRow("Timestamp", record.timestamp)
                }
            }
        }

        // Lab Tests Section
        // Lab Tests Section inside BatchDetailsContent
        if (labTests.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Laboratory Tests", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    labTests.forEach { test ->
                        LabTestItem(test)
                        if (test != labTests.last()) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        } else {
            // Balanced Placeholder: Inform the farmer that results are pending from Admin
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Lab results are currently being processed by the logistics team.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }


    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun LabTestItem(test: LabTest) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = test.testType,
                style = MaterialTheme.typography.bodyMedium
            )
            test.isPassed?.let { passed ->
                Surface(
                    color = if (passed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (passed) "PASSED" else "FAILED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Text(
            text = "Lab: ${test.labName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Date: ${test.testDate}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun calculateTotalWeight(quantity: Double, unitDescription: String): String {
    // This regex looks for any number followed by 'kg' in the unit string
    val weightPerUnit = when {
        unitDescription.contains("90kg", ignoreCase = true) -> 90.0
        unitDescription.contains("50kg", ignoreCase = true) -> 50.0
        unitDescription.contains("25kg", ignoreCase = true) -> 25.0
        unitDescription.contains("4kg", ignoreCase = true) -> 4.0
        else -> 1.0 // Default to 1kg if unknown
    }

    val total = quantity * weightPerUnit
   // Returns a formatted string, e.g., "450.0"
    return String.format("%.1f", total)
}