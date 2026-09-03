package com.base.iot.feature.demo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.iot.core.ui.AdaptiveContentLayout
import com.base.iot.core.ui.SystemBarEffect
import com.base.iot.core.ui.dialog.*
import com.base.iot.feature.demo.components.*
import com.base.iot.ui.theme.AppTheme

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
            title = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_confirm_reset_title),
            message = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_confirm_reset_msg),
            confirmText = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_confirm_reset_btn),
            isDanger = true,
            onConfirm = viewModel::onConfirmDialogConfirmed,
            onDismiss = { viewModel.setConfirmDialog(false) }
        )

        AppLoadingDialog(
            visible = uiState.showLoadingDialog,
            message = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_loading_gateway_msg)
        )
        if (uiState.showLoadingDialog) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.setLoadingDialog(false)
            }
        }

        AppInputDialog(
            visible = uiState.showInputDialog,
            title = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_input_node_title),
            hint = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_input_node_hint),
            initialText = "IOT_EDGE_DEV_ALPHA",
            confirmText = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_input_node_confirm),
            onConfirm = viewModel::onInputDialogConfirmed,
            onDismiss = { viewModel.setInputDialog(false) }
        )

        val currentCacheTitle = androidx.compose.ui.res.stringResource(uiState.currentCacheType.titleRes)
        val currentThemeTitle = androidx.compose.ui.res.stringResource(uiState.themeMode.titleRes)
        AppBottomSheetDialog(
            visible = uiState.showBottomSheet,
            title = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_bottom_sheet_title),
            onDismiss = { viewModel.setBottomSheet(false) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_bottom_sheet_active_protocols), color = AppTheme.colors.textPrimary, fontSize = 14.sp)
                Text(androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_bottom_sheet_cache_policy, currentCacheTitle), color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text(androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_bottom_sheet_theme_mode, currentThemeTitle), color = AppTheme.colors.accentCyan, fontSize = 13.sp)
                Text(androidx.compose.ui.res.stringResource(com.base.iot.R.string.dialog_bottom_sheet_tech_stack), color = AppTheme.colors.textSecondary, fontSize = 12.sp)
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

@Composable
private fun SinglePaneContent(
    uiState: DashboardUiState,
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DashboardHeader(viewModel, uiState) }
        item { LoadingAndErrorDemoCard(uiState, viewModel) }
        item { UnifiedDialogDemoCard(uiState, viewModel) }
        item { ProtocolSwitchesCard(uiState, viewModel) }
        item { HttpTestCard(viewModel) }
        item { CacheManagementCard(uiState, viewModel) }
        item { BrvahRecyclerCard(uiState) }
        item { MqttTestCard(uiState, viewModel) }
        item { RedisTestCard(uiState, viewModel) }
        item { SocketTestCard(uiState, viewModel) }
        item { DiagnosticsCard(viewModel) }
        item { UiSettingsCard(uiState, viewModel) }
        item {
            TerminalLogCard(
                logs = uiState.terminalLogs,
                onClear = viewModel::clearTerminal,
                fillHeight = false
            )
        }
    }
}

@Composable
private fun TwoPaneContent(
    uiState: DashboardUiState,
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { DashboardHeader(viewModel, uiState) }
            item { LoadingAndErrorDemoCard(uiState, viewModel) }
            item { UnifiedDialogDemoCard(uiState, viewModel) }
            item { ProtocolSwitchesCard(uiState, viewModel) }
            item { HttpTestCard(viewModel) }
            item { CacheManagementCard(uiState, viewModel) }
            item { BrvahRecyclerCard(uiState) }
            item { MqttTestCard(uiState, viewModel) }
            item { RedisTestCard(uiState, viewModel) }
            item { SocketTestCard(uiState, viewModel) }
            item { DiagnosticsCard(viewModel) }
            item { UiSettingsCard(uiState, viewModel) }
        }

        TerminalLogCard(
            logs = uiState.terminalLogs,
            onClear = viewModel::clearTerminal,
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight(),
            fillHeight = true
        )
    }
}
