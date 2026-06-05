package com.goldleaf.certification.ui.verification

// feature-certification-quality/src/main/java/com/goldleaf/certification/ui/verification/ConsumerVerificationScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.goldleaf.certification.presentation.verification.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerVerificationScreen(
    batchId: String?,
    navController: NavHostController,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    var batchNumber by remember { mutableStateOf(batchId ?: "") }
    val verificationResult by viewModel.verificationResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(batchId) {
        batchId?.let {
            viewModel.verifyProduct(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (batchId == null) {
                // Manual entry mode
                Text(
                    text = "Verify Product Authenticity",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Enter Batch Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.verifyProduct(batchNumber) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = batchNumber.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, "Verify")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* Launch QR scanner */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR Code")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show verification result
            verificationResult?.let { result ->
                VerificationResultCard(result)
            }
        }
    }
}

@Composable
fun VerificationResultCard(result: com.goldleaf.core.domain.model.VerificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isValid)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (result.isValid) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (result.isValid)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (result.isValid) "Product Verified" else "Verification Failed",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium
            )

            result.batch?.let { batchNumber ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Batch: $batchNumber",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}