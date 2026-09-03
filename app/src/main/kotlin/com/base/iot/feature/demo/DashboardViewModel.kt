package com.base.iot.feature.demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.base.iot.core.config.AppConfig
import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.config.IotProtocolSwitches
import com.base.iot.core.config.ProtocolDisabledException
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.diagnostics.LogExporter
import com.base.iot.core.iot.MqttManager
import com.base.iot.core.iot.RedisManager
import com.base.iot.core.iot.SocketManager
import com.base.iot.core.network.*
import com.base.iot.core.storage.CacheLocationManager
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.storage.FileShareManager
import com.base.iot.core.ui.recycler.IotDeviceItem
import com.base.iot.core.ui.theme.ThemeManager
import com.base.iot.core.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

// ==================== UI State ====================

import com.base.iot.core.diagnostics.ErrorParser
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.ui.dialog.LoadingConfig

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
    // ===== 耗时操作进度与错误弹窗体系 =====
    val loadingConfig: LoadingConfig = LoadingConfig(),
    val parsedError: ParsedError? = null,
    val showErrorDialog: Boolean = false,
    val isBlockingDefault: Boolean = true,
    val enableLoadingDialog: Boolean = true
)

// ==================== Events (One-shot) ====================

sealed class DashboardEvent {
    data class ShowSnackbar(val message: String) : DashboardEvent()
}

