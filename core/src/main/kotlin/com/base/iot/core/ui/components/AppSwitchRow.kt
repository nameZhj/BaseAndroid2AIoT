package com.base.iot.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.base.iot.core.R
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun AppSwitchRow(
    label: String,
    compiled: Boolean,
    enabled: Boolean,
    connected: Boolean? = null,
    onToggle: (Boolean) -> Unit,
    activeColor: Color = AppTheme.colors.accentPrimary
) {
    val colors = AppTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (compiled) colors.accentGreen.copy(alpha = 0.15f)
                        else colors.textSecondary.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(
                        if (compiled) R.string.protocol_compiled else R.string.protocol_not_compiled
                    ),
                    color = if (compiled) colors.accentGreen else colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (compiled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.protocol_locked_on),
                        tint = colors.accentGreen,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = stringResource(R.string.protocol_locked_on),
                        color = colors.accentGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (connected != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (connected) colors.accentGreen.copy(alpha = 0.15f)
                            else colors.accentRed.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (connected) R.string.protocol_connected else R.string.protocol_disconnected
                        ),
                        color = if (connected) colors.accentGreen else colors.accentRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Switch(
            checked = compiled,
            onCheckedChange = { onToggle(it) },
            enabled = false,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                disabledCheckedThumbColor = Color.White,
                disabledCheckedTrackColor = activeColor.copy(alpha = 0.6f),
                disabledUncheckedThumbColor = colors.textSecondary,
                disabledUncheckedTrackColor = colors.cardBorder
            )
        )
    }
}
