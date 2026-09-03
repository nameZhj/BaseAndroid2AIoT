package com.base.iot.feature.demo

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.base.iot.core.base.BaseViewModel
import com.base.iot.core.base.IUiEvent
import com.base.iot.core.base.IUiState
import com.base.iot.core.config.AppConfig
import com.base.iot.core.config.IotProtocolSwitches
import com.base.iot.core.config.ProtocolDisabledException
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.iot.IotHub
import com.base.iot.core.network.*
import com.base.iot.core.storage.CacheLocationManager
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.storage.FileShareManager
import com.base.iot.core.ui.dialog.LoadingConfig
import com.base.iot.core.ui.recycler.IotDeviceItem
import com.base.iot.core.ui.theme.ThemeManager
import com.base.iot.core.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

data class DashboardUiState(
    val switches: IotProtocolSwitches = IotProtocolSwitches(),
    val mqttConnected: Boolean = false,
    val redisConnected: Boolean = false,
    val socketConnected: Boolean = false,
    val isImmersive: Boolean = false,
    val terminalLogs: List<String> = emptyList(),
    val currentCacheType: CacheLocationType = CacheLocationType.INTERNAL,
    val currentCachePath: String = "",
    val cacheFilesCount: Int = 0,
    val latestDownloadedFile: File? = null,
    val brvahDevices: List<IotDeviceItem> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.DARK,
    val showConfirmDialog: Boolean = false,
    val showLoadingDialog: Boolean = false,
    val showInputDialog: Boolean = false,
    val showBottomSheet: Boolean = false,
    override val loadingConfig: LoadingConfig = LoadingConfig(),
    override val parsedError: ParsedError? = null,
    override val showErrorDialog: Boolean = false,
    val isBlockingDefault: Boolean = true,
    val enableLoadingDialog: Boolean = true
) : IUiState

