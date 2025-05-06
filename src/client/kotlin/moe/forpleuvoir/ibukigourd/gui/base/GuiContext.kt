package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope

interface GuiContext {

    val userData: MutableMap<String, Any>

    var layer: GuiLayer

    fun clearLayer()

}

val GuiScope<out GuiContext>.userData get() = owner().userData
