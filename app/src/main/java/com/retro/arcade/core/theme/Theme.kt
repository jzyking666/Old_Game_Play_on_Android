package com.retro.arcade.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RetroColorScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = NightInk,
    secondary = PixelLime,
    onSecondary = NightInk,
    tertiary = AmberGlow,
    background = NightInk,
    onBackground = WarmPaper,
    surface = PineShadow,
    onSurface = WarmPaper,
    surfaceVariant = MossPanel,
    onSurfaceVariant = WarmPaper,
    outline = RetroBorder
)

private val RetroShapes = Shapes(
    small = RoundedCornerShape(12),
    medium = RoundedCornerShape(18),
    large = RoundedCornerShape(26)
)

@Composable
fun RetroArcadeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        shapes = RetroShapes,
        content = content
    )
}
