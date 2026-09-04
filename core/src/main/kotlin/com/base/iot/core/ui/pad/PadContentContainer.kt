package com.base.iot.core.ui.pad

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.padContentBounds(
    maxPadWidth: Dp = 960.dp
): Modifier {
    val deviceClass = rememberPadDeviceClass()
    return if (deviceClass.isPad) {
        this.widthIn(max = maxPadWidth)
    } else {
        this.fillMaxWidth()
    }
}

@Composable
fun PadContentContainer(
    modifier: Modifier = Modifier,
    maxPadWidth: Dp = 960.dp,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable () -> Unit
) {
    val deviceClass = rememberPadDeviceClass()
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = if (deviceClass.isPad) {
                Modifier.widthIn(max = maxPadWidth)
            } else {
                Modifier.fillMaxWidth()
            }
        ) {
            content()
        }
    }
}