// ==================== ViewModel ====================

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val protocolConfig: IotProtocolConfig,
    private val httpManager: HttpManager,
    private val mqttManager: MqttManager,
    private val redisManager: RedisManager,
    private val socketManager: SocketManager,
    private val cacheLocationManager: CacheLocationManager,
    private val fileShareManager: FileShareManager,
    private val themeManager: ThemeManager
) : AndroidViewModel(application) {

    private val TAG = "DashboardVM"

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    init {
        // 监听协议开关变化
        viewModelScope.launch {
            protocolConfig.switchesFlow.collect { switches ->
                _uiState.update { it.copy(switches = switches) }
            }
        }

        // 监听缓存位置变化并刷新路径与文件统计
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

        // 监听主题模式变化
        viewModelScope.launch {
            themeManager.themeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }

        // 初始化 BRVAH 列表演示数据
        _uiState.update {
            it.copy(
                brvahDevices = listOf(
                    IotDeviceItem("DEV_ESP32_01", "环境监测网关-A1", "MQTT", "ONLINE", "192.168.1.101"),
                    IotDeviceItem("DEV_PLC_02", "西门子工业PLC-B2", "Socket", "ONLINE", "192.168.1.102"),
                    IotDeviceItem("DEV_EDGE_03", "边缘计算服务器-C3", "HTTP/REST", "ONLINE", "192.168.1.103"),
                    IotDeviceItem("DEV_SENSOR_04", "温湿度传感器-D4", "MQTT", "OFFLINE", "192.168.1.104")
                )
            )
        }

        // 监听 MQTT 连接状态
        viewModelScope.launch {
            mqttManager.connectionState.collect { state ->
                _uiState.update {
                    it.copy(mqttConnected = state == com.base.iot.core.iot.MqttConnectionState.CONNECTED)
                }
            }
        }

        // 监听 Socket 连接状态
        viewModelScope.launch {
            socketManager.connectionState.collect { state ->
                _uiState.update {
                    it.copy(socketConnected = state == com.base.iot.core.iot.SocketConnectionState.CONNECTED)
                }
            }
        }

        // 监听 Socket 接收数据
        viewModelScope.launch {
            socketManager.incomingData.collect { data ->
                appendLog("Socket RECV: $data")
            }
        }
    }

    // ==================== 协议开关 ====================

    fun toggleHttp(enabled: Boolean) = viewModelScope.launch {
        try {
            protocolConfig.setHttpEnabled(enabled)
            appendLog("HTTP 开关: ${if (enabled) "✅ 已开启" else "❌ 已关闭"}")
        } catch (e: Exception) {
            appendLog("HTTP ❌ 操作失败: ${e.message}")
        }
    }

    fun toggleMqtt(enabled: Boolean) = viewModelScope.launch {
        try {
            protocolConfig.setMqttEnabled(enabled)
            appendLog("MQTT 开关: ${if (enabled) "✅ 已开启" else "❌ 已关闭"}")
            if (!enabled) mqttManager.disconnect()
        } catch (e: Exception) {
            appendLog("MQTT ❌ 操作失败: ${e.message}")
        }
    }

    fun toggleRedis(enabled: Boolean) = viewModelScope.launch {
        try {
            protocolConfig.setRedisEnabled(enabled)
            appendLog("Redis 开关: ${if (enabled) "✅ 已开启" else "❌ 已关闭"}")
            if (!enabled) redisManager.disconnect()
        } catch (e: Exception) {
            appendLog("Redis ❌ 操作失败: ${e.message}")
        }
    }

    fun toggleSocket(enabled: Boolean) = viewModelScope.launch {
        try {
            protocolConfig.setSocketEnabled(enabled)
            appendLog("Socket 开关: ${if (enabled) "✅ 已开启" else "❌ 已关闭"}")
            if (!enabled) socketManager.disconnect()
        } catch (e: Exception) {
            appendLog("Socket ❌ 操作失败: ${e.message}")
        }
    }

    // ==================== HTTP 测试 ====================

    fun testHttpGet() = launchWithLoading(title = "HTTP GET 请求中...") {
        appendLog("HTTP GET → https://httpbin.org/get")
        val result = httpManager.get<Map<String, Any>>("https://httpbin.org/get")
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP ✅ ${result.code}: ${result.data.toString().take(200)}")
            is HttpResult.Error -> throw RuntimeException("HTTP 响应错误 [${result.code}]: ${result.message}")
        }
    }

    fun testHttpPost() = launchWithLoading(title = "HTTP POST 提交中...") {
        appendLog("HTTP POST → https://httpbin.org/post")
        val body = mapOf("key" to "value", "timestamp" to System.currentTimeMillis())
        val result = httpManager.post<Map<String, Any>>("https://httpbin.org/post", body)
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP ✅ ${result.code}: OK")
            is HttpResult.Error -> throw RuntimeException("HTTP 响应错误 [${result.code}]: ${result.message}")
        }
    }

    fun testHttpPut() = launchWithLoading(title = "HTTP PUT 更新中...") {
        appendLog("HTTP PUT → https://httpbin.org/put")
        val body = mapOf("deviceStatus" to "ONLINE", "updatedAt" to System.currentTimeMillis())
        val result = httpManager.put<Map<String, Any>>("https://httpbin.org/put", body)
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP PUT ✅ ${result.code}: 更新成功")
            is HttpResult.Error -> throw RuntimeException("HTTP 响应错误 [${result.code}]: ${result.message}")
        }
    }

    fun testHttpDelete() = launchWithLoading(title = "HTTP DELETE 删除中...") {
        appendLog("HTTP DELETE → https://httpbin.org/delete")
        val result = httpManager.delete<Map<String, Any>>("https://httpbin.org/delete", mapOf("id" to "1001"))
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP DELETE ✅ ${result.code}: 删除成功")
            is HttpResult.Error -> throw RuntimeException("HTTP 响应错误 [${result.code}]: ${result.message}")
        }
    }

    fun testHttpUpload() = launchWithLoading(title = "HTTP Multipart 文件上传中") { updateProgress ->
        val cacheDir = cacheLocationManager.getCurrentCacheDir()
        val sampleFile = File(cacheDir, "upload_sample_${System.currentTimeMillis()}.txt").apply {
            writeText("Hello IoT Cloud Server! Multipart payload test. Timestamp=${System.currentTimeMillis()}")
        }
        appendLog("HTTP UPLOAD → https://httpbin.org/post")
        appendLog("HTTP UPLOAD [文件] 名称: ${sampleFile.name}, 大小: ${sampleFile.length()}B, 路径: ${sampleFile.absolutePath}")

        val result = httpManager.upload<Map<String, Any>>(
            url = "https://httpbin.org/post",
            file = sampleFile,
            paramName = "file",
            formFields = mapOf("deviceId" to "android_iot_dev_01", "firmware" to "v1.0.0"),
            onProgress = { bytesWritten, totalBytes, percent ->
                Lg.d("Upload", "上传进度: $percent% ($bytesWritten/$totalBytes)")
                val p = bytesWritten.toFloat() / totalBytes
                val text = "${bytesWritten / 1024} KB / ${totalBytes / 1024} KB ($percent%)"
                updateProgress(p, text)
            }
        )
        when (result) {
            is HttpResult.Success<*> -> appendLog("HTTP UPLOAD ✅ ${result.code}: 上传成功")
            is HttpResult.Error -> throw RuntimeException("HTTP 上传错误 [${result.code}]: ${result.message}")
        }
        refreshCacheStats()
    }

    fun testHttpDownload() = launchWithLoading(title = "HTTP 流式文件下载中") { updateProgress ->
        val destFile = cacheLocationManager.createCacheFile("download_iot_${System.currentTimeMillis()}.bin")
        appendLog("HTTP DOWNLOAD → 开始流式下载...")
        appendLog("HTTP DOWNLOAD [目标] 名称: ${destFile.name}, 存储路径: ${destFile.absolutePath}")

        httpManager.download("https://httpbin.org/bytes/65536", destFile).collect { state ->
            when (state) {
                is DownloadState.Idle -> appendLog("HTTP 下载准备中...")
                is DownloadState.Progress -> {
                    val p = state.bytesRead.toFloat() / state.totalBytes
                    val text = "${state.bytesRead / 1024} KB / ${state.totalBytes / 1024} KB (${state.percent.toInt()}%)"
                    updateProgress(p, text)
                }
                is DownloadState.Success -> {
                    appendLog("HTTP 下载 ✅ 成功！文件大小: ${state.file.length()} 字节，已存入缓存: ${state.file.name}")
                    _uiState.update { it.copy(latestDownloadedFile = state.file) }
                    refreshCacheStats()
                }
                is DownloadState.Error -> throw RuntimeException("HTTP 下载失败: ${state.message}")
            }
        }
    }

    // ==================== 缓存位置与文件分享 ====================

    fun setCacheLocationType(type: CacheLocationType) = viewModelScope.launch {
        cacheLocationManager.setLocationType(type)
        appendLog("缓存存储策略已切换: ${type.title}")
        refreshCacheStats()
    }

    fun shareLatestDownloadedFile() {
        val file = _uiState.value.latestDownloadedFile
        if (file != null && file.exists()) {
            appendLog("正在唤起系统分享: ${file.name}")
            fileShareManager.shareFile(file, "分享 IoT 缓存文件")
        } else {
            appendLog("❌ 暂无可分享的已下载文件，请先执行文件下载")
        }
    }

    fun clearCache() = viewModelScope.launch {
        val freed = cacheLocationManager.clearCurrentCache()
        appendLog("已清空当前缓存区，释放: $freed 字节")
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

    // ==================== 主题与弹窗控制 ====================

    fun toggleTheme() = viewModelScope.launch {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        themeManager.setThemeMode(next)
        appendLog("切换显示模式: ${next.title}")
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        themeManager.setThemeMode(mode)
        appendLog("主题模式设置为: ${mode.title}")
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
        appendLog("确认弹窗 ✅ 用户已确认执行核心物联网操作")
    }

    fun onInputDialogConfirmed(text: String) {
        appendLog("输入弹窗 ✍️ 用户已提交数据: $text")
    }

    // ==================== 通用耗时操作进度与错误弹窗架构 ====================

    fun toggleBlockingDefault() {
        _uiState.update { it.copy(isBlockingDefault = !it.isBlockingDefault) }
        appendLog("耗时弹窗模式切换: " + if (_uiState.value.isBlockingDefault) "阻塞式（禁止取消/点击穿透）" else "非阻塞式（可取消/外部关闭）")
    }

    fun toggleEnableLoadingDialog() {
        _uiState.update { it.copy(enableLoadingDialog = !it.enableLoadingDialog) }
        appendLog("耗时进度弹窗开关: " + if (_uiState.value.enableLoadingDialog) "启用" else "禁用 (后台静默执行)")
    }

    /**
     * 核心封装：执行带进度或等待弹窗的耗时异步操作。
     *
     * @param title 弹窗标题
     * @param isBlocking 是否阻塞交互（不可穿透、不可取消），默认为当前全局配置
     * @param showLoading 是否显示弹窗（开发者可自由选择每项任务是否展示）
     * @param action 异步执行闭包，提供 updateProgress(percent, text) 动态更新百分比进度
     */
    fun launchWithLoading(
        title: String = "正在处理中...",
        isBlocking: Boolean = _uiState.value.isBlockingDefault,
        showLoading: Boolean = _uiState.value.enableLoadingDialog,
        action: suspend CoroutineScope.(updateProgress: (Float?, String?) -> Unit) -> Unit
    ): Job {
        return viewModelScope.launch {
            var currentJob: Job? = null
            if (showLoading) {
                _uiState.update {
                    it.copy(
                        loadingConfig = LoadingConfig(
                            visible = true,
                            title = title,
                            isBlocking = isBlocking,
                            progress = null,
                            progressText = null,
                            cancelable = true,
                            onCancel = {
                                currentJob?.cancel()
                                dismissLoading()
                                appendLog("用户已主动取消操作: $title")
                            }
                        )
                    )
                }
            }

            try {
                currentJob = coroutineContext[Job]
                action { progress, text ->
                    if (showLoading) {
                        _uiState.update { state ->
                            state.copy(
                                loadingConfig = state.loadingConfig.copy(
                                    progress = progress,
                                    progressText = text
                                )
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                appendLog("操作已安全中断: ${e.message ?: "用户取消"}")
            } catch (t: Throwable) {
                Lg.e(TAG, "耗时任务执行失败: ${t.message}", t)
                appendLog("❌ 执行异常: ${t.javaClass.simpleName} - ${t.message}")
                showError(t)
            } finally {
                if (showLoading) {
                    dismissLoading()
                }
            }
        }
    }

    fun dismissLoading() {
        _uiState.update { it.copy(loadingConfig = it.loadingConfig.copy(visible = false)) }
    }

    fun showError(throwable: Throwable) {
        val parsed = ErrorParser.parse(getApplication(), throwable)
        _uiState.update {
            it.copy(
                parsedError = parsed,
                showErrorDialog = true
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(showErrorDialog = false) }
    }

    fun shareErrorReport(error: ParsedError) {
        fileShareManager.shareText(
            text = error.fullDiagnosticReport,
            shareTitle = "分享错误报告 - ${error.errorType}"
        )
    }

    // ==================== 进度与错误弹窗实测模拟 ====================

    fun testBlockingProgress() = launchWithLoading(
        title = "安全握手与密钥分发",
        isBlocking = true
    ) {
        appendLog("测试【阻塞式加载弹窗】启动 (不可中断)...")
        delay(2500)
        appendLog("测试【阻塞式加载弹窗】执行完毕 ✅")
    }

    fun testNonBlockingProgress() = launchWithLoading(
        title = "后台固件包校验",
        isBlocking = false
    ) {
        appendLog("测试【非阻塞式加载弹窗】启动 (可轻触外部或点击取消)...")
        for (i in 1..5) {
            delay(1000)
            appendLog("非阻塞后台任务进度: ${i * 20}%")
        }
        appendLog("测试【非阻塞式加载弹窗】正常完成 ✅")
    }

    fun testPercentageProgress() = launchWithLoading(
        title = "OTA 固件下载中",
        isBlocking = _uiState.value.isBlockingDefault
    ) { updateProgress ->
        appendLog("测试【确定百分比进度弹窗】启动...")
        val totalBytes = 100 * 1024 * 1024L // 100MB
        var currentBytes = 0L
        while (currentBytes < totalBytes) {
            delay(150)
            currentBytes += (5 * 1024 * 1024L)
            val percent = currentBytes.toFloat() / totalBytes
            val text = "${currentBytes / (1024 * 1024)} MB / 100 MB (${(percent * 100).toInt()}%)"
            updateProgress(percent, text)
        }
        appendLog("测试【确定百分比进度弹窗】下载完成 ✅")
    }

    fun testSimulateTimeoutError() = launchWithLoading(
        title = "正在连接不可达工控网关..."
    ) {
        appendLog("故意连接超时不可达 IP (10.255.255.1:80)...")
        withContext(Dispatchers.IO) {
            val socket = java.net.Socket()
            // 设置 1.5 秒超时，必触发 SocketTimeoutException
            socket.connect(java.net.InetSocketAddress("10.255.255.1", 80), 1500)
            socket.close()
        }
    }

    fun testSimulateProtocolDisabledError() = launchWithLoading(
        title = "检查协议运行状态..."
    ) {
        delay(600)
        throw ProtocolDisabledException("MQTT 工业总线协议已被管理员在后台禁用 (iot.protocol.mqtt.enabled=false)")
    }

    // ==================== MQTT 测试 ====================

    fun connectMqtt() = launchWithLoading(title = "正在连接 MQTT Broker...") {
        appendLog("MQTT 正在连接 ${AppConfig.MQTT_HOST}:${AppConfig.MQTT_PORT}...")
        mqttManager.connect()
        appendLog("MQTT ✅ 连接成功")
    }

    fun mqttPublish() = viewModelScope.launch {
        val topic = "iot/test/android"
        val payload = """{"msg":"hello","ts":${System.currentTimeMillis()}}"""
        appendLog("MQTT PUBLISH → $topic: $payload")
        try {
            mqttManager.publish(topic, payload)
            appendLog("MQTT ✅ 发布成功")
        } catch (e: ProtocolDisabledException) {
            appendLog("MQTT ❌ 协议已禁用: ${e.message}")
        } catch (e: Exception) {
            appendLog("MQTT ❌ 发布失败: ${e.message}")
        }
    }

    fun mqttSubscribeTest() = viewModelScope.launch {
        val topic = "iot/test/android"
        appendLog("MQTT 订阅 → $topic")
        try {
            mqttManager.subscribe(topic)
                .take(3) // 最多接收 3 条消息后自动取消
                .onEach { msg -> appendLog("MQTT RECV ← $msg") }
                .catch { e -> appendLog("MQTT ❌ 订阅异常: ${e.message}") }
                .launchIn(this)
        } catch (e: ProtocolDisabledException) {
            appendLog("MQTT ❌ 协议已禁用: ${e.message}")
        }
    }

    // ==================== Redis 测试 ====================

    fun connectRedis() = launchWithLoading(title = "正在连接 Redis 服务器...") {
        appendLog("Redis 正在连接 ${AppConfig.REDIS_HOST}:${AppConfig.REDIS_PORT}...")
        redisManager.connect()
        _uiState.update { it.copy(redisConnected = true) }
        appendLog("Redis ✅ 连接成功")
    }

    fun redisSendCommand() = viewModelScope.launch {
        appendLog("Redis SET iot:test:key → 'hello_from_android'")
        try {
            val result = redisManager.set("iot:test:key", "hello_from_android", 60)
            appendLog("Redis SET 结果: $result")
            val value = redisManager.get("iot:test:key")
            appendLog("Redis GET iot:test:key = $value")
        } catch (e: ProtocolDisabledException) {
            appendLog("Redis ❌ 协议已禁用: ${e.message}")
        } catch (e: Exception) {
            appendLog("Redis ❌ 异常: ${e.message}")
        }
    }

    // ==================== Socket 测试 ====================

    fun connectSocket() = launchWithLoading(title = "正在建立 TCP Socket 工业连接...") {
        appendLog("Socket 正在连接 ${AppConfig.SOCKET_HOST}:${AppConfig.SOCKET_PORT}...")
        socketManager.connect(AppConfig.SOCKET_HOST, AppConfig.SOCKET_PORT)
        appendLog("Socket ✅ 连接成功")
    }

    fun socketPingPong() = viewModelScope.launch {
        appendLog("Socket SEND → PING")
        try {
            socketManager.send("PING")
        } catch (e: ProtocolDisabledException) {
            appendLog("Socket ❌ 协议已禁用: ${e.message}")
        } catch (e: Exception) {
            appendLog("Socket ❌ 发送失败: ${e.message}")
        }
    }

    // ==================== 诊断测试 ====================

    /** 触发超长日志测试 */
    fun triggerLongLog() {
        val longText = (1..50).joinToString(separator = "\n") {
            "[$it] 这是第 $it 行超长日志测试数据，用于验证 Lg 工具的分段打印功能是否正常工作。" +
                    "每行包含足够多的字符以触发分段逻辑。测试数据 = ${System.currentTimeMillis()}"
        }
        Lg.d("LongLogTest", longText)
        appendLog("已触发超长日志测试，请查看 Logcat [LongLogTest]")
    }

    /** 主动触发崩溃（用于测试 CrashHandler） */
    fun triggerCrash() {
        appendLog("⚠️ 即将触发崩溃，请查看 logs/ 目录下的崩溃日志文件")
        viewModelScope.launch {
            delay(500) // 让日志先显示
            throw RuntimeException("手动触发的崩溃 —— IoT Dashboard 崩溃测试")
        }
    }

    /** 分享日志文件 */
    fun shareLogFiles() {
        LogExporter.shareLogFiles(getApplication())
        appendLog("已调用系统分享面板，请选择分享目标")
    }

    // ==================== 沉浸式开关 ====================

    fun toggleImmersive() {
        _uiState.update { it.copy(isImmersive = !it.isImmersive) }
        appendLog("沉浸式模式: ${if (!_uiState.value.isImmersive) "❌ 已关闭" else "✅ 已开启"}")
    }

    // ==================== 工具 ====================

    private fun appendLog(msg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val line = "[$timestamp] $msg"
        Lg.d(TAG, line)
        _uiState.update { state ->
            val logs = (state.terminalLogs + line).takeLast(200) // 最多保留 200 条
            state.copy(terminalLogs = logs)
        }
    }

    fun clearTerminal() {
        _uiState.update { it.copy(terminalLogs = emptyList()) }
    }
}
