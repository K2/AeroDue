package com.aerodue.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Indigo700,
    onPrimary = Cloud,
    primaryContainer = Color(0xFFD9E4FB),
    onPrimaryContainer = Indigo900,
    secondary = Sky500,
    onSecondary = Ink900,
    secondaryContainer = Color(0xFFD4F1FE),
    onSecondaryContainer = Color(0xFF06324A),
    tertiary = Money600,
    onTertiary = Cloud,
    tertiaryContainer = Color(0xFFCDF3E4),
    onTertiaryContainer = Color(0xFF053D2B),
    error = Rose500,
    onError = Cloud,
    background = Mist50,
    onBackground = Ink900,
    surface = Cloud,
    onSurface = Ink900,
    surfaceVariant = Mist100,
    onSurfaceVariant = Ink500,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
)

private val DarkColors = darkColorScheme(
    primary = Sky400,
    onPrimary = Indigo900,
    primaryContainer = Indigo700,
    onPrimaryContainer = Color(0xFFD9E4FB),
    secondary = Sky300,
    onSecondary = Indigo900,
    secondaryContainer = Indigo800,
    onSecondaryContainer = Sky300,
    tertiary = Money400,
    onTertiary = Color(0xFF053D2B),
    tertiaryContainer = Color(0xFF0B5C42),
    onTertiaryContainer = Color(0xFFCDF3E4),
    error = Rose300,
    onError = Color(0xFF3A0512),
    background = Night,
    onBackground = Color(0xFFE6ECF7),
    surface = NightSurface,
    onSurface = Color(0xFFE6ECF7),
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = Color(0xFF9FB0CC),
    outline = NightLine,
    outlineVariant = Color(0xFF1B2741),
)

@Composable
fun AeroDueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Material You wallpaper-based color on Android 12+; falls back to brand palette. */
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = colorScheme.background.luminance() > 0.5f
            controller.isAppearanceLightNavigationBars = colorScheme.background.luminance() > 0.5f
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AeroDueTypography,
        shapes = AeroDueShapes,
        content = content,
    )
}
