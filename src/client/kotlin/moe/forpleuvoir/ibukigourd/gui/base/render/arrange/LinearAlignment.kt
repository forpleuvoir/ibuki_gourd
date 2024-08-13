@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.nebula.common.util.primitive.sumOf
import org.joml.Vector2fc

sealed interface LinearAlignment : Alignment {
    fun orientation(): Orientation

    fun arrangement(): Arrangement

}

fun interface HorizontalAlignment : LinearAlignment {

    override fun orientation(): Orientation = Orientation.Horizontal

    override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
        return when (arrangement()) {
            Arrangement.SpaceBetween -> spaceBetween(parent, sizes)
            Arrangement.SpaceAround  -> spaceAround(parent, sizes)
            Arrangement.SpaceEvenly  -> spaceEvenly(parent, sizes)
            Arrangement.Start        -> start(parent, sizes)
            Arrangement.Center       -> center(parent, sizes)
            Arrangement.End          -> end(parent, sizes)
        }
    }

    companion object {

        fun fromArrangement(arrangement: Arrangement): HorizontalAlignment = when (arrangement) {
            Arrangement.SpaceBetween -> SpaceBetween
            Arrangement.SpaceAround  -> SpaceAround
            Arrangement.SpaceEvenly  -> SpaceEvenly
            Arrangement.Start        -> Start
            Arrangement.Center       -> Center
            Arrangement.End          -> End
        }

        data object SpaceBetween : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceBetween
        }

        private fun spaceBetween(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val unitSpaceWidth = ((parent.width - sizes.sumOf { it.width }) / (sizes.lastIndex)).coerceAtLeast(0f)
            var xOffset = 0f
            return sizes.map { (width) ->
                val vec = Vector2f(x = parent.x + xOffset)
                xOffset += width
                xOffset += unitSpaceWidth
                vec
            }
        }

        data object SpaceAround : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceAround
        }

        private fun spaceAround(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val unitSpaceWidth = ((parent.width - sizes.sumOf { it.width }) / (sizes.size * 2)).coerceAtLeast(0f)
            var xOffset = 0f
            return sizes.map { (width) ->
                xOffset += unitSpaceWidth
                val vec = Vector2f(x = parent.x + xOffset)
                xOffset += width
                xOffset += unitSpaceWidth
                vec
            }
        }

        data object SpaceEvenly : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceEvenly
        }

        private fun spaceEvenly(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val unitSpaceWidth = ((parent.width - sizes.sumOf { it.width }) / (sizes.size + 1)).coerceAtLeast(0f)
            var xOffset = 0f
            return sizes.map { (width) ->
                xOffset += unitSpaceWidth
                val vec = Vector2f(x = parent.x + xOffset)
                xOffset += width
                vec
            }
        }

        data object Start : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Start
        }

        private fun start(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            var xOffset = 0f
            return sizes.map { (width) ->
                val vec = Vector2f(x = parent.x + xOffset)
                xOffset += width
                vec
            }
        }

        data object Center : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Center
        }

        private fun center(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val startX = parent.center.x() - sizes.sumOf { it.halfWidth }
            var xOffset = 0f
            return sizes.map { (width) ->
                val vec = Vector2f(x = startX + xOffset)
                xOffset += width
                vec
            }
        }

        data object End : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.End
        }

        private fun end(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val startX = parent.right - sizes.sumOf { it.width }
            var xOffset = 0f
            return sizes.map { (width) ->
                val vec = Vector2f(x = startX + xOffset)
                xOffset += width
                vec
            }
        }

    }

}

fun interface VerticalAlignment : LinearAlignment {

    override fun orientation(): Orientation = Orientation.Horizontal

    override fun align(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
        return when (arrangement()) {
            Arrangement.SpaceBetween -> spaceBetween(parent, sizes)
            Arrangement.SpaceAround  -> spaceAround(parent, sizes)
            Arrangement.SpaceEvenly  -> spaceEvenly(parent, sizes)
            Arrangement.Start        -> start(parent, sizes)
            Arrangement.Center       -> center(parent, sizes)
            Arrangement.End          -> end(parent, sizes)
        }
    }

    companion object {

        fun fromArrangement(arrangement: Arrangement): VerticalAlignment = when (arrangement) {
            Arrangement.SpaceBetween -> SpaceBetween
            Arrangement.SpaceAround  -> SpaceAround
            Arrangement.SpaceEvenly  -> SpaceEvenly
            Arrangement.Start        -> Start
            Arrangement.Center       -> Center
            Arrangement.End          -> End
        }

        data object SpaceBetween : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceBetween
        }

        private fun spaceBetween(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val unitSpaceHeight = ((parent.height - sizes.sumOf { it.height }) / (sizes.lastIndex)).coerceAtLeast(0f)
            var yOffset = 0f
            return sizes.map { (_, height) ->
                val vec = Vector2f(y = parent.y + yOffset)
                yOffset += height
                yOffset += unitSpaceHeight
                vec
            }
        }

        data object SpaceAround : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceAround
        }

        private fun spaceAround(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val unitSpaceHeight = ((parent.height - sizes.sumOf { it.height }) / (sizes.size * 2)).coerceAtLeast(0f)
            var yOffset = 0f
            return sizes.map { (_, height) ->
                yOffset += unitSpaceHeight
                val vec = Vector2f(y = parent.y + yOffset)
                yOffset += height
                yOffset += unitSpaceHeight
                vec
            }
        }

        data object SpaceEvenly : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceEvenly
        }

        private fun spaceEvenly(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val unitSpaceHeight = ((parent.height - sizes.sumOf { it.height }) / (sizes.size + 1)).coerceAtLeast(0f)
            var yOffset = 0f
            return sizes.map { (_, height) ->
                yOffset += unitSpaceHeight
                val vec = Vector2f(y = parent.y + yOffset)
                yOffset += height
                vec
            }
        }

        data object Start : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Start
        }

        private fun start(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            var yOffset = 0f
            return sizes.map { (_, height) ->
                val vec = Vector2f(y = parent.y + yOffset)
                yOffset += height
                vec
            }
        }

        data object Center : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Center
        }

        private fun center(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val startY = parent.center.y() - sizes.sumOf { it.halfHeight }
            var yOffset = 0f
            return sizes.map { (_, height) ->
                val vec = Vector2f(y = startY + yOffset)
                yOffset += height
                vec
            }
        }

        data object End : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.End
        }

        private fun end(parent: Box, sizes: List<Size<Float>>): List<Vector2fc> {
            val startY = parent.bottom - sizes.sumOf { it.height }
            var yOffset = 0f
            return sizes.map { (_, height) ->
                val vec = Vector2f(y = startY + yOffset)
                yOffset += height
                vec
            }
        }

    }

}