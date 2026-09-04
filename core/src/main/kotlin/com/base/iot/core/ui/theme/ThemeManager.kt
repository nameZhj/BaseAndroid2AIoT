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

import androidx.annotation.StringRes
import com.base.iot.core.R

/**
 * 主题显示模式枚举
 */
enum class ThemeMode(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int
) {
    SYSTEM(R.string.theme_system, R.string.theme_system_desc),
    LIGHT(R.string.theme_light, R.string.theme_light_desc),
    DARK(R.string.theme_dark, R.string.theme_dark_desc);

    fun getTitle(context: Context): String = context.getString(titleRes)
    fun getSubtitle(context: Context): String = context.getString(subtitleRes)
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
        Lg.i(TAG, "Global theme mode changed to: ${mode.name}")
    }
}
