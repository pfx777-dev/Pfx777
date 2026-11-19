package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VardaanDarkColorScheme = darkColorScheme(
    primary = IndigoElectric,
    onPrimary = TextPrimary,
    primaryContainer = IndigoElectric.copy(alpha = 0.22f),
    onPrimaryContainer = IndigoSoft,
    secondary = CyanSpark,
    onSecondary = StudioVoid,
    secondaryContainer = CyanMuted.copy(alpha = 0.25f),
    onSecondaryContainer = CyanSpark,
    tertiary = AmberGold,
    onTertiary = StudioVoid,
    tertiaryContainer = AmberGold.copy(alpha = 0.20f),
    onTertiaryContainer = AmberSoft,
    background = StudioObsidian,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder,
    outlineVariant = StudioCardHighlight,
    error = RoseNeon,
    onError = TextPrimary,
    errorContainer = RoseNeon.copy(alpha = 0.18f),
    onErrorContainer = ColorTokens.ErrorLight
)

private object ColorTokens {
    val ErrorLight = androidx.compose.ui.graphics.Color(0xFFFDA4AF)
}

val VardaanShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun VardaanCreationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VardaanDarkColorScheme,
        typography = VardaanTypography,
        shapes = VardaanShapes,
        content = content
    )
}
