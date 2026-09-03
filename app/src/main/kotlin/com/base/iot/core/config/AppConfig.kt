package com.base.iot.core.config

/**
 * 全局 App 配置常量。
 * 在正式项目中，运行时可通过 DataStore 或远程下发覆盖。
 */
object AppConfig {

    // ==================== 环境切换 ====================
    /**
     * 当前运行环境
     */
    enum class Env { DEV, STAGING, PRODUCTION }

    var currentEnv: Env = Env.DEV

    val baseHttpUrl: String
        get() = when (currentEnv) {
            Env.DEV -> "http://192.168.1.100:8080/"
            Env.STAGING -> "http://staging.example.com/"
            Env.PRODUCTION -> "https://api.example.com/"
        }

    // ==================== HTTP 配置 ====================
    const val HTTP_CONNECT_TIMEOUT_SEC = 15L
    const val HTTP_READ_TIMEOUT_SEC = 30L
    const val HTTP_WRITE_TIMEOUT_SEC = 30L
    const val HTTP_FILE_TRANSFER_TIMEOUT_SEC = 3600L // 大文件上传下载超时：1小时 (3600秒)

    // ==================== 演示与测试端点 (收拢防硬编码) ====================
    const val DEMO_HTTP_GET_URL = "https://httpbin.org/get"
    const val DEMO_HTTP_POST_URL = "https://httpbin.org/post"
    const val DEMO_DOWNLOAD_URL = "http://speedtest.tele2.net/100KB.zip"
    const val DEMO_MQTT_PUB_TOPIC = "iot/device/telemetry"
    const val DEMO_MQTT_SUB_TOPIC = "iot/device/cmd"
    const val DEMO_REDIS_KEY = "device:status"

    // ==================== MQTT 配置 ====================
    const val MQTT_HOST = "broker.hivemq.com"
    const val MQTT_PORT = 1883
    const val MQTT_CLIENT_ID_PREFIX = "android_iot_"
    const val MQTT_KEEP_ALIVE_SEC = 60
    const val MQTT_RECONNECT_DELAY_MS = 3000L

    // ==================== Redis 配置 ====================
    const val REDIS_HOST = "192.168.1.200"
    const val REDIS_PORT = 6379
    const val REDIS_PASSWORD = ""            // 为空则不鉴权
    const val REDIS_TIMEOUT_MS = 3000
    const val REDIS_HEARTBEAT_INTERVAL_MS = 30_000L

    // ==================== Socket 配置 ====================
    const val SOCKET_HOST = "192.168.1.201"
    const val SOCKET_PORT = 9000
    const val SOCKET_CONNECT_TIMEOUT_MS = 5000
    const val SOCKET_RECONNECT_DELAY_MS = 3000L
    const val SOCKET_HEARTBEAT_INTERVAL_MS = 20_000L
    const val SOCKET_HEARTBEAT_PAYLOAD = "PING"

    // ==================== 日志配置 ====================
    const val LOG_MAX_LENGTH = 4000
    const val LOG_DIR_NAME = "logs"
    const val LOG_FILE_PREFIX = "crash_"
    const val LOG_FILE_SUFFIX = ".log"
}
