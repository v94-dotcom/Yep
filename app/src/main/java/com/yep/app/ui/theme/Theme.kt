package com.yep.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = GreenDarkest,
    secondary = GreenLight,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Surface,
    onBackground = Charcoal,
    surface = Surface,
    onSurface = Charcoal,
    surfaceVariant = WarmGrayLight,
    onSurfaceVariant = NeutralGray,
    surfaceContainer = Surface,
    outline = BorderGray,
    outlineVariant = BorderGray,
    error = DangerRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenLight,
    onPrimary = DarkBackground,
    primaryContainer = GreenPrimary.copy(alpha = 0.18f),
    onPrimaryContainer = GreenLight,
    secondary = GreenPrimary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkBackground,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DangerRed,
)

@Composable
fun YepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YepTypography,
        content = content
    )
}
