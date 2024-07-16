@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import org.joml.Vector2fc

sealed class PlanarAlignment(val orientation: Orientation = Orientation.Vertical) : Alignment {

    class TopLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.mapPositions(parent.position, sizes)
        }
    }

    class TopCenter(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.peek(
                orientation.mapPositions(parent.position, sizes) { pos, rect -> pos.copy(x = parent.center.x() - rect.halfWidth) },
                orientation.mapPositions(parent.position.copy(x = parent.center.x() - orientation.contentSize(sizes).halfWidth), sizes)
            )
        }
    }

    class TopRight(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.peek(
                orientation.mapPositions(parent.position, sizes) { pos, rect -> pos.copy(x = parent.right - rect.width) },
                orientation.mapPositions(parent.position.copy(x = parent.right - orientation.contentSize(sizes).width), sizes)
            )
        }
    }

    class CenterLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.left
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes),
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class Center(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.center.x() - size.halfWidth
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class CenterRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.right - size.width
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class BottomLeft(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.left
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes),
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomCenter(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.center.x() - size.halfWidth
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.right - size.width
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.mapPositions(rect.position, sizes) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }
}