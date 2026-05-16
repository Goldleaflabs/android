package com.goldleaf.feature.farmermanagement.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSetupScreen(
    onNavigateBack: () -> Unit = {},
    onSetupComplete: (String) -> Unit,
    viewModel: FarmSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var farmName by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var lrNumber by remember { mutableStateOf("") }
    var locationQuery by remember { mutableStateOf("") }

    val parsedSize = farmSize.toDoubleOrNull()
    val isFormValid = farmName.isNotBlank() && parsedSize != null && uiState.currentLocation != null

    LaunchedEffect(uiState.farmSaved) {
        if (uiState.farmSaved) {
            onSetupComplete(uiState.newFarmId ?: "")
            viewModel.resetFarmSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Farm", color = MaterialTheme.colorScheme.surface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Register Your Farm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            uiState.error?.let { message ->
                ErrorBanner(
                    message = message,
                    onDismiss = { viewModel.clearError() }
                )
            }

            // Farm Name
            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text("Farm Name") },
                leadingIcon = { Icon(Icons.Default.Eco, "Farm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    focusedLabelColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF4CAF50)
                )
            )

            // Farm Size
            OutlinedTextField(
                value = farmSize,
                onValueChange = { farmSize = it },
                label = { Text("Farm Size (acres)") },
                leadingIcon = { Icon(Icons.Default.SquareFoot, "Size") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    focusedLabelColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF4CAF50)
                )
            )

            // Location Section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = locationQuery,
                        onValueChange = { locationQuery = it },
                        label = { Text("Search place or town") },
                        placeholder = { Text("e.g. Kitale, Trans Nzoia") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (locationQuery.isNotBlank()) {
                                        viewModel.selectLocation(locationQuery)
                                    }
                                },
                                enabled = locationQuery.isNotBlank()
                            ) {
                                Icon(Icons.Default.Search, "Search")
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (locationQuery.isNotBlank()) {
                                    viewModel.selectLocation(locationQuery)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF81C784),
                            focusedTextColor = Color(0xFF1B5E20),
                            unfocusedTextColor = Color(0xFF1B5E20),
                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color(0xFF558B2F),
                            focusedLeadingIconColor = Color(0xFF4CAF50),
                            unfocusedLeadingIconColor = Color(0xFF66BB6A),
                            focusedTrailingIconColor = Color(0xFF4CAF50),
                            unfocusedTrailingIconColor = Color(0xFF66BB6A),
                            cursorColor = Color(0xFF4CAF50),
                            focusedPlaceholderColor = Color(0xFF757575),
                            unfocusedPlaceholderColor = Color(0xFF9E9E9E)
                        )
                    )
                    Button(
                        onClick = { viewModel.getCurrentDeviceLocation() },
                        enabled = !uiState.isLoadingLocation,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        if (uiState.isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Use GPS")
                            }
                        }
                    }
                }

                // Show selected location
                if (uiState.currentLocation != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    uiState.locationName.ifBlank { "Selected Location" },
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    "${uiState.district}, ${uiState.region}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            } // end Location Section Column

            // Map Preview
            uiState.currentLocation?.let { loc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    FarmMapPreview(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        title = farmName.ifBlank { "Farm Location" },
                        onPositionChanged = { lat, lng ->
                            viewModel.updateLocation(lat, lng)
                        }
                    )
                }
            }

            // Land Reference (optional)
            OutlinedTextField(
                value = lrNumber,
                onValueChange = { lrNumber = it },
                label = { Text("Land Reference (Optional)") },
                leadingIcon = { Icon(Icons.Default.Numbers, "Reference") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    viewModel.saveFarmManually(
                        name = farmName.trim(),
                        size = parsedSize!!,
                        referenceNumber = lrNumber.trim()
                    )
                },
                enabled = isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Farm", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Error display
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun FarmMapPreview(
    latitude: Double,
    longitude: Double,
    title: String,
    onPositionChanged: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView },
        update = {
            val point = GeoPoint(latitude, longitude)
            it.controller.setZoom(13.0)
            it.controller.setCenter(point)
            it.overlays.clear()
            val marker = Marker(it).apply {
                position = point
                this.title = title
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                isDraggable = true
            }
            marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker?) {}
                override fun onMarkerDrag(marker: Marker?) {}
                override fun onMarkerDragEnd(marker: Marker?) {
                    val pos = marker?.position ?: return
                    onPositionChanged(pos.latitude, pos.longitude)
                }
            })
            it.overlays.add(marker)
        }
    )
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}
