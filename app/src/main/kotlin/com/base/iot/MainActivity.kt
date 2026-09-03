package com.base.iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.base.iot.core.ui.SystemBarManager
import com.base.iot.feature.demo.DashboardScreen
import com.base.iot.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主 Activity（单 Activity 架构）。
 *
 * 职责：
 * - 安装 Hilt 注入器
 * - 设置沉浸式边到边全屏模式
 * - 挂载 Compose 内容树与主题
 * - 托管 Navigation 路由（当前为单 Screen 示例）
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化边到边沉浸式（透明状态栏 + 浅色图标）
        SystemBarManager.applyDefault(this, darkStatusIcons = false, darkNavIcons = false)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 单 Screen 示例，多 Screen 时替换为 NavHost
                    DashboardScreen()
                }
            }
        }
    }
}
