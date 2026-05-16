package com.goldleaf.feature.weatherclimate.navigation


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.goldleaf.feature.weatherclimate.ui.WeatherScreen

const val WEATHER_ROUTE = "weather"

fun NavController.navigateToWeather() {
    navigate(WEATHER_ROUTE) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.weatherScreen(
    onNavigateBack: () -> Unit
) {
    composable(route = WEATHER_ROUTE) {
        WeatherScreen(
            onNavigateBack = onNavigateBack
        )
    }
}