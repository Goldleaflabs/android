package com.goldleaf.feature.advisoryservices.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilAnalysisScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SoilAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var soilType by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var nitrogen by remember { mutableStateOf("") }
    var phosphorus by remember { mutableStateOf("") }
    var potassium by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soil Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF795548)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Landscape,
                        contentDescription = null,
                        tint = Color(0xFF795548),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "AI Soil Analysis",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Get personalized soil recommendations",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Soil Type
            OutlinedTextField(
                value = soilType,
                onValueChange = { soilType = it },
                label = { Text("Soil Type *") },
                placeholder = { Text("e.g., Clay, Loam, Sandy") },
                leadingIcon = {
                    Icon(Icons.Default.Terrain, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // pH Level
            OutlinedTextField(
                value = ph,
                onValueChange = { ph = it },
                label = { Text("pH Level") },
                placeholder = { Text("e.g., 6.5") },
                leadingIcon = {
                    Icon(Icons.Default.Science, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // NPK Values
            Text(
                text = "Nutrient Levels (Optional)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nitrogen,
                    onValueChange = { nitrogen = it },
                    label = { Text("N") },
                    placeholder = { Text("Nitrogen") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phosphorus,
                    onValueChange = { phosphorus = it },
                    label = { Text("P") },
                    placeholder = { Text("Phosphorus") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = potassium,
                    onValueChange = { potassium = it },
                    label = { Text("K") },
                    placeholder = { Text("Potassium") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Crop Type
            OutlinedTextField(
                value = cropType,
                onValueChange = { cropType = it },
                label = { Text("Planned Crop *") },
                placeholder = { Text("What will you grow?") },
                leadingIcon = {
                    Icon(Icons.Default.Spa, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Analyze Button
            Button(
                onClick = {
                    if (soilType.isNotBlank() && cropType.isNotBlank()) {
                        viewModel.analyzeSoil(
                            soilType = soilType,
                            ph = ph.toDoubleOrNull(),
                            nitrogen = nitrogen.toDoubleOrNull(),
                            phosphorus = phosphorus.toDoubleOrNull(),
                            potassium = potassium.toDoubleOrNull(),
                            cropType = cropType
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = soilType.isNotBlank() && cropType.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF795548)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Soil", fontSize = 16.sp)
                }
            }

            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            // Results Section
            if (uiState.analysisResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Soil Analysis Complete",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.analysisResult!!,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}