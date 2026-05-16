package com.goldleaf.feature.farmermanagement.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PestControl
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.goldleaf.core.auth.UserRole
import com.goldleaf.feature.weatherclimate.ui.WeatherViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    farmId: String?,    // Already exists: Selected farm ID
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val currentFarmer by viewModel.currentFarmer.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // âœ… CHANGE: Pass both IDs to loadDashboardData
    // âœ… Get farmerId from currentFarmer (from session)
    LaunchedEffect(farmId, currentFarmer) {
        val farmerId = currentFarmer?.id
        if (farmerId != null && farmId != null) {
            viewModel.loadDashboardData(farmerId, farmId)
        }
    }

    val farmerId = currentFarmer?.id
    // Use a proper null check instead of !!
    if (currentFarmer == null ) {
        LoadingState(
            retryCount = 0,
            maxRetries = 2,
            onRetry = {
                // âœ… CHANGE: Pass both IDs to retry
                if (farmerId != null && farmId != null) {
                    viewModel.loadDashboardData(farmerId, farmId)
                }
            }
        )
    } else {
        DashboardContent(
            farmer = currentFarmer!!,
            farmerId = farmerId,  // âœ… ADD THIS: Pass farmerId down
            farmId = farmId,
            uiState = uiState,
            viewModel = viewModel,
            navController = navController,
            onNavigateBack = onNavigateBack
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    farmer: DashboardFarmer,
    farmerId: String?,  // âœ… ADD THIS: Accept farmerId
    farmId: String?,
    uiState: DashboardUiState,
    viewModel: DashboardViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = { navController.navigate("farmer_profile/${farmer.id}") }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        farmer.farmname?.let {
                            Text(
                                text = it,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            "Welcome, ${farmer.name.split(" ").first()}!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, "Profile", tint = MaterialTheme.colorScheme.surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController, farmerId = farmerId) }
    ) { padding ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                // âœ… CHANGE: Pass both IDs to refresh
                if (farmerId != null && farmId != null) {
                    viewModel.refreshDashboard(farmerId, farmId)
                }
            },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { GreetingCard(farmer.name, farmer.userRole) }

                item {
                    StatsOverview(
                        totalFarms = uiState.totalFarms,
                        activeCrops = uiState.activeCrops,
                        pendingTasks = uiState.pendingTasks
                    )
                }

                item {
                    QuickActionsSection(
                        navController = navController,
                        userRole = farmer.userRole,
                        farmId = farmId
                    )
                }

                item {
                    Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(uiState.recentActivities) { activity ->
                    ActivityCard(activity = activity)
                }

                item {
                    val weatherViewModel: WeatherViewModel = hiltViewModel()
                    WeatherSummaryCard(viewModel = weatherViewModel, onClick = { navController.navigate("weather") })
                }
            }
        }
    }
}


