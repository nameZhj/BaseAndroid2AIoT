package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
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

@Composable
fun DiagnosticsCard(vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = androidx.compose.ui.res.stringResource(com.base.iot.R.string.diag_card_title), icon = Icons.Filled.BugReport, iconTint = colors.accentRed) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.diag_card_desc),
                color = colors.textSecondary,
                fontSize = 11.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.diag_btn_long_log),
                    icon = Icons.AutoMirrored.Filled.Subject,
                    color = colors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = vm::triggerLongLog
                )
                AppButton(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.diag_btn_check_crash),
                    icon = Icons.Filled.FileDownload,
                    color = colors.accentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::exportCrashLogs
                )
                AppButton(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.diag_btn_share_log),
                    icon = Icons.Filled.Share,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::shareCrashLog
                )
            }
        }
    }
}

@Composable
fun UiSettingsCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = androidx.compose.ui.res.stringResource(com.base.iot.R.string.settings_card_title), icon = Icons.Filled.Settings, iconTint = colors.accentCyan) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.settings_immersive_title),
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        if (uiState.isImmersive) com.base.iot.R.string.settings_immersive_on else com.base.iot.R.string.settings_immersive_off
                    ),
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

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.settings_licenses_title),
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.settings_licenses_desc),
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
            AppButton(
                text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.settings_licenses_btn),
                icon = Icons.Filled.Info,
                color = colors.accentGreen,
                onClick = vm::showLicensesDialog
            )
        }
    }
}
