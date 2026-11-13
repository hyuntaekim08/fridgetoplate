package com.example.fridgetoplate.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FridgeColors = lightColorScheme(
    primary = Color(0xFF236B55),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCECE2),
    onPrimaryContainer = Color(0xFF163D30),
    secondary = Color(0xFF52665A),
    secondaryContainer = Color(0xFFDCECE2),
    onSecondaryContainer = Color(0xFF163D30),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF202420),
    surface = Color(0xFFFAF7F2),
    onSurface = Color(0xFF202420),
    surfaceVariant = Color(0xFFECE8E0),
    onSurfaceVariant = Color(0xFF555D55),
    surfaceContainer = Color(0xFFF1EDE6),
    surfaceContainerLow = Color(0xFFF7F3ED),
    surfaceContainerHigh = Color(0xFFEBE7DF),
    outline = Color(0xFF7A8279),
    outlineVariant = Color(0xFFD8DCD2)
)

@Composable
fun FridgeToPlateTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FridgeColors,
        content = content
    )
}