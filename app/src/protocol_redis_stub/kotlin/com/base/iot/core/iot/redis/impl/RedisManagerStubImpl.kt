package com.base.iot.core.iot.redis.impl

import com.base.iot.core.config.ProtocolDisabledException
import com.base.iot.core.iot.RedisManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 当 Redis 在编译期被关闭时使用的轻量 Stub 占位实现。
 * 完全不依赖 Jedis 与 Apache Commons Pool，彻底避免将相关框架打入 APK。
 */
@Singleton
class RedisManagerStubImpl @Inject constructor() : RedisManager {
    override val isCompiled: Boolean get() = false
    override val isEnabled: Boolean get() = false
    override val isConnected: Boolean get() = false

    override suspend fun connect(
        host: String,
        port: Int,
        password: String,
        timeoutMs: Int
    ) {
        throw ProtocolDisabledException(
            "Redis",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override fun disconnect() {
        // no-op
    }

    override suspend fun set(key: String, value: String, expireSeconds: Long): String? {
        throw ProtocolDisabledException(
            "Redis",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun get(key: String): String? {
        throw ProtocolDisabledException(
            "Redis",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun del(vararg keys: String): Long? {
        throw ProtocolDisabledException(
            "Redis",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun exists(key: String): Boolean = false

    override suspend fun publish(channel: String, message: String): Long? {
        throw ProtocolDisabledException(
            "Redis",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }

    override suspend fun subscribe(channel: String, onMessage: (channel: String, message: String) -> Unit) {
        throw ProtocolDisabledException(
            "Redis",
            "在编译期未被编译入包（对应框架已彻底剔除以削减体积）。请在 gradle.properties 中开启后重新编译。"
        )
    }
}
