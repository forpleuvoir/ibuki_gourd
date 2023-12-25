package moe.forpleuvoir.ibukigourd.gui.screen.tab

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.screen.AbstractScreen
import moe.forpleuvoir.ibukigourd.util.mc

class TabScreen(
    width: Float = mc.window.scaledWidth.toFloat(),
    height: Float = mc.window.scaledHeight.toFloat(),
    val tabs: Collection<Tab>,
    current: Tab = tabs.first()
) : AbstractScreen(width, height) {

    var current: Tab = if (current in tabs) current else throw IllegalArgumentException("current tab must be in tabs")
        set(value) {
            if (value in tabs) {
                field.onExit()
                field = value
                value.onEnter()
            } else throw IllegalArgumentException("current tab must be in tabs")
        }

    val titles: List<Element> by lazy { tabs.map { it.title } }

    val content by this.current::content

    override val handleElements: List<Element>
        get() = buildList {
            addAll(tipList.sortedByDescending { it.priority })
            addAll(super.handleElements)
        }

    override fun init() {
        current.onEnter()
        titles.forEach(::addElement)
        super.init()
    }


    fun switchTab(tab: Tab) {
        current = tab

    }

    override fun onClose() {
        current.onExit()
        super.onClose()
    }

}

