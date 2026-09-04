package com.base.iot.core.iot

import com.base.iot.core.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SerialManagerImpl @Inject constructor(
    private val usbDriver: AndroidUsbSerialDriver,
    private val httpDriver: RemoteHttpSerialDriver
) : SerialManager {

    private var isUsingLocalUsb = false

    override suspend fun listPorts(): List<SerialPortModel> {
        val usbPorts = usbDriver.listPorts()
        if (usbPorts.isNotEmpty()) return usbPorts
        return httpDriver.listPorts()
    }

    override suspend fun autoConnect(baudrate: Int): Result<SerialStatusModel> {
        val usbPorts = usbDriver.listPorts()
        if (usbPorts.isNotEmpty()) {
            val target = usbPorts.firstOrNull { it.isCh341 } ?: usbPorts.first()
            val res = usbDriver.open(target.port, baudrate)
            if (res.isSuccess) {
                isUsingLocalUsb = true
                return res
            }
        }
        val remoteRes = httpDriver.autoConnect(baudrate)
        if (remoteRes.isSuccess) isUsingLocalUsb = false
        return remoteRes
    }

    override suspend fun connect(port: String, baudrate: Int): Result<SerialStatusModel> {
        val usbPorts = usbDriver.listPorts()
        val isLocal = usbPorts.any { it.port == port } || (usbPorts.isNotEmpty() && !port.startsWith("http"))
        if (isLocal) {
            val res = usbDriver.open(port, baudrate)
            if (res.isSuccess) {
                isUsingLocalUsb = true
                return res
            }
        }
        val remoteRes = httpDriver.connect(port, baudrate)
        if (remoteRes.isSuccess) isUsingLocalUsb = false
        return remoteRes
    }

    override suspend fun disconnect(safetyStop: Boolean): Result<Unit> {
        if (isUsingLocalUsb) {
            usbDriver.close()
            isUsingLocalUsb = false
            return Result.success(Unit)
        }
        return httpDriver.disconnect(safetyStop)
    }

    override suspend fun getStatus(): SerialStatusModel {
        if (isUsingLocalUsb || usbDriver.getStatus().isConnected) {
            return usbDriver.getStatus()
        }
        return httpDriver.getStatus()
    }

    override suspend fun sendRaw(target: String, command: String): Result<Unit> {
        if (isUsingLocalUsb || usbDriver.getStatus().isConnected) {
            val packet = if (target.isNotEmpty() && !command.contains("|")) "$target|$command" else command
            return usbDriver.write(packet)
        }
        return httpDriver.sendRaw(target, command)
    }

    override suspend fun pollRecentMessages(sinceTimestamp: Double): List<SerialMessageModel> {
        if (isUsingLocalUsb || usbDriver.getStatus().isConnected) {
            return usbDriver.pollMessages(sinceTimestamp)
        }
        return httpDriver.pollRecentMessages(sinceTimestamp)
    }

    override fun registerDeviceListener(onAttached: () -> Unit, onDetached: () -> Unit) {
        usbDriver.onDeviceAttached = onAttached
        usbDriver.onDeviceDetached = onDetached
    }

    override fun notifyDeviceAttached() {
        usbDriver.notifyDeviceAttached()
    }
}
