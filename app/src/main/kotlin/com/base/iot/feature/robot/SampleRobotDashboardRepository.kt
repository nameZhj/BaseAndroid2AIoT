package com.base.iot.feature.robot

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class SampleRobotDashboardRepository @Inject constructor() : RobotDashboardRepository {
    override val dashboard: Flow<RobotDashboardUiState> =
        flowOf(RobotDashboardSampleData.state)
}
