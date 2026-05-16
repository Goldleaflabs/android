package com.goldleaf.farmerportal.ui.splash

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.feature.farmermanagement.navigation.AuthRoutes
import com.goldleaf.farmerportal.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    navController: NavHostController,
    userSession: UserSessionManager
) {
    LaunchedEffect(Unit) {
        delay(5000) // 5 seconds of pure luxury animation
        val isLoggedIn = userSession.isLoggedIn.first()
        val userId = userSession.getCurrentUserId()

        if (isLoggedIn && !userId.isNullOrBlank()) {
            navController.navigate(Routes.farmSelection(userId)) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(AuthRoutes.AUTH_GRAPH) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Splash")

    // Continuous Y-axis rotation (0 to 360 degrees)
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "yRotation"
    )

    // Determine color based on which side of the icon is facing the user
    // Front: 0-90 and 270-360 | Back: 90-270
    val animatedColor = if (rotationY in 90f..270f) {
        Color(0xFFD4AF37) // Golden
    } else {
        Color(0xFF4CAF50) // Green
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = "GoldLeaf Logo",
                tint = animatedColor,
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        cameraDistance = 12f * density
                        val currentRotation = rotationY
                        this.rotationY = if (currentRotation in 90f..270f) {
                            currentRotation + 180f
                        } else {
                            currentRotation
                        }



                    }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Gold Leaf Labs",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Farmer Portal",
                fontSize = 22.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(60.dp))

            CircularProgressIndicator(
                strokeWidth = 6.dp,
                color = animatedColor,
                modifier = Modifier.size(48.dp)
            )
        }
    }

}
