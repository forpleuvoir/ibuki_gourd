package moe.forpleuvoir.ibukigourd.gui.widget.drop

import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.rectBatchRender
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.Colors

class DropTip(private val dropMenu: DropMenu) : Tip({ dropMenu }, { dropMenu.screen() }) {

    val arrow: Button = flatButton(hoverColor = { Colors.RED.alpha(100) }) {
        fixed = true
        transform.fixedWidth = true
        transform.fixedHeight = true
        icon(WidgetTextures.DROP_MENU_ARROW_UP)
        click {
            dropMenu.expend = false
        }
    }

    init {
        transform.parent = { dropMenu.transform }
        padding(2, 2, 2, 2)
        spacing = 1f
    }

    override fun arrange() {
        layout.arrange(this.subElements, this.margin, this.padding)?.let {
            if (!transform.fixedHeight) {
                this.transform.height = it.height
            }
            if (!transform.fixedWidth) {
                this.transform.width = it.width - padding.width + dropMenu.transform.height - dropMenu.padding.height + spacing

                arrow.transform.width = dropMenu.arrow.transform.width
                arrow.transform.height = dropMenu.arrow.transform.height

                arrow.transform.y = dropMenu.arrow.transform.y
                arrow.transform.x = dropMenu.arrow.transform.x

            }
            if (!transform.fixedHeight || !transform.fixedWidth) parent().arrange()
        }
        arrow.layout.arrange(arrow.elements, arrow.margin, arrow.padding)
    }

    override var visible: Boolean = false
        get() = super.visible
        set(value) {
            field = value
            if (value) {
                push()
            } else {
                pop()
            }
        }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (!dropMenu.expend) return NextAction.Continue
        if (super.onMouseMove(mouseX, mouseY) == NextAction.Cancel) return NextAction.Cancel
        if (mouseHover()) return NextAction.Cancel
        return NextAction.Continue
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (super.onMouseClick(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
        if (!mouseHover() && dropMenu.expend) {
            dropMenu.expend = false
        }
        if (mouseHover()) {
            return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        if (!dropMenu.expend) return NextAction.Continue
        if (super.onMouseScrolling(mouseX, mouseY, amount) == NextAction.Cancel) return NextAction.Cancel
        if (mouseHover()) return NextAction.Cancel
        return NextAction.Continue
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderContext.postRender {
            renderBackground.invoke(renderContext)
            for (element in renderElements) {
                element.render(renderContext)
            }
            renderOverlay.invoke(renderContext)
        }
    }

    override fun onRenderOverlay(renderContext: RenderContext) {
        val padding1 = this.padding
        rectBatchRender {
            renderElements.filter { it != arrow }.let { list ->
                for ((index, element) in list.withIndex()) {
                    //选择颜色
                    dropMenu.selectedColor?.let {
                        if (element.mouseHover())
                            renderRect(
                                renderContext.matrixStack,
                                rect(element.transform.worldX, element.transform.worldTop, transform.worldZ, element.transform.width, element.transform.height),
                                it()
                            )
                    }
                    if (index != list.lastIndex) {
                        //元素下的横线
                        renderRect(
                            renderContext.matrixStack,
                            rect(element.transform.worldX, element.transform.worldBottom, transform.worldZ, element.transform.width, spacing),
                            Colors.GRAY.alpha(0.2f)
                        )
                        if (index == 0) {
                            //第一条横线延伸到箭头按钮下
                            renderRect(
                                renderContext.matrixStack,
                                rect(
                                    arrow.transform.worldX,
                                    element.transform.worldBottom,
                                    transform.worldZ,
                                    arrow.transform.width,
                                    spacing
                                ),
                                Colors.GRAY.alpha(0.2f)
                            )
                        }
                    }
                }
                //竖线
                renderRect(
                    renderContext.matrixStack,
                    rect(arrow.transform.worldX - spacing, transform.worldTop + padding1.top, transform.worldZ, spacing, transform.height - padding1.height),
                    Colors.GRAY.alpha(0.2f)
                )
            }
        }
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_EXPEND_BACKGROUND)
    }

}

fun DropMenu.items(scope: ElementContainer.() -> Unit) {
    this.tip = DropTip(this)
    this.tip!!.scope()
}
