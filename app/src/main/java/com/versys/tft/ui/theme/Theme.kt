package com.versys.tft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun VersysTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = VersysColors.KawasakiGreen,
            onPrimary = Color.Black,
            background = VersysColors.Background,
            onBackground = VersysColors.TextPrimary,
            surface = VersysColors.Surface1,
            onSurface = VersysColors.TextPrimary,
            error = VersysColors.Red,
        ),
        typography = Typography(),
        content = content,
    )
}
