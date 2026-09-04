package com.base.iot.core.iot

import android.content.Context
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.base.iot.core.config.AppConfig
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AndroidUsbSerialDriver @Inject constructor(
    @ApplicationContext private val context: Context
) : SerialInputOutputManager.Listener {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
    private var activePort: UsbSerialPort? = null
    private var activeConnection: UsbDeviceConnection? = null
    private var ioManager: SerialInputOutputManager? = null
    private var activePortName: String = ""
    private var activeBaudrate: Int = AppConfig.SERIAL_DEFAULT_BAUD

    private val recentMessages = CopyOnWriteArrayList<SerialMessageModel>()
    private val lineBuffer = StringBuilder()

    var onDeviceDetached: (() -> Unit)? = null
    var onDeviceAttached: (() -> Unit)? = null

    private val receiverHelper = UsbReceiverHelper(
        context = context,
        usbManager = usbManager,
        onAttached = { onDeviceAttached?.invoke() },
        onDetached = { devName ->
            if (devName == activePortName) {
                close()
                onDeviceDetached?.invoke()
            }
        }
    )

    fun listPorts(): List<SerialPortModel> {
        val manager = usbManager ?: return emptyList()
        val devices = manager.deviceList.values
        if (devices.isEmpty()) return emptyList()
        return devices.map { UsbDeviceProber.toModel(it, activePortName, activePort != null) }
    }

    suspend fun open(portName: String, baudrate: Int): Result<SerialStatusModel> = withContext(Dispatchers.IO) {
        val manager = usbManager ?: return@withContext Result.failure(Exception("UsbManager unavailable"))
        val device = manager.deviceList.values.find { it.deviceName == portName }
            ?: manager.deviceList.values.firstOrNull()
            ?: return@withContext Result.failure(Exception("USB device not found"))

        if (!manager.hasPermission(device)) {
            val granted = receiverHelper.requestPermission(device)
            if (!granted) return@withContext Result.failure(Exception("USB permission denied"))
        }

        close()
        val driver = UsbDeviceProber.findDriver(device) ?: return@withContext Result.failure(Exception("No driver for ${device.deviceName}"))
        val connection = manager.openDevice(device) ?: return@withContext Result.failure(Exception("Cannot open connection"))
        val port = driver.ports.firstOrNull() ?: return@withContext Result.failure(Exception("No ports on driver"))

        try {
            port.open(connection)
            port.setParameters(baudrate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            try { port.dtr = true; port.rts = true } catch (_: Exception) {}

            activePort = port
            activeConnection = connection
            activePortName = device.deviceName
            activeBaudrate = baudrate

            ioManager = SerialInputOutputManager(port, this@AndroidUsbSerialDriver).apply {
                Executors.newSingleThreadExecutor().submit(this)
            }
            val isCh341 = (device.vendorId == 0x1A86)
            val desc = if (isCh341) "CH340/CH341" else "USB-Serial"
            Result.success(SerialStatusModel(true, device.deviceName, baudrate, desc))
        } catch (e: Exception) {
            close()
            Result.failure(e)
        }
    }

    fun close() {
        try { ioManager?.listener = null; ioManager?.stop(); activePort?.close(); activeConnection?.close() } catch (_: Exception) {}
        activePort = null
        activeConnection = null
        ioManager = null
        activePortName = ""
    }

    fun notifyDeviceAttached() {
        onDeviceAttached?.invoke()
    }

    fun getStatus(): SerialStatusModel {
        val connected = (activePort != null)
        return SerialStatusModel(connected, activePortName, activeBaudrate, if (connected) "Connected" else "")
    }

    suspend fun write(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        val port = activePort ?: return@withContext Result.failure(Exception("Port not connected"))
        try {
            val payload = if (text.endsWith("\n")) text else "$text\n"
            val bytes = payload.toByteArray(StandardCharsets.UTF_8)
            port.write(bytes, 1000)
            addMessage("tx", text.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun pollMessages(sinceTs: Double): List<SerialMessageModel> = recentMessages.filter { it.timestamp > sinceTs }

    override fun onNewData(data: ByteArray?) {
        if (data == null || data.isEmpty()) return
        val str = String(data, StandardCharsets.UTF_8)
        synchronized(lineBuffer) {
            lineBuffer.append(str)
            while (lineBuffer.contains("\n")) {
                val idx = lineBuffer.indexOf("\n")
                val line = lineBuffer.substring(0, idx).trimEnd('\r')
                lineBuffer.delete(0, idx + 1)
                if (line.isNotEmpty()) addMessage("rx", line)
            }
        }
    }

    override fun onRunError(e: Exception?) {
        close()
        onDeviceDetached?.invoke()
    }

    private fun addMessage(type: String, text: String) {
        val ts = System.currentTimeMillis() / 1000.0
        recentMessages.add(SerialMessageModel(type, text, ts))
        if (recentMessages.size > 200) recentMessages.removeAt(0)
    }
}
