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

val CinzelDecorative = FontFamily(
    Font(R.font.cinzel_decorative)
)

private val DefaultFontFamily = FontFamily(
    Font(R.font.default_font_family)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E40AF),
    secondary = Color(0xFFF3E8C2),
    tertiary = Color(0xFFD4A017),
    primaryContainer = Color(0xFF4A90E2),
    secondaryContainer = Color(0xFFF8F0D5),
    tertiaryContainer = Color(0xFFFFE082),
    surfaceVariant = Color(0xFFE6E8F0),
    background = Color(0xFFF5F7FA),
    surface = Color(0xFFFCFDFF),
    onSurface = Color(0xFF1C2526),
    onSurfaceVariant = Color(0xFF334155)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1E3A8A),
    secondary = Color(0xFFA68B4C),
    tertiary = Color(0xFFCBA135),
    primaryContainer = Color(0xFF2D4E9F),
    secondaryContainer = Color(0xFF856D3A),
    tertiaryContainer = Color(0xFF9C7A26),
    surfaceVariant = Color(0xFF2C3442),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1A2233),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF828ED9)
)

// Define magical school colors for light mode
private val LightMagicColors = mapOf(
    "Abjuration" to Color(0xFF449D47),
    "Conjuration" to Color(0xFF9C27B0),
    "Divination" to Color(0xFF008B9F),
    "Enchantment" to Color(0xFFBE0F4B),
    "Evocation" to Color(0xFFD24719),
    "Ilussion" to Color(0xFF5736B6),
    "Necromancy" to Color(0xFF455F6C),
    "Transmutation" to Color(0xFFC99600)
)

// Define magical school colors for dark mode
private val DarkMagicColors = mapOf(
    "Abjuration" to Color(0xFF4CAF50),
    "Conjuration" to Color(0xFFD900FF),
    "Divination" to Color(0xFF00CEE8),
    "Enchantment" to Color(0xFFE12D6B),
    "Evocation" to Color(0xFFFF5722),
    "Ilussion" to Color(0xFF6553FC),
    "Necromancy" to Color(0xFF809DA9),
    "Transmutation" to Color(0xFFFFC107)
)

val MaterialTheme.magicColors: Map<String, Color>
    @Composable
    get() = if (isSystemInDarkTheme()) DarkMagicColors else LightMagicColors

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
