package com.base.iot.core.ui.pad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun PadNavigationScaffold(
    modifier: Modifier = Modifier,
    railHeader: (@Composable ColumnScope.() -> Unit)? = null,
    railContent: @Composable ColumnScope.() -> Unit,
    bottomBarForPhone: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val deviceClass = rememberPadDeviceClass()
    val isPad = deviceClass.isPad
    val colors = AppTheme.colors

    if (isPad) {
        Row(modifier = modifier.fillMaxSize().background(colors.background)) {
            NavigationRail(
                containerColor = colors.surface,
                contentColor = colors.textPrimary,
                header = railHeader
            ) {
                railContent()
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.cardBorder)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                content()
            }
        }
    } else {
        Scaffold(
            containerColor = colors.background,
            bottomBar = { bottomBarForPhone?.invoke() }
        ) { p ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(p)
            ) {
                content()
            }
        }
    }
}
