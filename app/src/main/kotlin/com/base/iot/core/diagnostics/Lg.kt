package com.base.iot.core.diagnostics

import android.util.Log
import com.base.iot.BuildConfig

/**
 * 超长日志打印工具（解决 Android Logcat 单次最长 4000 字符限制）。
 *
 * 特性：
 * - 自动识别超过 [MAX_LOG_LENGTH] 字符的日志，分片连续打印
 * - 通过 [isEnabled] 全局开关控制（DEBUG 模式自动开启，RELEASE 关闭）
 * - 提供 v/d/i/w/e 五个级别的打印方法
 * - 支持传入 Throwable 输出完整堆栈
 *
 * 使用示例：
 * ```kotlin
 * Lg.d("MyTag", "普通日志")
 * Lg.e("MyTag", "异常日志", exception)
 * Lg.d("MyTag", veryLongString) // 超长自动分段
 * ```
 */
object Lg {

    /** 全局日志开关，默认跟随 BuildConfig.ENABLE_LOG */
    var isEnabled: Boolean = BuildConfig.ENABLE_LOG

    /** 单片最大字符数（Android 系统限制约 4076，保守取 4000） */
    private const val MAX_LOG_LENGTH = 4000

    /** 分段打印的 Chunk 前缀格式 */
    private const val CHUNK_PREFIX = "──────────────── [%d/%d] ────────────────"

    // ==================== 公开 API ====================

    fun v(tag: String, msg: String, tr: Throwable? = null) =
        log(Log.VERBOSE, tag, msg, tr)

    fun d(tag: String, msg: String, tr: Throwable? = null) =
        log(Log.DEBUG, tag, msg, tr)

    fun i(tag: String, msg: String, tr: Throwable? = null) =
        log(Log.INFO, tag, msg, tr)

    fun w(tag: String, msg: String, tr: Throwable? = null) =
        log(Log.WARN, tag, msg, tr)

    fun e(tag: String, msg: String, tr: Throwable? = null) =
        log(Log.ERROR, tag, msg, tr)

    // ==================== 核心逻辑 ====================

    private fun log(level: Int, tag: String, msg: String, tr: Throwable?) {
        if (!isEnabled) return

        // 拼接 Throwable 堆栈
        val fullMsg = if (tr != null) {
            "$msg\n${Log.getStackTraceString(tr)}"
        } else {
            msg
        }

        if (fullMsg.length <= MAX_LOG_LENGTH) {
            // 短日志，直接打印
            printLine(level, tag, fullMsg)
        } else {
            // 超长日志，分段打印
            val chunks = fullMsg.chunked(MAX_LOG_LENGTH)
            val total = chunks.size
            chunks.forEachIndexed { index, chunk ->
                val header = CHUNK_PREFIX.format(index + 1, total)
                printLine(level, tag, header)
                printLine(level, tag, chunk)
            }
        }
    }

    private fun printLine(level: Int, tag: String, msg: String) {
        when (level) {
            Log.VERBOSE -> Log.v(tag, msg)
            Log.DEBUG -> Log.d(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
        }
    }
}
