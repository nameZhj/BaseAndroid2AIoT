package com.base.iot.feature.template

import androidx.annotation.StringRes
import com.base.iot.R
import com.base.iot.core.base.IUiEvent
import com.base.iot.core.base.IUiState
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.ui.dialog.LoadingConfig

data class TemplateUiState(
    @StringRes val titleRes: Int = R.string.template_feature_title,
    val isProcessing: Boolean = false,
    override val loadingConfig: LoadingConfig = LoadingConfig(),
    override val parsedError: ParsedError? = null,
    override val showErrorDialog: Boolean = false
) : IUiState

sealed class TemplateEvent : IUiEvent {
    data class ShowToast(val message: String) : TemplateEvent()
}
