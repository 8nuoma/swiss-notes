package com.swissnotes.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Swiss / International Typographic Style palette.
 *
 * Monochrome with a single signal accent, high contrast, no decoration.
 */
object SwissColors {
    val Black = Color(0xFF111111)
    val White = Color(0xFFFFFFFF)
    val OffWhite = Color(0xFFFAFAF8)
    val Grey = Color(0xFF6B6B6B)
    val LightGrey = Color(0xFFE5E5E2)
    val Accent = Color(0xFFE0392B)
}

private val LightColors = lightColorScheme(
    primary = SwissColors.Black,
    onPrimary = SwissColors.White,
    secondary = SwissColors.Grey,
    onSecondary = SwissColors.White,
    background = SwissColors.White,
    onBackground = SwissColors.Black,
    surface = SwissColors.White,
    onSurface = SwissColors.Black,
    surfaceVariant = SwissColors.OffWhite,
    onSurfaceVariant = SwissColors.Grey,
    outline = SwissColors.LightGrey,
    error = SwissColors.Accent,
    onError = SwissColors.White,
)

/**
 * Typographic system following the Swiss grid discipline:
 * a single sans-serif family, hierarchy expressed through weight
 * and spacing rather than many competing sizes.
 */
private val SwissTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

@Composable
fun SwissNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Swiss style favors light surfaces; keep a single deliberate look.
    MaterialTheme(
        colorScheme = LightColors,
        typography = SwissTypography,
        content = content,
    )
}
