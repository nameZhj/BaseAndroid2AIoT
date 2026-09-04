package com.base.iot.core.ui.pad

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

enum class PadDeviceClass {
    PHONE,
    TABLET_COMPACT,
    TABLET_EXPANDED
}

val PadDeviceClass.isPad: Boolean
    get() = this != PadDeviceClass.PHONE

val LocalPadDeviceClass: ProvidableCompositionLocal<PadDeviceClass> =
    staticCompositionLocalOf { PadDeviceClass.PHONE }

@Composable
fun rememberPadDeviceClass(): PadDeviceClass {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val sw = configuration.smallestScreenWidthDp
        when {
            sw >= 720 -> PadDeviceClass.TABLET_EXPANDED
            sw >= 600 -> PadDeviceClass.TABLET_COMPACT
            else -> PadDeviceClass.PHONE
        }
    }
}

@Composable
fun rememberIsPad(): Boolean {
    val deviceClass = rememberPadDeviceClass()
    return remember(deviceClass) { deviceClass.isPad }
}

@Composable
fun ProvidePadDeviceClass(
    content: @Composable () -> Unit
) {
    val deviceClass = rememberPadDeviceClass()
    CompositionLocalProvider(LocalPadDeviceClass provides deviceClass) {
        content()
    }
}
