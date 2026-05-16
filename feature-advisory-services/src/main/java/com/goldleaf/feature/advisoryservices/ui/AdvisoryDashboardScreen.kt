package com.goldleaf.feature.advisoryservices.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldleaf.feature.advisoryservices.data.AIRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisoryDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPestDetection: () -> Unit = {},
    onNavigateToSoilAnalysis: () -> Unit = {},
    onNavigateToDiseaseDetection: () -> Unit = {},
    viewModel: AdvisoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf(AdvisoryCategory.ALL) }

    LaunchedEffect(Unit) {
        viewModel.loadAdvisoryData()
        viewModel.generatePersonalizedRecommendations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("AI Advisory Services") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshRecommendations() },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isGeneratingRecommendations) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = { /* Chat with AI */ }) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF9C27B0),
                titleContentColor = MaterialTheme.colorScheme.surface,
                navigationIconContentColor = MaterialTheme.colorScheme.surface,
                actionIconContentColor = MaterialTheme.colorScheme.surface
            )
        )

        when {
            uiState.isLoading && uiState.recommendations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF9C27B0))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading AI recommendations...",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFE57373)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Unable to load advisory services",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = uiState.error ?: "Unknown error occurred",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadAdvisoryData() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9C27B0)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AIAssistantHeader(uiState.aiStatus)
                    }

                    item {
                        QuickActionsSection(
                            onPestDetection = onNavigateToPestDetection,
                            onSoilAnalysis = onNavigateToSoilAnalysis,
                            onDiseaseDetection = onNavigateToDiseaseDetection
                        )
                    }

                    item {
                        AdvisoryCategoryTabs(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                    }

                    if (uiState.urgentAlerts.isNotEmpty()) {
                        item {
                            UrgentAlertsSection(uiState.urgentAlerts)
                        }
                    }

                    item {
                        AIRecommendationsSection(
                            recommendations = uiState.recommendations.filter {
                                selectedCategory == AdvisoryCategory.ALL ||
                                        it.category.equals(selectedCategory.name, ignoreCase = true)
                            },
                            isGenerating = uiState.isGeneratingRecommendations
                        )
                    }

                    item {
                        WeatherBasedAdviceSection(uiState.weatherAdvice)
                    }

                    item {
                        CropHealthInsightsSection(uiState.cropHealthInsights)
                    }

                    item {
                        MarketInsightsSection(uiState.marketInsights)
                    }
                }
            }
        }
    }
}

