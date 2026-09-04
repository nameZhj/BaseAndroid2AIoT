// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.feature.demo.*
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun LoadingAndErrorDemoCard(
    uiState: DashboardUiState,
    vm: DashboardViewModel
) {
    val colors = AppTheme.colors

    AppCard(
        title = stringResource(R.string.loading_demo_title),
        icon = Icons.Filled.HourglassBottom,
        iconTint = colors.accentCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.loading_demo_desc),
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
                    Text(stringResource(R.string.loading_btn_blocking), color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.enableLoadingDialog) stringResource(R.string.progress_blocking_hint) else stringResource(R.string.progress_non_blocking_hint),
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
                    Text(stringResource(R.string.loading_btn_non_blocking), color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.isBlockingDefault) stringResource(R.string.progress_blocking_hint) else stringResource(R.string.progress_non_blocking_hint),
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(
                    text = stringResource(R.string.loading_btn_blocking),
                    icon = Icons.Filled.Lock,
                    color = colors.accentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testBlockingProgress
                )
                AppButton(
                    text = stringResource(R.string.loading_btn_non_blocking),
                    icon = Icons.Filled.LockOpen,
                    color = colors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testNonBlockingProgress
                )
                AppButton(
                    text = stringResource(R.string.loading_btn_percentage),
                    icon = Icons.Filled.LinearScale,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testPercentageProgress
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(
                    text = stringResource(R.string.loading_btn_mock_timeout),
                    icon = Icons.Filled.WifiOff,
                    color = colors.accentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testSimulateTimeoutError
                )
                AppButton(
                    text = stringResource(R.string.loading_btn_mock_unknown),
                    icon = Icons.Filled.ReportProblem,
                    color = colors.accentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testSimulateProtocolDisabledError
                )
            }
        }
    }
}