sealed class DashboardEvent : IUiEvent {
    data class ShowSnackbar(val message: String) : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    val iotHub: IotHub,
    private val cacheLocationManager: CacheLocationManager,
    fileShareManager: FileShareManager,
    private val themeManager: ThemeManager
) : BaseViewModel<DashboardUiState, DashboardEvent>(
    application = application,
    fileShareManager = fileShareManager,
    initialState = DashboardUiState()
) {
    override fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig) {
        _uiState.update { it.copy(loadingConfig = reducer(it.loadingConfig)) }
    }

    override fun updateErrorState(error: ParsedError?, visible: Boolean) {
        _uiState.update { it.copy(parsedError = error, showErrorDialog = visible) }
    }

    init {
        viewModelScope.launch {
            iotHub.config.switchesFlow.collect { switches ->
                _uiState.update { it.copy(switches = switches) }
            }
        }

        viewModelScope.launch {
            cacheLocationManager.locationTypeFlow.collect { type ->
                val dir = cacheLocationManager.getCacheDir(type)
                val files = cacheLocationManager.listCacheFiles()
                _uiState.update {
                    it.copy(
                        currentCacheType = type,
                        currentCachePath = dir.absolutePath,
                        cacheFilesCount = files.size,
                        latestDownloadedFile = files.firstOrNull()
                    )
                }
            }
        }

        viewModelScope.launch {
            themeManager.themeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }

        _uiState.update {
            it.copy(
                brvahDevices = listOf(
                    IotDeviceItem("DEV_ESP32_01", getString(com.base.iot.R.string.demo_device_gateway_a1), "MQTT", "ONLINE", "192.168.1.101"),
                    IotDeviceItem("DEV_PLC_02", getString(com.base.iot.R.string.demo_device_siemens_plc), "Socket", "ONLINE", "192.168.1.102"),
                    IotDeviceItem("DEV_EDGE_03", getString(com.base.iot.R.string.demo_device_edge_server), "HTTP/REST", "ONLINE", "192.168.1.103"),
                    IotDeviceItem("DEV_SENSOR_04", getString(com.base.iot.R.string.demo_device_temp_sensor), "MQTT", "OFFLINE", "192.168.1.104")
                )
            )
        }

        viewModelScope.launch {
            iotHub.mqtt.connectionState.collect { state ->
                _uiState.update {
                    it.copy(mqttConnected = state == com.base.iot.core.iot.MqttConnectionState.CONNECTED)
                }
            }
        }

        viewModelScope.launch {
            iotHub.socket.connectionState.collect { state ->
                _uiState.update {
                    it.copy(socketConnected = state == com.base.iot.core.iot.SocketConnectionState.CONNECTED)
                }
            }
        }

        appendLog(getString(com.base.iot.R.string.demo_dashboard_ready))
    }

    fun launchIotOperation(
        title: String? = null,
        action: suspend CoroutineScope.(updateProgress: (Float?, String?) -> Unit) -> Unit
    ): Job {
        return launchWithLoading(
            title = title,
            isBlocking = _uiState.value.isBlockingDefault,
            showLoading = _uiState.value.enableLoadingDialog,
            action = action
        )
    }

    fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val logEntry = "[$timestamp] $message"
        _uiState.update {
            it.copy(terminalLogs = (it.terminalLogs + logEntry).takeLast(200))
        }
    }

    fun clearTerminal() {
        _uiState.update { it.copy(terminalLogs = emptyList()) }
    }

    fun toggleHttp(enabled: Boolean) = viewModelScope.launch {
        if (_uiState.value.switches.isHttpCompiled && !enabled) {
            appendLog(getString(com.base.iot.R.string.demo_protocol_locked_hint, "HTTP"))
            return@launch
        }
        iotHub.config.setHttpEnabled(enabled)
        appendLog("HTTP switch: $enabled")
    }

    fun toggleMqtt(enabled: Boolean) = viewModelScope.launch {
        if (_uiState.value.switches.isMqttCompiled && !enabled) {
            appendLog(getString(com.base.iot.R.string.demo_protocol_locked_hint, "MQTT"))
            return@launch
        }
        iotHub.config.setMqttEnabled(enabled)
        appendLog("MQTT switch: $enabled")
    }

    fun toggleRedis(enabled: Boolean) = viewModelScope.launch {
        if (_uiState.value.switches.isRedisCompiled && !enabled) {
            appendLog(getString(com.base.iot.R.string.demo_protocol_locked_hint, "Redis"))
            return@launch
        }
        iotHub.config.setRedisEnabled(enabled)
        appendLog("Redis switch: $enabled")
    }

    fun toggleSocket(enabled: Boolean) = viewModelScope.launch {
        if (_uiState.value.switches.isSocketCompiled && !enabled) {
            appendLog(getString(com.base.iot.R.string.demo_protocol_locked_hint, "Socket"))
            return@launch
        }
        iotHub.config.setSocketEnabled(enabled)
        appendLog("Socket switch: $enabled")
    }

    fun showLicensesDialog() {
        appendLog(getString(com.base.iot.R.string.demo_license_summary_header))
        appendLog("1. XPopup (Apache-2.0) - https://github.com/li-xiaojun/XPopup")
        appendLog("2. BRVAH 4 (Apache-2.0) - https://github.com/CymChad/BaseRecyclerViewAdapterHelper")
        appendLog("3. Retrofit & OkHttp (Apache-2.0) - https://github.com/square/retrofit")
        appendLog("4. HiveMQ MQTT Client (Apache-2.0) - https://github.com/hivemq/hivemq-mqtt-client")
        appendLog("5. Jedis (MIT) - https://github.com/redis/jedis")
        appendLog("6. Coil (Apache-2.0) - https://github.com/coil-kt/coil")
        appendLog("7. Dagger Hilt (Apache-2.0) - https://github.com/google/dagger")
        appendLog("8. Google Gson (Apache-2.0) - https://github.com/google/gson")
        appendLog("9. Kotlinx Coroutines (Apache-2.0) - https://github.com/Kotlin/kotlinx.coroutines")
        appendLog(getString(com.base.iot.R.string.demo_license_compliance_desc))
    }

    fun testHttpGet() = launchIotOperation("HTTP GET") {
        appendLog("HTTP GET → ${AppConfig.DEMO_HTTP_GET_URL}")
        val result = iotHub.http.get<Map<String, Any>>(AppConfig.DEMO_HTTP_GET_URL)
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP Success [${result.code}]: ${result.data.toString().take(200)}")
            is HttpResult.Error -> throw RuntimeException("HTTP Error [${result.code}]: ${result.message}")
        }
    }

    fun testHttpPost() = launchIotOperation("HTTP POST") {
        appendLog("HTTP POST → ${AppConfig.DEMO_HTTP_POST_URL}")
        val body = mapOf("key" to "value", "timestamp" to System.currentTimeMillis())
        val result = iotHub.http.post<Map<String, Any>>(AppConfig.DEMO_HTTP_POST_URL, body)
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP Success [${result.code}]: OK")
            is HttpResult.Error -> throw RuntimeException("HTTP Error [${result.code}]: ${result.message}")
        }
    }

    fun testHttpPut() = launchIotOperation("HTTP PUT") {
        appendLog("HTTP PUT → https://httpbin.org/put")
        val body = mapOf("deviceStatus" to "ONLINE", "updatedAt" to System.currentTimeMillis())
        val result = iotHub.http.put<Map<String, Any>>("https://httpbin.org/put", body)
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP PUT Success [${result.code}]")
            is HttpResult.Error -> throw RuntimeException("HTTP Error [${result.code}]: ${result.message}")
        }
    }

    fun testHttpDelete() = launchIotOperation("HTTP DELETE") {
        appendLog("HTTP DELETE → https://httpbin.org/delete")
        val result = iotHub.http.delete<Map<String, Any>>("https://httpbin.org/delete", mapOf("id" to "1001"))
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP DELETE Success [${result.code}]")
            is HttpResult.Error -> throw RuntimeException("HTTP Error [${result.code}]: ${result.message}")
        }
    }

    fun testHttpUpload() = launchIotOperation("HTTP Upload") { updateProgress ->
        val cacheDir = cacheLocationManager.getCurrentCacheDir()
        val sampleFile = File(cacheDir, "upload_sample_${System.currentTimeMillis()}.txt").apply {
            writeText("Hello IoT Cloud Server! Multipart payload test. Timestamp=${System.currentTimeMillis()}")
        }
        appendLog("HTTP UPLOAD → ${AppConfig.DEMO_HTTP_POST_URL}")
        appendLog("HTTP UPLOAD [File] name: ${sampleFile.name}, size: ${sampleFile.length()}B, path: ${sampleFile.absolutePath}")

        val result = iotHub.http.upload<Map<String, Any>>(
            url = AppConfig.DEMO_HTTP_POST_URL,
            file = sampleFile,
            paramName = "file",
            formFields = mapOf("deviceId" to "android_iot_dev_01", "firmware" to "v1.0.0"),
            onProgress = { bytesWritten, totalBytes, percent ->
                Lg.d("Upload", "Upload progress: $percent% ($bytesWritten/$totalBytes)")
                val p = bytesWritten.toFloat() / totalBytes
                val text = "${bytesWritten / 1024} KB / ${totalBytes / 1024} KB ($percent%)"
                updateProgress(p, text)
            }
        )
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP UPLOAD Success [${result.code}]")
            is HttpResult.Error -> throw RuntimeException("HTTP Upload Error [${result.code}]: ${result.message}")
        }
        refreshCacheStats()
    }

    fun testHttpDownload() = launchIotOperation("HTTP Download") { updateProgress ->
        val destFile = cacheLocationManager.createCacheFile("download_iot_${System.currentTimeMillis()}.bin")
        appendLog("HTTP DOWNLOAD → starting stream download...")
        appendLog("HTTP DOWNLOAD [Target] name: ${destFile.name}, path: ${destFile.absolutePath}")

        iotHub.http.download("https://httpbin.org/bytes/65536", destFile).collect { state ->
            when (state) {
                is DownloadState.Idle -> appendLog("HTTP DOWNLOAD preparing...")
                is DownloadState.Progress -> {
                    val p = state.bytesRead.toFloat() / state.totalBytes
                    val text = "${state.bytesRead / 1024} KB / ${state.totalBytes / 1024} KB (${state.percent.toInt()}%)"
                    updateProgress(p, text)
                }
                is DownloadState.Success -> {
                    appendLog("HTTP DOWNLOAD Success! File size: ${state.file.length()} bytes, saved to: ${state.file.name}")
                    _uiState.update { it.copy(latestDownloadedFile = state.file) }
                    refreshCacheStats()
                }
                is DownloadState.Error -> throw RuntimeException("HTTP Download Failed: ${state.message}")
            }
        }
    }

    fun setCacheLocationType(type: CacheLocationType) = viewModelScope.launch {
        cacheLocationManager.setLocationType(type)
        appendLog("Cache location switched: ${type.name}")
        refreshCacheStats()
    }

    fun shareLatestDownloadedFile() {
        val file = _uiState.value.latestDownloadedFile
        if (file != null && file.exists()) {
            appendLog("Opening system share: ${file.name}")
            fileShareManager.shareFile(file, getString(com.base.iot.R.string.share_file_title))
        } else {
            appendLog("No downloaded files available to share")
        }
    }

    fun clearCache() = viewModelScope.launch {
        val freed = cacheLocationManager.clearCurrentCache()
        appendLog("Cleared cache dir, freed: $freed bytes")
        refreshCacheStats()
    }

    private suspend fun refreshCacheStats() {
        val dir = cacheLocationManager.getCurrentCacheDir()
        val files = cacheLocationManager.listCacheFiles()
        _uiState.update {
            it.copy(
                currentCachePath = dir.absolutePath,
                cacheFilesCount = files.size,
                latestDownloadedFile = files.firstOrNull()
            )
        }
    }

    fun toggleTheme() = viewModelScope.launch {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        themeManager.setThemeMode(next)
        appendLog("Display mode switched: ${next.name}")
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        themeManager.setThemeMode(mode)
        appendLog("Theme mode set to: ${mode.name}")
    }

    fun setConfirmDialog(visible: Boolean) {
        _uiState.update { it.copy(showConfirmDialog = visible) }
    }

    fun setLoadingDialog(visible: Boolean) {
        _uiState.update { it.copy(showLoadingDialog = visible) }
    }

    fun setInputDialog(visible: Boolean) {
        _uiState.update { it.copy(showInputDialog = visible) }
    }

    fun setBottomSheet(visible: Boolean) {
        _uiState.update { it.copy(showBottomSheet = visible) }
    }

    fun onConfirmDialogConfirmed() {
        appendLog("Confirm dialog: user confirmed execution")
    }

    fun onInputDialogConfirmed(text: String) {
        appendLog("Input dialog: user submitted text: $text")
    }

    fun toggleBlockingDefault() {
        _uiState.update { it.copy(isBlockingDefault = !it.isBlockingDefault) }
        appendLog("Progress dialog mode: " + if (_uiState.value.isBlockingDefault) "Blocking" else "Non-blocking")
    }

    fun toggleEnableLoadingDialog() {
        _uiState.update { it.copy(enableLoadingDialog = !it.enableLoadingDialog) }
        appendLog("Loading dialog switch: " + if (_uiState.value.enableLoadingDialog) "Enabled" else "Disabled")
    }

    fun testBlockingProgress() = launchWithLoading(
        title = "Security Handshake & Key Distribution",
        isBlocking = true
    ) {
        appendLog("[Blocking Task] started (cannot interrupt)...")
        delay(2500)
        appendLog("[Blocking Task] completed.")
    }

    fun testNonBlockingProgress() = launchWithLoading(
        title = "Firmware Verification",
        isBlocking = false
    ) {
        appendLog("[Non-blocking Task] started...")
        for (i in 1..5) {
            delay(1000)
            appendLog("[Non-blocking Task] progress: ${i * 20}%")
        }
        appendLog("[Non-blocking Task] completed.")
    }

    fun testPercentageProgress() = launchWithLoading(
        title = "OTA Firmware Download",
        isBlocking = _uiState.value.isBlockingDefault
    ) { updateProgress ->
        appendLog("[Percentage Progress] started...")
        val totalBytes = 100 * 1024 * 1024L
        var currentBytes = 0L
        while (currentBytes < totalBytes) {
            delay(150)
            currentBytes += (5 * 1024 * 1024L)
            val percent = currentBytes.toFloat() / totalBytes
            val text = "${currentBytes / (1024 * 1024)} MB / 100 MB (${(percent * 100).toInt()}%)"
            updateProgress(percent, text)
        }
        appendLog("[Percentage Progress] completed.")
    }

    fun testSimulateTimeoutError() = launchWithLoading(
        title = "Connecting to Gateway..."
    ) {
        appendLog("Connecting to unreachable IP (10.255.255.1:80)...")
        withContext(Dispatchers.IO) {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("10.255.255.1", 80), 1500)
            socket.close()
        }
    }

    fun testSimulateProtocolDisabledError() = launchWithLoading(
        title = "Checking Protocol Status..."
    ) {
        delay(600)
        throw ProtocolDisabledException("MQTT industrial bus protocol has been disabled in configuration")
    }

    fun connectMqtt() = launchIotOperation("Connecting to MQTT Broker...") {
        appendLog("MQTT connecting to ${AppConfig.MQTT_HOST}:${AppConfig.MQTT_PORT}...")
        iotHub.mqtt.connect()
        appendLog("MQTT connected successfully")
    }

    fun mqttPublish() = viewModelScope.launch {
        val topic = AppConfig.DEMO_MQTT_PUB_TOPIC
        val payload = """{"msg":"hello","ts":${System.currentTimeMillis()}}"""
        appendLog("MQTT PUBLISH → $topic: $payload")
        try {
            iotHub.mqtt.publish(topic, payload)
            appendLog("MQTT publish successfully")
        } catch (e: ProtocolDisabledException) {
            appendLog("MQTT protocol disabled: ${e.message}")
        } catch (e: Exception) {
            appendLog("MQTT publish failed: ${e.message}")
        }
    }

    fun mqttSubscribeTest() = viewModelScope.launch {
        val topic = AppConfig.DEMO_MQTT_SUB_TOPIC
        appendLog("MQTT SUBSCRIBE → $topic")
        try {
            iotHub.mqtt.subscribe(topic)
                .take(3)
                .onEach { msg -> appendLog("MQTT RECV ← $msg") }
                .catch { e -> appendLog("MQTT subscription error: ${e.message}") }
                .launchIn(this)
        } catch (e: ProtocolDisabledException) {
            appendLog("MQTT protocol disabled: ${e.message}")
        }
    }

    fun connectRedis() = launchIotOperation("Connecting to Redis Server...") {
        appendLog("Redis connecting to ${AppConfig.REDIS_HOST}:${AppConfig.REDIS_PORT}...")
        iotHub.redis.connect()
        _uiState.update { it.copy(redisConnected = true) }
        appendLog("Redis connected successfully")
    }

    fun redisSendCommand() = viewModelScope.launch {
        appendLog("Redis SET ${AppConfig.DEMO_REDIS_KEY} → 'hello_from_android'")
        try {
            val result = iotHub.redis.set(AppConfig.DEMO_REDIS_KEY, "hello_from_android", 60)
            appendLog("Redis SET result: $result")
            val value = iotHub.redis.get(AppConfig.DEMO_REDIS_KEY)
            appendLog("Redis GET ${AppConfig.DEMO_REDIS_KEY} = $value")
        } catch (e: ProtocolDisabledException) {
            appendLog("Redis protocol disabled: ${e.message}")
        } catch (e: Exception) {
            appendLog("Redis error: ${e.message}")
        }
    }

    fun connectSocket() = launchIotOperation("Connecting to TCP Socket...") {
        appendLog("Socket connecting to ${AppConfig.SOCKET_HOST}:${AppConfig.SOCKET_PORT}...")
        iotHub.socket.connect(AppConfig.SOCKET_HOST, AppConfig.SOCKET_PORT)
        appendLog("Socket connected successfully")
    }

    fun socketPingPong() = viewModelScope.launch {
        appendLog("Socket SEND → PING")
        try {
            iotHub.socket.send("PING")
        } catch (e: ProtocolDisabledException) {
            appendLog("Socket protocol disabled: ${e.message}")
        } catch (e: Exception) {
            appendLog("Socket send failed: ${e.message}")
        }
    }

    fun triggerLongLog() {
        val longText = (1..50).joinToString(separator = "\n") {
            "[$it] Long log chunk test line $it, validating Lg chunking behavior. Timestamp = ${System.currentTimeMillis()}"
        }
        Lg.d("LongLogTest", longText)
        appendLog("Triggered long log test. Please inspect Logcat [LongLogTest]")
    }

    fun exportCrashLogs() = viewModelScope.launch {
        appendLog("Checking crash logs...")
        val files = com.base.iot.core.diagnostics.CrashHandler.getCrashLogFiles(getApplication())
        if (files.isNotEmpty()) {
            val file = files.first()
            appendLog("Found crash log: ${file.name} (${file.length()}B)")
            _uiState.update { it.copy(latestDownloadedFile = file) }
        } else {
            appendLog("No crash log records found")
        }
    }

    fun shareCrashLog() = viewModelScope.launch {
        val files = com.base.iot.core.diagnostics.CrashHandler.getCrashLogFiles(getApplication())
        if (files.isNotEmpty()) {
            fileShareManager.shareFile(files.first(), getString(com.base.iot.R.string.share_crash_logs_title))
        } else {
            appendLog("No crash logs available to share")
        }
    }

    fun toggleImmersive(enable: Boolean) {
        _uiState.update { it.copy(isImmersive = enable) }
        appendLog("Fullscreen immersive mode: $enable")
    }
}
