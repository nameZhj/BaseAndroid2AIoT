package com.base.iot.core.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.base.iot.core.R
import com.base.iot.core.ui.pad.padDialogBounds
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun AppInputDialog(
    visible: Boolean,
    title: String,
    hint: String = stringResource(R.string.input_hint_default),
    initialText: String = "",
    confirmText: String = stringResource(R.string.btn_confirm),
    cancelText: String = stringResource(R.string.btn_cancel),
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
                .padDialogBounds(phoneFraction = 0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                    )

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_close),
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text(text = hint, color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.accentPrimary, unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.surfaceVariant, unfocusedContainerColor = colors.surfaceVariant
                    ),
                    trailingIcon = {
                        if (textState.isNotEmpty()) {
                            IconButton(onClick = { textState = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.btn_clear),
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                    ) {
                        Text(text = cancelText, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { onConfirm(textState); onDismiss() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary, contentColor = Color.White)
                    ) {
                        Text(text = confirmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
