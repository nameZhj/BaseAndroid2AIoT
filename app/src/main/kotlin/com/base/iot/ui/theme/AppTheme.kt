package com.base.iot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==================== 颜色系统 ====================

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4FF),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF00497A),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF3A1C6E),
    onSecondaryContainer = Color(0xFFD4BBFF),
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFF003A2A),
    error = Color(0xFFEF4444),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF30363D),
    outlineVariant = Color(0xFF21262D),
)

// ==================== 主题入口 ====================

/**
 * 应用主题。
 * 采用深色 Material3 配色，与 Dashboard 深色背景保持一致。
 *
 * @param content Composable 内容块
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
