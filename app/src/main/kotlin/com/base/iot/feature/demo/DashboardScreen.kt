// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.iot.core.ui.AdaptiveContentLayout
import com.base.iot.core.ui.SystemBarEffect
import com.base.iot.core.ui.dialog.*
import com.base.iot.core.ui.theme.AppTheme
import com.base.iot.feature.demo.components.*
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isLight = !AppTheme.colors.isDark
    SystemBarEffect(
        darkStatusBarIcons = isLight,
        darkNavBarIcons = isLight,
        hideStatusBar = uiState.isImmersive
    )

    Scaffold(
        containerColor = AppTheme.colors.background
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

        AdaptiveContentLayout { layoutState ->
            if (layoutState.showTwoPanes) {
                TwoPaneContent(uiState, viewModel, contentModifier)
            } else {
                SinglePaneContent(uiState, viewModel, contentModifier)
            }
        }

        AppConfirmDialog(
            visible = uiState.showConfirmDialog,
            title = stringResource(R.string.dialog_confirm_reset_title),
            message = stringResource(R.string.dialog_confirm_reset_msg),
            confirmText = stringResource(R.string.dialog_confirm_reset_btn),
            isDanger = true,
            onConfirm = viewModel::onConfirmDialogConfirmed,
            onDismiss = { viewModel.setConfirmDialog(false) }
        )

        AppLoadingDialog(
            visible = uiState.showLoadingDialog,
            message = stringResource(R.string.dialog_loading_gateway_msg)
        )
        if (uiState.showLoadingDialog) {
            LaunchedEffect(Unit) {
                delay(2000)
                viewModel.setLoadingDialog(false)
            }
        }

        AppInputDialog(
            visible = uiState.showInputDialog,
            title = stringResource(R.string.dialog_input_node_title),
            hint = stringResource(R.string.dialog_input_node_hint),
            initialText = DemoConfig.DEMO_DEFAULT_NODE_ID,
            confirmText = stringResource(R.string.dialog_input_node_confirm),
            onConfirm = viewModel::onInputDialogConfirmed,
            onDismiss = { viewModel.setInputDialog(false) }
        )

        val currentCacheTitle = stringResource(uiState.currentCacheType.titleRes)
        val currentThemeTitle = stringResource(uiState.themeMode.titleRes)
        AppBottomSheetDialog(
            visible = uiState.showBottomSheet,
            title = stringResource(R.string.dialog_bottom_sheet_title),
            onDismiss = { viewModel.setBottomSheet(false) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.dialog_bottom_sheet_active_protocols), color = AppTheme.colors.textPrimary, fontSize = 14.sp)
                Text(stringResource(R.string.dialog_bottom_sheet_cache_policy, currentCacheTitle), color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text(stringResource(R.string.dialog_bottom_sheet_theme_mode, currentThemeTitle), color = AppTheme.colors.accentCyan, fontSize = 13.sp)
                Text(stringResource(R.string.dialog_bottom_sheet_tech_stack), color = AppTheme.colors.textSecondary, fontSize = 12.sp)
            }
        }

        AppProgressDialog(
            config = uiState.loadingConfig,
            onDismissRequest = viewModel::dismissLoading
        )

        AppErrorDialog(
            visible = uiState.showErrorDialog,
            error = uiState.parsedError,
            onDismiss = viewModel::dismissError,
            onShareReport = viewModel::shareErrorReport
        )
    }
}
