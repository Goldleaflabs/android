package com.goldleaf.core.auth

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.api.AuthApiService
import com.goldleaf.core.data.dto.auth.DashboardFarmer
import com.goldleaf.core.data.dto.farm.Farmer
import com.goldleaf.core.data.local.FarmerEntity
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.dao.FarmerDao
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.core.data.local.dao.CropDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_session_goldleaf"
)

@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val farmerDao: FarmerDao,
    private val farmDao: FarmDao,
    private val cropDao: CropDao,
    private val apiservice: AuthApiService
) {
    private val dataStore: DataStore<Preferences> = context.sessionDataStore

    private companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ROLE = stringPreferencesKey("user_role")
        val SELECTED_FARM_ID = stringPreferencesKey("selected_farm_id")
    }

    // In-memory cache for fast, synchronous access
    private var cachedAuthToken: String? = null
    private var cachedUserId: String? = null
    private var cachedUserRole: UserRole? = null
    private var cachedFarmId: String? = null


    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // ========== FLOW PROPERTIES ==========
    init {
        kotlinx.coroutines.MainScope().launch {
            dataStore.data
                .onEach {
                    Log.d("UserSessionManager", "READ → $it")
                }
                .collect { preferences ->
                    Log.w("UserSessionManager", "WRITE COMPLETE → $preferences   [Thread: ${Thread.currentThread().name}]")
                }
        }

        // Load cache on startup
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.first().let { prefs ->
                cachedUserId = prefs[USER_ID]
                cachedAuthToken = prefs[AUTH_TOKEN]
                cachedUserRole = prefs[USER_ROLE]?.let {
                    try { UserRole.valueOf(it) } catch (e: Exception) { UserRole.FARMER }
                } ?: UserRole.FARMER
                cachedFarmId = prefs[SELECTED_FARM_ID]

                // Populate the StateFlow so consumers don't hang waiting for a
                // value that only gets set via startSession() at login time.
                _currentUserId.value = cachedUserId
            }
        }
    }

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val currentFarmer: Flow<FarmerEntity?> = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) return@flatMapLatest flowOf(null)
            syncFarmerFromServer(userId)
            farmerDao.getFarmerById(userId)
        }

    // 2. The helper function that handles the network "work"
    // Inside UserSessionManager.kt

    private fun syncFarmerFromServer(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val role = getUserRole()

                // ✅ FIX 1: Use firstOrNull() to prevent the crash
                val localFarmer = farmerDao.getFarmerById(userId).firstOrNull()

                // ✅ FIX 2: If we don't have a local name, we just use a placeholder
                // because the API response will give us the real name anyway.
                val localName = localFarmer?.name ?: "New Farmer"

                Log.d("UserSessionManager", "Syncing farmer: $userId (Found Local: ${localFarmer != null})")

                val requestBody = DashboardFarmer(
                    id = userId,
                    name = localName,
                    userRole = role
                )

                val response = apiservice.getFarmerById(requestBody)

                if (response.isSuccessful) {
                    response.body()?.let { dto ->
                        saveFarmerToDb(dto) // This updates Room, which triggers the UI Flow
                        Log.d("UserSessionManager", "Sync successful. Farmer saved: ${dto.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("UserSessionManager", "Sync failed for user $userId", e)
            }
        }
    }
    // 3. Simple mapping logic (moving data from Network to Database)
    private suspend fun saveFarmerToDb(dto: Farmer) {
                val entity = FarmerEntity(
                    id = dto.id,
                    name = dto.name,
                    phone = dto.phone,
                    email = dto.email,
                    location = dto.location,
                    district = dto.contactInfo?.address?.district,
                    region = dto.contactInfo?.address?.region,
                    street = dto.contactInfo?.address?.street,
                    country = dto.contactInfo?.address?.country,
                    latitude = dto.contactInfo?.address?.latitude,
                    longitude = dto.contactInfo?.address?.longitude,
                    firstName = dto.personalInfo?.firstName ?: "",
                    lastName = dto.personalInfo?.lastName ?: "",
                    nationalId = dto.personalInfo?.nationalId,
                    landSize = dto.farmInfo?.totalLandSize ?: 0.0,
                    landUnit = dto.farmInfo?.landUnit?.name ?: "ACRES",
                    farmingType = dto.farmInfo?.farmingType?.name ?: "CROP_FARMING",
                    experienceYears = dto.farmInfo?.farmingExperienceYears ?: 0,
                    profileImageUrl = dto.profileImageUrl,
                    status = try { dto.status?.name ?: "ACTIVE" } catch (_: Exception) { "ACTIVE" },
                    lastSyncTime = System.currentTimeMillis(),
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                    userRole = try { dto.userRole?.name ?: "FARMER" } catch (_: Exception) { "FARMER" }
                )
        farmerDao.insertFarmer(entity)
        try { setUserRole(dto.userRole) } catch (_: Exception) {}
    }

    /**
     * Gets the current farmer as a Flow
     * Flow that emits true when user is logged in
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[USER_ID] != null && prefs[AUTH_TOKEN] != null
    }

    /**
     * Flow that emits current user role
     */
    val userRole: Flow<UserRole> = dataStore.data.map { prefs ->
        val roleString = prefs[USER_ROLE] ?: return@map UserRole.FARMER
        try {
            UserRole.valueOf(roleString)
        } catch (e: IllegalArgumentException) {
            UserRole.FARMER
        }
    }
    // ========== SESSION MANAGEMENT ==========
  suspend fun startSession(
        userId: String,
        authToken: String,
        refreshToken: String? = null,
        role: UserRole = UserRole.FARMER
    ) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[AUTH_TOKEN] = authToken
            refreshToken?.let { prefs[REFRESH_TOKEN] = it }
            prefs[USER_ROLE] = role.name
        }

        // UPDATE THIS: Push the new ID into the flow
        _currentUserId.value = userId
        // Update cache
        cachedUserId = userId
        cachedAuthToken = authToken
        cachedUserRole = role
    }



    suspend fun getCurrentUserId(): String? {
        return dataStore.data.first()[USER_ID]
    }

    suspend fun setCurrentFarmId(farmId: String) {
        dataStore.edit { prefs ->
            prefs[SELECTED_FARM_ID] = farmId
        }
        cachedFarmId = farmId
    }

    suspend fun getCurrentFarmId(): String? {
        return dataStore.data.first()[SELECTED_FARM_ID]
    }

    fun getCurrentUserIdFlow(): Flow<String?> {
        return dataStore.data.map { it[USER_ID] }
    }

    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[AUTH_TOKEN]
    }

    suspend fun getBearerToken(): String {
        val token = getAuthToken()
        return if (token != null) "Bearer $token" else ""
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[USER_ID] != null && prefs[AUTH_TOKEN] != null
    }

    suspend fun getUserRole(): UserRole {
        val roleString = dataStore.data.first()[USER_ROLE]
        return roleString?.let {
            try {
                UserRole.valueOf(it)
            } catch (e: IllegalArgumentException) {
                UserRole.FARMER
            }
        } ?: UserRole.FARMER
    }


    // SET — THIS IS WHAT YOU NEED
    suspend fun setUserRole(role: UserRole) {
        dataStore.edit { prefs ->
            prefs[USER_ROLE] = role.name  // "FARMER", "VERIFIEDFARMER", "OFFICER", etc.
        }
    }

    suspend fun updateAuthToken(newToken: String) {
        dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = newToken
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    // ========== USER DATA HELPERS ==========

    suspend fun getCurrentFarmer(): Flow<FarmerEntity?>? {
        val userId = getCurrentUserId() ?:return null
        return farmerDao.getFarmerById(userId)
    }



    suspend fun getCurrentUserFarms(): List<FarmEntity> {
        val userId = getCurrentUserId() ?: return emptyList()
        return farmDao.getFarmsByFarmerIdlocal(userId)
    }

    suspend fun getCurrentUserCrops(): List<CropEntity> {
        val userId = getCurrentUserId() ?: return emptyList()
        val farms = farmDao.getFarmsByFarmerIdlocal(userId)
        return farms.flatMap { farm ->
            cropDao.getCropsByFarmId(farm.id)
        }
    }

    // ========== SYNCHRONOUS VERSIONS ==========
    fun getUserIdSync(): String? = runBlocking { getCurrentUserId() }
    fun isLoggedInSync(): Boolean = runBlocking { isLoggedIn() }
    fun getCurrentFarmerSync(): Flow<FarmerEntity?>? = runBlocking { getCurrentFarmer() }
    fun getCurrentUserFarmsSync(): List<FarmEntity> = runBlocking { getCurrentUserFarms() }
    fun getCurrentUserCropsSync(): List<CropEntity> = runBlocking { getCurrentUserCrops() }

    // Fast synchronous access for AuthInterceptor
    fun getAuthTokenSync(): String? = cachedAuthToken
    fun getCurrentUserIdSync(): String? = cachedUserId
    fun getCurrentFarmIdSync(): String? = cachedFarmId
    fun getUserRoleSync(): UserRole = cachedUserRole ?: UserRole.FARMER
}

typealias UserSession = UserSessionManager
