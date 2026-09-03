package com.base.iot.core.diagnostics

import android.app.Application
import android.os.Build
import com.base.iot.core.config.AppConfig
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashHandler : Thread.UncaughtExceptionHandler {
    private const val TAG = "CrashHandler"
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var application: Application? = null

    fun install(app: Application) {
        application = app
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Lg.i(TAG, "崩溃拦截器已安装")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Lg.e(TAG, "捕获到未处理异常，正在写入崩溃日志...", throwable)
        try {
            writeCrashLog(thread, throwable)
        } catch (e: Exception) {
            Lg.e(TAG, "写入崩溃日志失败: ${e.message}", e)
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun writeCrashLog(thread: Thread, throwable: Throwable) {
        val app = application ?: return
        val logDir = File(app.cacheDir, AppConfig.LOG_DIR_NAME).also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val logFile = File(logDir, "${AppConfig.LOG_FILE_PREFIX}${timestamp}${AppConfig.LOG_FILE_SUFFIX}")

        PrintWriter(FileWriter(logFile, false)).use { pw ->
            pw.println("============================== CRASH REPORT ==============================")
            pw.println("Time       : $timestamp")
            pw.println()
            pw.println("──────────── Device Info ────────────")
            pw.println("Brand      : ${Build.BRAND}")
            pw.println("Model      : ${Build.MODEL}")
            pw.println("Device     : ${Build.DEVICE}")
            pw.println("Android    : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            pw.println("Arch       : ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            pw.println()
            pw.println("──────────── App Info ────────────")
            try {
                val pm = app.packageManager
                val pi = pm.getPackageInfo(app.packageName, 0)
                pw.println("Package    : ${app.packageName}")
                pw.println("VersionName: ${pi.versionName}")
                pw.println("VersionCode: ${pi.longVersionCode}")
            } catch (e: Exception) {
                pw.println("App info unavailable: ${e.message}")
            }
            pw.println("Environment: ${AppConfig.currentEnv}")
            pw.println()
            pw.println("──────────── Thread Info ────────────")
            pw.println("Thread     : ${thread.name} (id=${thread.id})")
            pw.println()
            pw.println("──────────── Stack Trace ────────────")
            throwable.printStackTrace(pw)
            pw.println()
            pw.println("──────────── All Threads ────────────")
            Thread.getAllStackTraces().forEach { (t, frames) ->
                pw.println("[Thread: ${t.name}]")
                frames.forEach { frame -> pw.println("  at $frame") }
                pw.println()
            }
            pw.println("=============================== END OF REPORT ===============================")
            pw.flush()
        }

        Lg.i(TAG, "崩溃日志已写入: ${logFile.absolutePath}")
    }

    fun getCrashLogFiles(app: Application): List<File> {
        val logDir = File(app.cacheDir, AppConfig.LOG_DIR_NAME)
        if (!logDir.exists()) return emptyList()
        return logDir.listFiles { file ->
            file.name.startsWith(AppConfig.LOG_FILE_PREFIX) &&
                    file.name.endsWith(AppConfig.LOG_FILE_SUFFIX)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
