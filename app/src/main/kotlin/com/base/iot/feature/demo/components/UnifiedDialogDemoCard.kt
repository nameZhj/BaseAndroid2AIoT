package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.feature.demo.DashboardUiState
import com.base.iot.feature.demo.DashboardViewModel
import com.base.iot.ui.theme.AppTheme

@Composable
fun UnifiedDialogDemoCard(
    uiState: DashboardUiState,
    vm: DashboardViewModel
) {
    val colors = AppTheme.colors

    AppCard(
        title = "统一风格弹窗体系 (XPopup / AppDialog)",
        icon = Icons.Filled.SmartButton,
        iconTint = colors.accentPurple
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "针对物联网业务场景统一封装，在普通模式与夜间模式下均保证高对比度视觉质感：",
                color = colors.textSecondary,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = "确认/高危弹窗",
                    icon = Icons.Filled.CheckCircle,
                    color = colors.accentRed,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setConfirmDialog(true) }
                )
                AppButton(
                    text = "加载等待弹窗",
                    icon = Icons.Filled.HourglassTop,
                    color = colors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setLoadingDialog(true) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = "参数输入弹窗",
                    icon = Icons.Filled.EditNote,
                    color = colors.accentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setInputDialog(true) }
                )
                AppButton(
                    text = "底部抽屉面板",
                    icon = Icons.Filled.VerticalAlignTop,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setBottomSheet(true) }
                )
            }
        }
    }
}
