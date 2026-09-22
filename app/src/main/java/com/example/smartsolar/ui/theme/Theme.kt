package com.example.smartsolar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartSolarColorScheme = lightColorScheme(
    primary = LimeAccent,
    secondary = LimeAccentDark,
    tertiary = CharcoalText,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = CharcoalText,
    onSecondary = CharcoalText,
    onTertiary = Color.White,
    onBackground = CharcoalText,
    onSurface = CharcoalText
)

@Composable
fun SmartSolarTheme(
    darkTheme: Boolean = false, // Force light theme for Sopanel aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartSolarColorScheme,
        typography = Typography,
        content = content
    )
}