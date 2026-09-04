package com.base.iot.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun AppButton(
    text: String,
    icon: ImageVector? = null,
    color: Color = AppTheme.colors.accentPrimary,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val isDark = AppTheme.colors.isDark
    val bgAlpha = if (isDark) 0.16f else 0.12f
    val borderAlpha = if (isDark) 0.35f else 0.45f

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = bgAlpha),
            contentColor = color,
            disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
            disabledContentColor = Color.Gray
        ),
        border = BorderStroke(1.dp, color.copy(alpha = borderAlpha)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
