@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import org.joml.Vector2fc

sealed class LinearAlignment(val orientation: Orientation) : Alignment

sealed class HorizontalAlignment(orientation: Orientation) : LinearAlignment(orientation) {

    class Left(orientation: Orientation) : HorizontalAlignment(orientation) {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.mapPositions(parent.position, sizes)
        }

    }

    class Center(orientation: Orientation) : HorizontalAlignment(orientation) {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.peek(
                orientation.mapPositions(parent.position, sizes) { pos, box -> pos.copy(x = parent.center.x() - box.halfWidth) },
                orientation.mapPositions(parent.position.copy(x = parent.center.x() - orientation.contentSize(sizes).halfWidth), sizes)
            )
        }

    }

    class Right(orientation: Orientation) : HorizontalAlignment(orientation) {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.peek(
                orientation.mapPositions(parent.position, sizes) { pos, box -> pos.copy(x = parent.right - box.width) },
                orientation.mapPositions(parent.position.copy(x = parent.right - orientation.contentSize(sizes).width), sizes)
            )
        }

    }

}

sealed class VerticalAlignment(orientation: Orientation) : LinearAlignment(orientation) {

    class Top(orientation: Orientation) : VerticalAlignment(orientation) {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.mapPositions(parent.position, sizes)
        }

    }

    class Center(orientation: Orientation) : VerticalAlignment(orientation) {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.left
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes),
                orientation.mapPositions(rect.position, sizes) { pos, box -> pos.copy(y = rect.center.y() - box.halfHeight) }
            )
        }

    }

    class Bottom(orientation: Orientation) : VerticalAlignment(orientation) {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.left
            val rect = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(rect.position, sizes),
                orientation.mapPositions(rect.position, sizes) { pos, box -> pos.copy(y = rect.bottom - box.height) }
            )
        }

    }

}