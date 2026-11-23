package com.base.iot.feature.robot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun DeviceStatusCard(
    device: RobotDeviceSnapshot,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val stateColor = if (device.isConnected) colors.accentGreen else colors.accentAmber

    AppCard(
        title = stringResource(R.string.robot_device_title),
        icon = Icons.Filled.Devices,
        modifier = modifier,
        trailingContent = {
            StatusPill(
                text = stringResource(
                    if (device.isConnected) R.string.robot_device_online
                    else R.string.robot_device_offline
                ),
                color = stateColor
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(device.nameRes),
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(device.modelRes),
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.BatteryFull,
                    contentDescription = null,
                    tint = colors.accentGreen,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = stringResource(R.string.robot_device_battery_value, device.batteryPercent),
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.SignalCellularAlt,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = stringResource(R.string.robot_device_signal_value, device.signalPercent),
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
            LinearProgressIndicator(
                progress = { device.batteryPercent.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = colors.accentGreen,
                trackColor = colors.surfaceVariant
            )
            Text(
                text = stringResource(R.string.robot_device_network_waiting),
                color = colors.textTertiary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MissionCard(
    mission: RobotMissionSnapshot,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    AppCard(
        title = stringResource(R.string.robot_mission_title),
        icon = Icons.Filled.Schedule,
        modifier = modifier,
        trailingContent = {
            StatusPill(
                text = stringResource(R.string.robot_mission_waiting),
                color = colors.accentAmber
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(mission.nameRes),
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.robot_mission_waiting_desc),
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { mission.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = colors.accentPrimary,
                trackColor = colors.surfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.robot_mission_progress,
                        mission.completedPoints,
                        mission.totalPoints
                    ),
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.robot_mission_not_dispatched),
                    color = colors.accentAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    stats: RobotInspectionStats,
    modifier: Modifier = Modifier
) {
    AppCard(
        title = stringResource(R.string.robot_stats_title),
        icon = Icons.Filled.CheckCircle,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCell(
                    label = stringResource(R.string.robot_stats_patrols),
                    value = stats.patrolCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    label = stringResource(R.string.robot_stats_points),
                    value = stats.pointCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCell(
                    label = stringResource(R.string.robot_stats_resolved),
                    value = stats.resolvedEventCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    label = stringResource(R.string.robot_stats_coverage),
                    value = stringResource(R.string.robot_stats_percent, stats.routeCoveragePercent),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            color = colors.accentPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

