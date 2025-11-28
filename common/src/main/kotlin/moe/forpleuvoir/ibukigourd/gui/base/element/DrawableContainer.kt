package moe.forpleuvoir.ibukigourd.gui.base.element

interface DrawableContainer {

    fun renderableChildren(): List<GuiRenderable>

    fun <T : GuiRenderable> addRenderableChild(child: T): T

}