@Composable
fun LoadingState(
    retryCount: Int,
    maxRetries: Int,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (retryCount < maxRetries) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
                Spacer(Modifier.height(16.dp))
                Text("Fetching your farm profile...", style = MaterialTheme.typography.bodyMedium)
            } else {
                Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Text("Could not find local data.")
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
private fun GreetingCard(
    farmerName: String,
    role: UserRole?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ${farmerName.split(" ").first()}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (role == UserRole.VERIFIEDFARMER) "VERIFIED FARMER Access" else "Ready to grow today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Icon(
                if (role == UserRole.VERIFIEDFARMER) Icons.Default.Verified else Icons.Default.Agriculture,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun StatsOverview(
    totalFarms: Int,
    activeCrops: Int,
    pendingTasks: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Default.Agriculture,
            value = totalFarms.toString(),
            label = "Farms",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Spa,
            value = activeCrops.toString(),
            label = "Crops",
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.TaskAlt,
            value = pendingTasks.toString(),
            label = "Tasks",
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}



@Composable
private fun QuickActionsSection(
    navController: NavController,
    userRole: UserRole,
    farmId: String?
) {
    val showVerificationDialog = remember { mutableStateOf(false) }

    if (showVerificationDialog.value) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog.value = false },
            title = { Text("Verification Required") },
            text = { Text("This feature is only available to verified farmers.") },
            confirmButton = {
                TextButton(onClick = { showVerificationDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column {
        Text("Services & Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // Row 1: Primary Management
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Default.Add,
                title = "Add Farm",
                onClick = { navController.navigate("farm_setup") },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.Spa,
                title = "Add Crop",
                onClick = {
                    // Use a fallback to prevent navigating to an invalid empty path
                    val id = if (farmId.isNullOrBlank()) "unknown" else farmId
                    navController.navigate("crop_selection/$id")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 2: Monitoring & Records
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Default.Agriculture,
                title = "My Crops",
                onClick = { navController.navigate("my_crops/$farmId") },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "Crop Monitoring",
                onClick = {
                    // Bridge to activity logging via the monitoring list
                    navController.navigate("crop_monitoring")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 3: Compliance
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Default.Verified,
                title = "Certifications",
                onClick = { navController.navigate("certification_graph") },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.CheckCircle,
                title = "Quality",
                onClick = { navController.navigate("quality_graph") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 4: Verified Services
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Default.Fence,
                title = "Farm Fencing",
                onClick = {
                    if (userRole != UserRole.VERIFIEDFARMER) {
                        showVerificationDialog.value = true
                    } else {
                        val id = if (farmId.isNullOrBlank()) "unknown" else farmId
                        navController.navigate("farm_fencing/$id")
                    }
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.PestControl,
                title = "Pest/Disease",
                onClick = {
                    if (userRole != UserRole.VERIFIEDFARMER) {
                        showVerificationDialog.value = true
                    } else {
                        navController.navigate("pest_detection")
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 5: Training
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Default.School,
                title = "Training",
                onClick = { navController.navigate("training_catalog") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}



@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActivityCard(activity: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Circle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = activity,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun WeatherSummaryCard(
    viewModel: WeatherViewModel,
    onClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Today's Weather", color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                when {
                    uiState.isLoading -> Text("Loading...", color = MaterialTheme.colorScheme.surface)
                    uiState.error != null -> Text("No connection", color = Color.White.copy(alpha = 0.8f))
                    else -> {
                        Text(
                            text = "${uiState.currentWeather?.temperature?.toInt() ?: "--"}Â°C",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.currentWeather?.condition ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            Icon(
                Icons.Default.WbSunny,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavController,
    farmerId: String?
) {
    // 1. Observe the current backstack entry to react to navigation changes
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, "Home") },
                label = { Text("Home") },
                selected = currentDestination?.route?.startsWith("dashboard/") == true,
                onClick = {
                    // NOTE: Simply do nothing, we're already on the dashboard
                    // Avoid navigation to farm_selection which may not be on back stack
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Agriculture, "Farms") },
                label = { Text("Farms") },
                selected = currentRoute(navController) == "crop_monitoring",
                onClick = {
                    navController.navigate("crop_monitoring") {
                        launchSingleTop = true
                    }
                }
            )

        // HARVEST REMOVED â€” NOW ADVISORY IS HERE
        NavigationBarItem(
            icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Advisory") },
            label = { Text("Advisory") },
            selected = currentRoute(navController) == "advisory_dashboard",
            onClick = {
                navController.navigate("advisory_dashboard") {
                    launchSingleTop = true
                }
            }
        )


    }
}

private fun currentRoute(navController: NavController): String? {
    val currentDestination = navController.currentBackStackEntry?.destination?.route
    return when {
        currentDestination?.startsWith("certification_graph") == true -> "certification_graph"
        currentDestination?.startsWith("quality_graph") == true -> "quality_graph"
        else -> currentDestination
    }
}
