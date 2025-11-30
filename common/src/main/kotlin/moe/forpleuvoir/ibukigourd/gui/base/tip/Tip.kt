package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElementUserData.setName
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useMatrixStack
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import org.joml.Vector2f
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class Tip(
    val settings: Setting = DefaultSetting,
    val modifier: Modifier = DefaultModifier,
    val content: BoxScope.() -> Unit
) : Tickable {
    data class Setting(
        val showDelay: Duration = 200.milliseconds,
        val hideDelay: Duration = 0.milliseconds,
        val fadeInDuration: Duration = 200.milliseconds,
        val fadeInOffset: Float = -2f,
        val optionalDirection: List<Direction> = Direction.clockwiseFromTop,
        val backgroundColor: ARGBColor = Colors.WHITE
    )

    companion object {
        val DefaultModifier get() = Modifier.padding(4).margin(4)
        val DefaultSetting = Setting()
    }

    private lateinit var box: BoxWidget

    private var showTimeMark: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    private var currentDirection = mutableStateOf(settings.optionalDirection.isNotEmpty().pick(settings.optionalDirection.first(), Top))

    fun init(parent: () -> Transform) {
        val directions = settings.optionalDirection
        box = BoxWidget().apply {
            setName("HoverTip")
            renderBackground = { _, _, _, _ ->
                TipHelper.updatePosition(this.transform, this.margin, currentDirection, parent(), directions)
            }
            render = { guiGraphics, _, _, _ ->
                TipHelper.tipRender(this.transform, guiGraphics, currentDirection.getValue(), parent(), settings.backgroundColor)
            }
            if (transform.parent() != parent()) transform.parent = { parent() }
            modifier.foldInApply()

            Compose { BoxScope { this }.content() }

            measure(Constraints.of(0f, mc.window.guiScaledWidth.toFloat(), 0f, mc.window.guiScaledHeight.toFloat()))
            measureCompletion()
            layout()
        }
    }

    fun show() {
        showTimeMark = TimeSource.Monotonic.markNow() + settings.showDelay
    }

    fun render(guiGraphics: IGGuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!this::box.isInitialized || showTimeMark > TimeSource.Monotonic.markNow()) return
        val (alpha, offset) = calculateAlphaAndOffset()
        guiGraphics {
            useMatrixStack {
                it.translate(offset.x(), offset.y())
                //TODO 或许应该直接修改组件的位置,原来有裁剪偏移,现在不知道有没有用
                modulateColor(Colors.WHITE.alpha(alpha)) {
                    box.render(this, mouseX, mouseY, delta)
                }
            }
        }
    }

    override fun onTick() {
        if (!this::box.isInitialized) return
        box.onTick()
    }

    fun calculateAlphaAndOffset(): Pair<Float, Vector2f> {
        val currentMark = TimeSource.Monotonic.markNow() // 当前时间标记
        val elapsedTime = currentMark - showTimeMark

        // 计算 fadeIn 进度
        val fadeInProgress = (elapsedTime / settings.fadeInDuration).toFloat().coerceIn(0f, 1f)

        // 透明度 (alpha)：从 0 → 1
        val alpha = fadeInProgress

        // 偏移量 (offset)：从 fadeInOffset → 0
        val offset = settings.fadeInOffset * (1f - fadeInProgress)

        // 根据当前方向计算偏移向量
        return alpha to when (currentDirection.getValue()) {
            Top    -> Vector2f(0f, offset)
            Bottom -> Vector2f(0f, -offset)
            Left  -> Vector2f(offset, 0f)
            Right -> Vector2f(-offset, 0f)
        }
    }
}