package com.base.iot.core.config

/**
 * 全局 App 配置常量（单一权威源）。
 * 严禁在 ViewModel / Repository 中散落硬编码 URL、IP、Port 或超时数值。
 */
object AppConfig {

    enum class Env { DEV, STAGING, PRODUCTION }

    var currentEnv: Env = Env.DEV

    val baseHttpUrl: String
        get() = when (currentEnv) {
            Env.DEV -> "http://192.168.1.100:8080/"
            Env.STAGING -> "http://staging.example.com/"
            Env.PRODUCTION -> "https://api.example.com/"
        }

    const val HTTP_CONNECT_TIMEOUT_SEC = 15L
    const val HTTP_READ_TIMEOUT_SEC = 30L
    const val HTTP_WRITE_TIMEOUT_SEC = 30L
    /** 大文件上传/下载独立超时通道：1 小时 */
    const val HTTP_FILE_TRANSFER_TIMEOUT_SEC = 3600L


    const val MQTT_HOST = "broker.hivemq.com"
    const val MQTT_PORT = 1883
    const val MQTT_CLIENT_ID_PREFIX = "android_iot_"
    const val MQTT_KEEP_ALIVE_SEC = 60
    const val MQTT_RECONNECT_DELAY_MS = 3000L

    const val REDIS_HOST = "192.168.1.200"
    const val REDIS_PORT = 6379
    const val REDIS_PASSWORD = ""
    const val REDIS_TIMEOUT_MS = 3000
    const val REDIS_HEARTBEAT_INTERVAL_MS = 30_000L

    const val SOCKET_HOST = "192.168.1.201"
    const val SOCKET_PORT = 9000
    const val SOCKET_CONNECT_TIMEOUT_MS = 5000
    const val SOCKET_RECONNECT_DELAY_MS = 3000L
    const val SOCKET_HEARTBEAT_INTERVAL_MS = 20_000L
    const val SOCKET_HEARTBEAT_PAYLOAD = "PING"

    const val LOG_MAX_LENGTH = 4000
    const val LOG_DIR_NAME = "logs"
    const val LOG_FILE_PREFIX = "crash_"
    const val LOG_FILE_SUFFIX = ".log"
}
