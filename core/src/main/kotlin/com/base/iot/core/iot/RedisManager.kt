package com.base.iot.core.iot

import com.base.iot.core.config.AppConfig

/**
 * Redis 远控客户端接口。
 * 当编译期开关开启时，由 Jedis 驱动；
 * 当编译期开关关闭时，Jedis 及其底层依赖完全不编译进 APK。
 */
interface RedisManager {
    /** 编译期是否编译打包了该协议框架 */
    val isCompiled: Boolean

    /** 运行时协议是否处于开启状态 */
    val isEnabled: Boolean

    /** 当前是否已连接 */
    val isConnected: Boolean

    /** 建立连接 */
    suspend fun connect(
        host: String = AppConfig.REDIS_HOST,
        port: Int = AppConfig.REDIS_PORT,
        password: String = AppConfig.REDIS_PASSWORD,
        timeoutMs: Int = AppConfig.REDIS_TIMEOUT_MS
    )

    /** 断开连接 */
    fun disconnect()

    /** 写入键值对 */
    suspend fun set(key: String, value: String, expireSeconds: Long = -1): String?

    /** 读取键值 */
    suspend fun get(key: String): String?

    /** 删除键 */
    suspend fun del(vararg keys: String): Long?

    /** 检查键是否存在 */
    suspend fun exists(key: String): Boolean

    /** 发布消息 */
    suspend fun publish(channel: String, message: String): Long?

    /** 订阅频道 */
    suspend fun subscribe(channel: String, onMessage: (channel: String, message: String) -> Unit)
}
