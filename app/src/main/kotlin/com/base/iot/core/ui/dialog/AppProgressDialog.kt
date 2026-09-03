package com.base.iot.core.ui.dialog

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.base.iot.ui.theme.AppTheme

data class LoadingConfig(
    val visible: Boolean = false,
    val title: String = "正在处理中...",
    val message: String? = null,
    val isBlocking: Boolean = true,
    val progress: Float? = null,
    val progressText: String? = null,
    val cancelable: Boolean = true,
    val onCancel: (() -> Unit)? = null
)

@Composable
fun AppProgressDialog(
    config: LoadingConfig,
    onDismissRequest: () -> Unit = {}
) {
    if (!config.visible) return

    val colors = AppTheme.colors

    Dialog(
        onDismissRequest = {
            if (!config.isBlocking) {
                config.onCancel?.invoke()
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !config.isBlocking,
            dismissOnClickOutside = !config.isBlocking,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.title,
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (config.isBlocking) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = null,
                                tint = if (config.isBlocking) colors.accentAmber else colors.accentPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(
                                    if (config.isBlocking) com.base.iot.R.string.progress_blocking_hint else com.base.iot.R.string.progress_non_blocking_hint
                                ),
                                color = if (config.isBlocking) colors.accentAmber else colors.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!config.isBlocking && config.cancelable) {
                        IconButton(
                            onClick = {
                                config.onCancel?.invoke()
                                onDismissRequest()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = androidx.compose.ui.res.stringResource(com.base.iot.R.string.btn_cancel),
                                tint = colors.textSecondary
                            )
                        }
                    }
                }

                if (config.progress != null) {
                    val p = config.progress.coerceIn(0f, 1f)
                    val percentInt = (p * 100).toInt()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.accentPrimary,
                            trackColor = colors.surfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = config.progressText ?: androidx.compose.ui.res.stringResource(com.base.iot.R.string.progress_completed_format, percentInt),
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$percentInt%",
                                color = colors.accentPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.accentPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = config.message ?: androidx.compose.ui.res.stringResource(com.base.iot.R.string.progress_default_message),
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                if (!config.isBlocking && config.cancelable) {
                    OutlinedButton(
                        onClick = {
                            config.onCancel?.invoke()
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Text(text = androidx.compose.ui.res.stringResource(com.base.iot.R.string.progress_cancel_action), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
