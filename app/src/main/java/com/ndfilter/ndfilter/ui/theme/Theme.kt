package com.ndfilter.ndfilter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GreenBright,
    secondary = GreenDark,
    tertiary = GreenBright,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface,
    secondaryContainer = GreenDark,
    onSecondaryContainer = GreenBright,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = OnDarkSurface,
    outline = GreenBright,
)

@Composable
fun NdFilterTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
