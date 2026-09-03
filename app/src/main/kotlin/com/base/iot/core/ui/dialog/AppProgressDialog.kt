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

/**
 * 耗时操作加载与进度配置模型
 */
data class LoadingConfig(
    val visible: Boolean = false,
    val title: String = "正在处理中...",
    val message: String? = null,
    val isBlocking: Boolean = true,       // 开发者自主选择：阻塞式 vs 非阻塞式
    val progress: Float? = null,          // null = 不确定进度(菊花转圈)；0.0f..1.0f = 确定百分比
    val progressText: String? = null,     // 进度辅助说明 (如 "45.2 MB / 100 MB")
    val cancelable: Boolean = true,       // 非阻塞模式下是否允许取消
    val onCancel: (() -> Unit)? = null    // 取消回调
)

/**
 * 统一风格通用耗时操作加载与进度弹窗。
 *
 * 特性：
 * 1. 【自主选择阻塞与非阻塞】：
 *    - 阻塞式：禁止点击外部与返回键，屏蔽界面触摸，确保核心物联网通信事务不被并发破坏；
 *    - 非阻塞式：允许点击外部或底部“取消”按钮退出，协同取消协程；
 * 2. 【进度无缝双模】：
 *    - 循环转圈模式 (Indeterminate) 与 精准百分比模式 (0%~100% 带速度与字节统计) 动态自适应；
 * 3. 【高对比度主题保障】：完全遵循 AppTheme 设计系统，在普通与夜间模式下清晰可见。
 */
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
                // 顶部标题与模式标签行
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
                                text = if (config.isBlocking) "阻塞式（防并发防重复）" else "非阻塞式（可随时中断）",
                                color = if (config.isBlocking) colors.accentAmber else colors.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 非阻塞模式下右上角允许快捷关闭
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
                                contentDescription = "取消操作",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }

                // 进度与状态区
                if (config.progress != null) {
                    // ================= 确定百分比进度模式 =================
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
                                text = config.progressText ?: "已完成 $percentInt%",
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
                    // ================= 循环转圈等待模式 =================
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
                            text = config.message ?: "正在与边缘设备同步，请稍候...",
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // 非阻塞模式下的显式取消按钮
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
                        Text(text = "取消本次操作", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
