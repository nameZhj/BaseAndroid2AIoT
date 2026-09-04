package com.base.iot.core.iot

import com.base.iot.core.config.AppConfig
import com.base.iot.core.network.HttpManager
import com.base.iot.core.network.HttpResult
import com.base.iot.core.network.get
import com.base.iot.core.network.post
import javax.inject.Inject
import javax.inject.Singleton

private data class PortsDto(val success: Boolean, val ports: List<PortDto>?)
private data class PortDto(val port: String, val description: String, val is_ch341: Boolean = false, val is_active: Boolean = false)
private data class ConnectDto(val success: Boolean, val connected: Boolean = false, val port: String?, val baudrate: Int?, val description: String?)
private data class MessagesDto(val success: Boolean, val messages: List<SerialMessageModel>?)
private data class StatusDto(val success: Boolean, val data: StatusInnerDto?)
private data class StatusInnerDto(val connected: Boolean = false, val port: String? = null, val baudrate: Int? = null, val description: String? = null)

@Singleton
class RemoteHttpSerialDriver @Inject constructor(
    private val http: HttpManager
) {
    suspend fun listPorts(): List<SerialPortModel> {
        val url = AppConfig.baseHttpUrl + AppConfig.SERIAL_API_PORTS
        val res = http.get<PortsDto>(url)
        return if (res is HttpResult.Success && res.data.success) {
            res.data.ports.orEmpty().map { SerialPortModel(it.port, it.description, it.is_ch341, it.is_active) }
        } else emptyList()
    }

    suspend fun autoConnect(baud: Int): Result<SerialStatusModel> {
        val url = AppConfig.baseHttpUrl + AppConfig.SERIAL_API_AUTO_CONNECT
        val res = http.post<ConnectDto>(url, mapOf("baudrate" to baud))
        return if (res is HttpResult.Success && res.data.connected) {
            Result.success(SerialStatusModel(true, res.data.port.orEmpty(), res.data.baudrate ?: baud, res.data.description.orEmpty()))
        } else Result.failure(Exception("Remote auto-connect failed"))
    }

    suspend fun connect(port: String, baud: Int): Result<SerialStatusModel> {
        val url = AppConfig.baseHttpUrl + AppConfig.SERIAL_API_CONNECT
        val res = http.post<ConnectDto>(url, mapOf("port" to port, "baudrate" to baud))
        return if (res is HttpResult.Success && res.data.connected) {
            Result.success(SerialStatusModel(true, port, baud, res.data.description.orEmpty()))
        } else Result.failure(Exception("Remote connect failed"))
    }

    suspend fun disconnect(safetyStop: Boolean): Result<Unit> {
        val url = AppConfig.baseHttpUrl + AppConfig.SERIAL_API_DISCONNECT
        val res = http.post<Map<String, Any>>(url, mapOf("safety_stop" to safetyStop))
        return if (res is HttpResult.Success) Result.success(Unit) else Result.failure(Exception("Disconnect failed"))
    }

    suspend fun getStatus(): SerialStatusModel {
        val url = AppConfig.baseHttpUrl + AppConfig.SERIAL_API_STATUS
        val res = http.get<StatusDto>(url)
        return if (res is HttpResult.Success && res.data.success && res.data.data != null) {
            val d = res.data.data
            SerialStatusModel(d.connected, d.port.orEmpty(), d.baudrate ?: AppConfig.SERIAL_DEFAULT_BAUD, d.description.orEmpty())
        } else SerialStatusModel(false)
    }

    suspend fun sendRaw(target: String, command: String): Result<Unit> {
        val url = AppConfig.baseHttpUrl + AppConfig.SERIAL_API_SEND
        val res = http.post<Map<String, Any>>(url, mapOf("target" to target, "command" to command))
        return if (res is HttpResult.Success) Result.success(Unit) else Result.failure(Exception("Send failed"))
    }

    suspend fun pollRecentMessages(sinceTimestamp: Double): List<SerialMessageModel> {
        val url = "${AppConfig.baseHttpUrl}${AppConfig.SERIAL_API_MESSAGES}?since=$sinceTimestamp"
        val res = http.get<MessagesDto>(url)
        return if (res is HttpResult.Success && res.data.success) res.data.messages.orEmpty() else emptyList()
    }
}
