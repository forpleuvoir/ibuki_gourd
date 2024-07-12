package moe.forpleuvoir.ibukigourd.gui.base.element

interface DrawableContainer {

    fun drawableChildren(): List<IGDrawable>

    fun <T : IGDrawable> addDrawableChild(child: T): T

}