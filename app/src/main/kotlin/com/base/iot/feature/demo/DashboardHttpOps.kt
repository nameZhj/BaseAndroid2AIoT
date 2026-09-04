// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import com.base.iot.R
import com.base.iot.core.config.AppConfig
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.network.*
import java.io.File

fun DashboardViewModel.testHttpGet() = launchIotOperation(getString(R.string.op_http_get)) {
    appendLog(getString(R.string.demo_http_get_url, AppConfig.DEMO_HTTP_GET_URL))
    val result = iotHub.http.get<Map<String, Any>>(AppConfig.DEMO_HTTP_GET_URL)
    when (result) {
        is HttpResult.Success<*> -> appendLog(getString(R.string.demo_http_success_code, result.code, result.data.toString().take(200)))
        is HttpResult.Error -> throw RuntimeException(getString(R.string.demo_http_error_code, result.code, result.message))
    }
}

fun DashboardViewModel.testHttpPost() = launchIotOperation(getString(R.string.op_http_post)) {
    appendLog(getString(R.string.demo_http_post_url, AppConfig.DEMO_HTTP_POST_URL))
    val body = mapOf("key" to "value", "timestamp" to System.currentTimeMillis())
    val result = iotHub.http.post<Map<String, Any>>(AppConfig.DEMO_HTTP_POST_URL, body)
    when (result) {
        is HttpResult.Success<*> -> appendLog(getString(R.string.demo_http_success_code, result.code, "OK"))
        is HttpResult.Error -> throw RuntimeException(getString(R.string.demo_http_error_code, result.code, result.message))
    }
}

fun DashboardViewModel.testHttpPut() = launchIotOperation(getString(R.string.op_http_put)) {
    appendLog(getString(R.string.demo_http_put_url, AppConfig.DEMO_HTTP_PUT_URL))
    val body = mapOf("deviceStatus" to "ONLINE", "updatedAt" to System.currentTimeMillis())
    val result = iotHub.http.put<Map<String, Any>>(AppConfig.DEMO_HTTP_PUT_URL, body)
    when (result) {
        is HttpResult.Success<*> -> appendLog(getString(R.string.demo_http_put_success, result.code))
        is HttpResult.Error -> throw RuntimeException(getString(R.string.demo_http_put_error, result.code, result.message))
    }
}

fun DashboardViewModel.testHttpDelete() = launchIotOperation(getString(R.string.op_http_delete)) {
    appendLog(getString(R.string.demo_http_delete_url, AppConfig.DEMO_HTTP_DELETE_URL))
    val result = iotHub.http.delete<Map<String, Any>>(AppConfig.DEMO_HTTP_DELETE_URL, mapOf("id" to "1001"))
    when (result) {
        is HttpResult.Success<*> -> appendLog(getString(R.string.demo_http_delete_success, result.code))
        is HttpResult.Error -> throw RuntimeException(getString(R.string.demo_http_delete_error, result.code, result.message))
    }
}

fun DashboardViewModel.testHttpUpload() = launchIotOperation(getString(R.string.op_http_upload)) { updateProgress ->
    val cacheDir = cacheLocationManager.getCurrentCacheDir()
    val sampleFile = File(cacheDir, "upload_sample_${System.currentTimeMillis()}.txt").apply {
        writeText("Hello IoT Cloud Server! Multipart payload test. Timestamp=${System.currentTimeMillis()}")
    }
    appendLog(getString(R.string.demo_http_upload_url, AppConfig.DEMO_HTTP_POST_URL))
    appendLog(getString(R.string.demo_http_upload_file_info, sampleFile.name, sampleFile.length(), sampleFile.absolutePath))

    val result = iotHub.http.upload<Map<String, Any>>(
        url = AppConfig.DEMO_HTTP_POST_URL,
        file = sampleFile,
        paramName = "file",
        formFields = mapOf("deviceId" to "android_iot_dev_01", "firmware" to "v1.0.0"),
        onProgress = { bytesWritten: Long, totalBytes: Long, percent: Float ->
            Lg.d("Upload", "Upload progress: $percent% ($bytesWritten/$totalBytes)")
            val p = bytesWritten.toFloat() / totalBytes
            val text = getString(R.string.demo_progress_kb_format, bytesWritten / 1024, totalBytes / 1024, percent.toInt())
            updateProgress(p, text)
        }
    )
    when (result) {
        is HttpResult.Success<*> -> appendLog(getString(R.string.demo_http_upload_success, result.code))
        is HttpResult.Error -> throw RuntimeException(getString(R.string.demo_http_upload_error, result.code, result.message))
    }
    refreshCacheStats()
}

fun DashboardViewModel.testHttpDownload() = launchIotOperation(getString(R.string.op_http_download)) { updateProgress ->
    val destFile = cacheLocationManager.createCacheFile("download_iot_${System.currentTimeMillis()}.bin")
    appendLog(getString(R.string.demo_http_download_prep))
    appendLog(getString(R.string.demo_http_download_target, destFile.name, destFile.absolutePath))

    iotHub.http.download(AppConfig.DEMO_DOWNLOAD_URL, destFile).collect { state ->
        when (state) {
            is DownloadState.Idle -> appendLog(getString(R.string.demo_http_download_ready))
            is DownloadState.Progress -> {
                val p = state.bytesRead.toFloat() / state.totalBytes
                val text = getString(R.string.demo_progress_kb_format, state.bytesRead / 1024, state.totalBytes / 1024, state.percent.toInt())
                updateProgress(p, text)
            }
            is DownloadState.Success -> {
                appendLog(getString(R.string.demo_http_download_success, state.file.length(), state.file.name))
                updateUiState { it.copy(latestDownloadedFile = state.file) }
                refreshCacheStats()
            }
            is DownloadState.Error -> throw RuntimeException(getString(R.string.demo_http_download_error, state.message))
        }
    }
}
