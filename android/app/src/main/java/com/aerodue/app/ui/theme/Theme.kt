package com.aerodue.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SkyBlue = Color(0xFF4FC3F7)
private val Navy = Color(0xFF1B3A5C)

private val LightColors = lightColorScheme(
    primary = Navy,
    secondary = SkyBlue,
    tertiary = Color(0xFF2E7D32),
)

private val DarkColors = darkColorScheme(
    primary = SkyBlue,
    secondary = Navy,
    tertiary = Color(0xFF81C784),
)

@Composable
fun AeroDueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
