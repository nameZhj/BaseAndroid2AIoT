// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import androidx.lifecycle.viewModelScope
import com.base.iot.R
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private fun DashboardViewModel.toggleProto(name: String, compiled: Boolean, enabled: Boolean, action: suspend (Boolean) -> Unit) {
    if (compiled && !enabled) {
        appendLog(getString(R.string.demo_protocol_locked_hint, name))
        return
    }
    viewModelScope.launch {
        action(enabled)
        appendLog(getString(R.string.demo_protocol_switch_log, name, enabled.toString()))
    }
}

fun DashboardViewModel.toggleHttp(enabled: Boolean) = toggleProto("HTTP", uiState.value.switches.isHttpCompiled, enabled) {
    iotHub.config.setHttpEnabled(it)
}

fun DashboardViewModel.toggleMqtt(enabled: Boolean) = toggleProto("MQTT", uiState.value.switches.isMqttCompiled, enabled) {
    iotHub.config.setMqttEnabled(it)
}

fun DashboardViewModel.toggleRedis(enabled: Boolean) = toggleProto("Redis", uiState.value.switches.isRedisCompiled, enabled) {
    iotHub.config.setRedisEnabled(it)
}

fun DashboardViewModel.toggleSocket(enabled: Boolean) = toggleProto("Socket", uiState.value.switches.isSocketCompiled, enabled) {
    iotHub.config.setSocketEnabled(it)
}

fun DashboardViewModel.showLicensesDialog() {
    appendLog(getString(R.string.demo_license_summary_header))
    appendLog("1. XPopup (Apache-2.0) - https://github.com/li-xiaojun/XPopup")
    appendLog("2. BRVAH 4 (Apache-2.0) - https://github.com/CymChad/BaseRecyclerViewAdapterHelper")
    appendLog("3. Retrofit & OkHttp (Apache-2.0) - https://github.com/square/retrofit")
    appendLog("4. HiveMQ MQTT Client (Apache-2.0) - https://github.com/hivemq/hivemq-mqtt-client")
    appendLog("5. Jedis (MIT) - https://github.com/redis/jedis")
    appendLog("6. Coil (Apache-2.0) - https://github.com/coil-kt/coil")
    appendLog("7. Dagger Hilt (Apache-2.0) - https://github.com/google/dagger")
    appendLog("8. Google Gson (Apache-2.0) - https://github.com/google/gson")
    appendLog("9. Kotlinx Coroutines (Apache-2.0) - https://github.com/Kotlin/kotlinx.coroutines")
    appendLog(getString(R.string.demo_license_compliance_desc))
}

fun DashboardViewModel.setCacheLocationType(type: CacheLocationType) = viewModelScope.launch {
    cacheLocationManager.setLocationType(type)
    appendLog(getString(R.string.demo_cache_policy_switched, type.getTitle(getApplication())))
    refreshCacheStats()
}

fun DashboardViewModel.shareLatestDownloadedFile() {
    val file = uiState.value.latestDownloadedFile
    if (file != null && file.exists()) {
        appendLog(getString(R.string.demo_log_share_file, file.name))
        fileShare.shareFile(file, getString(R.string.share_file_title))
    } else {
        appendLog(getString(R.string.demo_log_no_downloaded_file))
    }
}

fun DashboardViewModel.clearCache() = viewModelScope.launch {
    val freed = cacheLocationManager.clearCurrentCache()
    appendLog(getString(R.string.demo_log_cache_cleared, freed))
    refreshCacheStats()
}

fun DashboardViewModel.toggleTheme() = viewModelScope.launch {
    val next = when (uiState.value.themeMode) {
        ThemeMode.DARK -> ThemeMode.LIGHT
        ThemeMode.LIGHT -> ThemeMode.DARK
        ThemeMode.SYSTEM -> ThemeMode.LIGHT
    }
    themeManager.setThemeMode(next)
    appendLog(getString(R.string.demo_theme_mode_switched, next.getTitle(getApplication())))
}

fun DashboardViewModel.setThemeMode(mode: ThemeMode) = viewModelScope.launch {
    themeManager.setThemeMode(mode)
    appendLog(getString(R.string.demo_theme_mode_switched, mode.getTitle(getApplication())))
}

fun DashboardViewModel.setConfirmDialog(visible: Boolean) {
    updateUiState { it.copy(showConfirmDialog = visible) }
}

fun DashboardViewModel.setLoadingDialog(visible: Boolean) {
    updateUiState { it.copy(showLoadingDialog = visible) }
}

fun DashboardViewModel.setInputDialog(visible: Boolean) {
    updateUiState { it.copy(showInputDialog = visible) }
}

fun DashboardViewModel.setBottomSheet(visible: Boolean) {
    updateUiState { it.copy(showBottomSheet = visible) }
}

fun DashboardViewModel.onConfirmDialogConfirmed() {
    appendLog(getString(R.string.demo_log_confirm_success))
}

fun DashboardViewModel.onInputDialogConfirmed(text: String) {
    appendLog(getString(R.string.demo_log_input_success, text))
}

fun DashboardViewModel.toggleImmersive(enable: Boolean) {
    updateUiState { it.copy(isImmersive = enable) }
    appendLog(getString(R.string.demo_log_immersive, enable.toString()))
}

fun DashboardViewModel.toggleBlockingDefault() {
    updateUiState { it.copy(isBlockingDefault = !it.isBlockingDefault) }
    val modeText = getString(if (uiState.value.isBlockingDefault) R.string.demo_mode_blocking else R.string.demo_mode_non_blocking)
    appendLog(getString(R.string.demo_log_blocking_mode, modeText))
}

fun DashboardViewModel.toggleEnableLoadingDialog() {
    updateUiState { it.copy(enableLoadingDialog = !it.enableLoadingDialog) }
    val stateText = getString(if (uiState.value.enableLoadingDialog) R.string.demo_state_enabled else R.string.demo_state_disabled_silent)
    appendLog(getString(R.string.demo_log_loading_switch, stateText))
}
