package moe.forpleuvoir.ibukigourd.gui.widget.tabs

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

interface Tab {

    val tab: Element

    var onEnter: () -> Unit

    var onExit: () -> Unit

    val content: Element

}


