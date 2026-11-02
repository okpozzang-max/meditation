package com.example.morningmeditation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ForestScheme = darkColorScheme(
    primary = Color(0xFFA6C8A0),
    onPrimary = Color(0xFF102214),
    background = Color(0xFF101812),
    surface = Color(0xFF18231B),
    onBackground = Color(0xFFE6ECE4),
    onSurface = Color(0xFFE6ECE4),
)

@Composable
fun MorningMeditationTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ForestScheme, content = content)
}
