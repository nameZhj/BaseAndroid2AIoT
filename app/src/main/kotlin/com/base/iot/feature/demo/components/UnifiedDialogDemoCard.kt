// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.feature.demo.*
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun UnifiedDialogDemoCard(
    uiState: DashboardUiState,
    vm: DashboardViewModel
) {
    val colors = AppTheme.colors

    AppCard(
        title = stringResource(R.string.dialog_demo_title),
        icon = Icons.Filled.SmartButton,
        iconTint = colors.accentPurple
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.dialog_demo_desc),
                color = colors.textSecondary,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = stringResource(R.string.dialog_btn_confirm),
                    icon = Icons.Filled.CheckCircle,
                    color = colors.accentRed,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setConfirmDialog(true) }
                )
                AppButton(
                    text = stringResource(R.string.dialog_btn_loading),
                    icon = Icons.Filled.HourglassTop,
                    color = colors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setLoadingDialog(true) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = stringResource(R.string.dialog_btn_custom),
                    icon = Icons.Filled.EditNote,
                    color = colors.accentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setInputDialog(true) }
                )
                AppButton(
                    text = stringResource(R.string.dialog_btn_bottom_sheet),
                    icon = Icons.Filled.VerticalAlignTop,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setBottomSheet(true) }
                )
            }
        }
    }
}
