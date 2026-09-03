package com.base.iot.feature.demo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.feature.demo.DashboardUiState
import com.base.iot.feature.demo.DashboardViewModel
import com.base.iot.ui.theme.AppTheme

@Composable
fun LoadingAndErrorDemoCard(
    uiState: DashboardUiState,
    vm: DashboardViewModel
) {
    val colors = AppTheme.colors

    AppCard(
        title = "耗时操作进度与错误诊断体系",
        icon = Icons.Filled.HourglassBottom,
        iconTint = colors.accentCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "支持开发者自主配置启用/禁用、阻塞/非阻塞，错误弹窗非阻塞呈现真实堆栈并支持一键系统分享：",
                color = colors.textSecondary,
                fontSize = 11.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("启用耗时进度弹窗", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.enableLoadingDialog) "耗时操作展示进度弹窗" else "静默后台执行 (无遮罩)",
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = uiState.enableLoadingDialog,
                    onCheckedChange = { vm.toggleEnableLoadingDialog() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.accentCyan
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("弹窗交互模式", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.isBlockingDefault) "阻塞式 (防并发点击穿透/不可取消)" else "非阻塞式 (可点击外部或手动取消)",
                        color = if (uiState.isBlockingDefault) colors.accentAmber else colors.accentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = uiState.isBlockingDefault,
                    onCheckedChange = { vm.toggleBlockingDefault() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.accentAmber
                    )
                )
            }

            Text("进度窗形态实测:", color = colors.textSecondary, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(
                    text = "阻塞式",
                    icon = Icons.Filled.Lock,
                    color = colors.accentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testBlockingProgress
                )
                AppButton(
                    text = "非阻塞",
                    icon = Icons.Filled.LockOpen,
                    color = colors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testNonBlockingProgress
                )
                AppButton(
                    text = "0%~100%",
                    icon = Icons.Filled.LinearScale,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testPercentageProgress
                )
            }

            Text("非阻塞真实错误诊断与一键分享实测:", color = colors.textSecondary, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(
                    text = "模拟网络超时",
                    icon = Icons.Filled.WifiOff,
                    color = colors.accentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testSimulateTimeoutError
                )
                AppButton(
                    text = "模拟协议禁用",
                    icon = Icons.Filled.ReportProblem,
                    color = colors.accentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testSimulateProtocolDisabledError
                )
            }
        }
    }
}
