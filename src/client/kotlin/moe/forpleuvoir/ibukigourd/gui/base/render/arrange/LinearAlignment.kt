@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import org.joml.Vector2fc

sealed interface LinearAlignment : Alignment {
    fun orientation(): Orientation

    fun arrangement(): Arrangement

}

fun interface HorizontalAlignment : LinearAlignment {

    override fun orientation(): Orientation = Orientation.Horizontal

    override fun align(parent: Box, children: List<Size<Float>>): List<Vector2fc> {
        return arrangement()
            .arrange(parent.x, parent.width, children.map { it.width })
            .map { Vector2f(x = it) }
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

        data object SpaceAround : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceAround
        }

        data object SpaceEvenly : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceEvenly
        }

        data object Start : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Start
        }

        data object Center : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Center
        }

        data object End : HorizontalAlignment {
            override fun arrangement(): Arrangement = Arrangement.End
        }

    }

}

fun interface VerticalAlignment : LinearAlignment {

    override fun orientation(): Orientation = Orientation.Horizontal

    override fun align(parent: Box, children: List<Size<Float>>): List<Vector2fc> {
        return arrangement()
            .arrange(parent.y, parent.height, children.map { it.height })
            .map { Vector2f(y = it) }
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

        data object SpaceAround : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceAround
        }

        data object SpaceEvenly : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.SpaceEvenly
        }

        data object Start : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Start
        }

        data object Center : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.Center
        }

        data object End : VerticalAlignment {
            override fun arrangement(): Arrangement = Arrangement.End
        }

    }

}