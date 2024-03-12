package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

interface Modifier {

    companion object : ModifierImpl() {

        override fun modify(element: Element) {}

    }

    var next: Modifier?

    fun apply(element: Element)

    fun modify(element: Element)

    fun then(other: Modifier): Modifier

}

abstract class ModifierImpl : Modifier {

    override var next: Modifier? = null

    override fun apply(element: Element) {
        modify(element)
        next?.apply(element)
    }

    override fun then(other: Modifier): Modifier {
        next = other
        return other
    }

}