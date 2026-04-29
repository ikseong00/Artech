package org.ikseong.artech.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF08285F),
    secondary = GraphiteSlate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E7EF),
    onSecondaryContainer = Color(0xFF111A26),
    tertiary = InsightAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4B8),
    onTertiaryContainer = Color(0xFF2C1900),
    error = ErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFE5E8EE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCFCFD),
    surfaceContainer = Color(0xFFF1F3F6),
    surfaceContainerHigh = Color(0xFFE8EBF0),
    surfaceContainerHighest = Color(0xFFDDE2EA),
)

private val DarkColorScheme = darkColorScheme(
    primary = TechBlueDark,
    onPrimary = Color(0xFF08285F),
    primaryContainer = Color(0xFF1C3F86),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = GraphiteSlateDark,
    onSecondary = Color(0xFF26303B),
    secondaryContainer = Color(0xFF3C4654),
    onSecondaryContainer = Color(0xFFE2E7EF),
    tertiary = InsightAmberDark,
    onTertiary = Color(0xFF482900),
    tertiaryContainer = Color(0xFF66400D),
    onTertiaryContainer = Color(0xFFFFE4B8),
    error = ErrorDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF363D49),
    surfaceContainerLowest = Color(0xFF111318),
    surfaceContainerLow = DarkBackground,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF303642),
)

@Composable
fun ArtechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArtechTypography(),
        content = content,
    )
}
