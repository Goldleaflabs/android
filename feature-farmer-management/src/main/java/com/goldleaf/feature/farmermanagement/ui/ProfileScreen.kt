package com.goldleaf.feature.farmermanagement.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.goldleaf.feature.farmermanagement.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val localContext = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Pre-fill fields when farmer data first loads, and keep them in sync
    val farmerData = uiState.farmer
    LaunchedEffect(farmerData?.name, farmerData?.email, farmerData?.location) {
        if (farmerData != null) {
            if (name.isBlank()) name = farmerData.name
            if (email.isBlank()) email = farmerData.email ?: ""
            if (location.isBlank()) location = farmerData.location ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & Settings", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Photo Header
            item { ProfileHeader(farmer = uiState.farmer) }

            // 2. Stats Section (Added back)
            item {
                StatsSection(
                    totalFarms = uiState.totalFarms,
                    activeCrops = uiState.activeCrops,
                    completedTrainings = uiState.completedTrainings
                )
            }

            // 3. Edit Profile Card
            item {
                SectionCard(title = "Personal Details", icon = Icons.Default.Person) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.farmer?.phone ?: "",
                        onValueChange = {},
                        label = { Text("Phone Number (Verified)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Verified,contentDescription = null,  tint = MaterialTheme.colorScheme.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Location Field with GPS Button
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("County/Location (e.g., Kiambu, Central)") },
                                modifier = Modifier
                                    .weight(1f),
                                placeholder = { Text("District, Region") },
                                supportingText = { Text("Format: County, Region (required for certification labels)") },
                                isError = location.isBlank(),
                                trailingIcon = {
                                    if (uiState.isLoadingLocation) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    }
                                }
                            )
                            // GPS Button
                            // Permission launcher: requests ACCESS_FINE_LOCATION and calls ViewModel on grant
                            val locationPermissionLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { granted: Boolean ->
                                if (granted) {
                                    viewModel.getCurrentDeviceLocation()
                                } else {
                                    // Let ViewModel set an error state if needed
                                    // (ViewModel.getCurrentDeviceLocation will handle permission check otherwise)
                                }
                            }

                            IconButton(
                                onClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        localContext,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.getCurrentDeviceLocation()
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                enabled = !uiState.isLoadingLocation && !uiState.isUpdating,
                                modifier = Modifier
                                    .size(56.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Get GPS Location",
                                    tint = if (!uiState.isLoadingLocation && !uiState.isUpdating)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                        
                        // Show GPS error if present
                        if (uiState.locationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "📍 ${uiState.locationError}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // Auto-fill location if GPS result is available
                        LaunchedEffect(uiState.gpsLocationRaw) {
                            if (uiState.gpsLocationRaw.isNotBlank()) {
                                location = uiState.gpsLocationRaw
                            }
                        }
                    }
                    
                    if (location.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Location is required to request certification labels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.updateProfile(name = name, email = email, location = location) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !uiState.isUpdating
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (uiState.isUpdating) "Updating..." else "Update Details")
                    }
                    if (uiState.successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ ${uiState.successMessage}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "❌ ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 4. Farm Info Card (Added back)
            item {
                SectionCard(title = "Farm Information", icon = Icons.Default.Agriculture) {
                    InfoRow(Icons.Default.Landscape, "Total Size", "${uiState.farmer?.totalFarmSize ?: 0} acres")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow(Icons.Default.Grass, "Primary Crops", uiState.primaryCrops.joinIfEmpty())
                }
            }

            // 5. App Settings Card
            item {
                SectionCard(title = "App Settings", icon = Icons.Default.Settings) {
                    SettingsClickableItem(Icons.Default.Lock, "Change Password", onNavigateToChangePassword)
                    SettingsClickableItem(Icons.Default.PrivacyTip, "Privacy Policy") {
                        uriHandler.openUri("https://www.goldleaflabs.co.ke/privacy")
                    }
                    SettingsItem(Icons.Default.Info, "Version") {
                        Text("1.10.0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            // 6. Logout
            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(); showLogoutDialog = false }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// --- ALL REQUIRED SUB-COMPOSABLES ---

@Composable
private fun ProfileHeader(farmer: FarmerProfile?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = farmer?.profileImage ?: "",
            contentDescription = null,
            modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(farmer?.name ?: "Loading...", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Member since ${farmer?.memberSince ?: "N/A"}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
    }
}

@Composable
private fun StatsSection(totalFarms: Int, activeCrops: Int, completedTrainings: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(Icons.Default.Agriculture, totalFarms.toString(), "Farms", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        StatCard(Icons.Default.Grass, activeCrops.toString(), "Crops", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        StatCard(Icons.Default.School, completedTrainings.toString(), "Trainings", MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color)
            Text(value, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsClickableItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f))
        trailing()
    }
}

// Extension & Data Class
private fun List<String>.joinIfEmpty() = if (isEmpty()) "None" else joinToString(", ")

data class FarmerProfile(
    val id: String, val name: String, val email: String, val phone: String,
    val profileImage: String?, val location: String?, val totalFarmSize: Double, val memberSince: String
)