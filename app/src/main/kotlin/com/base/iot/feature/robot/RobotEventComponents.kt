package com.base.iot.feature.robot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun EventsCard(
    events: List<RobotEventSnapshot>,
    modifier: Modifier = Modifier,
    maxItems: Int = events.size
) {
    AppCard(
        title = stringResource(R.string.robot_events_title),
        icon = Icons.Filled.WarningAmber,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            events.take(maxItems).forEach { event ->
                EventRow(event)
            }
        }
    }
}

@Composable
private fun EventRow(event: RobotEventSnapshot) {
    val colors = AppTheme.colors
    val markerColor = if (event.severity == RobotEventSeverity.HIGH) {
        colors.accentRed
    } else {
        colors.accentAmber
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(markerColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (event.severity == RobotEventSeverity.HIGH) {
                    Icons.Filled.WarningAmber
                } else {
                    Icons.Filled.Info
                },
                contentDescription = null,
                tint = markerColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(event.titleRes),
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(event.detailRes),
                color = colors.textSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.robot_event_meta, event.pointId, event.timestamp),
                color = colors.textTertiary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
        StatusPill(
            text = stringResource(
                if (event.isResolved) R.string.robot_event_resolved
                else R.string.robot_event_pending
            ),
            color = if (event.isResolved) colors.accentGreen else markerColor
        )
    }
}

@Composable
fun SampleDataNotice() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = colors.accentAmber,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.robot_sample_data_notice),
            color = colors.textSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 7.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
