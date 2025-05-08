package moe.forpleuvoir.ibukigourd.gui.base.element

interface DrawableContainer<D : IGDrawable> {

    companion object {

        fun getRenderLayer(container: DrawableContainer<*>): GuiRenderLayer =
            GuiRenderLayer { context, x, y, delta ->
                container.drawableChildren().forEach { drawable ->
                    if (drawable.visible) drawable.render(context, x, y, delta)
                }
            }

    }

    fun drawableChildren(): List<D>

}