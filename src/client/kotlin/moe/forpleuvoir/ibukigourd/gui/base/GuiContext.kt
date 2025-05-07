package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope

interface GuiContext {

    val userData: MutableMap<String, Any>

}

val GuiScope<out GuiContext>.userData get() = owner().userData
