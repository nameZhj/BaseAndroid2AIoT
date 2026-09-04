package com.base.iot.core.iot

import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.network.HttpManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IotHub @Inject constructor(
    val http: HttpManager,
    val mqtt: MqttManager,
    val redis: RedisManager,
    val socket: SocketManager,
    val serial: SerialManager,
    val config: IotProtocolConfig
)
