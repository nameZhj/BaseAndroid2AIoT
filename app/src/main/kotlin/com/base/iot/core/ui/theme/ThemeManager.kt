package com.base.iot.core.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.base.iot.core.diagnostics.Lg
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题显示模式枚举
 */
enum class ThemeMode(val title: String, val subtitle: String) {
    SYSTEM("跟随系统", "根据 Android 系统深色/浅色模式自动切换"),
    LIGHT("普通模式", "明亮日间模式，高对比度清爽视效"),
    DARK("夜间模式", "深色极夜模式，沉浸护眼低功耗")
}

private val Context.themeDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "app_theme_prefs")

/**
 * 全局主题模式管理器。
 * 提供普通模式与夜间模式持久化配置与实时响应式流。
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "ThemeManager"
    private val dataStore = context.themeDataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_display_mode")
    }

    /** 观察当前主题模式 */
    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE]
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.DARK
    }

    /** 设置主题模式并持久化 */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
        Lg.i(TAG, "全局主题已切换为: ${mode.title}")
    }
}
