package com.base.iot.core.diagnostics

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.base.iot.R
import java.io.File

object LogExporter {
    private const val TAG = "LogExporter"

    fun shareLogFiles(
        context: Context,
        authority: String = "${context.packageName}.fileprovider"
    ) {
        val app = context.applicationContext as? Application ?: return
        val logFiles = CrashHandler.getCrashLogFiles(app)

        if (logFiles.isEmpty()) {
            Lg.i(TAG, "No crash log files available to share")
            return
        }

        Lg.i(TAG, "Preparing to share ${logFiles.size} log files")

        val uris = logFiles.mapNotNull { file ->
            try {
                FileProvider.getUriForFile(context, authority, file)
            } catch (e: Exception) {
                Lg.e(TAG, "Failed to get URI: ${file.name}", e)
                null
            }
        }

        if (uris.isEmpty()) return

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/plain"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_crash_logs_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.share_crash_logs_body, logFiles.size)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_crash_logs_title))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun clearLogs(context: Context) {
        val app = context.applicationContext as? Application ?: return
        val logFiles = CrashHandler.getCrashLogFiles(app)
        var count = 0
        logFiles.forEach { file ->
            if (file.delete()) count++
        }
        Lg.i(TAG, "Cleared $count log files")
    }

    fun getLogsDirSize(context: Context): Long {
        val app = context.applicationContext as? Application ?: return 0L
        val logFiles = CrashHandler.getCrashLogFiles(app)
        return logFiles.sumOf { it.length() }
    }
}
