// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import androidx.lifecycle.viewModelScope
import com.base.iot.R
import com.base.iot.core.config.AppConfig
import com.base.iot.core.config.ProtocolDisabledException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

fun DashboardViewModel.connectMqtt() = launchIotOperation(getString(R.string.op_mqtt_connect)) {
    appendLog(getString(R.string.demo_mqtt_connecting, AppConfig.MQTT_HOST, AppConfig.MQTT_PORT))
    iotHub.mqtt.connect()
    appendLog(getString(R.string.demo_mqtt_connected))
}

fun DashboardViewModel.mqttPublish() = viewModelScope.launch {
    val topic = DemoConfig.DEMO_MQTT_PUB_TOPIC
    val payload = """{"msg":"hello","ts":${System.currentTimeMillis()}}"""
    appendLog(getString(R.string.demo_mqtt_publish_log, topic, payload))
    try {
        iotHub.mqtt.publish(topic, payload)
        appendLog(getString(R.string.demo_mqtt_publish_success))
    } catch (e: ProtocolDisabledException) {
        appendLog(getString(R.string.demo_protocol_disabled_log, "MQTT", e.message ?: ""))
    } catch (e: Exception) {
        appendLog(getString(R.string.demo_mqtt_publish_failed, e.message ?: ""))
    }
}

fun DashboardViewModel.mqttSubscribeTest() = viewModelScope.launch {
    val topic = DemoConfig.DEMO_MQTT_SUB_TOPIC
    appendLog(getString(R.string.demo_mqtt_subscribe_log, topic))
    try {
        iotHub.mqtt.subscribe(topic)
            .take(3)
            .onEach { msg -> appendLog(getString(R.string.demo_mqtt_receive_data, msg)) }
            .catch { e -> appendLog(getString(R.string.demo_mqtt_subscribe_error, e.message ?: "")) }
            .launchIn(this)
    } catch (e: ProtocolDisabledException) {
        appendLog(getString(R.string.demo_protocol_disabled_log, "MQTT", e.message ?: ""))
    }
}

fun DashboardViewModel.connectRedis() = launchIotOperation(getString(R.string.op_redis_connect)) {
    appendLog(getString(R.string.demo_redis_connecting, AppConfig.REDIS_HOST, AppConfig.REDIS_PORT))
    iotHub.redis.connect()
    updateUiState { it.copy(redisConnected = true) }
    appendLog(getString(R.string.demo_redis_connected))
}

fun DashboardViewModel.redisSendCommand() = viewModelScope.launch {
    appendLog(getString(R.string.demo_redis_set_log, DemoConfig.DEMO_REDIS_KEY, "hello_from_android"))
    try {
        val result = iotHub.redis.set(DemoConfig.DEMO_REDIS_KEY, "hello_from_android", 60)
        appendLog(getString(R.string.demo_redis_set_result, result.toString()))
        val value = iotHub.redis.get(DemoConfig.DEMO_REDIS_KEY)
        appendLog(getString(R.string.demo_redis_get_result, value.toString()))
    } catch (e: ProtocolDisabledException) {
        appendLog(getString(R.string.demo_protocol_disabled_log, "Redis", e.message ?: ""))
    } catch (e: Exception) {
        appendLog(getString(R.string.demo_redis_op_error, e.message ?: ""))
    }
}

fun DashboardViewModel.connectSocket() = launchIotOperation(getString(R.string.op_socket_connect)) {
    appendLog(getString(R.string.demo_socket_connecting, AppConfig.SOCKET_HOST, AppConfig.SOCKET_PORT))
    iotHub.socket.connect(AppConfig.SOCKET_HOST, AppConfig.SOCKET_PORT)
    appendLog(getString(R.string.demo_socket_connected))
}

fun DashboardViewModel.socketPingPong() = viewModelScope.launch {
    appendLog(getString(R.string.demo_socket_send_log, AppConfig.SOCKET_HEARTBEAT_PAYLOAD))
    try {
        iotHub.socket.send(AppConfig.SOCKET_HEARTBEAT_PAYLOAD)
    } catch (e: ProtocolDisabledException) {
        appendLog(getString(R.string.demo_protocol_disabled_log, "Socket", e.message ?: ""))
    } catch (e: Exception) {
        appendLog(getString(R.string.demo_socket_send_failed, e.message ?: ""))
    }
}
