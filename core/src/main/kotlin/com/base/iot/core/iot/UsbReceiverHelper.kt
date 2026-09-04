package com.base.iot.core.iot

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

class UsbReceiverHelper(
    private val context: Context,
    private val usbManager: UsbManager?,
    private val onAttached: () -> Unit,
    private val onDetached: (String) -> Unit
) {
    val actionPermission = "${context.packageName}.USB_PERMISSION"
    private var permissionDeferred: CompletableDeferred<Boolean>? = null

    init {
        register()
    }

    suspend fun requestPermission(device: UsbDevice): Boolean {
        val manager = usbManager ?: return false
        val deferred = CompletableDeferred<Boolean>()
        permissionDeferred = deferred
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pi = PendingIntent.getBroadcast(context, 0, Intent(actionPermission).setPackage(context.packageName), flags)
        manager.requestPermission(device, pi)
        val result = withTimeoutOrNull(15000L) { deferred.await() } ?: false
        permissionDeferred = null
        return result
    }

    private fun register() {
        val filter = IntentFilter().apply {
            addAction(actionPermission)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                when (intent.action) {
                    actionPermission -> {
                        val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        permissionDeferred?.complete(granted)
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        val dev = getDevice(intent)
                        if (dev != null) onDetached(dev.deviceName)
                    }
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> onAttached()
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun getDevice(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }
}
