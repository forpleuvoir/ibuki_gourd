package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
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

    val arrow: Button = flatButton(hoverColor = { Colors.BLACK.alpha(0) }, pressColor = { Colors.BLACK.alpha(0) }) {
        fixed = true
        icon(WidgetTextures.DROP_MENU_ARROW_DOWN)
        click {
            expend = true
        }
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

            val arrow: Button = flatButton(hoverColor = { Colors.BLACK.alpha(0) }, pressColor = { Colors.BLACK.alpha(0) }) {
                fixed = true
                icon(WidgetTextures.DROP_MENU_ARROW_UP)
                click {
                    expend = false
                }
            }

            init {
                transform.parent = { this@DropMenu.transform }
                padding(2, 2, 2, 2)
                spacing = 1f
                this@DropMenu.arrow.transform.positionChangeCallback = { x, y, _ ->
                    arrow.transform.x = x
                    arrow.transform.y = y
                }
            }

            override fun init() {
                super.init()

            }

            override fun arrange() {
                layout.arrange(this.subElements, this.margin, this.padding)?.let {
                    if (!transform.fixedWidth) {
                        this.transform.width = it.width
                        this.transform.width += this.padding.width + arrow.transform.width
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

            override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
                if (!expend) return NextAction.Continue
                if (super.onMouseMove(mouseX, mouseY) == NextAction.Cancel) return NextAction.Cancel
                if (mouseHover()) return NextAction.Cancel
                return NextAction.Continue
            }

            override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
                if (super.onMouseClick(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
                if (!mouseHover() && expend) {
                    expend = false
                }
                if (mouseHover()) {
                    return NextAction.Cancel
                }
                return NextAction.Continue
            }

            override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
                if (!expend) return NextAction.Continue
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
                        val maxWidth = list.maxOf { it.transform.width }
                        for ((index, element) in list.withIndex()) {
                            if (index != list.lastIndex)
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
                        renderRect(
                            renderContext.matrixStack,
                            rect(
                                transform.worldLeft + maxWidth + padding1.left + 1,
                                transform.worldTop + this@DropMenu.transform.height - this@DropMenu.padding.bottom / 2,
                                transform.worldZ,
                                arrow.transform.width + padding.right,
                                1f
                            ),
                            Colors.GRAY.alpha(0.2f)
                        )
                    }
                }
            }

            override fun onRenderBackground(renderContext: RenderContext) {
                renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_EXPEND_BACKGROUND)
            }

        }
    }

    override fun init() {
        super.init()
        arrow.transform.x = transform.width - arrow.transform.width - padding.right
        arrow.transform.y = transform.halfHeight - arrow.transform.halfHeight
    }

    fun items(scope: ElementContainer.() -> Unit) {
        tip!!.scope()
    }

    override fun arrange() {
        layout.arrange(this.subElements, margin, padding)?.let {
            if (!transform.fixedWidth) {
                this.transform.width = (it.width + padding.width + arrow.transform.width).coerceAtLeast(tip!!.transform.width)
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
//        val texture = arrowTexture
//        val x = transform.worldRight - transform.halfHeight - texture.uSize / 2
//        val y = transform.worldTop + transform.halfHeight - texture.vSize / 2
//        val rect = rect(x, y, transform.z, texture.uSize, texture.vSize)
//        renderContext.postRender(priority + 1) {
//            renderTexture(matrixStack, rect, texture)
//        }
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