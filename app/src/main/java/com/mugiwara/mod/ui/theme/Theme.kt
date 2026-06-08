package com.mugiwara.mod.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Red500,
    onPrimary = WhiteText,
    secondary = Green500,
    onSecondary = WhiteText,
    background = BlackBackground,
    onBackground = WhiteText,
    surface = BlackSurface,
    onSurface = WhiteText,
    surfaceVariant = BlackCard,
    onSurfaceVariant = GrayText,
)

@Composable
fun MUGIWARAMODTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
