package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Direction
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseDragEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SCROLLER_BACKGROUND
import moe.forpleuvoir.ibukigourd.gui.tip.hoverTip
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonThemes
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.arrange.Orientation
import moe.forpleuvoir.ibukigourd.render.base.arrange.peek
import moe.forpleuvoir.ibukigourd.render.helper.BatchDrawScope.renderTexture
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.clamp
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs

open class Scroller(
    length: Float,
    thickness: Float = 10f,
    var amountStep: () -> Float,
    var totalAmount: () -> Float,
    /**
     * 滚动条所占总长度的百分比 Range(0f..1f)
     */
    var barLength: () -> Float,
    private val Orientation: Orientation = Orientation.Vertical,
    var color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
) : ClickableElement() {

    val bar: ButtonWidget = button(color = barColor, pressOffset = 0f, theme = ButtonThemes.ScrollerBar) {
        fixed = true
    }

    override var visible: Boolean = true

    init {
        transform.fixedWidth = true
        transform.fixedHeight = true
        transform.resizeCallback = { width, height ->
            Orientation.peek(
                {
                    bar.transform.width = width
                }, {
                    bar.transform.height = height
                })
        }
        Orientation.peek(
            {
                transform.width = thickness
                transform.height = length
            }, {
                transform.width = length
                transform.height = thickness
            }
        )
    }

    var amount: Float
        get() = (totalAmount() * progress).clamp(0f..totalAmount())
        set(value) {
            val fixedValue = value.clamp(0f..totalAmount())
            val barPosition = scrollerLength * if (totalAmount() == 0f) 0f else fixedValue / totalAmount()
            Orientation.peek(
                {
                    bar.transform.y = barPosition
                }, {
                    bar.transform.x = barPosition
                })
            amountReceiver?.invoke(fixedValue)
        }

    var amountReceiver: ((amount: Float) -> Unit)? = null

    open val scrollerLength: Float
        get() = Orientation.peek(
            this.transform.height - (barLength() * this.transform.height),
            this.transform.width - (barLength() * this.transform.width)
        )

    val barPositionRange: ClosedFloatingPointRange<Float> get() = 0f..scrollerLength

    /**
     * 进度 Range(0.0f..1.0f)
     */
    var progress: Float
        set(value) {
            val fixed = value.clamp(0f..1f)
            amount = (totalAmount() * fixed).clamp(0f..totalAmount())
        }
        get() {
            if (scrollerLength == 0f) return 1f
            return (Orientation.peek({ bar.transform.y }, { bar.transform.x }) / scrollerLength).clamp(0f..1f)
        }


    private fun calcBarLength() {
        Orientation.peek(
            {
                bar.transform.height = barLength() * this.transform.height
            }, {
                bar.transform.width = barLength() * this.transform.width
            }
        )
    }

    override fun tick() {
        super.tick()
        if (pressed && mouseHover() && visible) {
            setFromMouse(screen().mousePosition.x, screen().mousePosition.y)
        }
    }

    protected open fun setFromMouse(mouseX: Float, mouseY: Float) {
        if (bar.mouseHover()) return
        Orientation.peek(
            {
                val a = mouseY - bar.transform.worldY
                if (a > 0) {
                    bar.transform.y = (bar.transform.y + amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
                } else {
                    bar.transform.y = (bar.transform.y - amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
                }
            },
            {
                val a = mouseX - bar.transform.worldX
                if (a > 0) {
                    bar.transform.x = (bar.transform.x + amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
                } else {
                    bar.transform.x = (bar.transform.x - amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
                }
            }
        )
        amountReceiver?.invoke(amount)
    }

    override fun onMouseDragging(event: MouseDragEvent): NextAction {
        if (!active || !dragging || !visible || !bar.dragging) return NextAction.Continue
        Orientation.peek(
            {
                bar.transform.y = (bar.transform.y + deltaY).clamp(barPositionRange)
            }, {
                bar.transform.x = (bar.transform.x + deltaX).clamp(barPositionRange)
            }
        )
        amountReceiver?.invoke(amount)
        return super.onMouseDragging()
    }

    override fun onMouseClick(event: MousePressEvent): NextAction {
        if (!visible) return NextAction.Continue
        mouseHover {
            setFromMouse(mouseX, mouseY)
        }
        return super.onMouseClick()
    }

    override fun onMouseScrolling(event: MouseScrollEvent): NextAction {
        mouseHover {
            this@Scroller.amount -= amountStep() * amount
        }
        return super.onMouseScrolling()
    }


    override fun onRenderBackground(renderContext: RenderContext) {
        calcBarLength()
        renderTexture(renderContext.matrixStack, this.transform, SCROLLER_BACKGROUND, color())
    }

}

/**
 * 在当前容器中添加一个[Scroller]
 * @receiver ElementContainer
 * @param length Float 滚动条长度
 * @param thickness Float 滚动条厚度
 * @param amountStep () -> Float 滚动步长 每次滚动鼠标时移动的距离
 * @param totalAmount () -> Float 滚动条总长度
 * @param barLength () -> Float 滚动条所占总长度的百分比 Range(0f..1f)
 * @param Orientation Arrangement 排列方式
 * @param color () -> ARGBColor 滚动条背景着色器颜色
 * @param barColor () -> ARGBColor  滚动条着色器颜色
 * @param scope Scroller.() -> Unit
 * @return Scroller
 */
@OptIn(ExperimentalContracts::class)
fun ElementContainer.scroller(
    length: Float,
    thickness: Float = 10f,
    amountStep: () -> Float,
    totalAmount: () -> Float,
    barLength: () -> Float,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(Scroller(length, thickness, amountStep, totalAmount, barLength, Orientation, color, barColor, scope))
}

/**
 * @param length Float 滚动条长度
 * @param thickness Float 滚动条厚度
 * @param amountStep () -> Float 滚动步长 每次滚动鼠标时移动的距离
 * @param totalAmount () -> Float 滚动条总长度
 * @param barLength () -> Float 滚动条所占总长度的百分比 Range(0f..1f)
 * @param Orientation Arrangement 排列方式
 * @param color () -> ARGBColor 滚动条背景着色器颜色
 * @param barColor () -> ARGBColor  滚动条着色器颜色
 * @param scope Scroller.() -> Unit
 * @return Scroller
 */
@OptIn(ExperimentalContracts::class)
fun Scroller(
    length: Float,
    thickness: Float = 10f,
    amountStep: () -> Float,
    totalAmount: () -> Float,
    barLength: () -> Float,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return Scroller(length, thickness, amountStep, totalAmount, barLength, Orientation, color, barColor).apply(scope)
}

/**
 * 在当前容器中添加一个[Scroller]
 * @param T Number
 * @receiver ElementContainer
 * @param initValue T 初始值
 * @param range ClosedRange<T> 值范围
 * @param valueMapper (Double) -> T 值映射器
 * @param valueReceiver (T) -> Unit 值接收器
 * @param valueRender (T) -> Text 值渲染器
 * @param length Float 滚动条长度
 * @param thickness Float 滚动条厚度
 * @param Orientation Arrangement 排列方式
 * @param color () -> ARGBColor 滚动条背景着色器颜色
 * @param barColor () -> ARGBColor 滚动条着色器颜色
 * @param scope Scroller.() -> Unit 滚动条作用域
 * @return Scroller
 */
@OptIn(ExperimentalContracts::class)
fun <T> ElementContainer.numberScroller(
    initValue: T,
    range: ClosedRange<T>,
    valueMapper: (Double) -> T,
    valueReceiver: (T) -> Unit,
    valueRender: (T) -> Text = { Literal(it.toString()) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller where T : Number, T : Comparable<T> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(
        NumberScroller(
            initValue,
            range,
            valueMapper,
            valueReceiver,
            valueRender,
            length,
            thickness,
            Orientation,
            color,
            barColor,
            scope
        )
    )
}

/**
 * @param T Number
 * @param initValue T 初始值
 * @param range ClosedRange<T> 值范围
 * @param valueMapper (Double) -> T 值映射器
 * @param valueReceiver (T) -> Unit 值接收器
 * @param valueRender (T) -> Text 值渲染器
 * @param length Float 滚动条长度
 * @param thickness Float 滚动条厚度
 * @param Orientation Arrangement 排列方式
 * @param color () -> ARGBColor 滚动条背景着色器颜色
 * @param barColor () -> ARGBColor 滚动条着色器颜色
 * @param scope Scroller.() -> Unit 滚动条作用域
 * @return Scroller
 */
@OptIn(ExperimentalContracts::class)
fun <T> NumberScroller(
    initValue: T,
    range: ClosedRange<T>,
    valueMapper: (Double) -> T,
    valueReceiver: (T) -> Unit,
    valueRender: (T) -> Text = { Literal(it.toString()) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller where T : Number, T : Comparable<T> {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    fun T.minus(target: T): Number {
        return when (this) {
            is Int    -> this - target.toInt()
            is Short  -> this - target.toShort()
            is Long   -> this - target.toLong()
            is Float  -> this - target.toFloat()
            is Double -> this - target.toDouble()
            is Byte   -> this - target.toByte()
            else      -> throw IllegalArgumentException("Unsupported type")
        }
    }
    return Scroller(
        length,
        thickness,
        { range.endInclusive.minus(range.start).toFloat() * 0.05f },
        { range.endInclusive.minus(range.start).toFloat() },
        { 10 / length },
        Orientation, color, barColor
    ).apply {
        scope()
        amount = initValue.toFloat()
        bar.hoverTip(
            displayDelay = 1u,
            optionalDirections = Orientation.peek(
                listOf(Direction.Left, Direction.Right),
                listOf(Direction.Top, Direction.Bottom)
            ),
            margin = Margin(4)
        ) {
            textField({ valueRender(valueMapper(amount + range.start.toDouble())) })
            tick = {
                if (this@apply.mouseHover() || bar.dragging) {
                    tickCounter++
                } else if (!keepDisplay && visible && active) {
                    visible = !pop()
                    if (!visible) {
                        tickCounter = 0u
                    }
                }
            }
        }
        amountReceiver = {
            valueReceiver(valueMapper(it + range.start.toDouble()))
        }
    }
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.intScroller(
    initValue: Int,
    range: ClosedRange<Int>,
    valueReceiver: (Int) -> Unit,
    valueRender: (Int) -> Text = { Literal(it.toString()) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(IntScroller(initValue, range, valueReceiver, valueRender, length, thickness, Orientation, color, barColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun IntScroller(
    initValue: Int,
    range: ClosedRange<Int>,
    valueReceiver: (Int) -> Unit,
    valueRender: (Int) -> Text = { Literal(it.toString()) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return NumberScroller(initValue, range, { it.toInt() }, valueReceiver, valueRender, length, thickness, Orientation, color, barColor, scope)
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.floatScroller(
    initValue: Float,
    range: ClosedRange<Float>,
    valueReceiver: (Float) -> Unit,
    valueRender: (Float) -> Text = { Literal("%.2f".format(it)) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(FloatScroller(initValue, range, valueReceiver, valueRender, length, thickness, Orientation, color, barColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun FloatScroller(
    initValue: Float,
    range: ClosedRange<Float>,
    valueReceiver: (Float) -> Unit,
    valueRender: (Float) -> Text = { Literal("%.2f".format(it)) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return NumberScroller(initValue, range, { it.toFloat() }, valueReceiver, valueRender, length, thickness, Orientation, color, barColor, scope)
}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.doubleScroller(
    initValue: Double,
    range: ClosedRange<Double>,
    valueReceiver: (Double) -> Unit,
    valueRender: (Double) -> Text = { Literal("%.2f".format(it)) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(DoubleScroller(initValue, range, valueReceiver, valueRender, length, thickness, Orientation, color, barColor, scope))
}

@OptIn(ExperimentalContracts::class)
fun DoubleScroller(
    initValue: Double,
    range: ClosedRange<Double>,
    valueReceiver: (Double) -> Unit,
    valueRender: (Double) -> Text = { Literal("%.2f".format(it)) },
    length: Float,
    thickness: Float = 10f,
    Orientation: Orientation = Orientation.Vertical,
    color: () -> ARGBColor = { Colors.WHITE },
    barColor: () -> ARGBColor = { Colors.WHITE },
    scope: Scroller.() -> Unit = {}
): Scroller {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return NumberScroller(initValue, range, { it }, valueReceiver, valueRender, length, thickness, Orientation, color, barColor, scope)
}