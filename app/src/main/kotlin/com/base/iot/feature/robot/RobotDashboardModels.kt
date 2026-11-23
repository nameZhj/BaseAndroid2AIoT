package com.base.iot.feature.robot

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.base.iot.R

enum class RobotEventSeverity { HIGH, NORMAL }

@Immutable
data class RobotDeviceSnapshot(
    @StringRes val nameRes: Int,
    @StringRes val modelRes: Int,
    val batteryPercent: Int,
    val signalPercent: Int,
    val isConnected: Boolean
)

@Immutable
data class RobotMissionSnapshot(
    @StringRes val nameRes: Int,
    val completedPoints: Int,
    val totalPoints: Int
) {
    val progress: Float
        get() = if (totalPoints <= 0) 0f else (completedPoints.toFloat() / totalPoints).coerceIn(0f, 1f)
}

@Immutable
data class RobotInspectionStats(
    val patrolCount: Int,
    val pointCount: Int,
    val resolvedEventCount: Int,
    val routeCoveragePercent: Int
)

@Immutable
data class RobotEventSnapshot(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val detailRes: Int,
    val pointId: String,
    val timestamp: String,
    val severity: RobotEventSeverity,
    val isResolved: Boolean
)

@Immutable
data class RobotDashboardUiState(
    @StringRes val siteNameRes: Int,
    val device: RobotDeviceSnapshot,
    val mission: RobotMissionSnapshot,
    val stats: RobotInspectionStats,
    val events: List<RobotEventSnapshot>,
    val isSampleData: Boolean = true
)

object RobotDashboardSampleData {
    val state = RobotDashboardUiState(
        siteNameRes = R.string.robot_site_name,
        device = RobotDeviceSnapshot(
            nameRes = R.string.robot_device_name,
            modelRes = R.string.robot_device_model,
            batteryPercent = 82,
            signalPercent = 76,
            isConnected = false
        ),
        mission = RobotMissionSnapshot(
            nameRes = R.string.robot_mission_name,
            completedPoints = 0,
            totalPoints = 12
        ),
        stats = RobotInspectionStats(
            patrolCount = 24,
            pointCount = 186,
            resolvedEventCount = 7,
            routeCoveragePercent = 96
        ),
        events = listOf(
            RobotEventSnapshot(
                id = "event-0942",
                titleRes = R.string.robot_event_fire_lane,
                detailRes = R.string.robot_event_fire_lane_detail,
                pointId = "P-03",
                timestamp = "09:42",
                severity = RobotEventSeverity.HIGH,
                isResolved = false
            ),
            RobotEventSnapshot(
                id = "event-0918",
                titleRes = R.string.robot_event_obstacle,
                detailRes = R.string.robot_event_obstacle_detail,
                pointId = "P-07",
                timestamp = "09:18",
                severity = RobotEventSeverity.NORMAL,
                isResolved = true
            ),
            RobotEventSnapshot(
                id = "event-0856",
                titleRes = R.string.robot_event_sensor,
                detailRes = R.string.robot_event_sensor_detail,
                pointId = "P-11",
                timestamp = "08:56",
                severity = RobotEventSeverity.NORMAL,
                isResolved = false
            )
        )
    )
}
