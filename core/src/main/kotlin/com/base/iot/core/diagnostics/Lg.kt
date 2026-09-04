package com.base.iot.core.diagnostics

import android.util.Log
import com.base.iot.core.BuildConfig

object Lg {
    var isEnabled: Boolean = BuildConfig.ENABLE_LOG
    private const val MAX_LOG_LENGTH = 4000
    private const val CHUNK_PREFIX = "──────────────── [%d/%d] ────────────────"

    fun v(tag: String, msg: String, tr: Throwable? = null) = log(Log.VERBOSE, tag, msg, tr)
    fun d(tag: String, msg: String, tr: Throwable? = null) = log(Log.DEBUG, tag, msg, tr)
    fun i(tag: String, msg: String, tr: Throwable? = null) = log(Log.INFO, tag, msg, tr)
    fun w(tag: String, msg: String, tr: Throwable? = null) = log(Log.WARN, tag, msg, tr)
    fun e(tag: String, msg: String, tr: Throwable? = null) = log(Log.ERROR, tag, msg, tr)

    private fun log(level: Int, tag: String, msg: String, tr: Throwable?) {
        if (!isEnabled) return
        val fullMsg = if (tr != null) "$msg\n${Log.getStackTraceString(tr)}" else msg

        if (fullMsg.length <= MAX_LOG_LENGTH) {
            printLine(level, tag, fullMsg)
        } else {
            val chunks = fullMsg.chunked(MAX_LOG_LENGTH)
            val total = chunks.size
            chunks.forEachIndexed { index, chunk ->
                printLine(level, tag, CHUNK_PREFIX.format(index + 1, total))
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
