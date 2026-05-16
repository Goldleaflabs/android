package com.goldleaf.feature.cropmanagement.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.CropEntity

/**
 * Status Progress Slider - Visual representation of crop lifecycle
 * Only allows forward progression, never backwards
 */
@Composable
fun CropStatusProgressSlider(
    currentStatus: CropStatus,
    onStatusSelected: (CropStatus) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val statuses = listOf(
        CropStatus.PLANNED,
        CropStatus.PLANTED,
        CropStatus.GROWING,
        CropStatus.HARVESTED,
        CropStatus.COMPLETED
    )
    
    val currentIndex = statuses.indexOf(currentStatus)
    val progressPercent = if (currentIndex >= 0) {
        (currentIndex.toFloat() / (statuses.size - 1)) * 100f
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 📊 Visual Progress Bar
        Text(
            "Crop Lifecycle Progression",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )


        LinearProgressIndicator(
            progress = { progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.extraSmall),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // 📍 Current Progress Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Current: ${currentStatus.getDisplayName()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${(currentIndex + 1)}/${statuses.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        // 🔘 Status Milestones
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            statuses.forEachIndexed { index, status ->
                val isCompleted = index <= currentIndex
                val isCurrent = index == currentIndex
                val canTransition = index > currentIndex && enabled

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = canTransition) {
                            onStatusSelected(status)
                        }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(if (isCurrent) 44.dp else 36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when {
                                isCompleted && !isCurrent -> {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        "${index + 1}",
                                        color = if (isCompleted) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        status.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(50.dp)
                    )
                }

                // Connecting line between circles (except last one)
                if (index < statuses.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .height(2.dp)
                            .background(
                                if (isCompleted && index < currentIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 📋 Valid Transitions Info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val validNext = currentStatus.getValidNextStatuses()
                
                if (currentStatus == CropStatus.COMPLETED || currentStatus == CropStatus.FAILED) {
                    Text(
                        "⚠️ This crop has reached a final state",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (validNext.isEmpty()) {
                    Text(
                        "No transitions available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Next possible status:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        validNext.forEach { status ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    if (enabled && status != CropStatus.FAILED) {
                                        onStatusSelected(status)
                                    }
                                },
                                label = {
                                    Text(
                                        status.getDisplayName(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                enabled = enabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ⚠️ Warning for FAILED status
        if (currentStatus != CropStatus.FAILED && 
            currentStatus.getValidNextStatuses().contains(CropStatus.FAILED)) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "⚠️ Mark as Failed",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "If this crop has failed or cannot proceed, you can mark it as failed. This is irreversible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Button(
                        onClick = { onStatusSelected(CropStatus.FAILED) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = enabled
                    ) {
                        Text("Mark as Failed")
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet for managing crop status transitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCropStatusSheet(
    crop: CropEntity,
    onStatusUpdated: (CropStatus) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Manage Crop Status",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Current Status Display
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Status:", fontWeight = FontWeight.SemiBold)
                        Surface(
                            color = when (crop.status) {
                                CropStatus.PLANNED -> Color(0xFF9E9E9E)
                                CropStatus.PLANTED -> Color(0xFF2196F3)
                                CropStatus.GROWING -> Color(0xFF4CAF50)
                                CropStatus.HARVESTED -> Color(0xFFFF9800)
                                CropStatus.COMPLETED -> Color(0xFF4CAF50)
                                CropStatus.FAILED -> Color(0xFFF44336)
                                else -> {Color(0xFF9E9E9E)}
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            crop.status?.let {
                                Text(
                                    it.getDisplayName(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Progress Slider
            crop.status?.let {
                CropStatusProgressSlider(
                    currentStatus = it,
                    onStatusSelected = { newStatus ->
                        onStatusUpdated(newStatus)
                        onDismiss()
                    }
                )
            }
        }
    }
}

