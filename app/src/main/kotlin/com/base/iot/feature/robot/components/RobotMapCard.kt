package com.base.iot.feature.robot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.iot.R
import com.base.iot.core.ui.theme.AppTheme

@Composable
fun RobotMapCard(
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, tint = colors.accentPrimary)
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.robot_map_title),
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(R.string.robot_map_source_demo),
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                MapStateTag()
            }

            RobotMapViewport(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapLegendDot(colors.accentGreen, stringResource(R.string.robot_map_legend_robot))
                MapLegendDot(colors.accentPrimary, stringResource(R.string.robot_map_legend_route))
                MapLegendDot(colors.accentRed, stringResource(R.string.robot_map_legend_event))
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.robot_map_not_loaded),
                    color = colors.textTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun MapStateTag() {
    val colors = AppTheme.colors
    Surface(
        color = colors.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = stringResource(R.string.robot_demo_mode),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = colors.accentAmber,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RobotMapViewport(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val gridColor = colors.cardBorder.copy(alpha = 0.36f)
            val gridStep = 28.dp.toPx()
            var x = 0f
            while (x <= size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                x += gridStep
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                y += gridStep
            }

            val wallColor = colors.textTertiary.copy(alpha = 0.72f)
            val wallStroke = Stroke(width = 2.dp.toPx())
            drawRect(
                wallColor,
                topLeft = Offset(size.width * 0.06f, size.height * 0.13f),
                size = Size(size.width * 0.25f, size.height * 0.36f),
                style = wallStroke
            )
            drawRect(
                wallColor,
                topLeft = Offset(size.width * 0.39f, size.height * 0.12f),
                size = Size(size.width * 0.19f, size.height * 0.3f),
                style = wallStroke
            )
            drawRect(
                wallColor,
                topLeft = Offset(size.width * 0.68f, size.height * 0.17f),
                size = Size(size.width * 0.24f, size.height * 0.39f),
                style = wallStroke
            )
            drawLine(
                wallColor,
                Offset(size.width * 0.18f, size.height * 0.49f),
                Offset(size.width * 0.18f, size.height * 0.83f),
                2.dp.toPx()
            )
            drawLine(
                wallColor,
                Offset(size.width * 0.49f, size.height * 0.42f),
                Offset(size.width * 0.49f, size.height * 0.82f),
                2.dp.toPx()
            )
            drawLine(
                wallColor,
                Offset(size.width * 0.8f, size.height * 0.56f),
                Offset(size.width * 0.8f, size.height * 0.86f),
                2.dp.toPx()
            )

            val routePoints = listOf(
                Offset(size.width * 0.13f, size.height * 0.76f),
                Offset(size.width * 0.13f, size.height * 0.62f),
                Offset(size.width * 0.34f, size.height * 0.62f),
                Offset(size.width * 0.34f, size.height * 0.34f),
                Offset(size.width * 0.62f, size.height * 0.34f),
                Offset(size.width * 0.62f, size.height * 0.72f),
                Offset(size.width * 0.85f, size.height * 0.72f)
            )
            val route = Path().apply {
                moveTo(routePoints.first().x, routePoints.first().y)
                routePoints.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                route,
                color = colors.accentPrimary,
                style = Stroke(width = 4.dp.toPx())
            )
            routePoints.drop(1).dropLast(1).forEach { point ->
                drawCircle(colors.accentPrimary, radius = 5.dp.toPx(), center = point)
            }

            val robot = routePoints.first()
            drawCircle(colors.accentGreen.copy(alpha = 0.2f), 18.dp.toPx(), robot)
            drawCircle(colors.accentGreen, 8.dp.toPx(), robot)
            drawLine(
                colors.accentGreen,
                robot,
                Offset(robot.x + 14.dp.toPx(), robot.y - 5.dp.toPx()),
                3.dp.toPx()
            )

            listOf(
                Offset(size.width * 0.62f, size.height * 0.34f),
                Offset(size.width * 0.85f, size.height * 0.72f)
            ).forEach { point ->
                drawCircle(colors.accentRed.copy(alpha = 0.18f), 13.dp.toPx(), point)
                drawCircle(colors.accentRed, 6.dp.toPx(), point)
            }

            drawRoundRect(
                color = colors.textTertiary.copy(alpha = 0.3f),
                topLeft = Offset(size.width * 0.35f, size.height * 0.84f),
                size = Size(size.width * 0.34f, size.height * 0.06f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
        }

        MapMarker(
            label = stringResource(R.string.robot_map_marker_robot),
            color = colors.accentGreen,
            modifier = Modifier.offset(
                x = maxWidth * 0.04f,
                y = maxHeight * 0.72f
            )
        )
        MapMarker(
            label = stringResource(R.string.robot_map_marker_event_1),
            color = colors.accentRed,
            modifier = Modifier.offset(
                x = maxWidth * 0.58f,
                y = maxHeight * 0.19f
            )
        )
        MapMarker(
            label = stringResource(R.string.robot_map_marker_event_2),
            color = colors.accentRed,
            modifier = Modifier.offset(
                x = maxWidth * 0.79f,
                y = maxHeight * 0.57f
            )
        )
        MapMarker(
            label = stringResource(R.string.robot_map_marker_event_3),
            color = colors.accentRed,
            modifier = Modifier.offset(
                x = maxWidth * 0.19f,
                y = maxHeight * 0.23f
            )
        )
        MapMarker(
            label = stringResource(R.string.robot_map_marker_point_8),
            color = colors.accentPrimary,
            modifier = Modifier.offset(
                x = maxWidth * 0.34f,
                y = maxHeight * 0.28f
            )
        )
    }
}

@Composable
private fun MapMarker(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            color = AppTheme.colors.onAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MapLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(label, color = AppTheme.colors.textSecondary, fontSize = 10.sp)
    }
}
