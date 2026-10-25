package com.swissnotes.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Palette fusing Swiss monochrome (high contrast, single signal accent)
 * with Google Material tones and Apple's restrained neutrals.
 */
object SwissColors {
    val Black = Color(0xFF1A1A1A)
    val White = Color(0xFFFFFFFF)
    val OffWhite = Color(0xFFF5F5F3)
    val Surface = Color(0xFFFAFAF9)
    val Grey = Color(0xFF6E6E73)
    val LightGrey = Color(0xFFE3E3E0)
    val Accent = Color(0xFFE0392B)
    val AccentSoft = Color(0xFFFCE8E6)

    // Note color marks (kept desaturated for a calm, flat look)
    val NoteRed = Color(0xFFF4DAD8)
    val NoteYellow = Color(0xFFF7EDC9)
    val NoteGreen = Color(0xFFD8EAD6)
    val NoteBlue = Color(0xFFD6E4F0)
    val NotePurple = Color(0xFFE6DEF0)
}

private val LightColors = lightColorScheme(
    primary = SwissColors.Black,
    onPrimary = SwissColors.White,
    secondary = SwissColors.Grey,
    onSecondary = SwissColors.White,
    tertiary = SwissColors.Accent,
    background = SwissColors.White,
    onBackground = SwissColors.Black,
    surface = SwissColors.White,
    onSurface = SwissColors.Black,
    surfaceVariant = SwissColors.Surface,
    onSurfaceVariant = SwissColors.Grey,
    outline = SwissColors.LightGrey,
    outlineVariant = SwissColors.LightGrey,
    error = SwissColors.Accent,
    onError = SwissColors.White,
)

/**
 * Typography: a single sans-serif family; hierarchy through weight and
 * spacing rather than many competing sizes (Swiss discipline), sized on
 * a clean scale (Apple clarity).
 */
private val SwissTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
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
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
)

/**
 * Shapes: flat with soft, consistent rounding (Google Material 3 large
 * rounding, Apple gentle radii — neither too sharp nor too pill-like).
 */
private val SwissShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun SwissNotesTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = SwissTypography,
        shapes = SwissShapes,
        content = content,
    )
}
