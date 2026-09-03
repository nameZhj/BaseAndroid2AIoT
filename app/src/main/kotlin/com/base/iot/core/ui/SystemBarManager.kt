package com.base.iot.core.ui

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 沉浸式状态栏与导航栏管理工具。
 *
 * 架构设计：
 * - [SystemBarManager] 对象提供全局默认配置
 * - [SystemBarEffect] Composable 用于页面级局部定制（退出页面自动还原）
 *
 * 使用示例：
 *
 * 1. 在 MainActivity 入口处设置全局默认（沉浸式透明）：
 * ```kotlin
 * SystemBarManager.applyDefault(window, darkIcons = true)
 * ```
 *
 * 2. 在某个 Screen 内设置局部状态栏颜色（退出后自动还原到上一层配置）：
 * ```kotlin
 * SystemBarEffect(
 *     statusBarColor = Color(0xFF1A1A2E),
 *     darkStatusBarIcons = false
 * )
 * ```
 */
object SystemBarManager {

    /**
     * 全局初始化：启用边到边绘制，设置透明状态栏/导航栏。
     * 通常在 [Activity.onCreate] 或 [setContent] 之前调用。
     *
     * @param activity 宿主 Activity
     * @param darkStatusIcons 状态栏图标是否为深色（true=深色图标，适合浅色背景）
     * @param darkNavIcons 导航栏图标是否为深色
     */
    fun applyDefault(
        activity: Activity,
        darkStatusIcons: Boolean = false,
        darkNavIcons: Boolean = false
    ) {
        // 启用边到边（Edge-to-Edge）布局
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = darkStatusIcons
        controller.isAppearanceLightNavigationBars = darkNavIcons

        // 透明状态栏与导航栏
        activity.window.statusBarColor = Color.Transparent.toArgb()
        activity.window.navigationBarColor = Color.Transparent.toArgb()
    }

    /**
     * 动态修改状态栏图标颜色风格（可在运行时切换）。
     *
     * @param activity 宿主 Activity
     * @param darkIcons true = 深色图标（适合浅色背景）
     */
    fun setStatusBarIconsDark(activity: Activity, darkIcons: Boolean) {
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = darkIcons
    }

    /**
     * 隐藏/显示状态栏。
     */
    fun setStatusBarVisible(activity: Activity, visible: Boolean) {
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        if (visible) {
            controller.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        }
    }
}

// ==================== Composable 页面级效果 ====================

/**
 * 页面级状态栏/导航栏定制 Composable Effect。
 *
 * 进入 Composition 时应用设置，退出时自动还原到默认状态（深色图标）。
 *
 * @param statusBarColor 状态栏背景色（仅 Android < 15 有效，高版本需配合 Edge-to-Edge）
 * @param darkStatusBarIcons 状态栏图标是否为深色
 * @param darkNavBarIcons 导航栏图标是否为深色
 * @param hideStatusBar 是否隐藏状态栏
 */
@Composable
fun SystemBarEffect(
    statusBarColor: Color = Color.Transparent,
    darkStatusBarIcons: Boolean = true,
    darkNavBarIcons: Boolean = true,
    hideStatusBar: Boolean = false
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context as? Activity ?: return

    DisposableEffect(statusBarColor, darkStatusBarIcons, darkNavBarIcons, hideStatusBar) {
        val window = activity.window
        val originalStatusColor = window.statusBarColor
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val originalLightStatus = controller.isAppearanceLightStatusBars
        val originalLightNav = controller.isAppearanceLightNavigationBars

        // 应用新配置
        window.statusBarColor = statusBarColor.toArgb()
        controller.isAppearanceLightStatusBars = darkStatusBarIcons
        controller.isAppearanceLightNavigationBars = darkNavBarIcons

        if (hideStatusBar) {
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        }

        onDispose {
            // 退出时还原
            window.statusBarColor = originalStatusColor
            controller.isAppearanceLightStatusBars = originalLightStatus
            controller.isAppearanceLightNavigationBars = originalLightNav
            if (hideStatusBar) {
                controller.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            }
        }
    }
}
