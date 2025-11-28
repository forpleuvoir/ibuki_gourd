package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseDragEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.mouseOverCursor
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.PressableWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.input.mouseX
import moe.forpleuvoir.ibukigourd.input.mouseY
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.util.primitive.pick
import kotlin.math.abs
import kotlin.math.max

open class ScrollerWidget(
    val scrollState: ScrollState = ScrollState(),
    val orientation: Orientation = Orientation.Vertical,
    private val barTheme: PressableTheme = PressableTheme.ScrollerBar,
    private val bgTheme: PressableTheme = PressableTheme.ScrollerBackground,
) : PressableWidgetImpl() {

    //------------ Override ------------\\

    init {
        transform.subscribeSizeChange { _, (width, height) ->
            orientation.peek(
                {
                    bar.width = width
                }, {
                    bar.height = height
                })
        }
    }

    override var constraints: Constraints = Constraints().copy(
        minWidth = max(barTheme.idle.corner.width, bgTheme.idle.corner.width).toFloat() + 1f,
        minHeight = max(barTheme.idle.corner.height, bgTheme.idle.corner.height).toFloat() + 1f,
    )

    override fun measure(constraints: Constraints): Placeable {
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.merge(constraints)
        orientation.peek(
            {
                transform.set(minWidth, maxHeight)
            },
            {
                transform.set(maxWidth, minHeight)
            }
        )
        if (scrollState.barProportion == 1f) {
            transform.set(0f, 0f)
            visible = false
            active = false
            scrollState.amount = scrollState.amount
        } else {
            clearActive()
            clearVisible()
        }
        return this
    }

    var remeasureFlag = false

    override fun onMeasureCompletion() {
        val progress = scrollState.progress
        orientation.peek(
            {
                bar.y = progress * scrollableLength
            }, {
                bar.x = progress * scrollableLength
            }
        )

        if (scrollState.barProportion == 1f && (transform.height != 0f || transform.width != 0f)) {
            remeasure()
            remeasureFlag = false
            return
        }

        if (transform.height == 0f || transform.width == 0f) {
            if (!remeasureFlag) {
                remeasureFlag = true
                remeasure()
            }
        } else {
            remeasureFlag = false
        }
    }

    override fun onRenderBackground(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) {
        guiGraphics.pushWidgetTexture(transform, theme(bgTheme))
    }

    override fun onRender(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) {
        if (scrollState.barProportion == 0f) return
        updateBar()
        guiGraphics.pushWidgetTexture(bar, theme(barTheme))
    }

    override fun onTick() {
        if (pressed && wasMouseOver && visible) {
            setFromMouse(mc.mouseX, mc.mouseY)
        }
    }

    //------------ Scroller ------------\\

    internal val bar = Transform(parent = { this.transform })

    private var barWasDragging = false

    private val scrollableLength: Float
        get() = orientation.peek(
            this.transform.height - (scrollState.barProportion * this.transform.height).coerceAtLeast(9f),
            this.transform.width - (scrollState.barProportion * this.transform.width).coerceAtLeast(9f)
        )

    private val barPositionRange: ClosedFloatingPointRange<Float> get() = 0f..if (scrollableLength.isFinite()) scrollableLength else 0f

    private fun updateBar() {
        scrollState.amount = scrollState.amount
        orientation.peek(
            {
                bar.height = (scrollState.barProportion * transform.height).coerceAtLeast(9f)
                bar.y = scrollState.progress * scrollableLength
            },
            {
                bar.width = (scrollState.barProportion * transform.width).coerceAtLeast(9f)
                bar.x = scrollState.progress * scrollableLength
            }
        )

    }

    protected open fun setFromMouse(mouseX: Float, mouseY: Float) {
        if (barWasDragging) return
        val step = 3f
        orientation.peek(
            {
                val offset = mouseY - bar.worldCenter.y()
                val y = if (offset > 0) {
                    (bar.y + offset / step).coerceIn(barPositionRange)
                } else {
                    (bar.y - abs(offset) / step).coerceIn(barPositionRange)
                }
                scrollState.progress = y / scrollableLength
            },
            {
                val offset = mouseX - bar.worldCenter.x()
                val x = if (offset > 0) {
                    (bar.x + offset / step).coerceIn(barPositionRange)
                } else {
                    (bar.x - abs(offset) / step).coerceIn(barPositionRange)
                }
                scrollState.progress = x / scrollableLength
            }
        )
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        event.tryUse {
            wasDragging && barWasDragging && visible && pressed
        }.onSuccess {
            orientation.peek(
                {
                    val y = (bar.y + event.deltaY).coerceIn(barPositionRange)
                    scrollState.progress = y / scrollableLength
                },
                {
                    val x = (bar.x + event.deltaX).coerceIn(barPositionRange)
                    scrollState.progress = x / scrollableLength
                }
            )
        }
    }

    override fun onPress() {
        if (wasMouseOver) {
            setFromMouse(mc.mouseX, mc.mouseY)
            barWasDragging = bar.isMouseOvered(mc.mouseX, mc.mouseY)
        }
    }


    open fun scroll(amount: Float) =
        this.scrollState.scroll(amount)


    override fun onMouseScrolling(event: MouseScrollEvent) {
        event.tryUse { wasMouseOver }.onSuccess {
            scroll(if (event.verticalAmount != 0f) event.verticalAmount else event.horizontalAmount)
        }
    }

    fun interface Scope : GuiScope<ScrollerWidget> {

        fun updateBar() = owner().updateBar()

    }

}

typealias ScrollerScope = ScrollerWidget.Scope


fun ContainerScope.Scroller(
    scrollState: ScrollState = ScrollState(),
    orientation: Orientation = Orientation.Vertical,
    barTheme: PressableTheme = PressableTheme.ScrollerBar,
    bgTheme: PressableTheme = PressableTheme.ScrollerBackground,
    modifier: Modifier = Modifier,
    scope: ScrollerScope.() -> Unit = {}
) = addWidgetChild(ScrollerWidget(scrollState, orientation, barTheme, bgTheme)) {
    Modifier.mouseOverCursor<ScrollerWidget> {
        it.bar.isMouseOvered(mc.mousePosition).pick(MouseCursor.POINTING_HAND_CURSOR, MouseCursor.default)
    }.then(modifier).foldInApply()
    ScrollerScope { this }.scope()
}

