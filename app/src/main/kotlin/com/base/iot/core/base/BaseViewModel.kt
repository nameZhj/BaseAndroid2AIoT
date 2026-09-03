package com.base.iot.core.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.base.iot.core.diagnostics.ErrorParser
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.storage.FileShareManager
import com.base.iot.core.ui.dialog.LoadingConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<STATE : IUiState, EVENT : IUiEvent>(
    application: Application,
    protected val fileShareManager: FileShareManager,
    initialState: STATE
) : AndroidViewModel(application) {

    protected val logTag: String = this.javaClass.simpleName

    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    protected val _events = MutableSharedFlow<EVENT>(extraBufferCapacity = 8)
    val events: SharedFlow<EVENT> = _events.asSharedFlow()

    protected fun emitEvent(event: EVENT) {
        viewModelScope.launch { _events.emit(event) }
    }

    protected fun updateState(reducer: (STATE) -> STATE) {
        _uiState.update(reducer)
    }

    /**
     * @param isBlocking true=不可取消防穿透, false=允许外部点击取消
     * @param showLoading false=后台静默执行
     */
    fun launchWithLoading(
        title: String = "正在处理中...",
        isBlocking: Boolean = true,
        showLoading: Boolean = true,
        onError: ((Throwable) -> Unit)? = null,
        action: suspend CoroutineScope.(updateProgress: (Float?, String?) -> Unit) -> Unit
    ): Job {
        return viewModelScope.launch {
            var currentJob: Job? = null
            if (showLoading) {
                updateLoadingConfig {
                    LoadingConfig(
                        visible = true,
                        title = title,
                        isBlocking = isBlocking,
                        progress = null,
                        progressText = null,
                        cancelable = true,
                        onCancel = {
                            currentJob?.cancel()
                            dismissLoading()
                            onOperationCancelled(title)
                        }
                    )
                }
            }

            try {
                currentJob = coroutineContext[Job]
                action { progress, text ->
                    if (showLoading) {
                        updateLoadingConfig {
                            it.copy(progress = progress, progressText = text)
                        }
                    }
                }
            } catch (e: CancellationException) {
                onOperationCancelled(title)
            } catch (t: Throwable) {
                Lg.e(logTag, "任务执行异常: ${t.message}", t)
                if (onError != null) {
                    onError(t)
                } else {
                    showError(t)
                }
            } finally {
                if (showLoading) {
                    dismissLoading()
                }
            }
        }
    }

    protected abstract fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig)
    protected abstract fun updateErrorState(error: ParsedError?, visible: Boolean)

    fun dismissLoading() {
        updateLoadingConfig { it.copy(visible = false) }
    }

    fun showError(throwable: Throwable) {
        val parsed = ErrorParser.parse(getApplication(), throwable)
        updateErrorState(error = parsed, visible = true)
    }

    fun dismissError() {
        updateErrorState(error = null, visible = false)
    }

    fun shareErrorReport(error: ParsedError) {
        fileShareManager.shareText(
            text = error.fullDiagnosticReport,
            shareTitle = "分享错误报告 - ${error.errorType}"
        )
    }

    protected open fun onOperationCancelled(title: String) {
        Lg.d(logTag, "任务已取消: $title")
    }
}
