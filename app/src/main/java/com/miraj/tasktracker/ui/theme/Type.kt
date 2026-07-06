package com.miraj.tasktracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Display = FontFamily.SansSerif
private val Body = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 44.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 36.sp, letterSpacing = (-0.3).sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    titleSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.2.sp),
)
