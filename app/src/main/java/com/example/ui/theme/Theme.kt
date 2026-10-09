package com.example.ui.theme

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
    primary = PrimaryGreen80,
    onPrimary = Color(0xFF003820),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = PrimaryGreen80,
    secondary = AccentAmberLight,
    onSecondary = Color(0xFF452200),
    secondaryContainer = Color(0xFF633400),
    onSecondaryContainer = AccentAmberContainer,
    background = SurfaceDark,
    surface = CardBackgroundDark,
    surfaceVariant = Color(0xFF242A27),
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerGreen,
    onPrimaryContainer = OnPrimaryContainerGreen,
    secondary = AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = AccentAmberContainer,
    onSecondaryContainer = OnAccentAmberContainer,
    background = SurfaceLight,
    surface = CardBackgroundLight,
    surfaceVariant = Color(0xFFEFF3F0),
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tailored brand green for consistent identity
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
        typography = Typography,
        content = content
    )
}
