package com.base.iot.core.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.base.iot.BuildConfig
import com.base.iot.core.diagnostics.Lg
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

class ProtocolDisabledException(
    protocol: String,
    reason: String = "当前已禁用，请先在配置文件中开启对应开关。"
) : IllegalStateException("协议 [$protocol] $reason")

data class IotProtocolSwitches(
    val isHttpCompiled: Boolean = BuildConfig.IS_HTTP_COMPILED,
    val isMqttCompiled: Boolean = BuildConfig.IS_MQTT_COMPILED,
    val isRedisCompiled: Boolean = BuildConfig.IS_REDIS_COMPILED,
    val isSocketCompiled: Boolean = BuildConfig.IS_SOCKET_COMPILED,

    // 核心规则：代码中确实调用的协议（编译期已编入包），默认锁定为常开，无法在运行期关闭
    val isHttpEnabled: Boolean = BuildConfig.IS_HTTP_COMPILED,
    val isMqttEnabled: Boolean = BuildConfig.IS_MQTT_COMPILED,
    val isRedisEnabled: Boolean = BuildConfig.IS_REDIS_COMPILED,
    val isSocketEnabled: Boolean = BuildConfig.IS_SOCKET_COMPILED,
)

private val Context.protocolDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "iot_protocol_switches")

@Singleton
class IotProtocolConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "IotProtocolConfig"
    private val dataStore = context.protocolDataStore

    private object Keys {
        val HTTP = booleanPreferencesKey("is_http_enabled")
        val MQTT = booleanPreferencesKey("is_mqtt_enabled")
        val REDIS = booleanPreferencesKey("is_redis_enabled")
        val SOCKET = booleanPreferencesKey("is_socket_enabled")
    }

    val switchesFlow: Flow<IotProtocolSwitches> = dataStore.data.map {
        IotProtocolSwitches(
            isHttpCompiled = BuildConfig.IS_HTTP_COMPILED,
            isMqttCompiled = BuildConfig.IS_MQTT_COMPILED,
            isRedisCompiled = BuildConfig.IS_REDIS_COMPILED,
            isSocketCompiled = BuildConfig.IS_SOCKET_COMPILED,
            isHttpEnabled = BuildConfig.IS_HTTP_COMPILED,
            isMqttEnabled = BuildConfig.IS_MQTT_COMPILED,
            isRedisEnabled = BuildConfig.IS_REDIS_COMPILED,
            isSocketEnabled = BuildConfig.IS_SOCKET_COMPILED,
        )
    }

    suspend fun setHttpEnabled(enabled: Boolean) {
        if (BuildConfig.IS_HTTP_COMPILED && !enabled) {
            Lg.w(TAG, "HTTP 协议已被代码调用并编译入包，默认锁定为常开，无法关闭")
            return
        }
        if (!BuildConfig.IS_HTTP_COMPILED && enabled) {
            throw ProtocolDisabledException("HTTP", "在编译期已被物理裁剪（未打包），无法在运行期开启。请在 gradle.properties 中配置并重新编译。")
        }
        dataStore.edit { it[Keys.HTTP] = enabled }
    }

    suspend fun setMqttEnabled(enabled: Boolean) {
        if (BuildConfig.IS_MQTT_COMPILED && !enabled) {
            Lg.w(TAG, "MQTT 协议已被代码调用并编译入包，默认锁定为常开，无法关闭")
            return
        }
        if (!BuildConfig.IS_MQTT_COMPILED && enabled) {
            throw ProtocolDisabledException("MQTT", "在编译期已被物理裁剪（未打包），无法在运行期开启。请在 gradle.properties 中配置并重新编译。")
        }
        dataStore.edit { it[Keys.MQTT] = enabled }
    }

    suspend fun setRedisEnabled(enabled: Boolean) {
        if (BuildConfig.IS_REDIS_COMPILED && !enabled) {
            Lg.w(TAG, "Redis 协议已被代码调用并编译入包，默认锁定为常开，无法关闭")
            return
        }
        if (!BuildConfig.IS_REDIS_COMPILED && enabled) {
            throw ProtocolDisabledException("Redis", "在编译期已被物理裁剪（未打包），无法在运行期开启。请在 gradle.properties 中配置并重新编译。")
        }
        dataStore.edit { it[Keys.REDIS] = enabled }
    }

    suspend fun setSocketEnabled(enabled: Boolean) {
        if (BuildConfig.IS_SOCKET_COMPILED && !enabled) {
            Lg.w(TAG, "Socket 协议已被代码调用并编译入包，默认锁定为常开，无法关闭")
            return
        }
        if (!BuildConfig.IS_SOCKET_COMPILED && enabled) {
            throw ProtocolDisabledException("Socket", "在编译期已被物理裁剪（未打包），无法在运行期开启。请在 gradle.properties 中配置并重新编译。")
        }
        dataStore.edit { it[Keys.SOCKET] = enabled }
    }

    fun assertEnabled(protocol: String, isCompiled: Boolean, isEnabled: Boolean) {
        if (!isCompiled) {
            throw ProtocolDisabledException(protocol, "在编译期已被物理裁剪（框架已彻底剔除以削减体积），请在 gradle.properties 中开启后重新编译。")
        }
        if (!isEnabled) {
            throw ProtocolDisabledException(protocol, "当前已禁用。")
        }
    }
}
