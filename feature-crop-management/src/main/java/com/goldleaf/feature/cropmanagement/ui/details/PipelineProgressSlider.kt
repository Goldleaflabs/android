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
import com.goldleaf.core.data.dto.PipelineStageDto

@Composable
fun PipelineProgressSlider(
    pipelineStages: List<PipelineStageDto>,
    currentStageId: Int?,
    onStageSelected: (PipelineStageDto) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val sortedStages = remember(pipelineStages) {
        pipelineStages.sortedBy { it.sortOrder }
    }

    val currentIndex = sortedStages.indexOfFirst { it.id == currentStageId }

    val progressPercent = if (currentIndex >= 0 && sortedStages.size > 1) {
        (currentIndex.toFloat() / (sortedStages.size - 1)) * 100f
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Current: ${sortedStages.getOrNull(currentIndex)?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${currentIndex + 1}/${sortedStages.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            sortedStages.forEachIndexed { index, stage ->
                val isCompleted = index <= currentIndex
                val isCurrent = index == currentIndex
                val canTransition = index > currentIndex && enabled && !stage.isTerminal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = canTransition) {
                            onStageSelected(stage)
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
                        stage.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(50.dp)
                    )
                }

                if (index < sortedStages.size - 1) {
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

        if (currentIndex >= 0) {
            val currentStage = sortedStages[currentIndex]
            if (currentStage.isTerminal) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "This crop has reached a final stage",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
