package com.base.iot.core.ui.dialog

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.base.iot.ui.theme.AppTheme

/**
 * 统一风格确认弹窗 (Confirm Dialog)
 * 支持普通操作或高危危险操作（红色高亮主按钮），具备极致清晰的高对比度排版。
 */
@Composable
fun AppConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "确定",
    cancelText: String = "取消",
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val colors = AppTheme.colors

        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isDanger) Icons.Filled.Warning else Icons.Filled.Info,
                        contentDescription = null,
                        tint = if (isDanger) colors.accentRed else colors.accentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 说明正文
                Text(
                    text = message,
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(8.dp))

                // 底部操作按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Text(text = cancelText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDanger) colors.accentRed else colors.accentPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = confirmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * 统一风格加载等待弹窗 (Loading Dialog)
 * 支持阻止用户点击外部与穿透，保证物联网长耗时任务（连接、握手、OTA更新）的安全性。
 */
@Composable
fun AppLoadingDialog(
    visible: Boolean,
    message: String = "正在处理中..."
) {
    if (!visible) return

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val colors = AppTheme.colors

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = colors.accentPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = message,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 统一风格输入弹窗 (Input Dialog)
 */
@Composable
fun AppInputDialog(
    visible: Boolean,
    title: String,
    hint: String = "请输入内容",
    initialText: String = "",
    confirmText: String = "确定",
    cancelText: String = "取消",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var textState by remember(visible) { mutableStateOf(initialText) }
    val colors = AppTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text(text = hint, color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.accentPrimary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant
                    ),
                    trailingIcon = {
                        if (textState.isNotEmpty()) {
                            IconButton(onClick = { textState = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "清空",
                                    tint = colors.textTertiary
                                )
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Text(text = cancelText, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            onConfirm(textState)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = confirmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * 统一风格底部抽屉弹窗 (Bottom Sheet Dialog)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheetDialog(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.cardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
            Spacer(Modifier.height(32.dp))
        }
    }
}
