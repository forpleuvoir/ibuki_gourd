package moe.forpleuvoir.ibukigourd.gui.base.toast

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.config.ModConfigContainer
import moe.forpleuvoir.ibukigourd.config.item.vector2f
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast.Config.FADE_IN_OFFSET
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast.Config.FADE_OUT_OFFSET
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.render.runWithZOffset
import moe.forpleuvoir.ibukigourd.render.shaderColor
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.scaledSize
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.item.impl.duration
import org.joml.Vector2fc
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

object Toast : Tickable {

    object Config : ModConfigContainer("toast") {
        // 动画参数
        val FADE_IN_OFFSET by vector2f("fade_in_offset", Vector2f(0f, 10f), Vector2f(-50, -50), Vector2f(50, 50)) // 开始的位移值
        val FADE_OUT_OFFSET by vector2f("fade_out_offset", Vector2f(0f, -2f), Vector2f(-50, -50), Vector2f(50, 50)) // 结束的位移值

        val FADE_IN_DURATION by duration("fade_in_duration", 0.2.seconds, 0.seconds, 10.seconds)  // 淡入时间
        val FADE_OUT_DURATION by duration("fade_out_duration", 0.2.seconds, 0.seconds, 10.seconds)  // 淡出时间

        val SHORT_DURATION by duration("short_duration", 2.seconds, maxDuration = 10.seconds)
        val LONG_DURATION by duration("long_duration", 5.seconds, maxDuration = 20.seconds)
    }

    val SHORT_DURATION get() = Config.SHORT_DURATION

    val LONG_DURATION get() = Config.LONG_DURATION

    private val toastQueue = mutableListOf<Pair<BoxWidget, ValueTimeMark>>() // 存储当前 Toast 和其时间戳

    @JvmStatic
    fun render(drawContent: IGDrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val iterator = toastQueue.iterator()
        runWithZOffset(IGScreen.currentScreenZOffset + IGScreen.TOAST_Z_OFFSET) {
            while (iterator.hasNext()) {
                val (box, timeMark) = iterator.next()
                val duration = box.duration
                val fadeInDuration = box.fadeInDuration
                val fadeOutDuration = box.fadeOutDuration
                // 检查是否已经超时
                if (timeMark.elapsedNow() <= duration) {
                    val (alpha, offset) = calculateAlphaAndOffset(duration, fadeInDuration, fadeOutDuration, timeMark)
                    drawContent.useMatrixStack {
                        scissorOffset(offset) {
                            it.translate(offset.x(), offset.y(), 0f)
                            shaderColor(Colors.WHITE.alpha(alpha)) {
                                box.render(this, mouseX, mouseY, delta)
                            }
                        }
                    }
                } else {
                    iterator.remove() // 移除已过期的 Toast
                }
            }
        }
    }

    private fun calculateAlphaAndOffset(
        duration: Duration,
        fadeInDuration: Duration,
        fadeOutDuration: Duration,
        timeMark: ValueTimeMark
    ): Pair<Float, Vector2fc> {
        val progress = (timeMark.elapsedNow() / duration).coerceIn(0.0, 1.0)
        val fadeInRatio = (fadeInDuration / duration).coerceIn(0.001, 1.0)
        val fadeOutRatio = (fadeOutDuration / duration).coerceIn(0.001, 1.0)

        // 计算透明度
        val alpha = when {
            progress < fadeInRatio      -> (progress / fadeInRatio).toFloat()
            progress < 1 - fadeOutRatio -> 1f
            else                        -> (1f - (progress - (1 - fadeOutRatio)) / fadeOutRatio).toFloat()
        }
        // 计算位移
        val offset = when {
            progress < fadeInRatio      -> FADE_IN_OFFSET * (1f - progress / fadeInRatio)
            progress < 1 - fadeOutRatio -> Vector2f(0f, 0f)
            else                        -> FADE_OUT_OFFSET * ((progress - (1 - fadeOutRatio)) / fadeOutRatio)
        }
        return Pair(alpha.coerceIn(0f..1f), offset)
    }

    private operator fun Vector2fc.times(scale: Number) = Vector2f(this.x() * scale.toDouble(), this.y() * scale.toDouble())


    @JvmStatic
    fun onResize() {
        updatePosition()
    }

    fun showToast(
        text: String,
        duration: Duration = SHORT_DURATION,
        fadeInDuration: Duration = Config.FADE_IN_DURATION,
        fadeOutDuration: Duration = Config.FADE_OUT_DURATION,
        alignment: Alignment = Alignment.biasedBy(0f, 0.75f),
        modifier: Modifier = Modifier
    ) = showToast(duration, fadeInDuration, fadeOutDuration, alignment, modifier) { TextLabel(text) }

    fun showToast(
        text: Text,
        duration: Duration = SHORT_DURATION,
        fadeInDuration: Duration = Config.FADE_IN_DURATION,
        fadeOutDuration: Duration = Config.FADE_OUT_DURATION,
        alignment: Alignment = Alignment.biasedBy(0f, 0.75f),
        modifier: Modifier = Modifier
    ) = showToast(duration, fadeInDuration, fadeOutDuration, alignment, modifier) { TextLabel(text) }

    fun showToast(
        duration: Duration = SHORT_DURATION,
        fadeInDuration: Duration = Config.FADE_IN_DURATION,
        fadeOutDuration: Duration = Config.FADE_OUT_DURATION,
        alignment: Alignment = Alignment.biasedBy(0f, 0.75f),
        modifier: Modifier = Modifier,
        content: BoxScope.() -> Unit
    ) {
        if (duration == Duration.ZERO) return
        // 强制现有 Toast 提前淡出
        toastQueue.replaceAll { (box, timeMark) ->
            val totalDuration = box.duration
            val elapsedTime = timeMark.elapsedNow()

            // 计算淡出起始时间
            val fadeOutStartDuration = totalDuration - fadeInDuration
            if (elapsedTime < fadeOutStartDuration) {
                // 强制跳到淡出阶段的时间点
                box to TimeSource.Monotonic.markNow() - fadeOutStartDuration
            } else {
                // 保持当前时间标记不变
                box to timeMark
            }
        }

        // 添加新 Toast
        val newToast = BoxWidget().apply {
            userData["#toast_duration"] = duration
            userData["#toast_duration_fade_in"] = fadeInDuration
            userData["#toast_duration_fade_out"] = fadeOutDuration
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
        // 在队列中追加新的 Toast
        toastQueue.add(Pair(newToast, TimeSource.Monotonic.markNow()))
        updatePosition()
    }

    private val BoxWidget.duration: Duration get() = userData["#toast_duration"] as? Duration ?: SHORT_DURATION
    private val BoxWidget.fadeInDuration: Duration get() = userData["#toast_duration_fade_in"] as? Duration ?: SHORT_DURATION
    private val BoxWidget.fadeOutDuration: Duration get() = userData["#toast_duration_fade_out"] as? Duration ?: SHORT_DURATION

    private fun updatePosition() {
        toastQueue.forEach { (box, _) ->
            box.apply {
                measure(Constraints.of(0f, mc.window.scaledWidth.toFloat(), 0f, mc.window.scaledHeight.toFloat()))
                measureCompletion()
                layout()
            }
        }
    }

    override fun onTick() {
        toastQueue.forEach { (box, _) ->
            box.tick()
        }
    }

}