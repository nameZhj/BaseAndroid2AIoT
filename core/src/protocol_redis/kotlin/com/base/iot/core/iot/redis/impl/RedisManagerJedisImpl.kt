package com.base.iot.core.iot.redis.impl

import com.base.iot.core.config.AppConfig
import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.iot.RedisManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedisManagerJedisImpl @Inject constructor(
    private val protocolConfig: IotProtocolConfig
) : RedisManager {

    private val TAG = "RedisManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val isCompiled: Boolean get() = true
    override val isEnabled: Boolean get() = jedis != null
    override val isConnected: Boolean get() = jedis?.isConnected ?: false

    @Volatile
    private var jedis: Jedis? = null

    @Volatile
    private var heartbeatJob: Job? = null

    @Volatile
    private var currentHost = AppConfig.REDIS_HOST

    @Volatile
    private var currentPort = AppConfig.REDIS_PORT

    @Volatile
    private var currentPassword = AppConfig.REDIS_PASSWORD

    override suspend fun connect(
        host: String,
        port: Int,
        password: String,
        timeoutMs: Int
    ) = withContext(Dispatchers.IO) {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("Redis", isCompiled, switches.isRedisEnabled)

        _connectionState.value = RedisConnectionState.CONNECTING
        currentHost = host
        currentPort = port
        currentPassword = password
        Lg.i(TAG, "[REDIS] [1/4 CONNECTING] 正在连接 Redis 节点: $host:$port (超时设置: ${timeoutMs}ms)")

        try {
            val j = Jedis(host, port, timeoutMs)
            if (password.isNotBlank()) j.auth(password)
            val pong = j.ping()
            jedis = j
            _connectionState.value = RedisConnectionState.CONNECTED
            Lg.i(TAG, "[REDIS] [2/4 CONNECTED] 握手与认证成功！PING -> $pong，节点就绪: $host:$port")
            startHeartbeat()
        } catch (e: Exception) {
            _connectionState.value = RedisConnectionState.ERROR
            Lg.e(TAG, "[REDIS] [4/4 ERROR] 连接失败: ${e.message}", e)
            scheduleReconnect(host, port, password, timeoutMs)
        }
    }

    override fun disconnect() {
        heartbeatJob?.cancel()
        Lg.i(TAG, "[REDIS] [4/4 DISCONNECTING] 正在关闭 Redis 客户端...")
        try {
            jedis?.close()
        } catch (e: Exception) {
            Lg.w(TAG, "[REDIS] [4/4 ERROR] 关闭异常: ${e.message}")
        } finally {
            jedis = null
            _connectionState.value = RedisConnectionState.DISCONNECTED
            Lg.i(TAG, "[REDIS] [4/4 CLOSED] Redis 连接已安全断开")
        }
    }

    override suspend fun set(key: String, value: String, expireSeconds: Long): String? =
        withContext(Dispatchers.IO) {
            val switches = protocolConfig.switchesFlow.first()
            protocolConfig.assertEnabled("Redis", isCompiled, switches.isRedisEnabled)
            val valPreview = if (value.length > 128) "${value.take(128)}...(${value.length}B)" else value
            Lg.d(TAG, "[REDIS] [3/4 TRANSFER] SET key=$key, value=$valPreview, expire=${expireSeconds}s")
            safeExecute("SET") {
                val result = jedis!!.set(key, value)
                if (expireSeconds > 0) jedis!!.expire(key, expireSeconds)
                result
            }
        }

    override suspend fun get(key: String): String? =
        withContext(Dispatchers.IO) {
            val switches = protocolConfig.switchesFlow.first()
            protocolConfig.assertEnabled("Redis", isCompiled, switches.isRedisEnabled)
            Lg.d(TAG, "[REDIS] [3/4 TRANSFER] GET key=$key")
            safeExecute("GET") { jedis!!.get(key) }
        }

    override suspend fun del(vararg keys: String): Long? =
        withContext(Dispatchers.IO) {
            val switches = protocolConfig.switchesFlow.first()
            protocolConfig.assertEnabled("Redis", isCompiled, switches.isRedisEnabled)
            Lg.d(TAG, "[REDIS] [3/4 TRANSFER] DEL keys=${keys.joinToString()}")
            safeExecute("DEL") { jedis!!.del(*keys) }
        }

    override suspend fun exists(key: String): Boolean =
        withContext(Dispatchers.IO) {
            val switches = protocolConfig.switchesFlow.first()
            protocolConfig.assertEnabled("Redis", isCompiled, switches.isRedisEnabled)
            Lg.d(TAG, "[REDIS] [3/4 TRANSFER] EXISTS key=$key")
            safeExecute("EXISTS") { jedis!!.exists(key) } ?: false
        }

    override suspend fun publish(channel: String, message: String): Long? =
        withContext(Dispatchers.IO) {
            val switches = protocolConfig.switchesFlow.first()
            protocolConfig.assertEnabled("Redis", isCompiled, switches.isRedisEnabled)
            val preview = if (message.length > 128) "${message.take(128)}...(${message.length}B)" else message
            Lg.i(TAG, "[REDIS] [3/4 TRANSFER] PUBLISH channel=$channel, message=$preview")
            safeExecute("PUBLISH") {
                jedis!!.publish(channel, message)
            }
        }

    override fun subscribe(channel: String, onMessage: (String, String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val subscriber = object : JedisPubSub() {
                override fun onMessage(ch: String, msg: String) {
                    val preview = if (msg.length > 128) "${msg.take(128)}...(${msg.length}B)" else msg
                    Lg.d(TAG, "[REDIS] [3/4 TRANSFER] [MSG] ← channel=$ch: $preview")
                    onMessage(ch, msg)
                }
            }
            try {
                val subJedis = Jedis(currentHost, currentPort, AppConfig.REDIS_TIMEOUT_MS)
                if (currentPassword.isNotBlank()) subJedis.auth(currentPassword)
                Lg.i(TAG, "[REDIS] [3/4 TRANSFER] 注册监听 Channel: $channel")
                subJedis.subscribe(subscriber, channel)
                subJedis.close()
            } catch (e: Exception) {
                Lg.e(TAG, "[REDIS] 订阅异常: ${e.message}", e)
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(AppConfig.REDIS_HEARTBEAT_INTERVAL_MS)
                try {
                    val pong = jedis?.ping()
                    Lg.v(TAG, "[REDIS] 心跳 PING → $pong")
                } catch (e: Exception) {
                    Lg.w(TAG, "[REDIS] 心跳检测失败: ${e.message}")
                    jedis = null
                }
            }
        }
    }

    private fun scheduleReconnect(
        host: String, port: Int, password: String, timeoutMs: Int
    ) {
        scope.launch {
            delay(AppConfig.MQTT_RECONNECT_DELAY_MS)
            connect(host, port, password, timeoutMs)
        }
    }

    private fun <T> safeExecute(op: String, block: () -> T): T? {
        return try {
            val res = block()
            Lg.d(TAG, "[REDIS] [3/4 TRANSFER] [$op] 执行成功")
            res
        } catch (e: Exception) {
            Lg.e(TAG, "[REDIS] [$op] 异常: ${e.message}", e)
            null
        }
    }
}
