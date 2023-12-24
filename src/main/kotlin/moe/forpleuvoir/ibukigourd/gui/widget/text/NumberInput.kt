package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHoverContent
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.rectBatchRender
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias Plus<T> = T.(T) -> T

typealias Minus<T> = T.(T) -> T

class NumberInput<T>(
    private val valueDelegate: DelegatedValue<T>,
    private val valueMapper: (String) -> T,
    private val valueReceiver: (T) -> Unit = {},
    private val plusAction: Plus<T>,
    private val minusAction: Minus<T>,
    private val valueStep: ValueStep<T>,
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
) : TextInput(width, height, padding, margin, textColor, hintColor, bgShaderColor, selectedColor, cursorColor, textRenderer) where T : Comparable<T>, T : Number {

    data class ValueStep<T>(val click: T, val shift: T, val ctrl: T, val alt: T, val mouseScroller: T) where T : Comparable<T>, T : Number


    private var preventRecursion = true

    @OptIn(ExperimentalContracts::class)
    private inline fun preventRecursion(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        preventRecursion = false
        block()
        preventRecursion = true
    }

    var value: T = valueDelegate.getValue()
        set(value) {
            if (field == value) return
            field = value
            selectionStart = 0
            selectionEnd = text.length
            if (preventRecursion) write(value.toString(), false)
        }

    val plus = flatButton(hoverColor = { Color(0XFFDBDBDB.toInt()) }, pressColor = { Colors.GRAY.alpha(0.2f) }) {
        fixed = true
        transform.fixedWidth = true
        transform.fixedHeight = true
        icon(IconTextures.PLUS, Size.create(8f, 8f), Colors.BLACK)

        click {
            value = when {
                InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)   -> value.plusAction(valueStep.shift)
                InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL) -> value.plusAction(valueStep.ctrl)
                InputHandler.hasKeyPressed(Keyboard.LEFT_ALT)     -> value.plusAction(valueStep.alt)
                else                                              -> value.plusAction(valueStep.click)
            }
        }
    }

    val minus = flatButton(hoverColor = { Color(0XFFDBDBDB.toInt()) }, pressColor = { Colors.GRAY.alpha(0.2f) }) {
        fixed = true
        transform.fixedWidth = true
        transform.fixedHeight = true
        icon(IconTextures.MINUS, Size.create(8f, 8f), Colors.BLACK)

        click {
            value = when {
                InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)   -> value.minusAction(valueStep.shift)
                InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL) -> value.minusAction(valueStep.ctrl)
                InputHandler.hasKeyPressed(Keyboard.LEFT_ALT)     -> value.minusAction(valueStep.alt)
                else                                              -> value.minusAction(valueStep.click)
            }
        }
    }

    init {
        value = valueDelegate.getValue()
        preventRecursion {
            text = value.toString()
        }
        valueDelegate.onSetValue = {
            if (it != value) this.value = it
            it
        }
        textPredicate = { Regex("-?\\d+(\\.\\d+)?").matches(if (it.endsWith('.') || it.isEmpty()) "${it}0" else it) }
        onTextChanged = {
            preventRecursion {
                valueDelegate.setValue(valueMapper(it))
                if (it.isEmpty()) text = "0"
            }
            valueReceiver(value)
        }
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        if (!isActive) {
            for (element in handleElements) {
                if (element.mouseScrolling(mouseX, mouseY, amount) == NextAction.Cancel) return NextAction.Cancel
            }
        } else {
            mouseHoverContent {
                value = if (amount > 0) value.plusAction(valueStep.mouseScroller)
                else value.minusAction(valueStep.mouseScroller)
                return NextAction.Cancel
            }
        }
        return NextAction.Continue
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
fun <T> ElementContainer.numberInput(
    valueDelegate: DelegatedValue<T>,
    valueReceiver: (T) -> Unit = {},
    valueMapper: (String) -> T,
    plusAction: Plus<T>,
    minusAction: Minus<T>,
    valueStep: NumberInput.ValueStep<T>,
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<T>.() -> Unit = {}
): NumberInput<T> where T : Comparable<T>, T : Number {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(NumberInput(valueDelegate, valueMapper, valueReceiver, plusAction, minusAction, valueStep, width, height, padding, margin).apply(scope))
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.intInput(
    valueDelegate: DelegatedValue<Int> = DelegatedValue(0),
    valueReceiver: (Int) -> Unit = {},
    valueMapper: (String) -> Int = { runCatching { it.toInt() }.getOrDefault(0) },
    valueStep: NumberInput.ValueStep<Int> = NumberInput.ValueStep(1, 5, 10, 15, 1),
    range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<Int>.() -> Unit = {}
): NumberInput<Int> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(NumberInput(valueDelegate, valueMapper, valueReceiver, { this + it }, { this - it }, valueStep, width, height, padding, margin).apply {
        scope()
        textPredicate = { (Regex("-?\\d+").matches(it) && runCatching { it.toInt() in range }.getOrElse { false }) || it.isEmpty() }
    })
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.floatInput(
    valueDelegate: DelegatedValue<Float> = DelegatedValue(0f),
    valueReceiver: (Float) -> Unit = {},
    valueMapper: (String) -> Float = { runCatching { it.toFloat() }.getOrDefault(0f) },
    valueStep: NumberInput.ValueStep<Float> = NumberInput.ValueStep(1f, 5f, 10f, 15f, 1f),
    range: ClosedFloatingPointRange<Float> = Float.NEGATIVE_INFINITY..Float.POSITIVE_INFINITY,
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<Float>.() -> Unit = {}
): NumberInput<Float> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return numberInput(valueDelegate, valueReceiver, valueMapper, { this + it }, { this - it }, valueStep, width, height, padding, margin, scope).apply {
        textPredicate = {
            val str = if (it.endsWith('.') || it.isEmpty()) "${it}0" else it
            Regex("-?\\d+(\\.\\d+)?").matches(str)
            && runCatching { str.toFloat() in range }.getOrElse { false }
        }
    }
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.doubleInput(
    valueDelegate: DelegatedValue<Double> = DelegatedValue(0.0),
    valueReceiver: (Double) -> Unit = {},
    valueMapper: (String) -> Double = { runCatching { it.toDouble() }.getOrDefault(0.0) },
    valueStep: NumberInput.ValueStep<Double> = NumberInput.ValueStep(1.0, 5.0, 10.0, 15.0, 1.0),
    range: ClosedRange<Double> = Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY,
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: NumberInput<Double>.() -> Unit = {}
): NumberInput<Double> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return numberInput(valueDelegate, valueReceiver, valueMapper, { this + it }, { this - it }, valueStep, width, height, padding, margin, scope).apply {
        textPredicate = {
            val str = if (it.endsWith('.') || it.isEmpty()) "${it}0" else it
            Regex("-?\\d+(\\.\\d+)?").matches(str)
            && runCatching { str.toDouble() in range }.getOrElse { false }
        }
    }
}