package com.base.iot.core.ui.dialog

import android.content.Context
import com.base.iot.R
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
        confirmText: String? = null,
        cancelText: String? = null,
        onConfirm: () -> Unit
    ): BasePopupView {
        val effectiveConfirm = confirmText ?: context.getString(R.string.btn_confirm)
        val effectiveCancel = cancelText ?: context.getString(R.string.btn_cancel)
        return XPopup.Builder(context)
            .isDarkTheme(isDark)
            .asConfirm(title, content, effectiveCancel, effectiveConfirm, OnConfirmListener {
                onConfirm()
            }, null, false)
            .show()
    }

    /**
     * 弹出风格统一的原生加载等待弹窗
     */
    fun showLoading(
        context: Context,
        title: String? = null,
        isDark: Boolean = true
    ): BasePopupView {
        val effectiveTitle = title ?: context.getString(R.string.processing)
        return XPopup.Builder(context)
            .isDarkTheme(isDark)
            .asLoading(effectiveTitle)
            .show()
    }

    /**
     * 弹出风格统一的原生文本输入弹窗
     */
    fun showInput(
        context: Context,
        title: String,
        hint: String? = null,
        isDark: Boolean = true,
        onConfirm: (String) -> Unit
    ): BasePopupView {
        val effectiveHint = hint ?: context.getString(R.string.input_hint_default)
        return XPopup.Builder(context)
            .isDarkTheme(isDark)
            .asInputConfirm(title, null, null, effectiveHint, OnInputConfirmListener { text ->
                onConfirm(text)
            })
            .show()
    }
}
