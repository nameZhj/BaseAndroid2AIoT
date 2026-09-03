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
import com.base.iot.ui.theme.AppTheme

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
                    text = if (compiled) "已编入" else "未编译·已裁剪",
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
                        contentDescription = "常开锁定",
                        tint = colors.accentGreen,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "常开锁定",
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
                        text = if (connected) "已连接" else "未连接",
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
