package com.example.tripcraft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TripColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    secondary = Color(0xFF4A6267),
    background = Color(0xFFF6FAFB)
)

@Composable
fun TripCraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TripColorScheme,
        content = content
    )
}
