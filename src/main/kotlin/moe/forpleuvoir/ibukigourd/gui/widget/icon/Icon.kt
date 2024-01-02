package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun ElementContainer.icon(
    texture: () -> WidgetTexture,
    dimension: () -> Dimension<Float> = { Dimension.create(texture().uSize.toFloat(), texture().vSize.toFloat()) },
    scale: () -> Float = { 1f },
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Icon(texture, dimension, scale, shaderColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun Icon(
    texture: () -> WidgetTexture,
    dimension: () -> Dimension<Float> = { Dimension.create(texture().uSize.toFloat(), texture().vSize.toFloat()) },
    scale: () -> Float = { 1f },
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return object : AbstractElement() {
        init {
            transform.height = dimension().height * scale()
            transform.width = dimension().width * scale()
            transform.resizeCallback = { _: Float, _: Float ->
                parent().arrange()
            }
        }


        override fun onRenderBackground(renderContext: RenderContext) {
            transform.height = dimension().height * scale()
            transform.width = dimension().width * scale()
            renderTexture(renderContext.matrixStack, transform, texture(), shaderColor())
        }
    }.apply(scope)
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.icon(
    texture: WidgetTexture,
    dimension: Dimension<Float> = Dimension.create(texture.uSize.toFloat(), texture.vSize.toFloat()),
    scale: Float = 1f,
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Icon(texture, dimension, scale, shaderColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun Icon(
    texture: WidgetTexture,
    dimension: Dimension<Float> = Dimension.create(texture.uSize.toFloat(), texture.vSize.toFloat()),
    scale: Float = 1f,
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return object : AbstractElement() {
        init {
            transform.height = dimension.height * scale
            transform.width = dimension.width * scale
        }

        override fun onRenderBackground(renderContext: RenderContext) {
            renderTexture(renderContext.matrixStack, transform, texture, shaderColor())
        }
    }.apply(scope)
}