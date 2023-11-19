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
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.rectBatchRender
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.NotifiableArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class DropMenu(
    width: Float? = null,
    height: Float? = null,
    padding: Margin = Theme.BUTTON.PADDING,
    margin: Margin? = null,
    var selectedColor: (() -> ARGBColor)? = { Color(0x00A4FF).alpha(75) }
) : ExpandableElement() {

    val arrow: Button = flatButton {
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

            val arrow: Button = flatButton {
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
                        val h = renderElements.firstOrNull { e -> !e.fixed }?.let { e -> e.transform.height + padding1.height } ?: 0f
                        for ((index, element) in list.withIndex()) {
                            selectedColor?.let {
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
    }

    fun items(scope: ElementContainer.() -> Unit) {
        tip!!.scope()
    }

    override fun arrange() {
        layout.arrange(this.subElements, margin, padding)?.let {
            if (!transform.fixedHeight) {
                this.transform.height = it.height
            }
            if (!transform.fixedWidth) {
                this.transform.width = (it.width + (transform.halfHeight - arrow.transform.halfHeight) * 2).coerceAtLeast(tip!!.transform.width)
                arrow.transform.y = transform.halfHeight - arrow.transform.halfHeight
                arrow.transform.x = transform.width - arrow.transform.y - arrow.transform.halfHeight - arrow.transform.halfWidth
            }
            if (!transform.fixedHeight || !transform.fixedWidth) parent().arrange()
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
        renderRect(
            renderContext.matrixStack,
            rect(
                arrow.transform.worldX - (arrow.transform.y + arrow.transform.halfHeight) + arrow.transform.halfWidth + tip!!.spacing,
                transform.worldTop + 2f,
                transform.worldZ,
                tip!!.spacing,
                transform.height - 4f
            ),
            Colors.GRAY.alpha(0.2f)
        )
    }


    override fun onRender(renderContext: RenderContext) {
        if (expend) return
        renderBackground.invoke(renderContext)
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
    selectedColor: (() -> ARGBColor)? = { Color(0x00A4FF).alpha(75) },
    scope: DropMenu.() -> Unit = {}
): DropMenu {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DropMenu(width, height, padding, margin, selectedColor).apply(scope))
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.dropSelector(
    width: Float? = null,
    height: Float? = null,
    options: NotifiableArrayList<String>,
    current: String = options.first(),
    onSelectionChange: (String) -> Unit,
    padding: Margin = Margin(6),
    margin: Margin? = null,
    selectedColor: (() -> ARGBColor)? = { Color(0x00A4FF).alpha(75) },
    scope: DropMenu.() -> Unit = {}
): DropMenu {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DropMenu(width, height, padding, margin, selectedColor).apply {
        var currentItem = current
        text({ currentItem })
        if (options.isNotEmpty() && options[0] != currentItem) {
            options.disableNotify {
                options.remove(currentItem)
            }
            options.add(0, currentItem)
        }
        fun ElementContainer.initOptions() {
            val max = options.maxOf { textRenderer.getWidth(it) }
            options.forEach {
                flatButton(padding = Margin(4)) {
                    text(it, width = max + 8f)
                    click {
                        currentItem = it
                        onSelectionChange.invoke(it)
                        if (options.isNotEmpty() && options[0] != currentItem) {
                            options.disableNotify {
                                options.remove(it)
                            }
                            options.add(0, it)
                        }
                        this@apply.expend = false
                    }
                }
            }
        }
        items {
            options.subscribe {
                this.clearElements { !it.fixed }
                initOptions()
                this.init()
                this.arrange()
            }
            this.initOptions()
        }
        scope()
    })
}