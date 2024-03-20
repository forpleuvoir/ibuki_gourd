package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

interface Modifier {

    companion object : ModifierImpl() {

        operator fun invoke(): Modifier = object : ModifierImpl() {
            override fun modify(element: Element) {}
        }

        override fun modify(element: Element) {}

        override fun then(other: Modifier): Modifier {
            return Modifier().then(other)
        }

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
        other.next = this
        return other
    }

}