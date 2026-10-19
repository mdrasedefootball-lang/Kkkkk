package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = Color(0xFFBBE9FF),
    secondary = ShieldEmerald,
    onSecondary = Color(0xFF002111),
    secondaryContainer = Color(0xFF00522E),
    onSecondaryContainer = Color(0xFF8CF8BE),
    tertiary = MobileOrange,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    error = BlockedRose,
    onError = Color.White,
    outline = SurfaceBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCyanLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBE9FF),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = ShieldEmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF8BE),
    onSecondaryContainer = Color(0xFF002111),
    tertiary = MobileOrange,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceCardLight,
    onSurfaceVariant = TextSecondaryLight,
    error = BlockedRoseDark,
    onError = Color.White,
    outline = SurfaceBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinctive cyber-security branding
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}
