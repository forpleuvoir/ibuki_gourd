package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun ElementContainer.icon(
    texture: () -> WidgetTexture,
    size: () -> Size<Float> = { Size.create(texture().uSize.toFloat(), texture().vSize.toFloat()) },
    shaderColor: ARGBColor = Colors.WHITE,
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(object : AbstractElement() {
        init {
            transform.height = size().height
            transform.width = size().width
            transform.resizeCallback = { _: Float, _: Float ->
                parent().arrange()
            }
        }


        override fun onRenderBackground(renderContext: RenderContext) {
            transform.height = size().height
            transform.width = size().width
            renderTexture(renderContext.matrixStack, transform, texture(), shaderColor)
        }
    }.apply(scope))
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.icon(
    texture: WidgetTexture,
    size: Size<Float> = Size.create(texture.uSize.toFloat(), texture.vSize.toFloat()),
    shaderColor: ARGBColor = Colors.WHITE,
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(object : AbstractElement() {
        init {
            transform.height = size.height
            transform.width = size.width
        }

        override fun onRenderBackground(renderContext: RenderContext) {
            renderTexture(renderContext.matrixStack, transform, texture, shaderColor)
        }
    }.apply(scope))
}