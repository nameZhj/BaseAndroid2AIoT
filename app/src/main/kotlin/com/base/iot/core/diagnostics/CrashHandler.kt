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

/**
 * 全局崩溃拦截器（Thread.UncaughtExceptionHandler）。
 *
 * 功能：
 * - 拦截所有线程的未捕获异常
 * - 写入完整崩溃报告到 App Cache 目录下 logs/ 文件夹
 * - 报告内容包含：时间戳、设备信息、App 版本、完整堆栈
 * - 完成写入后，调用系统默认异常处理器（保持原有行为）
 *
 * 安装方式（在 Application.onCreate() 中调用）：
 * ```kotlin
 * CrashHandler.install(this)
 * ```
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private const val TAG = "CrashHandler"
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var application: Application? = null

    /**
     * 安装崩溃拦截器。
     * @param app Application 实例，用于获取文件目录
     */
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

        // 调用系统默认处理器（确保 App 正常崩溃退出）
        defaultHandler?.uncaughtException(thread, throwable)
    }

    // ==================== 日志写入 ====================

    private fun writeCrashLog(thread: Thread, throwable: Throwable) {
        val app = application ?: return
        val logDir = File(app.cacheDir, AppConfig.LOG_DIR_NAME).also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val logFile = File(logDir, "${AppConfig.LOG_FILE_PREFIX}${timestamp}${AppConfig.LOG_FILE_SUFFIX}")

        PrintWriter(FileWriter(logFile, false)).use { pw ->
            // 1. 时间戳
            pw.println("============================== CRASH REPORT ==============================")
            pw.println("Time       : $timestamp")
            pw.println()

            // 2. 设备信息
            pw.println("──────────── Device Info ────────────")
            pw.println("Brand      : ${Build.BRAND}")
            pw.println("Model      : ${Build.MODEL}")
            pw.println("Device     : ${Build.DEVICE}")
            pw.println("Android    : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            pw.println("Arch       : ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            pw.println()

            // 3. App 信息
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

            // 4. 线程信息
            pw.println("──────────── Thread Info ────────────")
            pw.println("Thread     : ${thread.name} (id=${thread.id})")
            pw.println()

            // 5. 完整堆栈
            pw.println("──────────── Stack Trace ────────────")
            throwable.printStackTrace(pw)
            pw.println()

            // 6. 所有线程快照
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

    /** 返回所有崩溃日志文件列表（最新在前） */
    fun getCrashLogFiles(app: Application): List<File> {
        val logDir = File(app.cacheDir, AppConfig.LOG_DIR_NAME)
        if (!logDir.exists()) return emptyList()
        return logDir.listFiles { file ->
            file.name.startsWith(AppConfig.LOG_FILE_PREFIX) &&
                    file.name.endsWith(AppConfig.LOG_FILE_SUFFIX)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
