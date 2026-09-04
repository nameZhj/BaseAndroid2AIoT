// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.base.iot.R
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.components.AppSwitchRow
import com.base.iot.feature.demo.*
import com.base.iot.ui.theme.AppTheme

@Composable
fun ProtocolSwitchesCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = stringResource(R.string.protocol_switches_title), icon = Icons.Filled.ToggleOn, iconTint = colors.accentPurple) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppSwitchRow(
                label = stringResource(R.string.protocol_http),
                compiled = uiState.switches.isHttpCompiled,
                enabled = uiState.switches.isHttpEnabled,
                connected = null,
                onToggle = vm::toggleHttp,
                activeColor = colors.accentCyan
            )
            AppSwitchRow(
                label = stringResource(R.string.protocol_mqtt),
                compiled = uiState.switches.isMqttCompiled,
                enabled = uiState.switches.isMqttEnabled,
                connected = uiState.mqttConnected,
                onToggle = vm::toggleMqtt,
                activeColor = colors.accentPurple
            )
            AppSwitchRow(
                label = stringResource(R.string.protocol_redis),
                compiled = uiState.switches.isRedisCompiled,
                enabled = uiState.switches.isRedisEnabled,
                connected = uiState.redisConnected,
                onToggle = vm::toggleRedis,
                activeColor = colors.accentRed
            )
            AppSwitchRow(
                label = stringResource(R.string.protocol_socket),
                compiled = uiState.switches.isSocketCompiled,
                enabled = uiState.switches.isSocketEnabled,
                connected = uiState.socketConnected,
                onToggle = vm::toggleSocket,
                activeColor = colors.accentGreen
            )
        }
    }
}

@Composable
fun HttpTestCard(vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = stringResource(R.string.http_card_title), icon = Icons.Filled.Http, iconTint = colors.accentCyan) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(text = stringResource(R.string.http_btn_get), icon = Icons.Filled.Refresh, color = colors.accentCyan, modifier = Modifier.weight(1f), onClick = vm::testHttpGet)
                AppButton(text = stringResource(R.string.http_btn_post), icon = Icons.AutoMirrored.Filled.Send, color = colors.accentPurple, modifier = Modifier.weight(1f), onClick = vm::testHttpPost)
                AppButton(text = stringResource(R.string.http_btn_put), icon = Icons.Filled.Edit, color = colors.accentAmber, modifier = Modifier.weight(1f), onClick = vm::testHttpPut)
                AppButton(text = stringResource(R.string.http_btn_delete), icon = Icons.Filled.Delete, color = colors.accentRed, modifier = Modifier.weight(1f), onClick = vm::testHttpDelete)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppButton(text = stringResource(R.string.storage_btn_download_test), icon = Icons.Filled.FileUpload, color = colors.accentPurple, modifier = Modifier.weight(1f), onClick = vm::testHttpUpload)
                AppButton(text = stringResource(R.string.http_btn_download), icon = Icons.Filled.FileDownload, color = colors.accentGreen, modifier = Modifier.weight(1f), onClick = vm::testHttpDownload)
            }
        }
    }
}

@Composable
fun MqttTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = stringResource(R.string.mqtt_card_title), icon = Icons.Filled.Sensors, iconTint = colors.accentPurple) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = if (uiState.mqttConnected) stringResource(R.string.protocol_connected) else stringResource(R.string.mqtt_btn_connect),
                icon = if (uiState.mqttConnected) Icons.Filled.CheckCircle else Icons.Filled.PowerSettingsNew,
                color = if (uiState.mqttConnected) colors.accentGreen else colors.accentPurple,
                modifier = Modifier.weight(1f),
                onClick = vm::connectMqtt
            )
            AppButton(text = stringResource(R.string.mqtt_btn_publish), icon = Icons.AutoMirrored.Filled.Send, color = colors.accentCyan, modifier = Modifier.weight(1f), onClick = vm::mqttPublish)
            AppButton(text = stringResource(R.string.mqtt_btn_subscribe), icon = Icons.AutoMirrored.Filled.CallReceived, color = colors.accentAmber, modifier = Modifier.weight(1f), onClick = vm::mqttSubscribeTest)
        }
    }
}

@Composable
fun RedisTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = stringResource(R.string.redis_card_title), icon = Icons.Filled.Storage, iconTint = colors.accentRed) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = if (uiState.redisConnected) stringResource(R.string.protocol_connected) else stringResource(R.string.redis_btn_ping),
                icon = if (uiState.redisConnected) Icons.Filled.CheckCircle else Icons.Filled.PowerSettingsNew,
                color = if (uiState.redisConnected) colors.accentGreen else colors.accentRed,
                modifier = Modifier.weight(1f),
                onClick = vm::connectRedis
            )
            AppButton(text = stringResource(R.string.redis_btn_set), icon = Icons.AutoMirrored.Filled.Send, color = colors.accentAmber, modifier = Modifier.weight(1f), onClick = vm::redisSendCommand)
        }
    }
}

@Composable
fun SocketTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = stringResource(R.string.socket_card_title), icon = Icons.Filled.ElectricalServices, iconTint = colors.accentGreen) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = if (uiState.socketConnected) stringResource(R.string.protocol_connected) else stringResource(R.string.socket_btn_connect),
                icon = if (uiState.socketConnected) Icons.Filled.CheckCircle else Icons.Filled.PowerSettingsNew,
                color = if (uiState.socketConnected) colors.accentGreen else colors.accentCyan,
                modifier = Modifier.weight(1f),
                onClick = vm::connectSocket
            )
            AppButton(text = stringResource(R.string.socket_btn_send), icon = Icons.Filled.Sensors, color = colors.accentAmber, modifier = Modifier.weight(1f), onClick = vm::socketPingPong)
        }
    }
}
