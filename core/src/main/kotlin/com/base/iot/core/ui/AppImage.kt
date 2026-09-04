package com.base.iot.core.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.base.iot.core.R

/**
 * 通用 Compose 图片加载组件（基于 Coil）。
 *
 * 特性：
 * - 内置加载中占位图（Loading Shimmer）
 * - 内置加载失败图标
 * - 支持圆形裁剪 ([circular])
 * - 内置淡入动画（crossfade）
 * - 支持自定义 ContentScale 和 Shape
 *
 * 使用示例：
 * ```kotlin
 * // 普通图片
 * AppImage(url = "https://example.com/image.jpg", modifier = Modifier.size(100.dp))
 *
 * // 圆形头像
 * AppImage(url = avatarUrl, circular = true, modifier = Modifier.size(48.dp))
 * ```
 *
 * @param url 图片 URL（支持 http/https/file/content:// 等 Coil 支持的所有格式）
 * @param modifier 修饰符
 * @param contentDescription 无障碍描述
 * @param contentScale 缩放方式，默认 [ContentScale.Crop]
 * @param circular 是否裁剪为圆形
 * @param shape 自定义 Shape（优先于 [circular]）
 * @param crossfadeDuration 淡入动画时长（ms），0 表示禁用
 * @param placeholderColor 加载中占位背景色
 */
@Composable
fun AppImage(
    url: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    circular: Boolean = false,
    shape: Shape? = if (circular) CircleShape else null,
    crossfadeDuration: Int = 300,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val finalModifier = if (shape != null) modifier.clip(shape) else modifier

    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = finalModifier,
        contentScale = contentScale
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                // 加载中：显示占位色块（可替换为 Shimmer 动画）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            is AsyncImagePainter.State.Error -> {
                // 加载失败：显示错误图标
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = stringResource(R.string.image_load_failed),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            else -> {
                // 加载成功：展示图片（带淡入动画）
                SubcomposeAsyncImageContent()
            }
        }
    }
}

/**
 * 正方形圆形头像快捷组件。
 *
 * @param url 头像 URL
 * @param modifier 修饰符（建议设置 size）
 * @param contentDescription 无障碍描述
 */
@Composable
fun AvatarImage(
    url: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AppImage(
        url = url,
        modifier = modifier.aspectRatio(1f),
        contentDescription = contentDescription,
        circular = true,
        contentScale = ContentScale.Crop
    )
}
