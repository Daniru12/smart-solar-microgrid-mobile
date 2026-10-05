package com.example.smartsolar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
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

private val SmartSolarDarkColorScheme = darkColorScheme(
    primary = LimeAccent,
    secondary = LimeAccentDark,
    tertiary = TextDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = CharcoalText,
    onSecondary = CharcoalText,
    onTertiary = CharcoalText,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun SmartSolarTheme(
    darkTheme: Boolean = false, 
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SmartSolarDarkColorScheme else SmartSolarColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}