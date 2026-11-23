package com.base.iot.core.ui

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

data class AdaptiveLayoutState(
    val isLandscape: Boolean,
    val widthSizeClass: WindowWidthSizeClass,
    val showTwoPanes: Boolean
)

/** Uses the current app window width; orientation alone never enables two panes. */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AdaptiveContentLayout(content: @Composable (AdaptiveLayoutState) -> Unit) {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp
    val isLandscape = width > configuration.screenHeightDp
    val widthSizeClass = when {
        width < 600 -> WindowWidthSizeClass.Compact
        width < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    content(
        AdaptiveLayoutState(
            isLandscape = isLandscape,
            widthSizeClass = widthSizeClass,
            showTwoPanes = widthSizeClass == WindowWidthSizeClass.Expanded
        )
    )
}