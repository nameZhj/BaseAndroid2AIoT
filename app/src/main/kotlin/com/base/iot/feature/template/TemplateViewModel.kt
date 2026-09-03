package com.base.iot.feature.template

import android.app.Application
import com.base.iot.core.base.BaseViewModel
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.iot.IotHub
import com.base.iot.core.storage.FileShareManager
import com.base.iot.core.ui.dialog.LoadingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    application: Application,
    val iotHub: IotHub,
    fileShareManager: FileShareManager
) : BaseViewModel<TemplateUiState, TemplateEvent>(
    application = application,
    fileShareManager = fileShareManager,
    initialState = TemplateUiState()
) {
    override fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig) {
        _uiState.update { it.copy(loadingConfig = reducer(it.loadingConfig)) }
    }

    override fun updateErrorState(error: ParsedError?, visible: Boolean) {
        _uiState.update { it.copy(parsedError = error, showErrorDialog = visible) }
    }

    fun executeSampleTask() = launchWithLoading(
        title = "正在执行任务...",
        isBlocking = true
    ) { updateProgress ->
        updateProgress(0.5f, "处理中 50%")
    }
}
