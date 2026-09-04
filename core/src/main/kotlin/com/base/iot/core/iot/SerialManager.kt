package com.base.iot.core.iot

import com.base.iot.core.config.AppConfig

data class SerialPortModel(
    val port: String,
    val description: String,
    val isCh341: Boolean = false,
    val isActive: Boolean = false
)

data class SerialStatusModel(
    val isConnected: Boolean,
    val port: String = "",
    val baudrate: Int = AppConfig.SERIAL_DEFAULT_BAUD,
    val description: String = ""
)

data class SerialMessageModel(
    val type: String,
    val text: String,
    val timestamp: Double
)

interface SerialManager {
    suspend fun listPorts(): List<SerialPortModel>
    suspend fun autoConnect(baudrate: Int = AppConfig.SERIAL_DEFAULT_BAUD): Result<SerialStatusModel>
    suspend fun connect(port: String, baudrate: Int = AppConfig.SERIAL_DEFAULT_BAUD): Result<SerialStatusModel>
    suspend fun disconnect(safetyStop: Boolean = false): Result<Unit>
    suspend fun getStatus(): SerialStatusModel
    suspend fun sendRaw(target: String, command: String): Result<Unit>
    suspend fun pollRecentMessages(sinceTimestamp: Double): List<SerialMessageModel>
    fun registerDeviceListener(onAttached: () -> Unit, onDetached: () -> Unit)
    fun notifyDeviceAttached()
}
