package com.base.iot.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeAccent { ARCTIC, ELECTRIC_BLUE }

@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentPrimary: Color,
    val onAccent: Color,
    val accentCyan: Color,
    val accentPurple: Color,
    val accentGreen: Color,
    val accentAmber: Color,
    val accentRed: Color,
    val terminalBg: Color,
    val terminalText: Color
)

private val LightBase = AppColors(
    isDark = false,
    background = Color(0xFFF1F6F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8F0EF),
    cardBorder = Color(0xFFB8C9C6),
    textPrimary = Color(0xFF102321),
    textSecondary = Color(0xFF405653),
    textTertiary = Color(0xFF536B67),
    accentPrimary = Color(0xFF007A70),
    onAccent = Color.White,
    accentCyan = Color(0xFF007A70),
    accentPurple = Color(0xFF6845A5),
    accentGreen = Color(0xFF16734F),
    accentAmber = Color(0xFF875000),
    accentRed = Color(0xFFB4233C),
    terminalBg = Color(0xFF0B1114),
    terminalText = Color(0xFF65D6C2)
)

private val DarkBase = AppColors(
    isDark = true,
    background = Color(0xFF0B1114),
    surface = Color(0xFF141F24),
    surfaceVariant = Color(0xFF1C2B31),
    cardBorder = Color(0xFF34474D),
    textPrimary = Color(0xFFEAF4F3),
    textSecondary = Color(0xFFB7C9C7),
    textTertiary = Color(0xFF9AAEAB),
    accentPrimary = Color(0xFF46D7C5),
    onAccent = Color(0xFF071315),
    accentCyan = Color(0xFF46D7C5),
    accentPurple = Color(0xFFC3A5FF),
    accentGreen = Color(0xFFB7F36B),
    accentAmber = Color(0xFFFFB454),
    accentRed = Color(0xFFFF647C),
    terminalBg = Color(0xFF071013),
    terminalText = Color(0xFFB7F36B)
)

val LightAppColors: AppColors = LightBase
val DarkAppColors: AppColors = DarkBase
val LocalAppColors = staticCompositionLocalOf { DarkBase }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: ThemeAccent = ThemeAccent.ARCTIC,
    content: @Composable () -> Unit
) {
    val base = if (darkTheme) DarkBase else LightBase
    val primary = when (accent) {
        ThemeAccent.ARCTIC -> if (darkTheme) Color(0xFF46D7C5) else Color(0xFF007A70)
        ThemeAccent.ELECTRIC_BLUE -> if (darkTheme) Color(0xFF6EA8FE) else Color(0xFF245EAE)
    }
    val appColors = base.copy(accentPrimary = primary, accentCyan = primary)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = primary, onPrimary = appColors.onAccent,
            secondary = appColors.accentGreen, tertiary = appColors.accentAmber,
            error = appColors.accentRed, surface = appColors.surface,
            onSurface = appColors.textPrimary, background = appColors.background,
            onBackground = appColors.textPrimary, outline = appColors.cardBorder,
            surfaceVariant = appColors.surfaceVariant, onSurfaceVariant = appColors.textSecondary
        )
    } else {
        lightColorScheme(
            primary = primary, onPrimary = appColors.onAccent,
            secondary = appColors.accentGreen, tertiary = appColors.accentAmber,
            error = appColors.accentRed, surface = appColors.surface,
            onSurface = appColors.textPrimary, background = appColors.background,
            onBackground = appColors.textPrimary, outline = appColors.cardBorder,
            surfaceVariant = appColors.surfaceVariant, onSurfaceVariant = appColors.textSecondary
        )
    }
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}