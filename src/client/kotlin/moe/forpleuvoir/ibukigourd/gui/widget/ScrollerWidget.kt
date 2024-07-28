package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseDragEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.mouseX
import moe.forpleuvoir.ibukigourd.input.mouseY
import moe.forpleuvoir.ibukigourd.util.mc
import kotlin.math.abs
import kotlin.math.max

open class ScrollerWidget(
    val amountStep: () -> Float,
    val totalAmount: () -> Float,
    val barProportion: () -> Float,
    private val initialAmount: Float = 0f,
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
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraint(constraints)
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
        amount = initialAmount
        return this
    }

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            context.drawWidgetTexture(transform.asWorldBox, theme(bgTheme))
        }
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        updateBarLength()
        context.batchRenderTextureColored {
            context.drawWidgetTexture(bar.asWorldBox, theme(barTheme))
        }
    }

    override fun onTick() {
        if (pressed && wasMouseOver && visible) {
            setFromMouse(mc.mouseX, mc.mouseY)
        }
    }

    //------------ Scroller ------------\\

    private val bar = Transform(parent = { this.transform })

    private var barWasDragging = false

    var progress: Float
        set(value) {
            val fixedValue = value.coerceIn(0f..totalAmount())
            amount = (totalAmount() * fixedValue).coerceIn(0f..totalAmount())
        }
        get() {
            if (scrollableLength == 0f) return 1f
            return (orientation.peek(bar.y, bar.x) / scrollableLength).coerceIn(0f..1f)
        }

    private val scrollableLength: Float
        get() = orientation.peek(
            this.transform.height - (barProportion() * this.transform.height),
            this.transform.width - (barProportion() * this.transform.width)
        )

    private val barPositionRange: ClosedFloatingPointRange<Float> get() = 0f..scrollableLength

    var amount: Float
        get() = (totalAmount() * progress).coerceIn(0f..totalAmount())
        set(value) {
            val fixedValue = value.coerceIn(0f..totalAmount())
            val barPosition = scrollableLength * if (totalAmount() == 0f) 0f else fixedValue / totalAmount()
            orientation.peek(
                {
                    bar.y = barPosition
                },
                {
                    bar.x = barPosition
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
        if (!wasDragging || !barWasDragging || !visible || !pressed) return
        event.tryUse().onSuccess {
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
        if (!visible) return
        if (wasMouseOver) {
            setFromMouse(mc.mouseX, mc.mouseY)
        }
        barWasDragging = bar.isMouseOvered(mc.mouseX, mc.mouseY)
    }


    open fun scroller(amount: Float) {
        this.amount -= amountStep() * amount
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        if (wasMouseOver) {
            scroller(if (event.verticalAmount != 0f) event.verticalAmount else event.horizontalAmount)
        }
    }

}

fun GuiScope<out WidgetContainer>.scroller(
    /**
     * 进度步进
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
    initialAmount: Float = 0f,
    amountConsumer: (Float) -> Unit = {},
    orientation: Orientation = Orientation.Vertical,
    barTheme: PressableTheme = PressableTheme.ScrollerBar,
    bgTheme: PressableTheme = PressableTheme.ScrollerBackground,
    modifier: Modifier? = null
) = owner().addWidgetChild(ScrollerWidget(amountStep, totalAmount, barProportion, initialAmount, amountConsumer, orientation, barTheme, bgTheme)) {
    modifier?.foldIn(Unit) { _, e ->
        e.tryApplyModify(this)
    }
}

