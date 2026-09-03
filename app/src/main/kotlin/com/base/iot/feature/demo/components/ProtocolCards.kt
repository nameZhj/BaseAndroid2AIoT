package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.components.AppSwitchRow
import com.base.iot.feature.demo.DashboardUiState
import com.base.iot.feature.demo.DashboardViewModel
import com.base.iot.ui.theme.AppTheme

// ==================== 协议开关卡片 ====================

@Composable
fun ProtocolSwitchesCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "协议开关", icon = Icons.Filled.ToggleOn, iconTint = colors.accentPurple) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppSwitchRow(
                label = "HTTP",
                compiled = uiState.switches.isHttpCompiled,
                enabled = uiState.switches.isHttpEnabled,
                connected = null,
                onToggle = vm::toggleHttp,
                activeColor = colors.accentCyan
            )
            AppSwitchRow(
                label = "MQTT",
                compiled = uiState.switches.isMqttCompiled,
                enabled = uiState.switches.isMqttEnabled,
                connected = uiState.mqttConnected,
                onToggle = vm::toggleMqtt,
                activeColor = colors.accentPurple
            )
            AppSwitchRow(
                label = "Redis",
                compiled = uiState.switches.isRedisCompiled,
                enabled = uiState.switches.isRedisEnabled,
                connected = uiState.redisConnected,
                onToggle = vm::toggleRedis,
                activeColor = colors.accentRed
            )
            AppSwitchRow(
                label = "Socket",
                compiled = uiState.switches.isSocketCompiled,
                enabled = uiState.switches.isSocketEnabled,
                connected = uiState.socketConnected,
                onToggle = vm::toggleSocket,
                activeColor = colors.accentGreen
            )
        }
    }
}

// ==================== HTTP 测试卡片 ====================

@Composable
fun HttpTestCard(vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "HTTP 交互与文件传输 (Retrofit)", icon = Icons.Filled.Http, iconTint = colors.accentCyan) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "全类型支持：GET / POST / PUT / DELETE 及大文件流式上传下载：",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(text = "GET", icon = Icons.Filled.Refresh, color = colors.accentCyan, modifier = Modifier.weight(1f), onClick = vm::testHttpGet)
                AppButton(text = "POST", icon = Icons.Filled.Send, color = colors.accentPurple, modifier = Modifier.weight(1f), onClick = vm::testHttpPost)
                AppButton(text = "PUT", icon = Icons.Filled.Edit, color = colors.accentAmber, modifier = Modifier.weight(1f), onClick = vm::testHttpPut)
                AppButton(text = "DEL", icon = Icons.Filled.Delete, color = colors.accentRed, modifier = Modifier.weight(1f), onClick = vm::testHttpDelete)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(text = "大文件分块上传", icon = Icons.Filled.FileUpload, color = colors.accentPurple, modifier = Modifier.weight(1f), onClick = vm::testHttpUpload)
                AppButton(text = "流式下载 (防OOM)", icon = Icons.Filled.FileDownload, color = colors.accentGreen, modifier = Modifier.weight(1f), onClick = vm::testHttpDownload)
            }
        }
    }
}

// ==================== MQTT 测试卡片 ====================

@Composable
fun MqttTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "MQTT 客户端 (HiveMQ)", icon = Icons.Filled.Sensors, iconTint = colors.accentPurple) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = if (uiState.mqttConnected) "已连接" else "连接 Broker",
                icon = if (uiState.mqttConnected) Icons.Filled.CheckCircle else Icons.Filled.PowerSettingsNew,
                color = if (uiState.mqttConnected) colors.accentGreen else colors.accentPurple,
                modifier = Modifier.weight(1f),
                onClick = vm::connectMqtt
            )
            AppButton(
                text = "发布主题",
                icon = Icons.Filled.Send,
                color = colors.accentCyan,
                modifier = Modifier.weight(1f),
                onClick = vm::mqttPublish
            )
            AppButton(
                text = "订阅监听",
                icon = Icons.Filled.CallReceived,
                color = colors.accentAmber,
                modifier = Modifier.weight(1f),
                onClick = vm::mqttSubscribeTest
            )
        }
    }
}

// ==================== Redis 测试卡片 ====================

@Composable
fun RedisTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "Redis 远控与键值缓存", icon = Icons.Filled.Storage, iconTint = colors.accentRed) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = if (uiState.redisConnected) "已连接" else "连接 Redis",
                icon = if (uiState.redisConnected) Icons.Filled.CheckCircle else Icons.Filled.PowerSettingsNew,
                color = if (uiState.redisConnected) colors.accentGreen else colors.accentRed,
                modifier = Modifier.weight(1f),
                onClick = vm::connectRedis
            )
            AppButton(
                text = "下发控制指令",
                icon = Icons.Filled.Send,
                color = colors.accentAmber,
                modifier = Modifier.weight(1f),
                onClick = vm::redisSendCommand
            )
        }
    }
}

// ==================== TCP Socket 测试卡片 ====================

@Composable
fun SocketTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "TCP Socket 原生工业长连接", icon = Icons.Filled.ElectricalServices, iconTint = colors.accentGreen) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = if (uiState.socketConnected) "已连接" else "建立长连接",
                icon = if (uiState.socketConnected) Icons.Filled.CheckCircle else Icons.Filled.PowerSettingsNew,
                color = if (uiState.socketConnected) colors.accentGreen else colors.accentCyan,
                modifier = Modifier.weight(1f),
                onClick = vm::connectSocket
            )
            AppButton(
                text = "心跳 PING-PONG",
                icon = Icons.Filled.Sensors,
                color = colors.accentAmber,
                modifier = Modifier.weight(1f),
                onClick = vm::socketPingPong
            )
        }
    }
}
