package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.extensions.translate
import moe.forpleuvoir.ibukigourd.gui.extensions.translateTo
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.widget.Widget
import org.joml.Vector2i

interface IGParentElement : ParentElement, Tickable {
    fun tickableChildren(): List<Tickable> {
        return buildList {
            children().forEach {
                if (it is Tickable) add(it)
            }
        }
    }

    override fun tick() {
        tickableChildren().forEach { it.tick() }
    }

    fun translate(vector2i: Vector2i) {
        translate(vector2i.x, vector2i.y)
    }

    fun translate(x: Int, y: Int) {
        children().forEach {
            if (it is Widget) {
                it.translate(x, y)
            }
        }
    }

    fun translateTo(vector2i: Vector2i) {
        translateTo(vector2i.x, vector2i.y)
    }

    fun translateTo(x: Int, y: Int) {
        children().forEach {
            if (it is Widget) {
                it.translateTo(x, y)
            }
        }
    }

}