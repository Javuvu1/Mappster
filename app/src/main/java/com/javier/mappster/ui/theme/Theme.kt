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
private val CinzelDecorative = FontFamily(
    Font(R.font.cinzel_decorative) // referencia al XML cinzel_decorative.xml en res/font
)

private val DefaultFontFamily = FontFamily(
    Font(R.font.default_font_family)
)

// Modo Claro
val TurquesaRetro = Color(0xFF309898 )
val AmarilloRetro = Color(0xFFFF9F00)
val NaranjaRetro = Color(0xFFF4631E)
val RojoRetro = Color(0xFFCB0404)

// Modo oscuro
val TurquesaRetroDark = Color(0xFF4DC5C5)
val AmarilloRetroDark = Color(0xFFFFEB3B)
val NaranjaRetroDark = Color(0xFFF57C3A)
val RojoRetroDark = Color(0xFFE64A4A)

// Light Theme Colors
val ArcaneBlue = Color(0xFF1E3A8A) // Primary: Mystical blue for magic
val ParchmentBeige = Color(0xFFF5E8C7) // Secondary: Aged parchment
val DragonfireRed = Color(0xFFB91C1C) // Tertiary: Fiery red for danger
val ForestGreen = Color(0xFF166534) // OnSurfaceVariant: Nature-inspired green

// Dark Theme Colors
val MidnightBlue = Color(0xFF3B82F6) // Primary: Deep, starry blue
val TarnishedGold = Color(0xFFB89778) // Secondary: Ancient treasure gold
val CrimsonGlow = Color(0xFFDC2626) // Tertiary: Blood-red for intensity
val DarkForestGreen = Color(0xFF4B8A5C) // OnSurfaceVariant: Darker green

private val DarkColorScheme = darkColorScheme(
    primary = MidnightBlue,
    secondary = TarnishedGold,
    tertiary = CrimsonGlow,
    primaryContainer = Color(0xFF1E3A8A), // Darker arcane blue
    secondaryContainer = Color(0xFF8B6F47), // Darker gold
    tertiaryContainer = Color(0xFF7F1D1D), // Darker crimson
    surfaceVariant = Color(0xFF374151), // Shadowed stone gray
    background = Color(0xFF111827), // Dungeon-like dark background
    surface = Color(0xFF1C2526), // Dark stone surface
    onSurface = Color(0xFFD1D5DB), // Light gray for text
    onSurfaceVariant = DarkForestGreen // Dark green for accents
)

private val LightColorScheme = lightColorScheme(
    primary = ArcaneBlue,
    secondary = ParchmentBeige,
    tertiary = DragonfireRed,
    primaryContainer = Color(0xFFBFDBFE), // Light arcane blue
    secondaryContainer = Color(0xFFE8D5A1), // Light parchment
    tertiaryContainer = Color(0xFFFCA5A5), // Light rosy red
    surfaceVariant = Color(0xFFE5E7EB), // Light stone gray
    background = Color(0xFFEDE9E3), // Parchment-like background
    surface = Color(0xFFF8FAF5), // Off-white surface
    onSurface = Color(0xFF1F2937), // Dark gray for text
    onSurfaceVariant = ForestGreen // Green for accents
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
