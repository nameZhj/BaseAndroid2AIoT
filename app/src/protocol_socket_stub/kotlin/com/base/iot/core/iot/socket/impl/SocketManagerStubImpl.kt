package com.base.iot.core.iot.socket.impl

import com.base.iot.core.config.ProtocolDisabledException
import com.base.iot.core.iot.SocketConnectionState
import com.base.iot.core.iot.SocketManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 当 Socket 在编译期被关闭时使用的轻量 Stub 占位实现。
 */
@Singleton
class SocketManagerStubImpl @Inject constructor() : SocketManager {
    override val isCompiled: Boolean get() = false
    override val isEnabled: Boolean get() = false

    private val _connectionState = MutableStateFlow(SocketConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<SocketConnectionState> = _connectionState.asStateFlow()

    private val _incomingData = MutableSharedFlow<String>()
    override val incomingData: SharedFlow<String> = _incomingData.asSharedFlow()

    override suspend fun connect(host: String, port: Int) {
        throw ProtocolDisabledException(
            "Socket",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override fun disconnect() {
        // no-op
    }

    override suspend fun send(data: String) {
        throw ProtocolDisabledException(
            "Socket",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun sendBytes(data: ByteArray) {
        throw ProtocolDisabledException(
            "Socket",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override fun receive(): Flow<String> = flow {
        throw ProtocolDisabledException(
            "Socket",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }
}
