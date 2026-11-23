package com.base.iot.core.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.base.iot.core.R
import com.base.iot.core.diagnostics.Lg
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_theme_prefs")

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.themeDataStore
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_display_mode")
        val THEME_ACCENT = stringPreferencesKey("theme_accent")
    }

    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.DARK
    }

    val themeAccentFlow: Flow<ThemeAccent> = dataStore.data.map { prefs ->
        ThemeAccent.entries.firstOrNull { it.name == prefs[Keys.THEME_ACCENT] } ?: ThemeAccent.ARCTIC
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
        Lg.i("ThemeManager", "Theme mode changed: ${mode.name}")
    }

    suspend fun setThemeAccent(accent: ThemeAccent) {
        dataStore.edit { it[Keys.THEME_ACCENT] = accent.name }
        Lg.i("ThemeManager", "Theme accent changed: ${accent.name}")
    }
}