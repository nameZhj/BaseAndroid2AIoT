package com.base.iot.feature.robot

import kotlinx.coroutines.flow.Flow

interface RobotDashboardRepository {
    val dashboard: Flow<RobotDashboardUiState>
}
