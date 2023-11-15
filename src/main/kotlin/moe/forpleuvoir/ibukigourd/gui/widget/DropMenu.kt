package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
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

    var arrowIcon: Element = object : AbstractElement() {

        override fun onRender(renderContext: RenderContext) {
            renderTexture(renderContext.matrixStack, transform, arrowTexture)
        }

    }

    override var onExpand: (() -> Unit)? = {
        arrowIcon.transform.width = arrowTexture.uSize.toFloat()
        arrowIcon.transform.height = arrowTexture.vSize.toFloat()
        tip!!.visible = true
    }

    override var onCollapse: (() -> Unit)? = {
        arrowIcon.transform.width = arrowTexture.uSize.toFloat()
        arrowIcon.transform.height = arrowTexture.vSize.toFloat()
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
                this.transform.width = it.width + arrowIcon.transform.width
                parent().arrange()
            }
            if (!transform.fixedHeight) {
                this.transform.height = it.height + arrowIcon.transform.height
                parent().arrange()
            }
        }
    }

    override var layout: Layout = LinearLayout({ this }, Arrangement.Vertical)
        @Deprecated("Do not set the layout value of DropMenu") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of DropMenu")
        }


    override fun onRenderBackground(renderContext: RenderContext) = renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_BACKGROUND)

    override fun onRenderOverlay(renderContext: RenderContext) {
        val texture = if (expend) {
            WidgetTextures.DROP_MENU_ARROW_UP
        } else {
            WidgetTextures.DROP_MENU_ARROW_DOWN
        }
        val rect = rect(transform.worldPosition, texture.uSize, texture.vSize)
        renderTexture(renderContext.matrixStack, rect, texture)
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
    padding: Margin = Theme.BUTTON.PADDING,
    margin: Margin? = null,
    scope: DropMenu.() -> Unit = {}
): DropMenu {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DropMenu(width, height, padding, margin).apply(scope))
}