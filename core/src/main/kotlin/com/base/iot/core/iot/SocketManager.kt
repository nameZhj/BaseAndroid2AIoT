package com.base.iot.core.iot

import com.base.iot.core.config.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Socket 连接状态
 */
enum class SocketConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

/**
 * TCP Socket 长连接客户端接口。
 */
interface SocketManager {
    /** 编译期是否编译打包了该协议框架 */
    val isCompiled: Boolean

    /** 运行时协议是否处于开启状态 */
    val isEnabled: Boolean

    /** 连接状态流 */
    val connectionState: StateFlow<SocketConnectionState>

    /** 接收数据热流 */
    val incomingData: SharedFlow<String>

    /** 建立连接 */
    suspend fun connect(
        host: String = AppConfig.SOCKET_HOST,
        port: Int = AppConfig.SOCKET_PORT
    )

    /** 断开连接 */
    fun disconnect()

    /** 发送字符串 */
    suspend fun send(data: String)

    /** 发送字节数组 */
    suspend fun sendBytes(data: ByteArray)

    /** 接收数据流 */
    fun receive(): Flow<String>
}
