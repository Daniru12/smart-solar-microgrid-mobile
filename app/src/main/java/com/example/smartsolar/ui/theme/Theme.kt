package com.example.smartsolar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartSolarColorScheme = darkColorScheme(
    primary = Teal500,
    secondary = Teal300,
    tertiary = Teal400,
    background = Navy900,
    surface = Navy800,
    onPrimary = Navy900,
    onSecondary = Navy900,
    onTertiary = Navy900,
    onBackground = Color.White,
    onSurface = Slate300
)

@Composable
fun SmartSolarTheme(
    darkTheme: Boolean = true, // Force dark theme for brand aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartSolarColorScheme,
        typography = Typography,
        content = content
    )
}