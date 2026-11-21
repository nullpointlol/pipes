package com.pipes.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    secondary = BlueElectric,
    background = BgDark,
    surface = CardDark,
    onPrimary = BgDark,
    onSecondary = BgDark,
    onBackground = TextLight,
    onSurface = TextLight
)

@Composable
fun PipesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
