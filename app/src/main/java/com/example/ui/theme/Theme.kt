package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MoppiLightColorScheme = lightColorScheme(
    primary = MoppiSapphire,
    onPrimary = MoppiWhite,
    primaryContainer = MoppiSurfaceSubtle,
    onPrimaryContainer = MoppiSapphire,
    secondary = MoppiViolet,
    onSecondary = MoppiWhite,
    secondaryContainer = MoppiSurfaceMuted,
    onSecondaryContainer = MoppiViolet,
    tertiary = MoppiCyan,
    onTertiary = MoppiWhite,
    background = MoppiSurfaceSubtle,
    onBackground = MoppiTextPrimary,
    surface = MoppiWhite,
    onSurface = MoppiTextPrimary,
    surfaceVariant = MoppiSurfaceSubtle,
    onSurfaceVariant = MoppiTextSecondary,
    outline = MoppiBorder,
    outlineVariant = MoppiBorderHighlight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced pristine modern white theme as requested by user
    content: @Composable () -> Unit
) {
    val colorScheme = MoppiLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MoppiWhite.toArgb()
            window.navigationBarColor = MoppiWhite.toArgb()
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = true
            windowInsetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
