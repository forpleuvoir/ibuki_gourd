package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.rectBatchRender
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias Plus<T> = T.(T) -> T

typealias Minus<T> = T.(T) -> T

class NumberInput<T : Number>(
    private val valueDelegate: DelegatedValue<T>,
    private val valueMapper: (String) -> T,
    private val valueReceiver: (T) -> Unit = {},
    private val plusAction: Plus<T>,
    private val minusAction: Minus<T>,
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    textColor: ARGBColor = Theme.TEXT_INPUT.TEXT_COLOR,
    hintColor: ARGBColor = Theme.TEXT_INPUT.HINT_COLOR,
    bgShaderColor: ARGBColor = Theme.TEXT_INPUT.BACKGROUND_SHADER_COLOR,
    selectedColor: ARGBColor = Theme.TEXT_INPUT.SELECTED_COLOR,
    cursorColor: ARGBColor = Theme.TEXT_INPUT.CURSOR_COLOR,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : TextInput(width, height, padding, margin, textColor, hintColor, bgShaderColor, selectedColor, cursorColor, textRenderer) {

    var value: T
        get() {
            return valueMapper(text)
        }
        set(value) {
            text = value.toString()
        }

    init {
        valueDelegate.onSetValue = {
            if (it != value) this.value = it
            it
        }

        textPredicate = { Regex("-?\\d+(\\.\\d+)?").matches(if (it.endsWith('.') || it.isEmpty()) "${it}0" else it) }
        onTextChanged = {
            valueReceiver(valueMapper(it))
            valueDelegate.setValue(valueMapper(it))
        }
    }

    val plus = flatButton(hoverColor = { Colors.RED.alpha(50) }) {
        fixed = true
        transform.fixedWidth = true
        transform.fixedHeight = true
        icon(IconTextures.PLUS, Size.create(8f, 8f), Colors.BLACK)

        click {
            text = value.plusAction()
        }
    }

    val minus = flatButton(hoverColor = { Colors.RED.alpha(50) }) {
        fixed = true
        transform.fixedWidth = true
        transform.fixedHeight = true
        icon(IconTextures.MINUS, Size.create(8f, 8f), Colors.BLACK)

        click {

        }
    }

    override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
        val top = if (isWorld) transform.worldTop + padding.top else padding.top
        val bottom = if (isWorld) transform.worldBottom - padding.bottom else transform.height - padding.bottom
        val left = if (isWorld) transform.worldLeft + padding.left else padding.left
        val right = if (isWorld) transform.worldRight - padding.right - plus.transform.width else transform.width - padding.right - plus.transform.width
        return rect(
            vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
        )
    }


    override fun arrange() {
        super.arrange()

        plus.transform.height = (transform.height - 8f) / 2
        plus.transform.width = (transform.height - 8f)
        plus.transform.x = transform.width - plus.transform.width - 4f
        plus.transform.y = transform.top + 4f
        plus.layout.arrange(plus.elements, plus.margin, plus.padding)

        minus.transform.height = plus.transform.height
        minus.transform.width = plus.transform.width
        minus.transform.x = plus.transform.x
        minus.transform.y = plus.transform.bottom + 1f
        minus.layout.arrange(minus.elements, minus.margin, minus.padding)
    }


    override fun onRender(renderContext: RenderContext) {
        super.onRender(renderContext)
        plus.render(renderContext)
        minus.render(renderContext)
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        super.onRenderBackground(renderContext)
        rectBatchRender {
            renderRect(renderContext.matrixStack, rect(plus.transform.worldX - 1f, transform.worldTop + 4f, transform.worldZ, 1f, transform.height - 7f), Colors.GRAY.alpha(0.2f))
            renderRect(renderContext.matrixStack, rect(plus.transform.worldX, plus.transform.worldBottom, transform.worldZ, plus.transform.width, 1f), Colors.GRAY.alpha(0.2f))
        }
    }


}

/**
 * 在当前容器中添加一个[NumberInput]
 * @param T Number
 * @receiver ElementContainer
 * @param valueReceiver (T) -> Unit
 * @param valueMapper (String) -> T
 * @param width Float
 * @param height Float
 * @param padding Margin
 * @param margin Margin?
 * @param scope TextInput.() -> Unit
 * @return TextInput
 */
@OptIn(ExperimentalContracts::class)
fun <T : Number> ElementContainer.numberTextInput(
    valueDelegate: DelegatedValue<T>,
    valueReceiver: (T) -> Unit = {},
    valueMapper: (String) -> T,
    plusAction: Plus<T>,
    minusAction: Minus<T>,
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<T>.() -> Unit = {}
): NumberInput<T> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(NumberInput(valueDelegate, valueMapper, valueReceiver, plusAction, minusAction, width, height, padding, margin).apply(scope))
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.intTextInput(
    valueDelegate: DelegatedValue<Int> = DelegatedValue(0),
    valueReceiver: (Int) -> Unit = {},
    valueMapper: (String) -> Int = { runCatching { it.toInt() }.getOrDefault(0) },
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<Int>.() -> Unit = {}
): NumberInput<Int> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(NumberInput(valueDelegate, valueMapper, valueReceiver, { this + it }, { this - it }, width, height, padding, margin).apply {
        scope()
        textPredicate = { Regex("-?\\d+").matches(it) || it.isEmpty() }
    })
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.floatTextInput(
    valueDelegate: DelegatedValue<Float> = DelegatedValue(0f),
    valueReceiver: (Float) -> Unit = {},
    valueMapper: (String) -> Float = { runCatching { it.toFloat() }.getOrDefault(0f) },
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<Float>.() -> Unit = {}
): NumberInput<Float> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return numberTextInput(valueDelegate, valueReceiver, valueMapper, { this + it }, { this - it }, width, height, padding, margin, scope)
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.doubleTextInput(
    valueDelegate: DelegatedValue<Double> = DelegatedValue(0.0),
    valueReceiver: (Double) -> Unit = {},
    valueMapper: (String) -> Double = { runCatching { it.toDouble() }.getOrDefault(0.0) },
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<Double>.() -> Unit = {}
): NumberInput<Double> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return numberTextInput(valueDelegate, valueReceiver, valueMapper, { this + it }, { this - it }, width, height, padding, margin, scope)
}