package com.base.iot.core.diagnostics

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * 日志文件分享器。
 *
 * 功能：
 * - 枚举应用 Cache 目录下的崩溃日志文件
 * - 通过 [FileProvider] 构建安全的 content URI
 * - 弹出系统原生分享面板（Intent.ACTION_SEND_MULTIPLE）
 *
 * 使用示例：
 * ```kotlin
 * LogExporter.shareLogFiles(context)
 * ```
 */
object LogExporter {

    private const val TAG = "LogExporter"

    /**
     * 分享所有崩溃日志文件。
     * 若没有日志文件，不会弹出分享面板。
     *
     * @param context 调用方 Context（通常传 Activity 或 Application）
     * @param authority FileProvider 的 authority，默认读取 ${applicationId}.fileprovider
     */
    fun shareLogFiles(
        context: Context,
        authority: String = "${context.packageName}.fileprovider"
    ) {
        val app = context.applicationContext as? Application ?: return
        val logFiles = CrashHandler.getCrashLogFiles(app)

        if (logFiles.isEmpty()) {
            Lg.i(TAG, "没有崩溃日志文件可分享")
            return
        }

        Lg.i(TAG, "准备分享 ${logFiles.size} 个日志文件")

        val uris = logFiles.mapNotNull { file ->
            try {
                FileProvider.getUriForFile(context, authority, file)
            } catch (e: Exception) {
                Lg.e(TAG, "获取 URI 失败: ${file.name}", e)
                null
            }
        }

        if (uris.isEmpty()) return

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/plain"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_SUBJECT, "IoT App 崩溃日志")
            putExtra(
                Intent.EXTRA_TEXT,
                "附件包含 ${logFiles.size} 个崩溃日志文件，请协助排查问题。"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "分享日志文件")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * 清空所有崩溃日志文件。
     */
    fun clearLogs(context: Context) {
        val app = context.applicationContext as? Application ?: return
        val logFiles = CrashHandler.getCrashLogFiles(app)
        var count = 0
        logFiles.forEach { file ->
            if (file.delete()) count++
        }
        Lg.i(TAG, "已清除 $count 个日志文件")
    }

    /**
     * 获取日志目录大小（字节）
     */
    fun getLogsDirSize(context: Context): Long {
        val app = context.applicationContext as? Application ?: return 0L
        val logFiles = CrashHandler.getCrashLogFiles(app)
        return logFiles.sumOf { it.length() }
    }
}
