package com.goldleaf.farmerportal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 1. COLOR PALETTE ---
object GoldLeafColors {
    val PrimaryGreen = Color(0xFF2E7D32)
    val PrimaryGreenLight = Color(0xFF4CAF50)
    val PrimaryGreenDark = Color(0xFF1B5E20)
    val GoldPrimary = Color(0xFFFFB300)
    val GoldSecondary = Color(0xFFFFC107)
    val Error = Color(0xFFE53E3E)

    // Neutrals for Light Mode
    val BackgroundPrimary = Color(0xFFFFFEF7)
    val TextPrimary = Color(0xFF1B1B1B)

    // Neutrals for Dark Mode
    val DarkBg = Color(0xFF0F1419)
    val DarkSurface = Color(0xFF141B20)
    val OnDark = Color(0xFFE6E1E5)
}

// --- 2. COLOR SCHEMES ---
private val LightColorScheme = lightColorScheme(
    primary = GoldLeafColors.PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = GoldLeafColors.PrimaryGreenLight,
    secondary = GoldLeafColors.GoldPrimary,
    background = GoldLeafColors.BackgroundPrimary,
    onBackground = GoldLeafColors.TextPrimary,
    surface = Color.White,
    onSurface = GoldLeafColors.TextPrimary,
    error = GoldLeafColors.Error,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldLeafColors.PrimaryGreenLight,
    onPrimary = GoldLeafColors.PrimaryGreenDark,
    primaryContainer = GoldLeafColors.PrimaryGreenDark,
    secondary = GoldLeafColors.GoldSecondary,
    background = GoldLeafColors.DarkBg,
    onBackground = GoldLeafColors.OnDark,
    surface = GoldLeafColors.DarkSurface,
    onSurface = GoldLeafColors.OnDark, // This ensures white text in dark mode
    error = Color(0xFFCF6679),
    onError = Color(0xFF690005)
)

// --- 3. TYPOGRAPHY (Fixed: No hardcoded colors) ---
val GoldLeafTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

// --- 4. SHAPES ---
val GoldLeafShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

// --- 5. THEME COMPOSABLE ---
@Composable
fun GoldLeafFarmerPortalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to keep Gold/Green branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoldLeafTypography,
        shapes = GoldLeafShapes,
        content = content
    )
}