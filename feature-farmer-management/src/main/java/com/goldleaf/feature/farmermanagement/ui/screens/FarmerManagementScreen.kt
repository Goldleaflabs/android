package com.goldleaf.feature.farmermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.goldleaf.core.data.dto.farm.DashboardActivity

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.goldleaf.core.data.dto.farm.FarmSummary
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.dto.farm.FarmerDashboardData
import com.goldleaf.core.data.dto.farm.FarmerStatus
import com.goldleaf.core.data.dto.farm.Notification
import com.goldleaf.core.data.dto.farm.NotificationType
import com.goldleaf.core.data.dto.farm.Task
import com.goldleaf.core.data.dto.farm.TaskPriority
import com.goldleaf.core.data.dto.farm.WeatherSummary
import com.goldleaf.feature.farmermanagement.ui.viewmodels.FarmerManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerManagementScreen(
    farmerId: String, // 👈 add this parameter
    onNavigateToProfile: () -> Unit,
    onNavigateToFarmFencing: (String) -> Unit,
    viewModel: FarmerManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(farmerId) {
        viewModel.loadDashboard(farmerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Gold Leaf Farmer Portal",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            },
            actions = {
                // Profile / Account Settings Button
                Button(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorScheme.primary
                    )
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error!!,
                            color = colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDashboard(farmerId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.dashboardData != null -> {
                DashboardContent(
                    dashboardData = uiState.dashboardData!!,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToFarmFencing = onNavigateToFarmFencing
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    dashboardData: FarmerDashboardData,
    onNavigateToProfile: () -> Unit,
    onNavigateToFarmFencing: (String) -> Unit   // ← Change this name
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Farmer Profile Card
        item {
            FarmerProfileCard(
                farmer = dashboardData.farmer,
                onClick = onNavigateToProfile
            )
        }

        // Quick Stats
        item {
            QuickStatsRow(dashboardData.farmer)
        }

        // Active Farms
        if (dashboardData.activeFarms.isNotEmpty()) {
            item {
                Text(
                    text = "Active Farms",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(dashboardData.activeFarms) { farm ->
                        FarmCard(
                            farm = farm,
                            onClick = { onNavigateToFarmFencing(farm.id) }
                        )
                    }
                }
            }
        }

        // Weather Summary
        dashboardData.weatherSummary?.let { weather ->
            item {
                Text(
                    text = "Weather Today",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                WeatherCard(weather = weather)
            }
        }

        // Recent Activities
        if (dashboardData.recentActivities.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Activities",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(dashboardData.recentActivities.take(5)) { activity ->
                ActivityItem(activity = activity)
            }
        }

        // Upcoming Tasks
        if (dashboardData.upcomingTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming Tasks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(dashboardData.upcomingTasks.take(3)) { task ->
                TaskItem(task = task)
            }
        }

        // Notifications
        if (dashboardData.notifications.isNotEmpty()) {
            item {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(dashboardData.notifications.filter { !it.isRead }.take(3)) { notification ->
                NotificationItem(notification = notification)
            }
        }

        // Add spacing at the bottom
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
private fun FarmerProfileCard(
    farmer: Farmer,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = farmer.profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Farmer Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farmer.personalInfo.fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Text(
                    text = farmer.contactInfo.primaryPhone,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )

                farmer.contactInfo.email?.let { email ->
                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = buildString {
                        append(farmer.contactInfo.address!!.district)
                        append(", ")
                        append(farmer.contactInfo.address!!.region)
                    },
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status indicator
                Surface(
                    color = when (farmer.status) {
                        FarmerStatus.ACTIVE -> colorScheme.primary
                        FarmerStatus.PENDING_VERIFICATION -> colorScheme.tertiary
                        FarmerStatus.INACTIVE -> colorScheme.outline
                        FarmerStatus.SUSPENDED -> colorScheme.error
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(12.dp)
                ) {}

                // Action Button - View Profile
                Button(
                    onClick = onClick,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(farmer: Farmer) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            title = "Farms",
            value = farmer.totalFarms.toString(),
            icon = Icons.Default.Landscape,
            color = colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            title = "Crops",
            value = farmer.totalCrops.toString(),
            icon = Icons.Default.Grass,
            color = colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            title = "Experience",
            value = "${farmer.experienceYears}y",
            icon = Icons.Default.EmojiEvents,
            color = colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            title = "Achievements",
            value = farmer.achievements.size.toString(),
            icon = Icons.Default.Star,
            color = colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FarmCard(
    farm: FarmSummary,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val healthContainerColor = when {
        farm.healthScore >= 0.8f -> colorScheme.primary
        farm.healthScore >= 0.6f -> colorScheme.tertiary
        else -> colorScheme.error
    }
    val healthContentColor = when {
        farm.healthScore >= 0.8f -> colorScheme.onPrimary
        farm.healthScore >= 0.6f -> colorScheme.onTertiary
        else -> colorScheme.onError
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = farm.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Health Score Indicator
                Surface(
                    color = healthContainerColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${(farm.healthScore * 100).toInt()}%",
                        color = healthContentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = farm.location,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${farm.size} acres",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.primary
                    )
                    Text(
                        text = "Size",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = farm.activeCrops.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.secondary
                    )
                    Text(
                        text = "Crops",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
// =====================================================
// File: FarmerManagementScreen.kt - UI Components (Part 2)
// Location: feature-farmer-management/src/main/kotlin/com/goldleaf/feature/farmermanagement/ui/screens/FarmerManagementScreen.kt
// =====================================================

@Composable
private fun WeatherCard(weather: WeatherSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (weather.condition.lowercase()) {
                    "sunny", "clear" -> Icons.Default.WbSunny
                    "cloudy" -> Icons.Default.Cloud
                    "rainy", "rain" -> Icons.Default.CloudQueue
                    else -> Icons.Default.WbCloudy
                },
                contentDescription = weather.condition,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${weather.currentTemp.toInt()}°C",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = weather.condition,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = weather.location,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                Text(
                    text = "${weather.humidity}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Humidity",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: DashboardActivity) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                when (activity.type.name.lowercase()) {
                    "planting" -> Icons.Default.Eco
                    "watering" -> Icons.Default.Water
                    "harvesting" -> Icons.Default.Agriculture
                    "fertilizing" -> Icons.Default.Science
                    else -> Icons.Default.CalendarToday
                },
                contentDescription = activity.type.name,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                activity.description?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface
                    )
                }

                Text(
                    text = activity.timestamp.toString(), // Format this properly
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task) {
    val colorScheme = MaterialTheme.colorScheme
    val priorityContainerColor = when (task.priority) {
        TaskPriority.URGENT -> colorScheme.errorContainer
        TaskPriority.HIGH -> colorScheme.tertiaryContainer
        TaskPriority.MEDIUM -> colorScheme.secondaryContainer
        TaskPriority.LOW -> colorScheme.surfaceVariant
    }
    val priorityAccentColor = when (task.priority) {
        TaskPriority.URGENT -> colorScheme.error
        TaskPriority.HIGH -> colorScheme.tertiary
        TaskPriority.MEDIUM -> colorScheme.secondary
        TaskPriority.LOW -> colorScheme.primary
    }
    val priorityOnAccentColor = when (task.priority) {
        TaskPriority.URGENT -> colorScheme.onError
        TaskPriority.HIGH -> colorScheme.onTertiary
        TaskPriority.MEDIUM -> colorScheme.onSecondary
        TaskPriority.LOW -> colorScheme.onPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = priorityContainerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                when (task.priority) {
                    TaskPriority.URGENT -> Icons.Default.PriorityHigh
                    TaskPriority.HIGH -> Icons.Default.KeyboardArrowUp
                    TaskPriority.MEDIUM -> Icons.Default.Remove
                    TaskPriority.LOW -> Icons.Default.KeyboardArrowDown
                    else -> {Icons.Default.Remove}
                },
                contentDescription = task.priority.name,
                tint = priorityAccentColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )

                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Due: ${task.dueDate}", // Format this properly
                    fontSize = 12.sp,
                    color = priorityAccentColor,
                    fontWeight = FontWeight.Medium
                )
            }

            // Priority badge
            Surface(
                color = priorityAccentColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = task.priority.name,
                    color = priorityOnAccentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: Notification) {
    val colorScheme = MaterialTheme.colorScheme
    val notificationContainerColor = when (notification.type) {
        NotificationType.WEATHER -> Color(0xFFE3F2FD)
        NotificationType.CROP -> colorScheme.primaryContainer
        NotificationType.MARKET -> colorScheme.tertiaryContainer
        NotificationType.TRAINING -> colorScheme.surfaceVariant
        NotificationType.TASK -> colorScheme.errorContainer
        NotificationType.SYSTEM -> colorScheme.surfaceVariant
        else -> colorScheme.surface
    }
    val notificationAccentColor = when (notification.type) {
        NotificationType.WEATHER -> Color(0xFF1976D2)
        NotificationType.CROP -> colorScheme.primary
        NotificationType.MARKET -> colorScheme.tertiary
        NotificationType.TRAINING -> colorScheme.primary
        NotificationType.TASK -> colorScheme.error
        NotificationType.SYSTEM -> colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = notificationContainerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                when (notification.type) {
                    NotificationType.WEATHER -> Icons.Default.Cloud
                    NotificationType.CROP -> Icons.Default.Eco
                    NotificationType.MARKET -> Icons.AutoMirrored.Filled.TrendingUp
                    NotificationType.TRAINING -> Icons.Default.School
                    NotificationType.TASK -> Icons.Default.Task
                    NotificationType.SYSTEM -> Icons.Default.Info
                },
                contentDescription = notification.type.name,
                tint = notificationAccentColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = notification.timestamp.toString(), // Format this properly
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
