package com.base.iot.feature.robot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RobotDashboardModelsTest {

    @Test
    fun missionProgressIsBounded() {
        assertEquals(0f, RobotMissionSnapshot(0, -2, 10).progress, 0f)
        assertEquals(1f, RobotMissionSnapshot(0, 12, 10).progress, 0f)
    }

    @Test
    fun emptyMissionHasZeroProgress() {
        assertEquals(0f, RobotMissionSnapshot(0, 0, 0).progress, 0f)
    }

    @Test
    fun sampleDashboardNeverPretendsDeviceIsConnected() {
        assertFalse(RobotDashboardSampleData.state.device.isConnected)
        assertEquals(0f, RobotDashboardSampleData.state.mission.progress, 0f)
        assertEquals(true, RobotDashboardSampleData.state.isSampleData)
    }
}
