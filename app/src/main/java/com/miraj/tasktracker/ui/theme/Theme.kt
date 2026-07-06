package com.miraj.tasktracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Amber,
    onSecondary = Paper,
    tertiary = Rust,
    onTertiary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Ink,
    surfaceVariant = Paper2,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = LineSoft,
    error = Rust,
    onError = Paper,
)

@Composable
fun TaskTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // v1 is light only — dark theme can come later.
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
