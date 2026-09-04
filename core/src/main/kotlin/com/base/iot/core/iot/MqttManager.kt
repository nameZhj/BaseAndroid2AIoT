package com.base.iot.core.iot

import com.base.iot.core.config.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * MQTT 连接状态
 */
enum class MqttConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

/**
 * 通用 QoS 等级，解耦 HiveMQ 第三方枚举
 */
enum class IotQos {
    AT_MOST_ONCE,
    AT_LEAST_ONCE,
    EXACTLY_ONCE
}

/**
 * MQTT 通信管理器接口。
 * 当编译期开关开启时，由 HiveMQ MqttClient 驱动；
 * 当编译期开关关闭时，HiveMQ 及 Netty 整个框架（约 15MB）不参与编译打包。
 */
interface MqttManager {
    /** 编译期是否编译打包了该协议框架 */
    val isCompiled: Boolean

    /** 运行时协议是否处于开启状态 */
    val isEnabled: Boolean

    /** 连接状态响应式流 */
    val connectionState: StateFlow<MqttConnectionState>

    /** 建立连接 */
    suspend fun connect(
        host: String = AppConfig.MQTT_HOST,
        port: Int = AppConfig.MQTT_PORT,
        useTls: Boolean = false,
        username: String? = null,
        password: String? = null
    )

    /** 断开连接 */
    suspend fun disconnect()

    /** 订阅主题，返回 Flow 消息流 */
    fun subscribe(
        topic: String,
        qos: IotQos = IotQos.AT_LEAST_ONCE
    ): Flow<String>

    /** 发布消息 */
    suspend fun publish(
        topic: String,
        payload: String,
        qos: IotQos = IotQos.AT_LEAST_ONCE,
        retain: Boolean = false
    )
}
