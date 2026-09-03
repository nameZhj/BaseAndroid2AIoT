package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.recycler.IotDeviceQuickAdapter
import com.base.iot.feature.demo.DashboardUiState
import com.base.iot.feature.demo.DashboardViewModel
import com.base.iot.ui.theme.AppTheme

// ==================== 缓存管理卡片 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheManagementCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = "自定义缓存位置与系统分享", icon = Icons.Filled.FolderZip, iconTint = colors.accentAmber) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "当前策略: ${uiState.currentCacheType.title}",
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "落盘路径: ${uiState.currentCachePath.ifEmpty { "初始化中..." }}",
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 策略单选 Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CacheLocationType.entries.forEach { type ->
                    val selected = uiState.currentCacheType == type
                    FilterChip(
                        selected = selected,
                        onClick = { vm.setCacheLocationType(type) },
                        label = {
                            Text(
                                text = when (type) {
                                    CacheLocationType.INTERNAL -> "内部缓存"
                                    CacheLocationType.EXTERNAL -> "外部缓存"
                                    CacheLocationType.EXTERNAL_DOWNLOADS -> "专属下载区"
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accentAmber.copy(alpha = 0.2f),
                            selectedLabelColor = colors.accentAmber
                        )
                    )
                }
            }

            Text(
                text = "已缓存文件数: ${uiState.cacheFilesCount} 个",
                color = colors.textSecondary,
                fontSize = 12.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = "系统分享下载文件",
                    icon = Icons.Filled.Share,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::shareLatestDownloadedFile
                )
                AppButton(
                    text = "清空当前缓存区",
                    icon = Icons.Filled.DeleteSweep,
                    color = colors.accentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::clearCache
                )
            }
        }
    }
}

// ==================== BRVAH 4 列表框架演示卡片 ====================

@Composable
fun BrvahRecyclerCard(uiState: DashboardUiState) {
    val colors = AppTheme.colors

    AppCard(title = "BRVAH 4 顶流列表框架 (GitHub 24k+ ★)", icon = Icons.Filled.ListAlt, iconTint = colors.accentCyan) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "集成 BaseRecyclerViewAdapterHelper 4，原生 RecyclerView 与 Compose 无缝混编展示设备状态：",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
            val isDark = colors.isDark
            AndroidView(
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        val adapter = IotDeviceQuickAdapter(isDark = isDark)
                        this.adapter = adapter
                        adapter.submitList(uiState.brvahDevices)
                    }
                },
                update = { recyclerView ->
                    (recyclerView.adapter as? IotDeviceQuickAdapter)?.let { adapter ->
                        adapter.isDark = isDark
                        adapter.submitList(uiState.brvahDevices)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}
