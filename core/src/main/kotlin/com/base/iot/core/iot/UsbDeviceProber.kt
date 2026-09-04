package com.base.iot.core.iot

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.ProlificSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber

object UsbDeviceProber {

    private val defaultProber = UsbSerialProber.getDefaultProber()
    private val customProber = UsbSerialProber(ProbeTable().apply {
        addProduct(0x1a86, 0x7523, Ch34xSerialDriver::class.java)
        addProduct(0x1a86, 0x5523, Ch34xSerialDriver::class.java)
        addProduct(0x1a86, 0x7522, Ch34xSerialDriver::class.java)
        addProduct(0x1a86, 0x5512, Ch34xSerialDriver::class.java)
        addProduct(0x1a86, 0xe523, Ch34xSerialDriver::class.java)
        addProduct(0x10c4, 0xea60, Cp21xxSerialDriver::class.java)
        addProduct(0x10c4, 0xea70, Cp21xxSerialDriver::class.java)
        addProduct(0x0403, 0x6001, FtdiSerialDriver::class.java)
        addProduct(0x0403, 0x6010, FtdiSerialDriver::class.java)
        addProduct(0x067b, 0x2303, ProlificSerialDriver::class.java)
    })

    fun findDriver(device: UsbDevice): UsbSerialDriver? {
        return defaultProber.probeDevice(device)
            ?: customProber.probeDevice(device)
            ?: if (device.deviceClass == 2 || (device.interfaceCount > 0 && device.getInterface(0).interfaceClass == 2)) {
                CdcAcmSerialDriver(device)
            } else null
    }

    fun toModel(device: UsbDevice, activePortName: String, hasActive: Boolean): SerialPortModel {
        val isCh341 = (device.vendorId == 0x1A86)
        val driver = findDriver(device)
        val chipName = when {
            isCh341 -> "CH340/CH341"
            device.vendorId == 0x10C4 -> "CP210x"
            device.vendorId == 0x0403 -> "FTDI"
            device.vendorId == 0x067B -> "PL2303"
            else -> driver?.javaClass?.simpleName?.removeSuffix("SerialDriver") ?: "USB-Serial"
        }
        val vidHex = String.format("%04X", device.vendorId)
        val pidHex = String.format("%04X", device.productId)
        val rawName = try {
            device.productName ?: device.manufacturerName
        } catch (_: Exception) {
            null
        }
        val desc = "$chipName (${rawName ?: "VID:$vidHex PID:$pidHex"})"
        val portKey = device.deviceName
        return SerialPortModel(portKey, desc, isCh341, hasActive && activePortName == portKey)
    }
}