@Composable
private fun AIAssistantHeader(aiStatus: AIStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF9C27B0).copy(alpha = 0.2f),
                                Color(0xFF673AB7).copy(alpha = 0.1f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Farm Assistant",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = aiStatus.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = aiStatus.statusColor,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    aiStatus.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.surface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = aiStatus.statusText,
                                    color = MaterialTheme.colorScheme.surface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last updated: ${aiStatus.lastUpdated}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "AI Assistant",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onPestDetection: () -> Unit,
    onSoilAnalysis: () -> Unit,
    onDiseaseDetection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "AI-Powered Tools",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Pest Detection",
                    description = "AI identifies pests from photos",
                    icon = Icons.Default.BugReport,
                    color = Color(0xFFE91E63),
                    onClick = onPestDetection,
                    modifier = Modifier.weight(1f),
                    badge = "AI Powered"
                )

                QuickActionCard(
                    title = "Disease Scan",
                    description = "Plant disease identification",
                    icon = Icons.Default.LocalHospital,
                    color = Color(0xFF4CAF50),
                    onClick = onDiseaseDetection,
                    modifier = Modifier.weight(1f),
                    badge = "Plant.id API"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Soil Analysis",
                    description = "Smart soil recommendations",
                    icon = Icons.Default.Terrain,
                    color = Color(0xFF795548),
                    onClick = onSoilAnalysis,
                    modifier = Modifier.weight(1f),
                    badge = "Expert System"
                )

                QuickActionCard(
                    title = "Ask AI",
                    description = "Chat with farming expert",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    color = Color(0xFF2196F3),
                    onClick = { /* Navigate to AI chat */ },
                    modifier = Modifier.weight(1f),
                    badge = "Gemini AI"
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                    badge?.let {
                        Surface(
                            color = color,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.surface,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvisoryCategoryTabs(
    selectedCategory: AdvisoryCategory,
    onCategorySelected: (AdvisoryCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(AdvisoryCategory.values()) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                selected = selectedCategory == category,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF9C27B0),
                    selectedLabelColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun UrgentAlertsSection(alerts: List<UrgentAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚨 Urgent Alerts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Surface(
                    color = Color(0xFFFF5722),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${alerts.size}",
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            alerts.forEach { alert ->
                UrgentAlertItem(alert)
                if (alert != alerts.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AIRecommendationsSection(
    recommendations: List<AIRecommendation>,
    isGenerating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Recommendations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                if (isGenerating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generating...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recommendations.isEmpty() && !isGenerating) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "No recommendations",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No recommendations available",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                recommendations.forEach { recommendation ->
                    AIRecommendationItem(recommendation)
                    if (recommendation != recommendations.last()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherBasedAdviceSection(advice: List<WeatherAdvice>) {
    if (advice.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Weather-Based Advice",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                advice.forEach { item ->
                    WeatherAdviceItem(item)
                    if (item != advice.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CropHealthInsightsSection(insights: List<CropHealthInsight>) {
    if (insights.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Crop Health Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                insights.forEach { insight ->
                    CropHealthInsightItem(insight)
                    if (insight != insights.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketInsightsSection(insights: List<MarketInsight>) {
    if (insights.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Market Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                insights.forEach { insight ->
                    MarketInsightItem(insight)
                    if (insight != insights.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// Helper Composables
@Composable
private fun UrgentAlertItem(alert: UrgentAlert) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            alert.icon,
            contentDescription = alert.type,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFFD32F2F)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = alert.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
            if (alert.action.isNotEmpty()) {
                Text(
                    text = "Action: ${alert.action}",
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = alert.timeAgo,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun AIRecommendationItem(recommendation: AIRecommendation) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = recommendation.priorityColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    recommendation.icon,
                    contentDescription = recommendation.category,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                    tint = recommendation.priorityColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recommendation.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Surface(
                        color = recommendation.priorityColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = recommendation.priority,
                            color = MaterialTheme.colorScheme.surface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = recommendation.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (recommendation.steps.isNotEmpty()) {
                    recommendation.steps.forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. $step",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confidence: ${recommendation.confidence}%",
                        fontSize = 12.sp,
                        color = if (recommendation.confidence >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                    Text(
                        text = "Generated by AI • ${recommendation.source}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherAdviceItem(advice: WeatherAdvice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            advice.weatherIcon,
            contentDescription = advice.weatherCondition,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = advice.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = advice.advice,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CropHealthInsightItem(insight: CropHealthInsight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            insight.healthIcon,
            contentDescription = insight.cropName,
            modifier = Modifier.size(24.dp),
            tint = insight.healthColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${insight.cropName} - ${insight.healthStatus}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = insight.insight,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MarketInsightItem(insight: MarketInsight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = "Market Trend",
            modifier = Modifier.size(24.dp),
            tint = if (insight.trend == "up") Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${insight.commodity} - KES ${insight.currentPrice}/kg",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = insight.recommendation,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Data Classes
data class AIStatus(
    val statusText: String,
    val description: String,
    val statusColor: Color,
    val icon: ImageVector,
    val lastUpdated: String
)

data class UrgentAlert(
    val title: String,
    val description: String,
    val type: String,
    val action: String,
    val timeAgo: String,
    val icon: ImageVector
)


data class WeatherAdvice(
    val title: String,
    val advice: String,
    val weatherCondition: String,
    val weatherIcon: ImageVector
)

data class CropHealthInsight(
    val cropName: String,
    val healthStatus: String,
    val healthColor: Color,
    val insight: String,
    val healthIcon: ImageVector
)

data class MarketInsight(
    val commodity: String,
    val currentPrice: Double,
    val trend: String,
    val recommendation: String
)

enum class AdvisoryCategory(val displayName: String) {
    ALL("All"),
    PEST_DISEASE("Pest & Disease"),
    SOIL_HEALTH("Soil Health"),
    WEATHER("Weather"),
    CROP_CARE("Crop Care"),
    FERTILIZER("Fertilizer"),
    IRRIGATION("Irrigation"),
    HARVEST("Harvest"),
    MARKET("Market")
}