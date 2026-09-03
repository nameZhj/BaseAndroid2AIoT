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
    fun shareFile(file: File, shareTitle: String? = null) {
        if (!file.exists()) {
            Lg.e(TAG, "Share failed: target file does not exist -> ${file.absolutePath}")
            return
        }

        try {
            val uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = getMimeType(file)
            val effectiveTitle = shareTitle ?: context.getString(com.base.iot.R.string.share_file_title)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                putExtra(Intent.EXTRA_TEXT, context.getString(com.base.iot.R.string.share_file_body, file.name, file.length()))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, effectiveTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Lg.i(TAG, "System share panel opened: ${file.name}")
        } catch (e: Exception) {
            Lg.e(TAG, "Share error: ${e.message}", e)
        }
    }

    /**
     * 批量分享多个文件。
     */
    fun shareFiles(files: List<File>, shareTitle: String? = null) {
        val validFiles = files.filter { it.exists() }
        if (validFiles.isEmpty()) {
            Lg.e(TAG, "Batch share failed: no valid files")
            return
        }

        try {
            val uris = ArrayList(validFiles.map { FileProvider.getUriForFile(context, authority, it) })
            val effectiveTitle = shareTitle ?: context.getString(com.base.iot.R.string.share_files_title)
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, effectiveTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Lg.i(TAG, "Batch share panel opened with ${validFiles.size} files")
        } catch (e: Exception) {
            Lg.e(TAG, "Batch share error: ${e.message}", e)
        }
    }

    /**
     * 分享文本内容（如错误诊断报告、系统日志文本）到系统分享面板。
     */
    fun shareText(text: String, shareTitle: String? = null) {
        try {
            val effectiveTitle = shareTitle ?: context.getString(com.base.iot.R.string.share_report_title)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, effectiveTitle)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, effectiveTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Lg.i(TAG, "Text share panel opened: $effectiveTitle")
        } catch (e: Exception) {
            Lg.e(TAG, "Text share error: ${e.message}", e)
        }
    }

    private fun getMimeType(file: File): String {
        val extension = file.extension
        if (extension.isEmpty()) return "*/*"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"
    }
}
