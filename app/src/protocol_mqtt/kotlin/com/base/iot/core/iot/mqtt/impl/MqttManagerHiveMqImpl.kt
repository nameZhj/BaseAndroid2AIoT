package com.base.iot.core.iot.mqtt.impl

import com.base.iot.core.config.AppConfig
import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.iot.IotQos
import com.base.iot.core.iot.MqttConnectionState
import com.base.iot.core.iot.MqttManager
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttManagerHiveMqImpl @Inject constructor(
    private val protocolConfig: IotProtocolConfig
) : MqttManager {

    private val TAG = "MqttManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val isCompiled: Boolean get() = true
    override val isEnabled: Boolean get() = _connectionState.value == MqttConnectionState.CONNECTED

    @Volatile
    private var client: Mqtt3AsyncClient? = null

    @Volatile
    private var reconnectJob: Job? = null

    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    override suspend fun connect(
        host: String,
        port: Int,
        useTls: Boolean,
        username: String?,
        password: String?
    ) = withContext(Dispatchers.IO) {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("MQTT", isCompiled, switches.isMqttEnabled)

        if (_connectionState.value == MqttConnectionState.CONNECTED) {
            Lg.d(TAG, "已连接，跳过重复连接")
            return@withContext
        }

        _connectionState.value = MqttConnectionState.CONNECTING
        val clientId = "${AppConfig.MQTT_CLIENT_ID_PREFIX}${UUID.randomUUID().toString().take(8)}"
        Lg.i(TAG, "[MQTT] [1/4 CONNECTING] 正在连接 Broker: $host:$port (useTls=$useTls, clientId=$clientId)")

        try {
            val builder = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                .automaticReconnectWithDefaultConfig()

            if (useTls) builder.sslWithDefaultConfig()

            val mqtt3Client = builder.buildAsync()

            val connAckFuture = if (username != null && password != null) {
                mqtt3Client.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(password.toByteArray())
                    .applySimpleAuth()
                    .keepAlive(AppConfig.MQTT_KEEP_ALIVE_SEC)
                    .send()
            } else {
                mqtt3Client.connectWith()
                    .keepAlive(AppConfig.MQTT_KEEP_ALIVE_SEC)
                    .send()
            }

            connAckFuture.get()
            client = mqtt3Client
            _connectionState.value = MqttConnectionState.CONNECTED
            Lg.i(TAG, "[MQTT] [2/4 CONNECTED] 握手成功！已连接至 $host:$port (clientId=$clientId)")

            startReconnectWatcher(host, port, useTls, username, password)
        } catch (e: Exception) {
            _connectionState.value = MqttConnectionState.ERROR
            Lg.e(TAG, "[MQTT] [4/4 ERROR] 连接异常失败: ${e.message}", e)
            scheduleReconnect(host, port, useTls, username, password)
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            reconnectJob?.cancel()
            Lg.i(TAG, "[MQTT] [4/4 DISCONNECTING] 正在断开连接...")
            try {
                client?.disconnect()?.get()
            } catch (e: Exception) {
                Lg.w(TAG, "[MQTT] [4/4 ERROR] 断开时异常: ${e.message}")
            } finally {
                client = null
                _connectionState.value = MqttConnectionState.DISCONNECTED
                Lg.i(TAG, "[MQTT] [4/4 CLOSED] 连接已安全断开")
            }
        }
    }

    override fun subscribe(topic: String, qos: IotQos): Flow<String> = callbackFlow {
        val switches = protocolConfig.switchesFlow.first()
        if (!switches.isMqttEnabled) {
            close(com.base.iot.core.config.ProtocolDisabledException("MQTT"))
            return@callbackFlow
        }

        val mqttClient = client
        if (mqttClient == null) {
            close(IllegalStateException("MQTT 客户端未连接，请先调用 connect()"))
            return@callbackFlow
        }

        val hiveQos = when (qos) {
            IotQos.AT_MOST_ONCE -> MqttQos.AT_MOST_ONCE
            IotQos.AT_LEAST_ONCE -> MqttQos.AT_LEAST_ONCE
            IotQos.EXACTLY_ONCE -> MqttQos.EXACTLY_ONCE
        }

        Lg.i(TAG, "[MQTT] [3/4 TRANSFER] [SUBSCRIBE] 注册主题监听 → Topic: $topic (QoS: $qos)")

        mqttClient.publishes(MqttGlobalPublishFilter.SUBSCRIBED) { publish: Mqtt3Publish ->
            val payload = publish.payload
                .map { buf -> StandardCharsets.UTF_8.decode(buf).toString() }
                .orElse("")
            // 防 OOM / 刷屏：大报文摘要打印
            val preview = if (payload.length > 256) "${payload.take(256)}... (共${payload.length}字符)" else payload
            Lg.d(TAG, "[MQTT] [3/4 TRANSFER] [MESSAGE] ← Topic: ${publish.topic}, 载荷大小: ${payload.length}B: $preview")
            if (!isClosedForSend) {
                trySend(payload)
            }
        }

        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(hiveQos)
            .send()

        awaitClose {
            Lg.i(TAG, "[MQTT] [3/4 TRANSFER] [UNSUBSCRIBE] 取消主题监听 → Topic: $topic")
            try {
                mqttClient.unsubscribeWith().topicFilter(topic).send()
            } catch (e: Exception) {
                Lg.w(TAG, "[MQTT] 取消订阅异常: ${e.message}")
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun publish(
        topic: String,
        payload: String,
        qos: IotQos,
        retain: Boolean
    ) = withContext(Dispatchers.IO) {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("MQTT", isCompiled, switches.isMqttEnabled)

        val mqttClient = client
            ?: throw IllegalStateException("MQTT 客户端未连接，请先调用 connect()")

        val hiveQos = when (qos) {
            IotQos.AT_MOST_ONCE -> MqttQos.AT_MOST_ONCE
            IotQos.AT_LEAST_ONCE -> MqttQos.AT_LEAST_ONCE
            IotQos.EXACTLY_ONCE -> MqttQos.EXACTLY_ONCE
        }

        val preview = if (payload.length > 256) "${payload.take(256)}... (共${payload.length}字符)" else payload
        Lg.i(TAG, "[MQTT] [3/4 TRANSFER] [PUBLISH] → Topic: $topic, QoS: $qos, Retain: $retain, 载荷大小: ${payload.length}B: $preview")
        mqttClient.publishWith()
            .topic(topic)
            .payload(payload.toByteArray(StandardCharsets.UTF_8))
            .qos(hiveQos)
            .retain(retain)
            .send()
            .get()
        Lg.i(TAG, "[MQTT] [3/4 TRANSFER] [PUBLISH] ✅ 发布确认成功 (Topic: $topic)")
    }

    private fun startReconnectWatcher(
        host: String, port: Int, useTls: Boolean,
        username: String?, password: String?
    ) {
        scope.launch {
            connectionState
                .filter { it == MqttConnectionState.DISCONNECTED || it == MqttConnectionState.ERROR }
                .collect {
                    scheduleReconnect(host, port, useTls, username, password)
                }
        }
    }

    private fun scheduleReconnect(
        host: String, port: Int, useTls: Boolean,
        username: String?, password: String?
    ) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            Lg.i(TAG, "将在 ${AppConfig.MQTT_RECONNECT_DELAY_MS}ms 后重连...")
            delay(AppConfig.MQTT_RECONNECT_DELAY_MS)
            connect(host, port, useTls, username, password)
        }
    }
}
