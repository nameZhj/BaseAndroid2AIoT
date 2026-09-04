// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo

import com.base.iot.core.base.IUiEvent
import com.base.iot.core.base.IUiState
import com.base.iot.core.config.IotProtocolSwitches
import com.base.iot.core.diagnostics.ParsedError
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.ui.dialog.LoadingConfig
import com.base.iot.feature.demo.adapter.IotDeviceItem
import com.base.iot.core.ui.theme.ThemeMode
import java.io.File

data class DashboardUiState(
    val switches: IotProtocolSwitches = IotProtocolSwitches(),
    val mqttConnected: Boolean = false,
    val redisConnected: Boolean = false,
    val socketConnected: Boolean = false,
    val isImmersive: Boolean = false,
    val terminalLogs: List<String> = emptyList(),
    val currentCacheType: CacheLocationType = CacheLocationType.INTERNAL,
    val currentCachePath: String = "",
    val cacheFilesCount: Int = 0,
    val latestDownloadedFile: File? = null,
    val brvahDevices: List<IotDeviceItem> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.DARK,
    val showConfirmDialog: Boolean = false,
    val showLoadingDialog: Boolean = false,
    val showInputDialog: Boolean = false,
    val showBottomSheet: Boolean = false,
    override val loadingConfig: LoadingConfig = LoadingConfig(),
    override val parsedError: ParsedError? = null,
    override val showErrorDialog: Boolean = false,
    val isBlockingDefault: Boolean = true,
    val enableLoadingDialog: Boolean = true
) : IUiState

sealed class DashboardEvent : IUiEvent {
    data class ShowSnackbar(val message: String) : DashboardEvent()
}
