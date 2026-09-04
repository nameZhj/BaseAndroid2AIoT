// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.base.iot.feature.demo.components.*

@Composable
fun SinglePaneContent(
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
fun TwoPaneContent(
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
