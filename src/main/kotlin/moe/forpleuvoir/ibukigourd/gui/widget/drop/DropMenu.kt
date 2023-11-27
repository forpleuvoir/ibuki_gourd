package moe.forpleuvoir.ibukigourd.gui.widget.drop

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.ExpandableElement
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
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
    padding: Padding? = Padding(6),
    margin: Margin? = null,
    var selectedColor: (() -> ARGBColor)? = { Color(0x00A4FF).alpha(75) }
) : ExpandableElement() {

    val arrow: Button = flatButton(hoverColor = { Colors.RED.alpha(100) }) {
        transform.fixedWidth = true
        transform.fixedHeight = true
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
        padding?.let(::padding)
        margin?.let(::margin)
    }

    override fun arrange() {
        layout.arrange(this.subElements, margin, padding)?.let {
            if (!transform.fixedHeight) {
                this.transform.height = it.height
            }
            if (!transform.fixedWidth) {
                this.transform.width = (it.width + (transform.halfHeight - arrow.transform.halfHeight) * 2).coerceAtLeast(tip!!.transform.width)
            }
            if (!transform.fixedHeight || !transform.fixedWidth) parent().arrange()
        }
        arrow.transform.width = this.transform.height - 4f
        arrow.transform.height = this.transform.height - 4f
        arrow.transform.y = transform.halfHeight - arrow.transform.halfHeight
        arrow.transform.x = transform.width - arrow.transform.y - arrow.transform.halfHeight - arrow.transform.halfWidth
        arrow.layout.arrange(arrow.elements, arrow.margin, arrow.padding)
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
    padding: Padding? = Padding(6),
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
    height: Float? = 20f,
    options: NotifiableArrayList<String>,
    current: String = options.first(),
    onSelectionChange: (String) -> Unit,
    padding: Padding? = Padding(left = 6f),
    margin: Margin? = null,
    selectedColor: (() -> ARGBColor)? = { Color(0x00A4FF).alpha(75) },
    scope: DropMenu.() -> Unit = {}
): DropMenu {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DropMenu(width, height, padding, margin, selectedColor).apply {
        var currentItem = current
        text({ currentItem }, width = width?.let { it - this.transform.height })
        if (options.isNotEmpty() && options[0] != currentItem) {
            options.disableNotify {
                options.remove(currentItem)
            }
            options.add(0, currentItem)
        }
        fun ElementContainer.initOptions() {
            val max = options.maxOf { textRenderer.getWidth(it) }
            options.forEach {
                flatButton(
                    padding = padding?.let {
                        val p = this@apply.tip!!.padding
                        Padding(
                            (it.left - p.left).coerceAtLeast(0f),
                            (it.right - p.right).coerceAtLeast(0f),
                            (it.top - p.top).coerceAtLeast(0f),
                            (it.bottom - p.bottom).coerceAtLeast(0f)
                        )
                    },
                    height = height?.let { it - this@apply.tip!!.padding.height },
                    width = width?.let { it - this.transform.height }
                ) {
                    text(it, width = max.toFloat())
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