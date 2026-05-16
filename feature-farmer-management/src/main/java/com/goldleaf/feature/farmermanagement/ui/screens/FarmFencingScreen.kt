package com.goldleaf.feature.farmermanagement.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.GeoPoint
import com.goldleaf.feature.farmermanagement.ui.viewmodels.FarmFencingViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FarmFencingScreen(
    farmId: String? = null,
    onNavigateBack: () -> Unit,
    onFarmSaved: (Farm) -> Unit,
    viewModel: FarmFencingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Save OSM config on dispose
    DisposableEffect(Unit) {
        onDispose {
            org.osmdroid.config.Configuration.getInstance().save(
                context,
                context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
            )
        }
    }

    LaunchedEffect(farmId) {
        if (farmId != null) viewModel.loadFarm(farmId)
        if (!locationPermissions.allPermissionsGranted)
            locationPermissions.launchMultiplePermissionRequest()
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved && uiState.farm != null) onFarmSaved(uiState.farm!!)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Map Layer
        FarmMapView(
            boundaries = uiState.boundaries,
            center = uiState.mapCenter,
            onLocationClick = viewModel::addBoundaryPoint,
            onBoundaryComplete = viewModel::completeBoundary,
            onMarkerDragged = { index, newPoint ->
                viewModel.updateBoundaryPoint(index, newPoint)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Floating Back Button
        FilledIconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // 3. Floating Action Controls (Top Right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = viewModel::getCurrentLocation,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = MaterialTheme.colorScheme.surface)
            }

            if (uiState.boundaries.isNotEmpty()) {
                SmallFloatingActionButton(
                    onClick = viewModel::undoLastPoint,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.surface)
                }

                SmallFloatingActionButton(
                    onClick = viewModel::clearBoundaries,
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.surface)
                }
            }
        }

        // 4. Toggleable Farm Statistics (Bottom)
        if (uiState.boundaries.isNotEmpty()) {
            FarmStatistics(
                area = uiState.calculatedArea,
                perimeter = uiState.calculatedPerimeter,
                boundaryPoints = uiState.boundaries.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        // 5. Save Button (Bottom Right)
        if (uiState.boundaries.size >= 3) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveFarmFencing() },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save Fence") },
                containerColor = Color(0xFF2E7D32),
                contentColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        // 6. Loading Overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun FarmMapView(
    boundaries: List<GeoPoint>,
    center: GeoPoint?,
    onLocationClick: (GeoPoint) -> Unit,
    onBoundaryComplete: () -> Unit,
    onMarkerDragged: (Int, GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track if we've already centered the map on initial load
    var hasInitializedCenter by remember { mutableStateOf(false) }
    
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)

                controller.setZoom(17.0)
                val initialCenter = center?.let { OsmGeoPoint(it.latitude, it.longitude) }
                    ?: OsmGeoPoint(-1.2921, 36.8219)
                controller.setCenter(initialCenter)
                
                // Mark as initialized if center was provided
                if (center != null) {
                    hasInitializedCenter = true
                }

                val mReceive = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                        onLocationClick(GeoPoint(p.latitude, p.longitude))
                        onBoundaryComplete()
                        return true
                    }
                    override fun longPressHelper(p: OsmGeoPoint): Boolean = false
                }
                overlays.add(MapEventsOverlay(mReceive))
            }
        },
        modifier = modifier,
        update = { mapView ->
            // Only move camera once when data is first loaded
            if (!hasInitializedCenter && center != null) {
                val newCenter = OsmGeoPoint(center.latitude, center.longitude)
                mapView.controller.animateTo(newCenter)
                hasInitializedCenter = true
            }

            // Always update markers and polygon
            mapView.overlays.removeIf { it !is MapEventsOverlay }

            boundaries.forEachIndexed { index, point ->
                val marker = Marker(mapView).apply {
                    position = OsmGeoPoint(point.latitude, point.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Point ${index + 1}"
                    isDraggable = true
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker?) {}
                        override fun onMarkerDragStart(marker: Marker?) {}
                        override fun onMarkerDragEnd(marker: Marker?) {
                            marker?.position?.let {
                                onMarkerDragged(index, GeoPoint(it.latitude, it.longitude))
                            }
                        }
                    })
                }
                mapView.overlays.add(marker)
            }

            if (boundaries.size >= 3) {
                val polygon = Polygon().apply {
                    points = boundaries.map { OsmGeoPoint(it.latitude, it.longitude) }
                    fillPaint.color = AndroidColor.argb(60, 76, 175, 80)
                    outlinePaint.color = AndroidColor.rgb(46, 125, 50)
                    outlinePaint.strokeWidth = 4f
                }
                mapView.overlays.add(polygon)
            }
            mapView.invalidate()
        }
    )
}

@Composable
private fun FarmStatistics(
    area: Double,
    perimeter: Double,
    boundaryPoints: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8).copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.large,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.Assessment,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
                Text(
                    text = "Farm Stats",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF2E7D32).copy(alpha = 0.2f))
                StatisticRow("Area", "${String.format("%.2f", area)} acres")
                StatisticRow("Perimeter", "${String.format("%.2f", perimeter)} km")
                StatisticRow("Points", "$boundaryPoints")
            }
        }
    }
}

@Composable
private fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.6f), // Keep it compact
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
    }
}