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

    val arrow: Button = flatButton {
        fixed = true
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
                val h = this.renderElements.firstOrNull { e -> !e.fixed }?.transform?.height ?: 0f
                this.transform.width = it.width + h

                arrow.transform.y = (h + 4f) / 2 - arrow.transform.halfHeight
                arrow.transform.x = transform.width - arrow.transform.y - arrow.transform.halfHeight - arrow.transform.halfWidth

            }
            if (!transform.fixedHeight || !transform.fixedWidth) parent().arrange()
        }
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
                val h = renderElements.firstOrNull { e -> !e.fixed }?.let { e -> e.transform.height + padding1.height } ?: 0f
                for ((index, element) in list.withIndex()) {
                    dropMenu.selectedColor?.let {
                        if (element.mouseHover())
                            renderRect(
                                renderContext.matrixStack,
                                rect(element.transform.worldX, element.transform.worldTop, transform.worldZ, transform.width - h - spacing, element.transform.height),
                                it()
                            )
                    }
                    if (index != list.lastIndex) {
                        renderRect(
                            renderContext.matrixStack,
                            rect(element.transform.worldX, element.transform.worldBottom, transform.worldZ, transform.width - h - spacing, spacing),
                            Colors.GRAY.alpha(0.2f)
                        )
                        if (index == 0) {
                            renderRect(
                                renderContext.matrixStack,
                                rect(transform.worldRight - h + padding1.left / 2 + spacing, element.transform.worldBottom, transform.worldZ, h - padding1.width, spacing),
                                Colors.GRAY.alpha(0.2f)
                            )
                        }
                    }
                }
                renderRect(
                    renderContext.matrixStack,
                    rect(transform.worldRight - h + spacing, transform.worldTop + padding1.top, transform.worldZ, spacing, transform.height - padding1.height),
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
