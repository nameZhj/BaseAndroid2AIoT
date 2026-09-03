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
            title = "核心协议复位确认",
            message = "您正在请求重置物联网核心连接池与网关通道，操作将重新协商握手密钥，请确认是否执行？",
            confirmText = "确认复位",
            isDanger = true,
            onConfirm = viewModel::onConfirmDialogConfirmed,
            onDismiss = { viewModel.setConfirmDialog(false) }
        )

        AppLoadingDialog(
            visible = uiState.showLoadingDialog,
            message = "正在与物联网网关建立安全长连接..."
        )
        if (uiState.showLoadingDialog) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.setLoadingDialog(false)
            }
        }

        AppInputDialog(
            visible = uiState.showInputDialog,
            title = "修改边缘节点标识",
            hint = "如: ANDROID_AIOT_NODE_01",
            initialText = "IOT_EDGE_DEV_ALPHA",
            confirmText = "保存更改",
            onConfirm = viewModel::onInputDialogConfirmed,
            onDismiss = { viewModel.setInputDialog(false) }
        )

        AppBottomSheetDialog(
            visible = uiState.showBottomSheet,
            title = "物联网实时监控抽屉面板",
            onDismiss = { viewModel.setBottomSheet(false) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("● 当前生效协议: HTTP(REST), MQTT, TCP Socket", color = AppTheme.colors.textPrimary, fontSize = 14.sp)
                Text("● 缓存策略: ${uiState.currentCacheType.title}", color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text("● 当前显示模式: ${uiState.themeMode.title}", color = AppTheme.colors.accentCyan, fontSize = 13.sp)
                Text("● 弹窗技术方案: XPopup 4 + Compose AppDialog 统一设计标准", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
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
