package com.base.iot

import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.base.iot.core.iot.IotHub
import com.base.iot.core.ui.SystemBarManager
import com.base.iot.core.ui.theme.AppTheme
import com.base.iot.core.ui.theme.ThemeManager
import com.base.iot.core.ui.theme.ThemeMode
import com.base.iot.feature.demo.DashboardScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 主 Activity（单 Activity 架构）。
 *
 * 职责：
 * - 安装 Hilt 注入器
 * - 响应式监听 ThemeManager 主题模式切换（普通模式 / 夜间模式）
 * - 动态自适应系统状态栏与导航栏图标颜色，保证文字永不与底色冲突
 * - 响应 USB OTG 串口插拔生命周期意图（ACTION_USB_DEVICE_ATTACHED）
 * - 托管统一主题与 Compose 内容树
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var iotHub: IotHub

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        handleUsbIntent(intent)

        setContent {
            val themeMode by themeManager.themeModeFlow.collectAsState(initial = ThemeMode.DARK)
            val systemDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }

            // 动态调节状态栏与导航栏图标：暗色背景使用浅色高亮图标，浅色背景使用深色图标，杜绝颜色相近无法看清
            LaunchedEffect(isDark) {
                SystemBarManager.applyDefault(
                    this@MainActivity,
                    darkStatusIcons = !isDark,
                    darkNavIcons = !isDark
                )
            }

            AppTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.colors.background
                ) {
                    // [DEMO_MOUNT_POINT] Replace DashboardScreen() with your feature screen on formal dev
                    DashboardScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUsbIntent(intent)
    }

    private fun handleUsbIntent(intent: Intent?) {
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            iotHub.serial.notifyDeviceAttached()
        }
    }
}
