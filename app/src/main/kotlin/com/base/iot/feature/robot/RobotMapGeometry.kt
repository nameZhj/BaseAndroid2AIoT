package com.base.iot.feature.robot

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class MapOrigin(
    val xMeters: Double,
    val yMeters: Double,
    val yawRadians: Double
)

data class MapWorldPoint(
    val xMeters: Double,
    val yMeters: Double
)

data class MapGridPoint(
    val column: Double,
    val row: Double
)

data class MapViewportPoint(
    val xPixels: Double,
    val yPixels: Double
)

/**
 * Converts world coordinates into top-left-origin image pixels.
 * Row zero is the top image row; the map origin is the lower-left world origin
 * rotated by origin.yawRadians.
 */
class OccupancyGridTransform(
    val widthCells: Int,
    val heightCells: Int,
    val resolutionMetersPerCell: Double,
    val origin: MapOrigin
) {
    init {
        require(widthCells > 0) { "widthCells must be positive" }
        require(heightCells > 0) { "heightCells must be positive" }
        require(resolutionMetersPerCell.isFinite() && resolutionMetersPerCell > 0.0) {
            "resolutionMetersPerCell must be finite and positive"
        }
        require(origin.xMeters.isFinite() && origin.yMeters.isFinite() && origin.yawRadians.isFinite()) {
            "Map origin must contain finite values"
        }
    }

    fun worldToGrid(point: MapWorldPoint): MapGridPoint {
        val dx = point.xMeters - origin.xMeters
        val dy = point.yMeters - origin.yMeters
        val cosYaw = cos(origin.yawRadians)
        val sinYaw = sin(origin.yawRadians)
        val localX = cosYaw * dx + sinYaw * dy
        val localY = -sinYaw * dx + cosYaw * dy

        return MapGridPoint(
            column = localX / resolutionMetersPerCell,
            row = heightCells - 1.0 - localY / resolutionMetersPerCell
        )
    }

    fun gridToWorld(point: MapGridPoint): MapWorldPoint {
        val localX = point.column * resolutionMetersPerCell
        val localY = (heightCells - 1.0 - point.row) * resolutionMetersPerCell
        val cosYaw = cos(origin.yawRadians)
        val sinYaw = sin(origin.yawRadians)

        return MapWorldPoint(
            xMeters = origin.xMeters + cosYaw * localX - sinYaw * localY,
            yMeters = origin.yMeters + sinYaw * localX + cosYaw * localY
        )
    }

    /** Fits the complete map into the viewport without changing its aspect ratio. */
    fun gridToViewport(
        point: MapGridPoint,
        viewportWidthPixels: Double,
        viewportHeightPixels: Double
    ): MapViewportPoint {
        val projection = projection(viewportWidthPixels, viewportHeightPixels)
        return MapViewportPoint(
            xPixels = projection.offsetX + point.column * projection.scale,
            yPixels = projection.offsetY + point.row * projection.scale
        )
    }

    fun viewportToGrid(
        point: MapViewportPoint,
        viewportWidthPixels: Double,
        viewportHeightPixels: Double
    ): MapGridPoint {
        val projection = projection(viewportWidthPixels, viewportHeightPixels)
        return MapGridPoint(
            column = (point.xPixels - projection.offsetX) / projection.scale,
            row = (point.yPixels - projection.offsetY) / projection.scale
        )
    }

    private fun projection(widthPixels: Double, heightPixels: Double): ViewportProjection {
        require(widthPixels.isFinite() && widthPixels > 0.0) { "Viewport width must be positive" }
        require(heightPixels.isFinite() && heightPixels > 0.0) { "Viewport height must be positive" }

        val scale = min(
            widthPixels / widthCells.toDouble(),
            heightPixels / heightCells.toDouble()
        )
        return ViewportProjection(
            scale = scale,
            offsetX = (widthPixels - widthCells * scale) / 2.0,
            offsetY = (heightPixels - heightCells * scale) / 2.0
        )
    }

    private data class ViewportProjection(
        val scale: Double,
        val offsetX: Double,
        val offsetY: Double
    )
}
