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

/**
 * 企业级 Agent 友好型 BaseViewModel 基类。
 *
 * 为所有子类提供统一基建，杜绝 Agent 开发新功能时重复编写样板代码：
 * 1. 响应式 StateFlow 状态管理与单次事件 SharedFlow 管道；
 * 2. 通用 launchWithLoading 异步执行器（自动进度上报、取消响应、异常拦截与弹窗驱动）；
 * 3. 真实错误诊断与一键系统分享；
 * 4. 规范化生命周期日志管理。
 */
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

    /**
     * 发送单次 UI 事件（如 Toast、导航）
     */
    protected fun emitEvent(event: EVENT) {
        viewModelScope.launch { _events.emit(event) }
    }

    /**
     * 更新当前 UI 状态
     */
    protected fun updateState(reducer: (STATE) -> STATE) {
        _uiState.update(reducer)
    }

    /**
     * 核心封装：发起带进度/等待弹窗的耗时异步操作。
     *
     * @param title 弹窗标题
     * @param isBlocking 是否为阻塞式（true=防并发防穿透；false=可随时轻触外部取消）
     * @param showLoading 是否展示进度弹窗（false=后台静默执行）
     * @param onError 自定义错误回调（若为 null，则自动解析并呼出非阻塞式 AppErrorDialog）
     * @param action 异步业务逻辑闭包，提供 updateProgress(percent, text) 动态更新百分比进度
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
                Lg.e(logTag, "耗时任务执行异常: ${t.message}", t)
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

    /**
     * 子类状态更新 LoadingConfig 的具体映射
     */
    protected abstract fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig)

    /**
     * 子类状态更新 ParsedError 的具体映射
     */
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
