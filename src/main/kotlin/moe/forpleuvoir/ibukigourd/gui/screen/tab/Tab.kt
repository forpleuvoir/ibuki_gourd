package moe.forpleuvoir.ibukigourd.gui.screen.tab

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer

interface Tab {

    val title: Element

    var onEnter: () -> Unit

    var onExit: () -> Unit

    val content: Element

}

abstract class AbstractTab() : Tab {

    override lateinit var title: Element


}

fun TabScreen.tab(
    titleScope: ElementContainer.() -> Unit,
    contentScope: ElementContainer.() -> Unit,
    scope: Tab.() -> Unit = {}
): Tab {
    return object : Tab {
        override val title: Element = object : AbstractElement() {}.apply(titleScope)
        override var onEnter: () -> Unit = {}
        override var onExit: () -> Unit = {}
        override val content: Element get() = object : AbstractElement() {}.apply(contentScope)
    }.apply(scope)
}

