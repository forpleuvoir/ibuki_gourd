package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.context.extension.batchRenderTexture
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun ElementContainer.icon(
    texture: () -> WidgetTexture,
    size: () -> Size<Float> = { Size(texture().uSize.toFloat(), texture().vSize.toFloat()) },
    scale: () -> Float = { 1f },
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Icon(texture, size, scale, shaderColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun Icon(
    texture: () -> WidgetTexture,
    size: () -> Size<Float> = { Size(texture().uSize.toFloat(), texture().vSize.toFloat()) },
    scale: () -> Float = { 1f },
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return object : AbstractElement() {
        init {
            transform.height = size().height * scale()
            transform.width = size().width * scale()
            transform.subscribeSizeChange { _, _ ->
                //todo 重新布局父元素
            }
        }

        override fun onRenderBackground(renderContext: RenderContext) {
            transform.height = size().height * scale()
            transform.width = size().width * scale()
            renderContext.batchRenderTexture { context ->
                context.drawTexture(transform, texture(), shaderColor())
            }
        }
    }.apply(scope)
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.icon(
    texture: WidgetTexture,
    size: Size<Float> = Size(texture.uSize.toFloat(), texture.vSize.toFloat()),
    scale: Float = 1f,
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Icon(texture, size, scale, shaderColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun Icon(
    texture: WidgetTexture,
    size: Size<Float> = Size(texture.uSize.toFloat(), texture.vSize.toFloat()),
    scale: Float = 1f,
    shaderColor: () -> ARGBColor = { Colors.WHITE },
    scope: AbstractElement.() -> Unit = {}
): AbstractElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return object : AbstractElement() {
        init {
            transform.height = size.height * scale
            transform.width = size.width * scale
        }

        override fun onRenderBackground(renderContext: RenderContext) {
            renderContext.batchRenderTexture { context ->
                context.drawTexture(transform, texture, shaderColor())
            }
        }
    }.apply(scope)
}