package com.norvexa.onestep.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.norvexa.onestep.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFF5F5C71),
    tertiary = Color(0xFF7A5367),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    secondary = Color(0xFFC9C5D8),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF21005D),
    primaryContainer = Color(0xFF381E72),
    background = Color.Black,
    surface = Color.Black,
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF171717),
)

@Composable
fun OneStepTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val scheme = when (mode) {
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.AMOLED -> AmoledColors
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
