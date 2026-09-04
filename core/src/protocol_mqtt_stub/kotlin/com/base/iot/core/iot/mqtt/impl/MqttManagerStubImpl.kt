package com.base.iot.core.iot.mqtt.impl

import com.base.iot.core.config.ProtocolDisabledException
import com.base.iot.core.iot.IotQos
import com.base.iot.core.iot.MqttConnectionState
import com.base.iot.core.iot.MqttManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 当 MQTT 在编译期被关闭时使用的轻量 Stub 占位实现。
 * 完全不依赖 HiveMQ 与 Netty，彻底避免将相关框架打入 APK。
 */
@Singleton
class MqttManagerStubImpl @Inject constructor() : MqttManager {
    override val isCompiled: Boolean get() = false
    override val isEnabled: Boolean get() = false

    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    override suspend fun connect(
        host: String,
        port: Int,
        useTls: Boolean,
        username: String?,
        password: String?
    ) {
        throw ProtocolDisabledException(
            "MQTT",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun disconnect() {
        // no-op
    }

    override fun subscribe(topic: String, qos: IotQos): Flow<String> = flow {
        throw ProtocolDisabledException(
            "MQTT",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun publish(
        topic: String,
        payload: String,
        qos: IotQos,
        retain: Boolean
    ) {
        throw ProtocolDisabledException(
            "MQTT",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }
}
