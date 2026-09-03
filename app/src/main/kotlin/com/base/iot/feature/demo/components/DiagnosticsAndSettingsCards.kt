package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.feature.demo.DashboardUiState
import com.base.iot.feature.demo.DashboardViewModel
import com.base.iot.ui.theme.AppTheme

// ==================== 诊断卡片 ====================

@Composable
fun DiagnosticsCard(vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "日志与诊断", icon = Icons.Filled.BugReport, iconTint = colors.accentRed) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "超长日志分段打印 (防止Logcat截断) & 异常捕捉与分享：",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = "超长日志打印",
                    icon = Icons.Filled.Subject,
                    color = colors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = vm::triggerLongLog
                )
                AppButton(
                    text = "导出崩溃日志",
                    icon = Icons.Filled.FileDownload,
                    color = colors.accentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::exportCrashLogs
                )
                AppButton(
                    text = "分享日志",
                    icon = Icons.Filled.Share,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::shareCrashLog
                )
            }
        }
    }
}

// ==================== UI 设置卡片 ====================

@Composable
fun UiSettingsCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "系统栏与界面设置", icon = Icons.Filled.Settings, iconTint = colors.accentCyan) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "全屏沉浸式模式",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (uiState.isImmersive) "已隐藏系统栏，边缘滑动呼出" else "已显示系统栏，透明背景",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = uiState.isImmersive,
                onCheckedChange = vm::toggleImmersive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.accentCyan
                )
            )
        }
    }
}
