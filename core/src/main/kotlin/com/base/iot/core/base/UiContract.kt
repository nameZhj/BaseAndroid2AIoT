package com.base.iot.core.base

import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.ui.dialog.LoadingConfig

interface IUiState {
    val loadingConfig: LoadingConfig
    val parsedError: ParsedError?
    val showErrorDialog: Boolean
}

interface IUiEvent
