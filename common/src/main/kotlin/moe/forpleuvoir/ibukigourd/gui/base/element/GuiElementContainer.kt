package moe.forpleuvoir.ibukigourd.gui.base.element

interface GuiElementContainer {

    fun elementChildren(): List<GuiElement>

    fun <T : GuiElement> addElementChild(child: T): T

}