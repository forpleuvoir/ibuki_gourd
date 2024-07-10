package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.IGParentElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ScreenRect
import java.util.function.Consumer

abstract class IGParentWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : IGWidget(x, y, width, height), IGParentElement {

    private val children: MutableList<IGWidget> = ArrayList()

    private var focusedElement: Element? = null

    private var dragging = false

    override fun children(): MutableList<out IGWidget> {
        return children
    }

    override fun forEachElement(consumer: Consumer<IGWidget>) {
        children.forEach {
            consumer.accept(it)
        }
    }

    override fun tick() {}

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        children.forEach {
            it.render(context, mouseX, mouseY, delta)
        }
    }

    override fun setFocused(focused: Element?) {
        this.focusedElement = focused
    }


    override fun setFocused(focused: Boolean) {
        super<IGWidget>.setFocused(focused)
    }

    override fun isFocused(): Boolean {
        return super<IGWidget>.isFocused()
    }

    override fun getNavigationFocus(): ScreenRect {
        return super<IGWidget>.getNavigationFocus()
    }

    override fun isDragging(): Boolean = this.dragging

    override fun setDragging(dragging: Boolean) {
        this.dragging = dragging
    }

    override fun getFocused(): Element? = this.focusedElement

}