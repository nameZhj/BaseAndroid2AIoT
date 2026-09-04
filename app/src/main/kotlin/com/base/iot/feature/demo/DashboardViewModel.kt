// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.base.iot.R
import com.base.iot.core.base.BaseViewModel
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.iot.IotHub
import com.base.iot.core.iot.MqttConnectionState
import com.base.iot.core.iot.SocketConnectionState
import com.base.iot.core.storage.CacheLocationManager
import com.base.iot.core.storage.FileShareManager
import com.base.iot.core.ui.dialog.LoadingConfig
import com.base.iot.feature.demo.adapter.IotDeviceItem
import com.base.iot.core.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    val iotHub: IotHub,
    val cacheLocationManager: CacheLocationManager,
    fileShareManager: FileShareManager,
    val themeManager: ThemeManager
) : BaseViewModel<DashboardUiState, DashboardEvent>(
    application = application,
    fileShareManager = fileShareManager,
    initialState = DashboardUiState()
) {
    val fileShare: FileShareManager get() = fileShareManager

    override fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig) {
        _uiState.update { it.copy(loadingConfig = reducer(it.loadingConfig)) }
    }

    override fun updateErrorState(error: ParsedError?, visible: Boolean) {
        _uiState.update { it.copy(parsedError = error, showErrorDialog = visible) }
    }

    fun updateUiState(reducer: (DashboardUiState) -> DashboardUiState) {
        _uiState.update(reducer)
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
                    IotDeviceItem("DEV_ESP32_01", getString(R.string.demo_device_gateway_a1), "MQTT", "ONLINE", "192.168.1.101"),
                    IotDeviceItem("DEV_PLC_02", getString(R.string.demo_device_siemens_plc), "Socket", "ONLINE", "192.168.1.102"),
                    IotDeviceItem("DEV_EDGE_03", getString(R.string.demo_device_edge_server), "HTTP/REST", "ONLINE", "192.168.1.103"),
                    IotDeviceItem("DEV_SENSOR_04", getString(R.string.demo_device_temp_sensor), "MQTT", "OFFLINE", "192.168.1.104")
                )
            )
        }

        viewModelScope.launch {
            iotHub.mqtt.connectionState.collect { state ->
                _uiState.update {
                    it.copy(mqttConnected = state == MqttConnectionState.CONNECTED)
                }
            }
        }

        viewModelScope.launch {
            iotHub.socket.connectionState.collect { state ->
                _uiState.update {
                    it.copy(socketConnected = state == SocketConnectionState.CONNECTED)
                }
            }
        }

        appendLog(getString(R.string.demo_dashboard_ready))
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
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $message"
        _uiState.update {
            it.copy(terminalLogs = (it.terminalLogs + logEntry).takeLast(200))
        }
    }

    fun clearTerminal() {
        _uiState.update { it.copy(terminalLogs = emptyList()) }
    }

    suspend fun refreshCacheStats() {
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
}
