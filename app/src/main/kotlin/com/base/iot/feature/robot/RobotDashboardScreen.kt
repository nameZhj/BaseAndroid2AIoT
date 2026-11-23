package com.base.iot.feature.robot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import com.base.iot.core.R as CoreR
import com.base.iot.core.ui.theme.AppTheme
import com.base.iot.feature.robot.components.RobotMapCard

@Composable
fun RobotDashboardScreen(
    state: RobotDashboardUiState = RobotDashboardSampleData.state
) {
    var showVideoDialog by rememberSaveable { mutableStateOf(false) }
    val colors = AppTheme.colors

    Scaffold(containerColor = colors.background) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isExpanded = maxWidth >= 840.dp && maxHeight >= 560.dp
            val isMedium = maxWidth >= 600.dp
            val useTwoColumnCards = maxWidth >= 720.dp

            if (isExpanded) {
                ExpandedDashboard(
                    state = state,
                    onOpenVideo = { showVideoDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DashboardHeader(
                            siteName = stringResource(state.siteNameRes),
                            isSampleData = state.isSampleData,
                            onOpenVideo = { showVideoDialog = true }
                        )
                    }
                    item {
                        RobotMapCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isMedium) 340.dp else 280.dp)
                        )
                    }
                    if (useTwoColumnCards) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DeviceStatusCard(
                                    device = state.device,
                                    modifier = Modifier.weight(1f)
                                )
                                MissionCard(
                                    mission = state.mission,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatsCard(
                                    stats = state.stats,
                                    modifier = Modifier.weight(0.9f)
                                )
                                EventsCard(
                                    events = state.events,
                                    modifier = Modifier.weight(1.1f)
                                )
                            }
                        }
                    } else {
                        item { DeviceStatusCard(state.device) }
                        item { MissionCard(state.mission) }
                        item { StatsCard(state.stats) }
                        item { EventsCard(state.events) }
                    }
                    item { SampleDataNotice() }
                }
            }
        }
    }

    if (showVideoDialog) {
        AlertDialog(
            onDismissRequest = { showVideoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.VideoCameraFront,
                    contentDescription = null,
                    tint = colors.accentPrimary
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.robot_video_unavailable_title),
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.robot_video_unavailable_desc),
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showVideoDialog = false }) {
                    Text(stringResource(CoreR.string.btn_close), color = colors.accentPrimary)
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun ExpandedDashboard(
    state: RobotDashboardUiState,
    onOpenVideo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardHeader(
            siteName = stringResource(state.siteNameRes),
            isSampleData = state.isSampleData,
            onOpenVideo = onOpenVideo
        )

        Row(
            modifier = Modifier
                .weight(1.35f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RobotMapCard(
                modifier = Modifier
                    .weight(1.7f)
                    .fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeviceStatusCard(
                    device = state.device,
                    modifier = Modifier.weight(0.95f)
                )
                MissionCard(
                    mission = state.mission,
                    modifier = Modifier.weight(1.05f)
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(0.85f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                stats = state.stats,
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxSize()
            )
            EventsCard(
                events = state.events,
                maxItems = 2,
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize()
            )
        }

        if (state.isSampleData) SampleDataNotice()
    }
}

@Composable
private fun DashboardHeader(
    siteName: String,
    isSampleData: Boolean,
    onOpenVideo: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = stringResource(R.string.robot_dashboard_title),
                color = colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.robot_dashboard_subtitle),
                color = colors.textSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = siteName,
                    color = colors.textTertiary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isSampleData) {
                    StatusPill(
                        text = stringResource(R.string.robot_demo_mode),
                        color = colors.accentAmber
                    )
                }
            }
        }

        TextButton(
            onClick = onOpenVideo,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.VideoCameraFront,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.robot_video_button),
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

