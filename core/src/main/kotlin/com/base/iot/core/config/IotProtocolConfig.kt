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
    reason: String = "is disabled in configuration."
) : IllegalStateException("Protocol [$protocol] $reason")

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
            Lg.w(TAG, "HTTP protocol is compiled and locked always-on, cannot disable at runtime")
            return
        }
        if (!BuildConfig.IS_HTTP_COMPILED && enabled) {
            throw ProtocolDisabledException("HTTP", "trimmed at compile-time (not compiled into APK). Please enable in gradle.properties and recompile.")
        }
        dataStore.edit { it[Keys.HTTP] = enabled }
    }

    suspend fun setMqttEnabled(enabled: Boolean) {
        if (BuildConfig.IS_MQTT_COMPILED && !enabled) {
            Lg.w(TAG, "MQTT protocol is compiled and locked always-on, cannot disable at runtime")
            return
        }
        if (!BuildConfig.IS_MQTT_COMPILED && enabled) {
            throw ProtocolDisabledException("MQTT", "trimmed at compile-time (not compiled into APK). Please enable in gradle.properties and recompile.")
        }
        dataStore.edit { it[Keys.MQTT] = enabled }
    }

    suspend fun setRedisEnabled(enabled: Boolean) {
        if (BuildConfig.IS_REDIS_COMPILED && !enabled) {
            Lg.w(TAG, "Redis protocol is compiled and locked always-on, cannot disable at runtime")
            return
        }
        if (!BuildConfig.IS_REDIS_COMPILED && enabled) {
            throw ProtocolDisabledException("Redis", "trimmed at compile-time (not compiled into APK). Please enable in gradle.properties and recompile.")
        }
        dataStore.edit { it[Keys.REDIS] = enabled }
    }

    suspend fun setSocketEnabled(enabled: Boolean) {
        if (BuildConfig.IS_SOCKET_COMPILED && !enabled) {
            Lg.w(TAG, "Socket protocol is compiled and locked always-on, cannot disable at runtime")
            return
        }
        if (!BuildConfig.IS_SOCKET_COMPILED && enabled) {
            throw ProtocolDisabledException("Socket", "trimmed at compile-time (not compiled into APK). Please enable in gradle.properties and recompile.")
        }
        dataStore.edit { it[Keys.SOCKET] = enabled }
    }

    fun assertEnabled(protocol: String, isCompiled: Boolean, isEnabled: Boolean) {
        if (!isCompiled) {
            throw ProtocolDisabledException(protocol, "trimmed at compile-time. Please configure in gradle.properties and recompile.")
        }
        if (!isEnabled) {
            throw ProtocolDisabledException(protocol, "is currently disabled.")
        }
    }
}
