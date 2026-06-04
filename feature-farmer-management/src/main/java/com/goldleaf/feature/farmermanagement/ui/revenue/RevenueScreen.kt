package com.goldleaf.feature.farmermanagement.ui.revenue

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
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    farmerId: String,
    onNavigateBack: () -> Unit,
    viewModel: RevenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPhoneDialog by remember { mutableStateOf(false) }

    LaunchedEffect(farmerId) { viewModel.loadRevenue(farmerId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Revenue") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { showPhoneDialog = true }) { Icon(Icons.Default.Phone, "Update M-PESA") }
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
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Earned", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                            Text("KSh ${NumberFormat.getNumberInstance(Locale.US).format(uiState.totalPaid)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(Modifier.weight(1f)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Declared", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${NumberFormat.getNumberInstance(Locale.US).format(uiState.totalDeclaredKg)} kg", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Card(Modifier.weight(1f)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Deductions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("KSh ${NumberFormat.getNumberInstance(Locale.US).format(uiState.totalDeductions)}", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Card(Modifier.weight(1f)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("M-PESA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(uiState.mpesaPhone.ifBlank { "—" }, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("How payments work", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "1. You declare your harvest → Admin confirms delivery\n" +
                                "2. Admin sells the produce → records trade income\n" +
                                "3. Rate = Total income ÷ Total declared (all farmers)\n" +
                                "4. Your gross = Your declared kg × Rate\n" +
                                "5. Deductions (transport, commission) are subtracted\n" +
                                "6. Net amount is sent to your M-PESA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPhoneDialog) {
        var phone by remember(uiState.mpesaPhone) { mutableStateOf(uiState.mpesaPhone) }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("M-PESA Phone") },
            text = {
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (e.g. 2547XXXXXXXX)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updatePayoutPhone(farmerId, phone); showPhoneDialog = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showPhoneDialog = false }) { Text("Cancel") } }
        )
    }
}
