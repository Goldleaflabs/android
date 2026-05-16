package com.goldleaf.feature.advisoryservices.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.*
import com.goldleaf.core.data.local.dao.AdvisoryDao
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.core.data.local.dao.FarmerDao
import com.goldleaf.core.data.local.dao.SoilDao
import com.goldleaf.core.data.local.dao.WeatherDao
import com.goldleaf.feature.advisoryservices.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AdvisoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val farmerDao: FarmerDao,
    private val farmDao: FarmDao,
    private val cropDao: CropDao,
    private val weatherDao: WeatherDao,
    private val advisoryDao: AdvisoryDao,
    private val soilDao: SoilDao
) {

    suspend fun getFarmerData(): FarmerData {
        return withContext(Dispatchers.IO) {
            val farmers = farmerDao.getAllFarmers().first()
            val farmer = farmers.firstOrNull()
                ?: throw NoSuchElementException("No farmer found in database")

             val farms = farmDao.getFarmsByFarmerId(farmer.id)
             val totalFarmSize: Flow<Double> = farms.map { farmEntities ->
                 farmEntities.sumOf { it.size }
            }

            // Get advisories to determine challenges
            val advisories = advisoryDao.getAdvisoriesByFarmerId(farmer.id)
            val challenges = advisories
                .filter { it.priority == "HIGH" || it.priority == "URGENT" }
                .map { it.category }
                .distinct()
                .take(3)

         val farmSize: Double = totalFarmSize.first()

            FarmerData(
                location = farmer.location ?: "Location not set",
                farmSize = farmSize,
                challenges = challenges.ifEmpty { listOf("No critical issues") }
            )
        }
    }

    suspend fun getCurrentWeatherData(): WeatherContextData {
        return withContext(Dispatchers.IO) {
            val farmers = farmerDao.getAllFarmers().first()
            val farmer = farmers.firstOrNull()
                ?: throw NoSuchElementException("No farmer found in database")

            val farms = farmDao.getFarmsByFarmerIdlocal(farmer.id)
            val primaryFarm = farms.firstOrNull()
                ?: throw NoSuchElementException("No farm found for farmer")

            val latitude = primaryFarm.latitude
                ?: throw IllegalStateException("Farm location not set")
            val longitude = primaryFarm.longitude
                ?: throw IllegalStateException("Farm location not set")

            val weather = weatherDao.getWeather(latitude, longitude).firstOrNull()
                ?: throw NoSuchElementException("No weather data available")

            // Calculate rain chance based on weather condition
            val rainChance = when (weather.weatherCondition.lowercase()) {
                "rain", "thunderstorm" -> 90
                "drizzle" -> 70
                "clouds" -> 40
                else -> 10
            }

            WeatherContextData(
                temperature = weather.temperature.toInt(),
                condition = weather.weatherDescription,
                humidity = weather.humidity,
                rainChance = rainChance,
                rainfall = weather.rainfall
            )
        }
    }

    suspend fun getCurrentCropData(): List<CropData> {
        return withContext(Dispatchers.IO) {
            val crops = cropDao.getAllCrops().first()

            crops
                .filter { it.status == CropStatus.PLANTED || it.status == CropStatus.GROWING  }
                .map { crop ->
                    // Calculate health score based on crop age and status

                    val plantingDate = LocalDate.parse(crop.plantingDate!!)
                        .atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                    val now = Clock.System.now()
                    val daysGrowing = ((now.toEpochMilliseconds() - plantingDate) / (1000 * 60 * 60 * 24)).toInt()

                    val healthScore = when (crop.status) {
                        CropStatus.GROWING -> 85
                        CropStatus.PLANTED -> if (daysGrowing < 7) 75 else 80
                        else -> 70
                    }

                    val plantingDateStr = Instant.fromEpochMilliseconds(plantingDate)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .let { "${it.month.name.take(3)} ${it.year}" }

                    CropData(
                        name = crop.name,
                        variety = crop.variety.toString(),
                        plantingDate = plantingDateStr,
                        healthScore = healthScore
                    )
                }
        }
    }

    suspend fun getSoilData(): SoilData {
        return withContext(Dispatchers.IO) {
            val farmers = farmerDao.getAllFarmers().first()
            val farmer = farmers.firstOrNull()
                ?: throw NoSuchElementException("No farmer found in database")

            val farms = farmDao.getFarmsByFarmerIdlocal(farmer.id)
            val primaryFarm = farms.firstOrNull()
                ?: throw NoSuchElementException("No farm found for farmer")

            // Get the most recent soil test
            val soilTest = soilDao.getLatestSoilTest(primaryFarm.id)
                ?: throw NoSuchElementException("No soil test data available for this farm")

            // Check if data is too old (older than 1 year)
            val oneYearAgo = Clock.System.now().toEpochMilliseconds() - (365L * 24 * 60 * 60 * 1000)
            if (soilTest.testDate < oneYearAgo) {
                // Data is old but still return it with a warning
                // You could log this or show a warning to the user
            }

            SoilData(
                type = soilTest.soilType,
                ph = soilTest.ph,
                nitrogen = soilTest.nitrogen,
                phosphorus = soilTest.phosphorus,
                potassium = soilTest.potassium
            )
        }
    }

    // Optional: Add function to get detailed soil test
    suspend fun getDetailedSoilTest(): SoilTestEntity? {
        return withContext(Dispatchers.IO) {
            val farmers = farmerDao.getAllFarmers().first()
            val farmer = farmers.firstOrNull() ?: return@withContext null

            val farms = farmDao.getFarmsByFarmerIdlocal(farmer.id)
            val primaryFarm = farms.firstOrNull() ?: return@withContext null

            soilDao.getLatestSoilTest(primaryFarm.id)
        }
    }

    // Optional: Add function to get soil test history
    suspend fun getSoilTestHistory(farmId: String): List<SoilTestEntity> {
        return withContext(Dispatchers.IO) {
            soilDao.getSoilTestsByFarmId(farmId)
        }
    }

    // Optional: Add function to save new soil test
    suspend fun saveSoilTest(farmId: String, soilTest: SoilTestEntity) {
        withContext(Dispatchers.IO) {
            soilDao.insertSoilTest(soilTest.copy(farmId = farmId))
        }
    }

    suspend fun getUrgentAlerts(): List<UrgentAlert> {
        return withContext(Dispatchers.IO) {
            val alerts = mutableListOf<UrgentAlert>()

            try {
                // Get weather-based alerts
                val farmers = farmerDao.getAllFarmers().first()
                val farmer = farmers.firstOrNull()

                if (farmer != null) {
                    val farms = farmDao.getFarmsByFarmerIdlocal(farmer.id)
                    val primaryFarm = farms.firstOrNull()

                    // ✅ FIX: Store nullable values in local variables first
                    if (primaryFarm != null && primaryFarm.latitude != null && primaryFarm.longitude != null) {
                        val latitude = primaryFarm.latitude!!  // Safe to use !! here after null check
                        val longitude = primaryFarm.longitude!!
                        val weather = weatherDao.getWeather(latitude, longitude).firstOrNull()

                        if (weather != null) {
                            // Extreme heat warning
                            if (weather.temperature > 35) {
                                val hoursAgo = calculateHoursAgo(weather.timestamp)
                                alerts.add(
                                    UrgentAlert(
                                        title = "Extreme Heat Warning",
                                        description = "Temperature is ${weather.temperature.toInt()}°C. Crops at risk of heat stress.",
                                        type = "weather",
                                        action = "Increase irrigation immediately",
                                        timeAgo = "$hoursAgo hours ago",
                                        icon = Icons.Default.WbSunny
                                    )
                                )
                            }

                            // Heavy rain alert
                            if (weather.weatherCondition.lowercase() in listOf("rain", "thunderstorm")) {
                                val hoursAgo = calculateHoursAgo(weather.timestamp)
                                alerts.add(
                                    UrgentAlert(
                                        title = "Heavy Rain Alert",
                                        description = "${weather.weatherDescription}. Potential flooding risk.",
                                        type = "weather",
                                        action = "Secure loose materials and check drainage",
                                        timeAgo = "$hoursAgo hours ago",
                                        icon = Icons.Default.WaterDrop
                                    )
                                )
                            }
                        }
                    }

                    // Get pest/disease alerts from advisories
                    val urgentAdvisories = advisoryDao.getAdvisoriesByFarmerId(farmer.id)
                        .filter {
                            it.priority == "URGENT" &&
                                    !it.isRead &&
                                    (it.category == "PEST" || it.category == "DISEASE")
                        }
                        .take(2)

                    urgentAdvisories.forEach { advisory ->
                        val hoursAgo = calculateHoursAgo(advisory.createdAt)
                        alerts.add(
                            UrgentAlert(
                                title = advisory.title,
                                description = advisory.content.take(100),
                                type = advisory.category.lowercase(),
                                action = "Check advisory details for action plan",
                                timeAgo = "$hoursAgo hours ago",
                                icon = when (advisory.category) {
                                    "PEST" -> Icons.Default.BugReport
                                    "DISEASE" -> Icons.Default.Healing
                                    else -> Icons.Default.Warning
                                }
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                // Log error but return whatever alerts we collected
                e.printStackTrace()
            }

            alerts
        }
    }


    suspend fun saveAdvisoryRecommendation(recommendation: AIRecommendation) {
        withContext(Dispatchers.IO) {
            val farmers = farmerDao.getAllFarmers().first()
            val farmer = farmers.firstOrNull()
                ?: throw NoSuchElementException("No farmer found in database")

            val advisoryEntity = AdvisoryEntity(
                id = generateId(),
                farmerId = farmer.id,
                title = recommendation.title,
                content = recommendation.description,
                category = recommendation.category,
                priority = recommendation.priority,
                isRead = false,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                expiresAt = null
            )

            advisoryDao.insertAdvisory(advisoryEntity)
        }
    }

    suspend fun getHistoricalRecommendations(): List<AIRecommendation> {
        return withContext(Dispatchers.IO) {
            val farmers = farmerDao.getAllFarmers().first()
            val farmer = farmers.firstOrNull()
                ?: return@withContext emptyList()

            val advisories = advisoryDao.getAdvisoriesByFarmerId(farmer.id)

            advisories.map { entity ->
                AIRecommendation(
                    title = entity.title,
                    description = entity.content,
                    category = entity.category,
                    priority = entity.priority,
                    priorityColor = when (entity.priority) {
                        "HIGH", "URGENT" -> androidx.compose.ui.graphics.Color(0xFFE91E63)
                        "NORMAL" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                        else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    },
                    confidence = 85,
                    steps = entity.content.split(". ").filter { it.isNotBlank() },
                    source = "Historical",
                    icon = Icons.Default.History
                )
            }
        }
    }

    // Add this to AdvisoryRepository or create a separate SoilRepository

    suspend fun createSoilTest(
        farmId: String,
        soilType: String,
        ph: Double,
        nitrogen: Double,
        phosphorus: Double,
        potassium: Double,
        organicMatter: Double? = null,
        testLocation: String? = null,
        labName: String? = null,
        notes: String? = null
    ): Result<SoilTestEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val soilTest = SoilTestEntity(
                    id = "soil_test_${Clock.System.now().toEpochMilliseconds()}",
                    farmId = farmId,
                    testDate = Clock.System.now().toEpochMilliseconds(),
                    soilType = soilType,
                    ph = ph,
                    nitrogen = nitrogen,
                    phosphorus = phosphorus,
                    potassium = potassium,
                    organicMatter = organicMatter,
                    testLocation = testLocation,
                    labName = labName,
                    notes = notes
                )

                soilDao.insertSoilTest(soilTest)
                Result.success(soilTest)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun calculateHoursAgo(timestamp: Long): Int {
        val now = Clock.System.now().toEpochMilliseconds()
        return ((now - timestamp) / (1000 * 60 * 60)).toInt()
    }

    private fun generateId(): String {
        return "advisory_${Clock.System.now().toEpochMilliseconds()}"
    }
    
}