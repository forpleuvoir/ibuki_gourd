package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark

@GuiDslMark
interface GuiScope<T : Any> {
    val owner: T
}