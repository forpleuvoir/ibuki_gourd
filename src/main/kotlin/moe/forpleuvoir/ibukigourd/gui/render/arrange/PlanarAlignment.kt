package moe.forpleuvoir.ibukigourd.gui.render.arrange

import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import org.joml.Vector2fc

sealed class PlanarAlignment(val orientation: Orientation = Orientation.Vertical) : Alignment {

    class TopLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            return orientation.calcPosition(parent.position, boxes)
        }
    }

    class TopCenter(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            return orientation.peek(
                orientation.calcPosition(parent.position, boxes) { pos, rect -> pos.copy(x = parent.center.x() - rect.halfWidth) },
                orientation.calcPosition(parent.position.copy(x = parent.center.x() - orientation.contentSize(boxes).halfWidth), boxes)
            )
        }
    }

    class TopRight(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            return orientation.peek(
                orientation.calcPosition(parent.position, boxes) { pos, rect -> pos.copy(x = parent.right - rect.width) },
                orientation.calcPosition(parent.position.copy(x = parent.right - orientation.contentSize(boxes).width), boxes)
            )
        }
    }

    class CenterLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            val size = orientation.contentSize(boxes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.left
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, boxes),
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class Center(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            val size = orientation.contentSize(boxes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.center.x() - size.halfWidth
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class CenterRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            val size = orientation.contentSize(boxes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.right - size.width
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class BottomLeft(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            val size = orientation.contentSize(boxes)
            val y = parent.bottom - size.height
            val x = parent.left
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, boxes),
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomCenter(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            val size = orientation.contentSize(boxes)
            val y = parent.bottom - size.height
            val x = parent.center.x() - size.halfWidth
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, boxes: List<Box>): List<Vector2fc> {
            val size = orientation.contentSize(boxes)
            val y = parent.bottom - size.height
            val x = parent.right - size.width
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.calcPosition(rect.position, boxes) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }
}