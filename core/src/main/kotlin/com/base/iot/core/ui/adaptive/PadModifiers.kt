package com.base.iot.core.ui.adaptive

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.padConstrainedWidth(
    maxWidth: Dp = 640.dp,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
): Modifier {
    return this
        .fillMaxWidth()
        .wrapContentWidth(alignment)
        .widthIn(max = maxWidth)
}

fun Modifier.padDialogWidth(
    minWidth: Dp = 340.dp,
    maxWidth: Dp = 520.dp
): Modifier {
    return this.widthIn(min = minWidth, max = maxWidth)
}
