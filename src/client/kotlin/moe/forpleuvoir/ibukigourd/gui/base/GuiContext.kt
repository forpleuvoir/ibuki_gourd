package moe.forpleuvoir.ibukigourd.gui.base

interface GuiContext {

    val customData: MutableMap<String, Any>

    var layer: GuiLayer

    fun clearLayer()

}