package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementCustomData.setName
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverText
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverTextBGColor
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverTextDirection
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverTextMargin
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverTextPadding
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverTextShowDelay
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Top
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextWidget
import moe.forpleuvoir.ibukigourd.gui.widget.tip.TipHelper
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.util.primitive.pick
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

object TextTipRenderer : Tickable {

    private var currentWidget: IGWidget? = null

    private var box: BoxWidget? = null

    private var textLabel: TextWidget? = null

    private var timeMark: ValueTimeMark = TimeSource.Monotonic.markNow()

    private fun update(widget: IGWidget) {
        val directions = widget.hoverTextDirection()
        val direction = mutableStateOf(directions.isNotEmpty().pick(directions.first(), Top))
        box = BoxWidget().apply {
            layer = GuiLayer.Pop
            margin = widget.hoverTextMargin()
            padding = widget.hoverTextPadding()
            setName("TextTipRenderContainer")
            renderBackground = { context, _, _, _ ->
                TipHelper.updatePosition(this.transform, this.margin, direction, widget.transform, widget.hoverTextDirection())
            }
            render = { context, _, _, _ ->
                TipHelper.tipRender(this.transform, context, direction.getValue(), widget.transform, widget.hoverTextBGColor())
            }
            if (transform.parent() != widget.transform) transform.parent = { widget.transform }
        }
        textLabel = TextWidget(mutableStateBy<Text> { widget.hoverText.invoke()!! }, autoNewLine = true)
        box!!.addWidgetChild(textLabel!!)
        timeMark = TimeSource.Monotonic.markNow()
    }

    fun render(widget: IGWidget, drawContext: IGDrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (widget.hoverText() == null) return

        if (currentWidget != widget) {
            currentWidget = widget
            update(widget)
        }

        if (timeMark.elapsedNow() < widget.hoverTextShowDelay) return
        box?.measure(Constraints.of(0f, mc.window.scaledWidth.toFloat(), 0f, mc.window.scaledHeight.toFloat()))
        box?.layout()
        box?.render(drawContext, mouseX, mouseY, delta)
    }

    override fun onTick() {
        box?.onTick()
    }

}