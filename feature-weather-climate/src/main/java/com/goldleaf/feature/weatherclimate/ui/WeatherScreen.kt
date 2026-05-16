package com.goldleaf.feature.weatherclimate.ui


import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.core.data.api.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   // LocalContext.current

    // Handle Location Permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Request permissions on start and trigger GPS
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.loadDeviceLocation()
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather & Climate", color = MaterialTheme.colorScheme.surface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.surface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDeviceLocation() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = MaterialTheme.colorScheme.surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. OpenStreetMap Section
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                WeatherMapView(
                    selectedLocation = uiState.selectedLocation,
                    onMapClick = { lat, lon -> viewModel.updateLocationFromMap(lat, lon) }
                )

                // Overlay "Tap to check weather" hint
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Tap map to check weather in other areas",
                        color = MaterialTheme.colorScheme.surface,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. Weather Content Section
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.currentWeather == null -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF4CAF50)
                        )
                    }
                    uiState.error != null -> {
                        ErrorView(
                            error = uiState.error!!,
                            onRetry = { viewModel.loadDeviceLocation() }
                        )
                    }
                    uiState.currentWeather != null -> {
                        WeatherContent(
                            weather = uiState.currentWeather!!,
                            forecast = uiState.forecast,
                            alerts = uiState.alerts
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMapView(
    selectedLocation: Pair<Double, Double>?,
    onMapClick: (Double, Double) -> Unit
) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(12.0)

                val mReceive = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onMapClick(p.latitude, p.longitude)
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                overlays.add(MapEventsOverlay(mReceive))
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            selectedLocation?.let { (lat, lon) ->
                val point = GeoPoint(lat, lon)

                // Update Marker
                mapView.overlays.removeIf { it is Marker }
                val marker = Marker(mapView)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Selected Location"
                mapView.overlays.add(marker)

                // Smoothly move camera if it's the first time or a map click
                mapView.controller.animateTo(point)
            }
            mapView.invalidate()
        }
    )
}

@Composable
private fun WeatherContent(
    weather: Weather,
    forecast: WeatherForecast?,
    alerts: List<WeatherAlert>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Weather Summary
        item {
            CurrentWeatherCard(weather = weather)
        }

        // Active Alerts
        if (alerts.isNotEmpty()) {
            items(alerts) { alert -> AlertCard(alert = alert) }
        }

        // 7-Day Forecast Section
        if (forecast != null && forecast.dailyForecast.isNotEmpty()) {
            item {
                Text("7-Day Forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(forecast.dailyForecast) { day -> DailyForecastCard(day = day) }
        }

        // Detailed Stats
        item { WeatherDetailsCard(weather = weather) }
    }
}

// --- Helper UI Components (CurrentWeatherCard, AlertCard, DailyForecastCard, etc.) ---
// Paste your existing card implementations here (they remain the same as your old code)

@Composable
private fun CurrentWeatherCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF81C784)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = weather.location,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))
                // Use the helper here for a main condition icon
                Icon(
                    imageVector = getWeatherIcon(weather.condition), // <--- Calling your function
                    contentDescription = weather.condition,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "${weather.temperature.toInt()}°C",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = weather.condition,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )

                Text(
                    text = "Feels like ${weather.feelsLike.toInt()}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherStat(
                        icon = Icons.Default.WaterDrop,
                        label = "Humidity",
                        value = "${weather.humidity}%"
                    )
                    WeatherStat(
                        icon = Icons.Default.Air,
                        label = "Wind",
                        value = "${weather.windSpeed} km/h"
                    )
                    WeatherStat(
                        icon = Icons.Default.Umbrella,
                        label = "Rain",
                        value = "${weather.precipitation} mm"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.surface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AlertCard(alert: WeatherAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                AlertSeverity.EXTREME -> Color(0xFFFFEBEE)
                AlertSeverity.SEVERE -> Color(0xFFFFEBEE)
                AlertSeverity.HIGH -> Color(0xFFFFF3E0)
                AlertSeverity.MODERATE -> Color(0xFFFFF9C4)
                AlertSeverity.LOW -> Color(0xFFE3F2FD)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = when (alert.severity) {
                    AlertSeverity.EXTREME, AlertSeverity.SEVERE -> Color(0xFFD32F2F)
                    AlertSeverity.HIGH -> Color(0xFFFF6F00)
                    AlertSeverity.MODERATE -> Color(0xFFF57C00)
                    AlertSeverity.LOW -> Color(0xFF1976D2)
                },
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Until ${alert.validTo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(hourlyForecast: List<HourlyForecast>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(hourlyForecast.take(12)) { hour ->
                HourlyForecastItem(hour = hour)
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(hour: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = hour.time,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector= hour.condition.icon,  // ← hour.condition),
            contentDescription = hour.condition.displayName,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${hour.temperature.toInt()}°",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${hour.precipitation.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2196F3)
        )
    }
}

@Composable
private fun DailyForecastCard(day: DailyForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.dayOfWeek,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                day.condition.icon,
                 day.condition.displayName,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${day.tempMax.toInt()}° / ${day.tempMin.toInt()}°",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${day.rainfall.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailsCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Weather Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(
                icon = Icons.Default.Visibility,
                label = "Visibility",
                value = "${weather.visibility} km"
            )
            DetailRow(
                icon = Icons.Default.Speed,
                label = "Pressure",
                value = "${weather.pressure} hPa"
            )
            DetailRow(
                icon = Icons.Default.WbSunny,
                label = "UV Index",
                value = weather.uvIndex.toString()
            )
            DetailRow(
                icon = Icons.Default.Cloud,
                label = "Cloud Cover",
                value = "${weather.cloudCover}%"
            )
            DetailRow(
                icon = Icons.Default.WbTwilight,
                label = "Sunrise",
                value = weather.sunrise
            )
            DetailRow(
                icon = Icons.Default.Nightlight,
                label = "Sunset",
                value = weather.sunset
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text("Retry")
        }
    }
}

private fun getWeatherIcon(condition: String): ImageVector {
    return when (condition.lowercase()) {
        "clear", "sunny" -> Icons.Default.WbSunny
        "cloudy", "overcast" -> Icons.Default.Cloud
        "rain", "rainy" -> Icons.Default.WaterDrop
        "storm", "thunderstorm" -> Icons.Default.Thunderstorm
        "snow" -> Icons.Default.AcUnit
        "fog", "mist" -> Icons.Default.Cloud  // Foggy doesn't exist, use Cloud
        else -> Icons.Default.WbCloudy
    }
}