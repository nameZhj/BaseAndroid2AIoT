package com.base.iot.core.ui.pad

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.padDialogBounds(
    phoneFraction: Float = 0.9f,
    minPadWidth: Dp = 380.dp,
    maxPadWidth: Dp = 540.dp
): Modifier {
    val deviceClass = rememberPadDeviceClass()
    return if (deviceClass.isPad) {
        this
            .widthIn(min = minPadWidth, max = maxPadWidth)
            .wrapContentHeight()
    } else {
        this
            .fillMaxWidth(phoneFraction)
            .wrapContentHeight()
    }
}

@Composable
fun Modifier.padLargeDialogBounds(
    phoneFraction: Float = 0.92f,
    minPadWidth: Dp = 460.dp,
    maxPadWidth: Dp = 720.dp
): Modifier {
    val deviceClass = rememberPadDeviceClass()
    return if (deviceClass.isPad) {
        this
            .widthIn(min = minPadWidth, max = maxPadWidth)
            .wrapContentHeight()
    } else {
        this
            .fillMaxWidth(phoneFraction)
            .wrapContentHeight()
    }
}
