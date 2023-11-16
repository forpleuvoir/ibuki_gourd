package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.rectBatchRender
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class DropMenu(
    width: Float? = null,
    height: Float? = null,
    padding: Margin = Theme.BUTTON.PADDING,
    margin: Margin? = null,
) : ExpandableElement() {

    val arrowTexture: WidgetTexture
        get() = if (expend) {
            WidgetTextures.DROP_MENU_ARROW_UP
        } else {
            WidgetTextures.DROP_MENU_ARROW_DOWN
        }

    override var onExpand: (() -> Unit)? = {
        tip!!.visible = true
    }

    override var onCollapse: (() -> Unit)? = {
        tip!!.visible = false
    }

    init {
        transform.width = width?.also { transform.fixedWidth = true } ?: 20f
        transform.height = height?.also { transform.fixedHeight = true } ?: 20f
        padding(padding)
        margin?.let(::margin)
        tip = object : Tip({ this }, { this.screen() }) {
            init {
                transform.parent = { this@DropMenu.transform }
                padding(2, 2, 2, 2)
                spacing = 1f
            }

            override fun arrange() {
                layout.arrange(this.subElements, this.margin, this.padding)?.let {
                    if (!transform.fixedWidth) {
                        this.transform.width = it.width + renderElements.firstOrNull().let { e -> e?.transform?.height ?: 0f }
                        parent().arrange()
                    }
                    if (!transform.fixedHeight) {
                        this.transform.height = it.height
                        parent().arrange()
                    }
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

            override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
                if (!mouseHover() && expend) {
                    expend = false
                }
                return super.onMouseClick(mouseX, mouseY, button)
            }

            override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
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
                    val maxWidth = renderElements.maxOf { it.transform.width }
                    for ((index, element) in renderElements.withIndex()) {
                        if (index != renderElements.lastIndex)
                            renderRect(
                                renderContext.matrixStack,
                                rect(element.transform.worldX, element.transform.worldBottom, transform.worldZ, maxWidth, 1f),
                                Colors.GRAY.alpha(0.2f)
                            )
                    }
                    renderRect(
                        renderContext.matrixStack,
                        rect(transform.worldLeft + maxWidth + padding1.left, transform.worldTop + padding1.top, transform.worldZ, 1f, transform.height - padding1.height),
                        Colors.GRAY.alpha(0.2f)
                    )
                }
            }

            override fun onRenderBackground(renderContext: RenderContext) {
                renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_EXPEND_BACKGROUND)
            }

        }
    }

    fun items(scope: ElementContainer.() -> Unit) {
        tip!!.scope()
    }

    override fun arrange() {
        layout.arrange(this.subElements, margin, padding)?.let {
            if (!transform.fixedWidth) {
                this.transform.width = it.width.coerceAtLeast(tip!!.transform.width)
                parent().arrange()
            }
            if (!transform.fixedHeight) {
                this.transform.height = it.height
                parent().arrange()
            }
        }
    }

    override var layout: Layout = LinearLayout({ this }, Arrangement.Vertical)
        @Deprecated("Do not set the layout value of DropMenu") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of DropMenu")
        }


    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_BACKGROUND)
    }

    override fun onRenderOverlay(renderContext: RenderContext) {
        val texture = arrowTexture
        val x = transform.worldRight - transform.halfHeight - texture.uSize / 2
        val y = transform.worldTop + transform.halfHeight - texture.vSize / 2
        val rect = rect(x, y, transform.z, texture.uSize, texture.vSize)
        renderContext.postRender(priority + 1) {
            renderTexture(matrixStack, rect, texture)
        }
    }


    override fun onRender(renderContext: RenderContext) {
        if (!expend) renderBackground.invoke(renderContext)
        for (element in renderElements) element.render(renderContext)
        renderOverlay.invoke(renderContext)
    }

}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.dropMenu(
    width: Float? = null,
    height: Float? = null,
    padding: Margin = Margin(4),
    margin: Margin? = null,
    scope: DropMenu.() -> Unit = {}
): DropMenu {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DropMenu(width, height, padding, margin).apply(scope))
}