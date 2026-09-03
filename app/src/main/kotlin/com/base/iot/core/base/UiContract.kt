package com.base.iot.core.base

import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.ui.dialog.LoadingConfig

/**
 * 界面状态基类接口契约。
 * 强制子类包含标准加载窗与错误弹窗状态，保证 Agent 新建页面时自动满足架构规范。
 */
interface IUiState {
    val loadingConfig: LoadingConfig
    val parsedError: ParsedError?
    val showErrorDialog: Boolean
}

/**
 * 单次单向 UI 事件接口标记契约（如 Navigation 路由跳转、Snackbar 提示等）
 */
interface IUiEvent
