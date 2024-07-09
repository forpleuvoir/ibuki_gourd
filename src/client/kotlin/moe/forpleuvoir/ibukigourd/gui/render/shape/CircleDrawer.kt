package moe.forpleuvoir.ibukigourd.gui.render.shape

import moe.forpleuvoir.ibukigourd.util.Point
import kotlin.math.atan2

/**
 * Calculates the points within a circle given the center coordinates and radius.
 *
 * @param x0 The x-coordinate of the center of the circle.
 * @param y0 The y-coordinate of the center of the circle.
 * @param radius The radius of the circle.
 * @param octant One or more octants to include in the result. The octant number represents the position of the point
 * within a circle (1 to 8). For example, octant 1 represents the 45-degree arc from 0 to 45 degrees.
 *
 * @return A set of Point objects representing the points within the circle.
 */
fun pointsInCircle(x0: Int, y0: Int, radius: Int, vararg octant: Int = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8)): Set<Point> {
    pointsInCircleCache[PointsInCircleData(x0, y0, radius, octant)]?.let { return it }
    val points = mutableSetOf<Point>()
    var x = radius
    var y = 0
    var decisionOver2 = 1 - x

    while (y <= x) {
        if (1 in octant) points.add(Point(x + x0, y + y0)) // Octant 1
        if (2 in octant) points.add(Point(y + x0, x + y0)) // Octant 2
        if (3 in octant) points.add(Point(-y + x0, x + y0)) // Octant 3
        if (4 in octant) points.add(Point(-x + x0, y + y0)) // Octant 4
        if (5 in octant) points.add(Point(-x + x0, -y + y0)) // Octant 5
        if (6 in octant) points.add(Point(-y + x0, -x + y0)) // Octant 6
        if (7 in octant) points.add(Point(x + x0, -y + y0)) // Octant 7
        if (8 in octant) points.add(Point(y + x0, -x + y0)) // Octant 8

        y++
        decisionOver2 += if (decisionOver2 <= 0) {
            2 * y + 1
        } else {
            x--
            2 * (y - x) + 1
        }
    }
    pointsInCircleCache[PointsInCircleData(x0, y0, radius, octant)] = points
    if (pointsInCircleCache.size > pointsInCircleCacheSize) pointsInCircleCache.remove(pointsInCircleCache.keys.first())
    return points
}

private const val pointsInCircleCacheSize: Int = 50

private data class PointsInCircleData(
    val x0: Int,
    val y0: Int,
    val radius: Int,
    val octant: IntArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PointsInCircleData

        if (x0 != other.x0) return false
        if (y0 != other.y0) return false
        if (radius != other.radius) return false
        if (!octant.contentEquals(other.octant)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x0
        result = 31 * result + y0
        result = 31 * result + radius
        result = 31 * result + octant.contentHashCode()
        return result
    }
}

private val pointsInCircleCache = mutableMapOf<PointsInCircleData, Set<Point>>()

/**
 * Draws a circle within the specified angle range.
 *
 * @param x0 The x-coordinate of the circle's center.
 * @param y0 The y-coordinate of the circle's center.
 * @param radius The radius of the circle.
 * @param angleRange The angle range in which to draw the circle, specified as a closed floating point range.
 * @return A set of points that represent the coordinates of the pixels within the circle and within the specified angle range.
 */
fun pointsInCircleRange(x0: Int, y0: Int, radius: Int, angleRange: ClosedFloatingPointRange<Double>): Set<Point> {
    pointsInCircleRangeCache[PointsInCircleRangeCacheData(x0, y0, radius, angleRange)]?.let { return it }
    val points = mutableSetOf<Point>()
    var x = radius
    var y = 0
    var decisionOver2 = 1 - x

    val start = angleRange.start.coerceAtMost(angleRange.endInclusive)
    val end = angleRange.start.coerceAtLeast(angleRange.endInclusive)

    while (y <= x) {
        sequenceOf(
            Point(x + x0, y + y0), // Octant 1
            Point(y + x0, x + y0), // Octant 2
            Point(-x + x0, y + y0), // Octant 4
            Point(-y + x0, x + y0), // Octant 3
            Point(-x + x0, -y + y0), // Octant 5
            Point(-y + x0, -x + y0), // Octant 6
            Point(x + x0, -y + y0), // Octant 7
            Point(y + x0, -x + y0)  // Octant 8
        ).filter {
            Math.toDegrees(atan2((it.y - y0).toDouble(), (it.x - x0).toDouble())).let { angle ->
                (if (angle < 0) angle + 360 else angle) in start..end
            }
        }.forEach {
            points.add(it)
        }

        y++
        decisionOver2 += if (decisionOver2 <= 0) {
            2 * y + 1
        } else {
            x--
            2 * (y - x) + 1
        }
    }
    pointsInCircleRangeCache[PointsInCircleRangeCacheData(x0, y0, radius, angleRange)] = points
    if (pointsInCircleRangeCache.size > pointsInCircleRangeCacheSize) pointsInCircleRangeCache.remove(pointsInCircleRangeCache.keys.first())
    return points
}

private const val pointsInCircleRangeCacheSize: Int = 50

private data class PointsInCircleRangeCacheData(
    val x0: Int,
    val y0: Int,
    val radius: Int,
    val angleRange: ClosedFloatingPointRange<Double>,
)

private val pointsInCircleRangeCache = mutableMapOf<PointsInCircleRangeCacheData, Set<Point>>()