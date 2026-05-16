package com.goldleaf.feature.farmermanagement.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.goldleaf.feature.farmermanagement.ui.viewmodels.FarmSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSelectionScreen(
    userId: String, // Assigned here from the NavGraph
    navController: NavController,
    onFarmSelected: (farmId: String) -> Unit,  // ✅ CHANGE 1: Added farmerId parameter
    viewModel: FarmSelectionViewModel = hiltViewModel()  // ✅ CHANGE 2: New ViewModel type
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFarmer by viewModel.currentFarmer.collectAsStateWithLifecycle()  // ✅ CHANGE 3: Get farmerName from new ViewModel
    val farmerName=currentFarmer?.name

    // 🔍 LOG: Screen composition
    Log.d("FarmSelectionScreen", "🎨 Screen composing - userId param: '$userId'")
    Log.d("FarmSelectionScreen", "👤 farmername: '${currentFarmer?.name}'")
    Log.d("FarmSelectionScreen", "📊 uiState: isLoading=${uiState.isLoading}, farms.size=${uiState.farms.size}, error=${uiState.error}")

    // Animation for the refresh icon when loading
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // ✅ Simplified load
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            Log.d("FarmSelectionScreen", "🎯 Loading farms for userId: '$userId'")
            viewModel.loadFarms(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Farm",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (userId.isNotBlank()) {
                            viewModel.refreshFarms(userId)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = if (uiState.isLoading) {
                                Modifier.graphicsLayer { rotationZ = rotation }
                            } else {
                                Modifier
                            }
                        )
                    }

                    TextButton(onClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Text(
                            "Logout",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("farm_setup") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add new farm",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            FarmerHeader(
                farmerName = farmerName ?: "Farmer",
                isLoading = uiState.isLoading && farmerName == null
            )

            uiState.error?.let { message ->
                ErrorBanner(
                    message = message,
                    onRetry = { if (userId.isNotBlank()) viewModel.refreshFarms(userId) }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Farms",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (uiState.isLoading && uiState.farms.isNotEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(uiState.farms, key = { it.id }) { farm ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        FarmItemCard(
                            name = farm.name,
                            onSelect = { onFarmSelected( farm.id) }  // ✅ CHANGE 4: Pass both IDs
                        )
                    }
                }

                if (uiState.isLoading && uiState.farms.isEmpty()) {
                    items(3) { FarmItemSkeleton() }
                }

                if (!uiState.isLoading && uiState.farms.isEmpty()) {
                    item {
                        EmptyFarmState(onAddFarm = { navController.navigate("farm_setup") })
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun FarmerHeader(farmerName: String, isLoading: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Welcome back,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                if (isLoading) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        modifier = Modifier.width(120.dp).height(32.dp).shimmer()
                    ) {}
                } else {
                    Text(
                        farmerName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Icon(Icons.Default.Agriculture, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun FarmItemCard(name: String, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Agriculture, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Tap to manage this farm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyFarmState(onAddFarm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Agriculture, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Text("No farms yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Button(onClick = onAddFarm) { Text("Add Farm") }
        }
    }
}

@Composable
private fun FarmItemSkeleton() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(16.dp)).shimmer())
            Spacer(Modifier.width(20.dp))
            Column {
                Box(modifier = Modifier.width(140.dp).height(20.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp)).shimmer())
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.width(180.dp).height(14.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(6.dp)).shimmer())
            }
        }
    }
}

@Composable
private fun Modifier.shimmer(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))
    this.graphicsLayer { this.alpha = alpha }
}

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unable to load farms",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
