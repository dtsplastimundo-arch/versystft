package com.versys.tft.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Números en tabular ("tnum") para que el velocímetro no "baile" al cambiar de dígito. */
val NumberStyle = TextStyle(
    color = VersysColors.TextPrimary,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum",
    letterSpacing = (-1).sp,
)

val LabelStyle = TextStyle(
    color = VersysColors.TextSecondary,
    fontSize = 10.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 1.sp,
)
