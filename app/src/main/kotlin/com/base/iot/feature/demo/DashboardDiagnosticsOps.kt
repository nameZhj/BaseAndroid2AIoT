// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import androidx.lifecycle.viewModelScope
import com.base.iot.R
import com.base.iot.core.config.ProtocolDisabledException
import com.base.iot.core.diagnostics.CrashHandler
import com.base.iot.core.diagnostics.Lg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

fun DashboardViewModel.testBlockingProgress() = launchWithLoading(
    title = getString(R.string.demo_task_handshake_title),
    isBlocking = true
) {
    appendLog(getString(R.string.demo_log_blocking_task_start))
    delay(2500)
    appendLog(getString(R.string.demo_log_blocking_task_done))
}

fun DashboardViewModel.testNonBlockingProgress() = launchWithLoading(
    title = getString(R.string.demo_task_firmware_verify_title),
    isBlocking = false
) {
    appendLog(getString(R.string.demo_log_non_blocking_task_start))
    for (i in 1..5) {
        delay(1000)
        appendLog(getString(R.string.demo_log_non_blocking_progress, i * 20))
    }
    appendLog(getString(R.string.demo_log_non_blocking_task_done))
}

fun DashboardViewModel.testPercentageProgress() = launchWithLoading(
    title = getString(R.string.demo_task_ota_download_title),
    isBlocking = uiState.value.isBlockingDefault
) { updateProgress ->
    appendLog(getString(R.string.demo_log_percentage_task_start))
    val totalBytes = 100 * 1024 * 1024L
    var currentBytes = 0L
    while (currentBytes < totalBytes) {
        delay(150)
        currentBytes += (5 * 1024 * 1024L)
        val percent = currentBytes.toFloat() / totalBytes
        val text = getString(R.string.demo_progress_mb_format, currentBytes / (1024 * 1024), 100, (percent * 100).toInt())
        updateProgress(percent, text)
    }
    appendLog(getString(R.string.demo_log_percentage_task_done))
}

fun DashboardViewModel.testSimulateTimeoutError() = launchWithLoading(
    title = getString(R.string.demo_task_connect_gateway_title)
) {
    appendLog(getString(R.string.demo_log_simulate_timeout, DemoConfig.DEMO_UNREACHABLE_IP, DemoConfig.DEMO_UNREACHABLE_PORT))
    withContext(Dispatchers.IO) {
        val socket = Socket()
        socket.connect(InetSocketAddress(DemoConfig.DEMO_UNREACHABLE_IP, DemoConfig.DEMO_UNREACHABLE_PORT), DemoConfig.DEMO_TIMEOUT_MOCK_MS)
        socket.close()
    }
}

fun DashboardViewModel.testSimulateProtocolDisabledError() = launchWithLoading(
    title = getString(R.string.demo_task_check_protocol_title)
) {
    delay(600)
    throw ProtocolDisabledException(getString(R.string.err_protocol_disabled))
}

fun DashboardViewModel.triggerLongLog() {
    val longText = (1..50).joinToString(separator = "\n") {
        "[$it] Long log chunk test line $it, validating Lg chunking behavior. Timestamp = ${System.currentTimeMillis()}"
    }
    Lg.d("LongLogTest", longText)
    appendLog(getString(R.string.demo_log_long_test_triggered))
}

fun DashboardViewModel.exportCrashLogs() = viewModelScope.launch {
    appendLog(getString(R.string.demo_log_crash_check))
    val files = CrashHandler.getCrashLogFiles(getApplication())
    if (files.isNotEmpty()) {
        val file = files.first()
        appendLog(getString(R.string.demo_log_crash_found, file.name, file.length()))
        updateUiState { it.copy(latestDownloadedFile = file) }
    } else {
        appendLog(getString(R.string.demo_log_no_crash_file))
    }
}

fun DashboardViewModel.shareCrashLog() = viewModelScope.launch {
    val files = CrashHandler.getCrashLogFiles(getApplication())
    if (files.isNotEmpty()) {
        fileShare.shareFile(files.first(), getString(R.string.share_crash_logs_title))
    } else {
        appendLog(getString(R.string.demo_log_no_crash_file_share))
    }
}
