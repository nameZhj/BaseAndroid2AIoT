package com.base.iot.core.ui.adaptive

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DeviceType {
    PHONE,
    TABLET,
    FOLDABLE
}

@Immutable
data class PadSpec(
    val isTablet: Boolean,
    val deviceType: DeviceType,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val smallestScreenWidthDp: Int,
    val isLandscape: Boolean,
    val joystickSize: Dp,
    val actionButtonHeight: Dp,
    val centerPanelWidth: Dp,
    val dialogMaxWidth: Dp,
    val sheetMaxWidth: Dp,
    val canShowCompanionPane: Boolean
)

val LocalPadSpec: ProvidableCompositionLocal<PadSpec> = staticCompositionLocalOf {
    PadSpec(
        isTablet = false,
        deviceType = DeviceType.PHONE,
        screenWidthDp = 640,
        screenHeightDp = 360,
        smallestScreenWidthDp = 360,
        isLandscape = true,
        joystickSize = 130.dp,
        actionButtonHeight = 40.dp,
        centerPanelWidth = 170.dp,
        dialogMaxWidth = 440.dp,
        sheetMaxWidth = 640.dp,
        canShowCompanionPane = false
    )
}

@Composable
fun rememberPadSpec(): PadSpec {
    val config = LocalConfiguration.current
    val sw = config.smallestScreenWidthDp
    val w = config.screenWidthDp
    val h = config.screenHeightDp
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = sw >= 600

    return remember(sw, w, h, isLandscape) {
        val deviceType = when {
            isTablet -> DeviceType.TABLET
            w >= 600 && sw < 600 -> DeviceType.FOLDABLE
            else -> DeviceType.PHONE
        }
        val joystick = if (isTablet) 165.dp else 130.dp
        val btnHeight = if (isTablet) 46.dp else 40.dp
        val centerWidth = if (isTablet) 240.dp else 170.dp
        val dialogMax = if (isTablet) 540.dp else 440.dp
        val sheetMax = 640.dp
        val canShowCompanion = isTablet && w >= 960 && isLandscape

        PadSpec(
            isTablet = isTablet,
            deviceType = deviceType,
            screenWidthDp = w,
            screenHeightDp = h,
            smallestScreenWidthDp = sw,
            isLandscape = isLandscape,
            joystickSize = joystick,
            actionButtonHeight = btnHeight,
            centerPanelWidth = centerWidth,
            dialogMaxWidth = dialogMax,
            sheetMaxWidth = sheetMax,
            canShowCompanionPane = canShowCompanion
        )
    }
}

@Composable
fun ProvidePadSpec(
    spec: PadSpec = rememberPadSpec(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPadSpec provides spec) {
        content()
    }
}
