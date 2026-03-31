package com.zenbuddy.ui.theme

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
    primary = LavenderLight,
    onPrimary = DarkPurple,
    primaryContainer = LavenderDark,
    onPrimaryContainer = LavenderLight,
    secondary = MintLight,
    onSecondary = DarkPurple,
    secondaryContainer = MintDark,
    onSecondaryContainer = MintLight,
    tertiary = PeachLight,
    onTertiary = DarkPurple,
    tertiaryContainer = PeachDark,
    onTertiaryContainer = PeachLight,
    background = DarkPurple,
    onBackground = Color(0xFFE6E1E5),
    surface = DarkSurface,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3D3550),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = CoralRed
)

private val LightColorScheme = lightColorScheme(
    primary = Lavender,
    onPrimary = Color.White,
    primaryContainer = LavenderLight,
    onPrimaryContainer = LavenderDark,
    secondary = Mint,
    onSecondary = Color.White,
    secondaryContainer = MintLight,
    onSecondaryContainer = MintDark,
    tertiary = Peach,
    onTertiary = Color.White,
    tertiaryContainer = PeachLight,
    onTertiaryContainer = PeachDark,
    background = CreamWhite,
    onBackground = Color(0xFF1C1B1F),
    surface = PureWhite,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    error = CoralRed
)

@Composable
fun ZenBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic to keep our cute palette
    content: @Composable () -> Unit
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
        typography = ZenTypography,
        content = content
    )
}
