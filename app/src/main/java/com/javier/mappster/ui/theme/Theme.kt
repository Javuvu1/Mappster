package com.javier.mappster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.javier.mappster.R

// Define la familia de fuentes usando el XML font-family
val CinzelDecorative = FontFamily(
    Font(R.font.cinzel_decorative) // referencia al XML cinzel_decorative.xml en res/font
)

private val DefaultFontFamily = FontFamily(
    Font(R.font.default_font_family)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E40AF), // Sapphire blue, vibrant and mystical
    secondary = Color(0xFFF3E8C2), // Light golden beige, like illuminated parchment
    tertiary = Color(0xFFD4A017), // Bright gold for ornate details
    primaryContainer = Color(0xFF4A90E2), // Darker sapphire blue
    secondaryContainer = Color(0xFFF8F0D5), // Lighter golden beige
    tertiaryContainer = Color(0xFFFFE082), // Soft golden tone
    surfaceVariant = Color(0xFFE6E8F0), // Light blue-gray for subtle contrast
    background = Color(0xFFF5F7FA), // Soft white-blue background
    surface = Color(0xFFFCFDFF), // Near-white surface for clarity
    onSurface = Color(0xFF1C2526), // Dark blue-gray for text
    onSurfaceVariant = Color(0xFF334155) // Slate blue for accents
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1E3A8A), // Midnight blue, deep and starry
    secondary = Color(0xFFA68B4C), // Tarnished gold, like ancient ornaments
    tertiary = Color(0xFFCBA135), // Warm gold for glowing highlights
    primaryContainer = Color(0xFF2D4E9F), // Darker midnight blue
    secondaryContainer = Color(0xFF856D3A), // Darker tarnished gold
    tertiaryContainer = Color(0xFF9C7A26), // Darker gold
    surfaceVariant = Color(0xFF2C3442), // Dark blue-gray for depth
    background = Color(0xFF0F172A), // Deep slate blue background
    surface = Color(0xFF1A2233), // Dark sapphire surface
    onSurface = Color(0xFFE2E8F0), // Light blue-gray for text
    onSurfaceVariant = Color(0xFF475569) // Dark slate blue for accents
)

private val MappsterTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelDecorative,
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CinzelDecorative,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CinzelDecorative,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun MappsterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MappsterTypography,
        content = content
    )
}
