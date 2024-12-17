package moe.forpleuvoir.ibukigourd.gui.base.toast

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.scaledSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

object Toast {

    val SHORT_DURATION = 2.seconds

    val LONG_DURATION = 5.seconds

    private var box: BoxWidget? = null

    private var timeMark: ValueTimeMark = TimeSource.Monotonic.markNow()

    @JvmStatic
    fun render(drawContent: IGDrawContext) {
        box?.apply {
            if (timeMark.elapsedNow() <= duration) {
                render(drawContent, 0, 0, 0f)
            }
        }
    }

    @JvmStatic
    fun onResize() {
        updatePosition()
    }

    fun showToast(
        duration: Duration = SHORT_DURATION,
        alignment: Alignment = Alignment.biasedBy(0f, 0.75f),
        modifier: Modifier = Modifier,
        text: String
    ) = showToast(duration, alignment, modifier) { TextLabel(text) }

    fun showToast(
        duration: Duration = SHORT_DURATION,
        alignment: Alignment = Alignment.biasedBy(0f, 0.75f),
        modifier: Modifier = Modifier,
        text: Text
    ) = showToast(duration, alignment, modifier) { TextLabel(text) }

    fun showToast(
        duration: Duration = SHORT_DURATION,
        alignment: Alignment = Alignment.biasedBy(0f, 0.75f),
        modifier: Modifier = Modifier,
        content: BoxScope.() -> Unit
    ) {
        timeMark = TimeSource.Monotonic.markNow()
        box = BoxWidget().apply {
            customData["#toast_duration"] = duration
            padding = Padding(4f)
            measureCompletion = {
                transform.translateTo(alignment.align(mc.window.scaledSize.toFloat(), transform), true)
            }
            render = { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.TIP)
                }
            }
            modifier.foldInApply()
            BoxScope { this }.content()
        }
        updatePosition()
    }

    private val BoxWidget.duration: Duration get() = customData["#toast_duration"] as? Duration ?: SHORT_DURATION

    private fun updatePosition() {
        box?.apply {
            measure(Constraints.of(0f, mc.window.scaledWidth.toFloat(), 0f, mc.window.scaledHeight.toFloat()))
            measureCompletion()
            layout()
        }
    }

}