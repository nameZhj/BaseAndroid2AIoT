package com.base.iot.feature.robot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OccupancyGridTransformTest {

    @Test
    fun worldGridRoundTripIncludesOriginOffset() {
        val transform = OccupancyGridTransform(
            widthCells = 200,
            heightCells = 100,
            resolutionMetersPerCell = 0.05,
            origin = MapOrigin(12.5, -4.0, 0.0)
        )
        val world = MapWorldPoint(14.0, -2.5)

        val actual = transform.gridToWorld(transform.worldToGrid(world))

        assertEquals(world.xMeters, actual.xMeters, EPSILON)
        assertEquals(world.yMeters, actual.yMeters, EPSILON)
    }

    @Test
    fun worldGridRoundTripIncludesRotatedOrigin() {
        val transform = OccupancyGridTransform(
            widthCells = 120,
            heightCells = 80,
            resolutionMetersPerCell = 0.1,
            origin = MapOrigin(-2.0, 3.0, Math.PI / 2.0)
        )
        val world = MapWorldPoint(-3.5, 5.0)

        val actual = transform.gridToWorld(transform.worldToGrid(world))

        assertEquals(world.xMeters, actual.xMeters, EPSILON)
        assertEquals(world.yMeters, actual.yMeters, EPSILON)
    }

    @Test
    fun viewportProjectionPreservesAspectRatioAndRoundTrips() {
        val transform = OccupancyGridTransform(
            widthCells = 200,
            heightCells = 100,
            resolutionMetersPerCell = 0.05,
            origin = MapOrigin(0.0, 0.0, 0.0)
        )
        val grid = MapGridPoint(75.0, 30.0)
        val viewport = transform.gridToViewport(grid, 600.0, 600.0)
        val actual = transform.viewportToGrid(viewport, 600.0, 600.0)

        assertEquals(grid.column, actual.column, EPSILON)
        assertEquals(grid.row, actual.row, EPSILON)
        assertEquals(225.0, viewport.xPixels, EPSILON)
        assertEquals(240.0, viewport.yPixels, EPSILON)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroResolution() {
        OccupancyGridTransform(10, 10, 0.0, MapOrigin(0.0, 0.0, 0.0))
    }

    @Test
    fun sampleMissionProgressRemainsBounded() {
        assertTrue(RobotMissionSnapshot(0, -2, 10).progress == 0f)
        assertTrue(RobotMissionSnapshot(0, 12, 10).progress == 1f)
    }

    private companion object {
        const val EPSILON = 1e-9
    }
}
