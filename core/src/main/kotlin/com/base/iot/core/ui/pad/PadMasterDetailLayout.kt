package com.base.iot.core.ui.pad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun PadMasterDetailLayout(
    modifier: Modifier = Modifier,
    masterWeight: Float = 0.38f,
    detailWeight: Float = 0.62f,
    dividerWidth: Dp = 1.dp,
    showDetailOnPhone: Boolean = false,
    masterContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit
) {
    val deviceClass = rememberPadDeviceClass()
    val isPad = deviceClass.isPad

    if (isPad) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(masterWeight)
                    .fillMaxHeight()
            ) {
                masterContent()
            }

            Box(
                modifier = Modifier
                    .width(dividerWidth)
                    .fillMaxHeight()
                    .background(AppTheme.colors.cardBorder)
            )

            Box(
                modifier = Modifier
                    .weight(detailWeight)
                    .fillMaxHeight()
            ) {
                detailContent()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            if (showDetailOnPhone) {
                detailContent()
            } else {
                masterContent()
            }
        }
    }
}
