package moe.forpleuvoir.ibukigourd.gui.base.element

interface GuiRenderableContainer {

    fun renderableChildren(): List<GuiRenderable>

    fun <T : GuiRenderable> addRenderableChild(child: T): T

}