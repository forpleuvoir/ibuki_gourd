package moe.forpleuvoir.ibukigourd.render.base.arrange

import moe.forpleuvoir.ibukigourd.render.base.math.copy
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rect
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import org.joml.Vector3fc

sealed class PlanarAlignment(val orientation: Orientation = Orientation.Vertical) : Alignment {

    class TopLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            return orientation.calcPosition(parent.position, rectangles)
        }
    }

    class TopCenter(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            return orientation.peek(
                orientation.calcPosition(parent.position, rectangles) { pos, rect -> pos.copy(x = parent.center.x() - rect.halfWidth) },
                orientation.calcPosition(parent.position.copy(x = parent.center.x() - orientation.contentSize(rectangles).halfWidth), rectangles)
            )
        }
    }

    class TopRight(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            return orientation.peek(
                orientation.calcPosition(parent.position, rectangles) { pos, rect -> pos.copy(x = parent.right - rect.width) },
                orientation.calcPosition(parent.position.copy(x = parent.right - orientation.contentSize(rectangles).width), rectangles)
            )
        }
    }

    class CenterLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.center.y() - size.halfHeight
            val x = parent.left
            val rect = Rect(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, rectangles),
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class Center(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.center.y() - size.halfHeight
            val x = parent.center.x() - size.halfWidth
            val rect = Rect(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class CenterRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.center.y() - size.halfHeight
            val x = parent.right - size.width
            val rect = Rect(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class BottomLeft(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.left
            val rect = Rect(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, rectangles),
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomCenter(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.center.x() - size.halfWidth
            val rect = Rect(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.right - size.width
            val rect = Rect(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }
}