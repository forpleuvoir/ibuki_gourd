package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseDragEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.input.mouseX
import moe.forpleuvoir.ibukigourd.input.mouseY
import moe.forpleuvoir.ibukigourd.render.math.x
import moe.forpleuvoir.ibukigourd.render.math.y
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.util.primitive.pick
import kotlin.math.abs
import kotlin.math.max

open class ScrollerWidget(
    val amountStep: () -> Float,
    val totalAmount: () -> Float,
    val barProportion: () -> Float,
    private val initialAmount: () -> Float? = { null },
    private val amountConsumer: (Float) -> Unit = {},
    val orientation: Orientation = Orientation.Vertical,
    private val barTheme: PressableTheme = PressableTheme.ScrollerBar,
    private val bgTheme: PressableTheme = PressableTheme.ScrollerBackground,
) : IGPressableWidgetImpl() {

    //------------ Override ------------\\

    override var constraints: Constraints = Constraints().copy(
        minWidth = max(barTheme.idle.corner.width, bgTheme.idle.corner.width).toFloat() + 1f,
        minHeight = max(barTheme.idle.corner.height, bgTheme.idle.corner.height).toFloat() + 1f,
    )

    override fun measure(constraints: Constraints): Placeable {
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraintAs(constraints)
        transform.subscribeSizeChange { _, (width, height) ->
            orientation.peek(
                {
                    bar.width = width
                }, {
                    bar.height = height
                })
        }
        orientation.peek(
            {
                transform.set(minWidth, maxHeight)
            },
            {
                transform.set(maxWidth, minHeight)
            }
        )
        return this
    }

    override fun measureCompleted() {
        initialAmount()?.let {
            amount = it
        } ?: run {
            amount = _amount
        }
    }

    override val mouseOverCursor: MouseCursor.Cursor
        get() = bar.isMouseOvered(mc.mousePosition).pick(MouseCursor.Cursor.POINTING_HAND_CURSOR, MouseCursor.default)

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            pushWidgetTexture(transform, theme(bgTheme))
        }
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        updateBarLength()
        context.batchRenderTextureColored {
            pushWidgetTexture(bar, theme(barTheme))
        }
    }

    override fun onTick() {
        if (pressed && wasMouseOver && visible) {
            setFromMouse(mc.mouseX, mc.mouseY)
        }
    }

    //------------ Scroller ------------\\

    private val bar = Transform(parent = { this.transform }).apply {
        subscribePositionChange { _, current ->
            progress = orientation.peek(current.y, current.x) / scrollableLength
        }
    }

    private var barWasDragging = false

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f..1f)
            _amount = totalAmount() * field
        }


    private val scrollableLength: Float
        get() = orientation.peek(
            this.transform.height - (barProportion() * this.transform.height),
            this.transform.width - (barProportion() * this.transform.width)
        )

    private val barPositionRange: ClosedFloatingPointRange<Float> get() = 0f..scrollableLength

    private var _amount: Float = 0f
        set(value) {
            field = value.coerceIn(0f..totalAmount())
        }

    var amount: Float
        get() = _amount
        set(value) {
            val fixedValue = value.coerceIn(0f..totalAmount())
            val barPosition = scrollableLength * (fixedValue / totalAmount())
            orientation.peek(
                {
                    bar.y = (barPosition.isNaN()).pick(0f, barPosition).coerceIn(barPositionRange)
                },
                {
                    bar.x = if (barPosition.isNaN()) 0f else barPosition.coerceIn(barPositionRange)
                }
            )
            amountConsumer.invoke(fixedValue)
        }

    private fun updateBarLength() {
        orientation.peek(
            {
                bar.height = barProportion() * transform.height
            },
            {
                bar.width = barProportion() * transform.width
            }
        )
    }

    protected open fun setFromMouse(mouseX: Float, mouseY: Float) {
        if (barWasDragging) return
        val step = 3f
        orientation.peek(
            {
                val offset = mouseY - bar.worldCenter.y()
                bar.y = if (offset > 0) {
                    (bar.y + offset / step).coerceIn(barPositionRange)
                } else {
                    (bar.y - abs(offset) / step).coerceIn(barPositionRange)
                }
            },
            {
                val offset = mouseX - bar.worldCenter.x()
                bar.x = if (offset > 0) {
                    (bar.x + offset / step).coerceIn(barPositionRange)
                } else {
                    (bar.x - abs(offset) / step).coerceIn(barPositionRange)
                }
            }
        )
        amountConsumer.invoke(amount)
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        event.tryUse {
            wasDragging && barWasDragging && visible && pressed
        }.onSuccess {
            orientation.peek(
                {
                    bar.y = (bar.y + event.deltaY).coerceIn(barPositionRange)
                },
                {
                    bar.x = (bar.x + event.deltaX).coerceIn(barPositionRange)
                }
            )
            amountConsumer.invoke(amount)
        }
    }

    override fun onPress() {
        if (wasMouseOver) {
            setFromMouse(mc.mouseX, mc.mouseY)
        barWasDragging = bar.isMouseOvered(mc.mouseX, mc.mouseY)
        }
    }


    open fun scroller(amount: Float) {
        this.amount -= amountStep() * amount
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        event.tryUse { wasMouseOver }.onSuccess {
            scroller(if (event.verticalAmount != 0f) event.verticalAmount else event.horizontalAmount)
        }
    }

    companion object

    fun interface ScrollerScope : GuiScope<ScrollerWidget>

}

typealias ScrollerScope = ScrollerWidget.ScrollerScope


fun WidgetContainerScope.Scroller(
    /**
     * 进度步幅
     */
    amountStep: () -> Float,
    /**
     * 总进度
     */
    totalAmount: () -> Float,
    /**
     * bar百分比 0f..1f
     */
    barProportion: () -> Float,
    initialAmount: () -> Float? = { null },
    amountConsumer: (Float) -> Unit = {},
    orientation: Orientation = Orientation.Vertical,
    barTheme: PressableTheme = PressableTheme.ScrollerBar,
    bgTheme: PressableTheme = PressableTheme.ScrollerBackground,
    modifier: Modifier = Modifier,
    scope: ScrollerScope.() -> Unit = {}
) = addWidgetChild(ScrollerWidget(amountStep, totalAmount, barProportion, initialAmount, amountConsumer, orientation, barTheme, bgTheme)) {
    modifier.foldInApply()
    ScrollerScope { this }.scope()
}

