package com.base.iot.core.ui.dialog

import android.content.Context
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxj.xpopup.interfaces.OnInputConfirmListener

/**
 * 针对 Android 原生 View / Activity 场景集成 XPopup 的统一门面。
 * 保证在非 Compose 界面中弹出的 Dialog 与整体 UI 风格、Dark/Light 主题保持严格一致。
 */
object XPopupBridge {

    /**
     * 弹出风格统一的原生确认弹窗
     */
    fun showConfirm(
        context: Context,
        title: String,
        content: String,
        isDark: Boolean = true,
        confirmText: String = "确定",
        cancelText: String = "取消",
        onConfirm: () -> Unit
    ): BasePopupView {
        return XPopup.Builder(context)
            .isDarkTheme(isDark)
            .asConfirm(title, content, cancelText, confirmText, OnConfirmListener {
                onConfirm()
            }, null, false)
            .show()
    }

    /**
     * 弹出风格统一的原生加载等待弹窗
     */
    fun showLoading(
        context: Context,
        title: String = "正在处理中...",
        isDark: Boolean = true
    ): BasePopupView {
        return XPopup.Builder(context)
            .isDarkTheme(isDark)
            .asLoading(title)
            .show()
    }

    /**
     * 弹出风格统一的原生文本输入弹窗
     */
    fun showInput(
        context: Context,
        title: String,
        hint: String = "请输入内容",
        isDark: Boolean = true,
        onConfirm: (String) -> Unit
    ): BasePopupView {
        return XPopup.Builder(context)
            .isDarkTheme(isDark)
            .asInputConfirm(title, null, null, hint, OnInputConfirmListener { text ->
                onConfirm(text)
            })
            .show()
    }
}
