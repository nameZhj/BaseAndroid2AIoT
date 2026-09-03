package com.base.iot.feature.demo.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.feature.demo.DashboardUiState
import com.base.iot.feature.demo.DashboardViewModel
import com.base.iot.ui.theme.AppTheme

@Composable
fun DashboardHeader(
    vm: DashboardViewModel,
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            Icon(
                imageVector = Icons.Filled.Hub,
                contentDescription = null,
                tint = colors.accentCyan,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dashboard_title),
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.dashboard_subtitle),
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = vm::toggleTheme,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (colors.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = androidx.compose.ui.res.stringResource(
                        if (colors.isDark) com.base.iot.R.string.theme_light else com.base.iot.R.string.theme_dark
                    ),
                    tint = if (colors.isDark) colors.accentAmber else colors.accentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
