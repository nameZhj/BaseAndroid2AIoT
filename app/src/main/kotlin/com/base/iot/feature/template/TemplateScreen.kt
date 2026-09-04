package com.base.iot.feature.template

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.iot.R
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.dialog.AppErrorDialog
import com.base.iot.core.ui.dialog.AppProgressDialog
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppCard(title = stringResource(uiState.titleRes)) {
                AppButton(
                    text = stringResource(R.string.template_execute_btn),
                    icon = Icons.Filled.PlayArrow,
                    color = AppTheme.colors.accentPrimary,
                    onClick = viewModel::executeSampleTask
                )
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
