package com.goldleaf.farmerportal

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.farmerportal.navigation.AppNavigation
import com.goldleaf.farmerportal.navigation.DeepLinkHandler
import com.goldleaf.farmerportal.ui.theme.GoldLeafFarmerPortalTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSession: UserSessionManager

    // ✅ State to track intent changes
    private var intentState by mutableStateOf<Intent?>(null)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSM Configuration - The modern way
        val osmConfig = Configuration.getInstance()
   // Replace the deprecated PreferenceManager
        val sharedPrefs = getSharedPreferences("osmdroid", MODE_PRIVATE)
        osmConfig.load(this, sharedPrefs)
      // Set User Agent to avoid being blocked by OSM servers
        osmConfig.userAgentValue = packageName
     // ✅ Set initial intent
        intentState = intent

        setContent {
            GoldLeafFarmerPortalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // ✅ Handle deep links - now reacts to intentState changes
                    LaunchedEffect(intentState) {
                        intentState?.let { currentIntent ->
                            DeepLinkHandler.handleDeepLink(currentIntent, navController)
                        }
                    }

                    AppNavigation(
                        navController = navController,
                        userSession = userSession
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // ✅ Update state to trigger LaunchedEffect
        intentState = intent
    }
}