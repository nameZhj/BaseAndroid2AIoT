package com.base.iot.core.ui

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * 屏幕自适应状态，由 [AdaptiveContentLayout] 注入到内容函数。
 *
 * @param isLandscape 当前是否为横屏
 * @param widthSizeClass 当前窗口宽度等级（Compact/Medium/Expanded）
 * @param showTwoPanes 是否应当显示双栏布局（横屏 或 Expanded 宽度）
 */
data class AdaptiveLayoutState(
    val isLandscape: Boolean,
    val widthSizeClass: WindowWidthSizeClass,
    val showTwoPanes: Boolean
)

/**
 * 多端与横竖屏自适应布局容器。
 *
 * 规则：
 * - 横屏 或 WindowWidthSizeClass.Expanded → [showTwoPanes] = true（双栏）
 * - 竖屏 且 Compact/Medium               → [showTwoPanes] = false（单栏）
 *
 * 宽度等级判断（参考 Material Design 断点）：
 * - Compact  : width < 600dp
 * - Medium   : 600dp ≤ width < 840dp
 * - Expanded : width ≥ 840dp
 *
 * 使用示例：
 * ```kotlin
 * AdaptiveContentLayout { state ->
 *     if (state.showTwoPanes) {
 *         Row {
 *             MasterPanel(Modifier.weight(0.4f))
 *             DetailPanel(Modifier.weight(0.6f))
 *         }
 *     } else {
 *         SingleColumnContent()
 *     }
 * }
 * ```
 *
 * @param content 接收 [AdaptiveLayoutState] 的内容 lambda
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AdaptiveContentLayout(
    content: @Composable (AdaptiveLayoutState) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp

    // 根据屏幕宽度计算 WindowWidthSizeClass（无需传入 Activity）
    val widthSizeClass = when {
        screenWidthDp < 600 -> WindowWidthSizeClass.Compact
        screenWidthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }

    val showTwoPanes = isLandscape || widthSizeClass == WindowWidthSizeClass.Expanded

    val state = AdaptiveLayoutState(
        isLandscape = isLandscape,
        widthSizeClass = widthSizeClass,
        showTwoPanes = showTwoPanes
    )

    content(state)
}
