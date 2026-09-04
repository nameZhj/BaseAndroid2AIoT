package com.base.iot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ==================== 设计系统色彩令牌 (Design Tokens) ====================

@Immutable
data class AppColors(
    val isDark: Boolean,
    // 基础容器与表面背景
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBorder: Color,
    // 文本层级（严格遵循 WCAG AAA 对比度标准，严防文字与背景相近）
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // 核心强调色
    val accentPrimary: Color,
    val accentCyan: Color,
    val accentPurple: Color,
    val accentGreen: Color,
    val accentAmber: Color,
    val accentRed: Color,
    // 控制台终端专用
    val terminalBg: Color,
    val terminalText: Color
)

// ==================== 普通模式 (Light Mode) ====================
// 保证高对比度：卡片纯白+轻边框，主文字 Slate-900 对比度 > 15:1，按钮与背景区分鲜明
val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF1F5F9),        // 柔和低眩目 Slate-100
    surface = Color(0xFFFFFFFF),           // 纯白卡片
    surfaceVariant = Color(0xFFF8FAFC),    // 浅灰高亮容器
    cardBorder = Color(0xFFCBD5E1),        // 明显轮廓线 Slate-300
    textPrimary = Color(0xFF0F172A),       // 深玄武黑 Slate-900 (对比度 > 15:1)
    textSecondary = Color(0xFF334155),     // 沉稳灰 Slate-700 (对比度 > 7:1)
    textTertiary = Color(0xFF64748B),      // 辅助弱灰 Slate-500
    accentPrimary = Color(0xFF0284C7),     // 科技深蓝 Sky-600
    accentCyan = Color(0xFF0284C7),        // 普通模式下采用可读性极高的高对比海蓝
    accentPurple = Color(0xFF6D28D9),      // 典雅紫 Violet-700
    accentGreen = Color(0xFF059669),       // 饱满翠绿 Emerald-600
    accentAmber = Color(0xFFD97706),       // 活力金黄 Amber-600
    accentRed = Color(0xFFDC2626),         // 告警艳红 Red-600
    terminalBg = Color(0xFF0F172A),        // 控制台保持暗色专业底
    terminalText = Color(0xFF34D399)       // 高亮绿字
)

// ==================== 夜间模式 (Dark Mode) ====================
// 保证高对比度：深邃黑曜石背景，文字皓白 F8FAFC 对比度 > 15:1，高饱和霓虹强调色
val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0B0F19),        // 黑曜石背景 Obsidian
    surface = Color(0xFF161B26),           // 深暗夜蓝卡片 Slate-900
    surfaceVariant = Color(0xFF1E2638),    // 卡片内嵌高亮区
    cardBorder = Color(0xFF263346),        // 暗夜轮廓边框线
    textPrimary = Color(0xFFF8FAFC),       // 荧光皓白 Slate-50 (对比度 > 15:1)
    textSecondary = Color(0xFF94A3B8),     // 浅灰冷蓝 Slate-400 (对比度 > 7:1)
    textTertiary = Color(0xFF64748B),      // 次要灰 Slate-500
    accentPrimary = Color(0xFF00D4FF),     // 电光霓虹青 Neon Cyan
    accentCyan = Color(0xFF00D4FF),
    accentPurple = Color(0xFF9061F9),      // 幻彩紫 Violet-400
    accentGreen = Color(0xFF10B981),       // 极光绿 Emerald-400
    accentAmber = Color(0xFFF59E0B),       // 警示暖橙 Amber-500
    accentRed = Color(0xFFEF4444),         // 危险烈焰红 Red-500
    terminalBg = Color(0xFF010409),        // 纯黑控制台
    terminalText = Color(0xFF39D353)       // 荧光绿
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

// ==================== 全局主题访问器 ====================

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// ==================== 主题入口 ====================

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appColors.accentPrimary,
            surface = appColors.surface,
            background = appColors.background,
            onPrimary = Color.Black,
            onSurface = appColors.textPrimary,
            onBackground = appColors.textPrimary,
            outline = appColors.cardBorder
        )
    } else {
        lightColorScheme(
            primary = appColors.accentPrimary,
            surface = appColors.surface,
            background = appColors.background,
            onPrimary = Color.White,
            onSurface = appColors.textPrimary,
            onBackground = appColors.textPrimary,
            outline = appColors.cardBorder
        )
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
