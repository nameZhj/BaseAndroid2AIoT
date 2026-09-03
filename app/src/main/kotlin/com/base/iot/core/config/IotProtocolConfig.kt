package com.base.iot.core.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.base.iot.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ==================== 异常定义 ====================

/**
 * 当某个 IoT 协议被禁用（编译期未打包或运行期已关闭）时抛出。
 */
class ProtocolDisabledException(
    protocol: String,
    reason: String = "当前已禁用，请先在配置文件或控制面板中开启对应开关。"
) : IllegalStateException("协议 [$protocol] $reason")

// ==================== 协议开关数据模型 ====================

/**
 * 所有 IoT 协议开关的快照，同时记录编译期打包状态与运行时启用状态。
 */
data class IotProtocolSwitches(
    // 编译期打包状态（由 gradle.properties 编译时写入 BuildConfig）
    val isHttpCompiled: Boolean = BuildConfig.IS_HTTP_COMPILED,
    val isMqttCompiled: Boolean = BuildConfig.IS_MQTT_COMPILED,
    val isRedisCompiled: Boolean = BuildConfig.IS_REDIS_COMPILED,
    val isSocketCompiled: Boolean = BuildConfig.IS_SOCKET_COMPILED,

    // 运行时开关（未编译入包时恒为 false）
    val isHttpEnabled: Boolean = BuildConfig.IS_HTTP_COMPILED,
    val isMqttEnabled: Boolean = BuildConfig.IS_MQTT_COMPILED,
    val isRedisEnabled: Boolean = BuildConfig.IS_REDIS_COMPILED,
    val isSocketEnabled: Boolean = BuildConfig.IS_SOCKET_COMPILED,
)

// ==================== DataStore 扩展 ====================

private val Context.protocolDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "iot_protocol_switches")

// ==================== IotProtocolConfig ====================

/**
 * IoT 协议开关配置。
 * 结合编译期开关（BuildConfig）与运行期持久化（DataStore）。
 */
@Singleton
class IotProtocolConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.protocolDataStore

    private object Keys {
        val HTTP = booleanPreferencesKey("is_http_enabled")
        val MQTT = booleanPreferencesKey("is_mqtt_enabled")
        val REDIS = booleanPreferencesKey("is_redis_enabled")
        val SOCKET = booleanPreferencesKey("is_socket_enabled")
    }

    /** 协议开关的实时 Flow */
    val switchesFlow: Flow<IotProtocolSwitches> = dataStore.data.map { prefs ->
        IotProtocolSwitches(
            isHttpCompiled = BuildConfig.IS_HTTP_COMPILED,
            isMqttCompiled = BuildConfig.IS_MQTT_COMPILED,
            isRedisCompiled = BuildConfig.IS_REDIS_COMPILED,
            isSocketCompiled = BuildConfig.IS_SOCKET_COMPILED,
            isHttpEnabled = BuildConfig.IS_HTTP_COMPILED && (prefs[Keys.HTTP] ?: true),
            isMqttEnabled = BuildConfig.IS_MQTT_COMPILED && (prefs[Keys.MQTT] ?: true),
            isRedisEnabled = BuildConfig.IS_REDIS_COMPILED && (prefs[Keys.REDIS] ?: true),
            isSocketEnabled = BuildConfig.IS_SOCKET_COMPILED && (prefs[Keys.SOCKET] ?: true),
        )
    }

    suspend fun setHttpEnabled(enabled: Boolean) {
        if (!BuildConfig.IS_HTTP_COMPILED && enabled) {
            throw ProtocolDisabledException("HTTP", "在编译期未被编译入包，无法在运行期开启。请在 gradle.properties 中配置开启并重新编译。")
        }
        dataStore.edit { it[Keys.HTTP] = enabled }
    }

    suspend fun setMqttEnabled(enabled: Boolean) {
        if (!BuildConfig.IS_MQTT_COMPILED && enabled) {
            throw ProtocolDisabledException("MQTT", "在编译期未被编译入包，无法在运行期开启。请在 gradle.properties 中配置开启并重新编译。")
        }
        dataStore.edit { it[Keys.MQTT] = enabled }
    }

    suspend fun setRedisEnabled(enabled: Boolean) {
        if (!BuildConfig.IS_REDIS_COMPILED && enabled) {
            throw ProtocolDisabledException("Redis", "在编译期未被编译入包，无法在运行期开启。请在 gradle.properties 中配置开启并重新编译。")
        }
        dataStore.edit { it[Keys.REDIS] = enabled }
    }

    suspend fun setSocketEnabled(enabled: Boolean) {
        if (!BuildConfig.IS_SOCKET_COMPILED && enabled) {
            throw ProtocolDisabledException("Socket", "在编译期未被编译入包，无法在运行期开启。请在 gradle.properties 中配置开启并重新编译。")
        }
        dataStore.edit { it[Keys.SOCKET] = enabled }
    }

    /**
     * 在协议调用入口处断言状态
     */
    fun assertEnabled(protocol: String, isCompiled: Boolean, isEnabled: Boolean) {
        if (!isCompiled) {
            throw ProtocolDisabledException(protocol, "在编译期未被编译入包（框架已彻底剔除以削减体积），请在 gradle.properties 中开启后重新编译。")
        }
        if (!isEnabled) {
            throw ProtocolDisabledException(protocol, "当前已禁用，请先在控制面板中开启对应开关。")
        }
    }
}
