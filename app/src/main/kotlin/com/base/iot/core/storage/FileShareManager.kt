package com.base.iot.core.storage

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.base.iot.core.diagnostics.Lg
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通用文件分享管理器。
 * 基于 FileProvider 实现 Android 7.0+ 安全且规范的系统级分享。
 */
@Singleton
class FileShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "FileShareManager"
    private val authority = "${context.packageName}.fileprovider"

    /**
     * 分享单个文件到系统分享面板（微信、QQ、邮件、蓝牙等）。
     */
    fun shareFile(file: File, shareTitle: String = "分享文件") {
        if (!file.exists()) {
            Lg.e(TAG, "分享失败：目标文件不存在 -> ${file.absolutePath}")
            return
        }

        try {
            val uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                putExtra(Intent.EXTRA_TEXT, "来自 BaseAndroid2AIoT 物联网终端的文件: ${file.name} (${file.length()} 字节)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, shareTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Lg.i(TAG, "已唤起系统分享面板: ${file.name}")
        } catch (e: Exception) {
            Lg.e(TAG, "唤起分享异常: ${e.message}", e)
        }
    }

    /**
     * 批量分享多个文件。
     */
    fun shareFiles(files: List<File>, shareTitle: String = "批量分享文件") {
        val validFiles = files.filter { it.exists() }
        if (validFiles.isEmpty()) {
            Lg.e(TAG, "批量分享失败：无有效文件")
            return
        }

        try {
            val uris = ArrayList(validFiles.map { FileProvider.getUriForFile(context, authority, it) })
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, shareTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Lg.i(TAG, "已唤起批量分享面板，共 ${validFiles.size} 个文件")
        } catch (e: Exception) {
            Lg.e(TAG, "批量分享异常: ${e.message}", e)
        }
    }

    /**
     * 分享文本内容（如错误诊断报告、系统日志文本）到系统分享面板。
     */
    fun shareText(text: String, shareTitle: String = "分享错误诊断报告") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, shareTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Lg.i(TAG, "已唤起文本分享面板: $shareTitle")
        } catch (e: Exception) {
            Lg.e(TAG, "唤起文本分享异常: ${e.message}", e)
        }
    }

    private fun getMimeType(file: File): String {
        val extension = file.extension
        if (extension.isEmpty()) return "*/*"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"
    }
}
