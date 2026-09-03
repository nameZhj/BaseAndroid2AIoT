package com.base.iot.core.iot

import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.network.HttpManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 物联网多协议统一调度门面 (IotHub)。
 *
 * 聚合系统支持的四大通信协议与配置，使 Agent 或开发者在开发新业务 Feature 时，
 * 仅需注入一个 IotHub 对象，即可调动所有通信能力，减少注入样板代码：
 * - iotHub.http
 * - iotHub.mqtt
 * - iotHub.redis
 * - iotHub.socket
 * - iotHub.config
 */
@Singleton
class IotHub @Inject constructor(
    val http: HttpManager,
    val mqtt: MqttManager,
    val redis: RedisManager,
    val socket: SocketManager,
    val config: IotProtocolConfig
)
