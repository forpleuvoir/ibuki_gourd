@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import org.joml.Vector2fc

sealed interface BoxAlignment : Alignment {

    val orientation: Orientation

    interface Horizontal : BoxAlignment

    interface Vertical : BoxAlignment

    interface Center : BoxAlignment

    interface Left : Horizontal

    interface Right : Horizontal

    interface Top : Vertical

    interface Bottom : Vertical


    class TopLeft(override val orientation: Orientation = Orientation.Vertical) : Top, Left {

        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.mapPositions(parent.position, sizes)
        }
    }

    class TopCenter(override val orientation: Orientation = Orientation.Vertical) : Top, Center {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.peek(
                orientation.mapPositions(parent.position, sizes) { pos, rect -> pos.copy(x = parent.center.x() - rect.halfWidth) },
                orientation.mapPositions(parent.position.copy(x = parent.center.x() - orientation.contentSize(sizes).halfWidth), sizes)
            )
        }
    }

    class TopRight(override val orientation: Orientation = Orientation.Vertical) : Top, Right {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            return orientation.peek(
                orientation.mapPositions(parent.position, sizes) { pos, rect -> pos.copy(x = parent.right - rect.width) },
                orientation.mapPositions(parent.position.copy(x = parent.right - orientation.contentSize(sizes).width), sizes)
            )
        }
    }

    class CenterLeft(override val orientation: Orientation = Orientation.Vertical) : Center, Left {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.left
            val box = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(box.position, sizes),
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(y = box.center.y() - r.halfHeight) }
            )
        }
    }

    class CenterCenter(override val orientation: Orientation = Orientation.Vertical) : Center {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.center.x() - size.halfWidth
            val box = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(x = box.center.x() - r.halfWidth) },
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(y = box.center.y() - r.halfHeight) }
            )
        }
    }

    class CenterRight(override val orientation: Orientation = Orientation.Vertical) : Center, Right {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.center.y() - size.halfHeight
            val x = parent.right - size.width
            val box = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(x = box.right - r.width) },
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(y = box.center.y() - r.halfHeight) }
            )
        }
    }

    class BottomLeft(override val orientation: Orientation = Orientation.Vertical) : Bottom, Left {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.left
            val box = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(box.position, sizes),
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(y = box.bottom - r.height) }
            )
        }
    }

    class BottomCenter(override val orientation: Orientation = Orientation.Vertical) : Bottom, Center {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.center.x() - size.halfWidth
            val box = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(x = box.center.x() - r.halfWidth) },
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(y = box.bottom - r.height) }
            )
        }
    }

    class BottomRight(override val orientation: Orientation = Orientation.Vertical) : Bottom, Right {
        override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val size = orientation.contentSize(sizes)
            val y = parent.bottom - size.height
            val x = parent.right - size.width
            val box = Box(parent.position.copy(x = x, y = y), size)
            return orientation.peek(
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(x = box.right - r.width) },
                orientation.mapPositions(box.position, sizes) { pos, r -> pos.copy(y = box.bottom - r.height) }
            )
        }
    }
}