package com.base.iot.core.storage

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.base.iot.core.diagnostics.Lg
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 缓存位置策略枚举
 */
enum class CacheLocationType(val title: String, val desc: String) {
    INTERNAL("内部私有缓存", "应用内部 cache 目录，卸载自动清除，隔离保护最高"),
    EXTERNAL("外部私有缓存", "位于 Android/data/包名/cache，空间充裕，大文件推荐"),
    EXTERNAL_DOWNLOADS("外部专属下载目录", "位于 Android/data/包名/files/Downloads，易于持久保留")
}

private val Context.cacheLocationDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "cache_location_prefs")

/**
 * 自定义文件缓存位置管理器。
 * 支持用户动态切换缓存目录策略，持久化到 DataStore，并提供统一的存取与清理入口。
 */
@Singleton
class CacheLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "CacheLocationManager"
    private val dataStore = context.cacheLocationDataStore

    private object Keys {
        val LOCATION_TYPE = stringPreferencesKey("cache_location_type")
    }

    /** 观察当前生效的缓存策略 */
    val locationTypeFlow: Flow<CacheLocationType> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.LOCATION_TYPE]
        CacheLocationType.entries.firstOrNull { it.name == raw } ?: CacheLocationType.INTERNAL
    }

    /** 切换缓存策略并持久化 */
    suspend fun setLocationType(type: CacheLocationType) {
        dataStore.edit { it[Keys.LOCATION_TYPE] = type.name }
        Lg.i(TAG, "缓存位置已切换为: ${type.title} -> ${getCacheDir(type).absolutePath}")
    }

    /** 获取当前选中的实际物理缓存目录对象（自动创建目录） */
    suspend fun getCurrentCacheDir(): File {
        val type = locationTypeFlow.first()
        return getCacheDir(type)
    }

    /** 同步获取当前缓存目录（默认策略回退） */
    fun getCacheDir(type: CacheLocationType = CacheLocationType.INTERNAL): File {
        val dir = when (type) {
            CacheLocationType.INTERNAL -> context.cacheDir
            CacheLocationType.EXTERNAL -> context.externalCacheDir ?: context.cacheDir
            CacheLocationType.EXTERNAL_DOWNLOADS ->
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        }
        val target = File(dir, "iot_cache")
        if (!target.exists()) target.mkdirs()
        return target
    }

    /** 在当前缓存目录中创建指定文件名前缀的文件 */
    suspend fun createCacheFile(fileName: String): File {
        val dir = getCurrentCacheDir()
        return File(dir, fileName)
    }

    /** 列出当前缓存目录中的所有文件 */
    suspend fun listCacheFiles(): List<File> {
        val dir = getCurrentCacheDir()
        return dir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /** 一键清空当前缓存目录 */
    suspend fun clearCurrentCache(): Long {
        val dir = getCurrentCacheDir()
        var freedBytes = 0L
        dir.listFiles()?.forEach { file ->
            freedBytes += file.length()
            file.delete()
        }
        Lg.i(TAG, "已清理缓存目录: ${dir.absolutePath}，共释放: $freedBytes 字节")
        return freedBytes
    }
}
