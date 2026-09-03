package com.base.iot.feature.template

import com.base.iot.core.base.IUiEvent
import com.base.iot.core.base.IUiState
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.ui.dialog.LoadingConfig

data class TemplateUiState(
    @androidx.annotation.StringRes val titleRes: Int = com.base.iot.R.string.template_feature_title,
    val isProcessing: Boolean = false,
    override val loadingConfig: LoadingConfig = LoadingConfig(),
    override val parsedError: ParsedError? = null,
    override val showErrorDialog: Boolean = false
) : IUiState

sealed class TemplateEvent : IUiEvent {
    data class ShowToast(val message: String) : TemplateEvent()
}
