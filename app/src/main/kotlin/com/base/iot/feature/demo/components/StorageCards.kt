// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.ui.components.AppButton
import com.base.iot.core.ui.components.AppCard
import com.base.iot.core.ui.theme.AppTheme
import com.base.iot.feature.demo.*
import com.base.iot.feature.demo.adapter.IotDeviceQuickAdapter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheManagementCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    val colors = AppTheme.colors

    AppCard(title = stringResource(R.string.storage_card_title), icon = Icons.Filled.FolderZip, iconTint = colors.accentAmber) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val policyName = stringResource(uiState.currentCacheType.titleRes)
            val pendingText = stringResource(R.string.storage_init_pending)
            Text(
                text = stringResource(R.string.storage_current_policy_prefix, policyName),
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(
                    R.string.storage_path_prefix,
                    uiState.currentCachePath.ifEmpty { pendingText }
                ),
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

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
                                text = stringResource(type.titleRes),
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
                text = stringResource(R.string.storage_cached_files_count, uiState.cacheFilesCount),
                color = colors.textSecondary,
                fontSize = 12.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = stringResource(R.string.storage_btn_share_file),
                    icon = Icons.Filled.Share,
                    color = colors.accentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::shareLatestDownloadedFile
                )
                AppButton(
                    text = stringResource(R.string.btn_clear),
                    icon = Icons.Filled.DeleteSweep,
                    color = colors.accentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::clearCache
                )
            }
        }
    }
}

@Composable
fun BrvahRecyclerCard(uiState: DashboardUiState) {
    val colors = AppTheme.colors

    AppCard(
        title = stringResource(R.string.brvah_card_title),
        icon = Icons.AutoMirrored.Filled.ListAlt,
        iconTint = colors.accentCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
