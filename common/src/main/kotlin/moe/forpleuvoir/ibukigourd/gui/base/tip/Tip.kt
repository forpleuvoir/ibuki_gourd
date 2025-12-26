package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElementUserData.setName
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useMatrixStack
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.minSize
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import org.joml.Vector2f
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class Tip(
    val setting: Setting = DefaultSetting,
    val modifier: Modifier = DefaultModifier,
    val content: BoxScope.() -> Unit
) : Tickable {
    data class Setting(
        val showDelay: Duration = 50.milliseconds,
        val hideDelay: Duration = 0.milliseconds,
        val fadeInDuration: Duration = 200.milliseconds,
        val fadeInOffset: Float = -2f,
        val optionalDirection: List<Direction> = Direction.clockwiseFromTop,
        val backgroundColor: ARGBColor = Colors.WHITE
    ) {
        init {
            require(optionalDirection.isNotEmpty()) { "optionalDirection must not be empty" }
            require(optionalDirection.size <= 4) { "optionalDirection must not be more than 4" }
        }
    }

    companion object {
        val minSize = Size(18f, 18f)
        val DefaultModifier get() = Modifier.padding(4).margin(4).minSize(minSize.width, minSize.height)
        val DefaultSetting = Setting()
    }

    private lateinit var box: BoxWidget

    private var showTimeMark: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    private var currentDirection = setting.optionalDirection.first()

    fun init(parent: () -> Transform) {
        val directions = setting.optionalDirection
        box = BoxWidget().apply {
            //第一次测量之后才能选择合适的方向,所以第一次渲染会重新测量一次选择更好的位置
            var firstRemeasure = true
            setName("HoverTip")
            renderBackground = { _, _, _, _ ->
                val parentBox = parent().asWorldCoordinateBox
                //如果当前Box不在可放置的方向上,则重新测量最合适的方向
                TipHelper.canPlaceDirections(transform, margin, parentBox, setting.optionalDirection).let { directions ->
                    if (directions.isEmpty()) {
                        val (maxConstraints, direction) = TipHelper.evaluatePlacementOptions(transform, parentBox, margin, setting.optionalDirection)
                        constraints = Constraints.of(minSize = minSize, maxSize = maxConstraints)
                        remeasure()
                        currentDirection = direction
                        if (!firstRemeasure) firstRemeasure = true
                        return@let
                    }
                    if (currentDirection !in directions || firstRemeasure) {
                        val (maxConstraints, direction) = TipHelper.evaluatePlacementOptions(transform, parentBox, margin, directions)
                        constraints = Constraints.of(minSize = minSize, maxSize = maxConstraints)
                        remeasure()
                        currentDirection = direction
                        if (firstRemeasure) firstRemeasure = false
                    }
                }
                TipHelper.updatePosition(transform, margin, parentBox, currentDirection)
            }
            render = { guiGraphics, _, _, _ ->
                TipHelper.tipRender(transform, guiGraphics, currentDirection, parent().asWorldCoordinateBox, setting.backgroundColor)
            }
            if (transform.parent() != parent()) transform.parent = { parent() }
            modifier.foldInApply()

            Compose { BoxScope { this }.content() }

            //或许应该先无约束测量然后根据当前的尺寸选择合适的方向, calculateScore 方法应该根据当前的尺寸重构
            val parentBox = parent().asWorldCoordinateBox
            val (maxConstraints, direction) = TipHelper.evaluatePlacementOptions(null, parentBox, margin, directions)
            currentDirection = direction
            constraints = Constraints.of(minSize = minSize, maxSize = maxConstraints)
            measure(Constraints.of())
            measureCompletion()
            layout()
        }

    }

    fun show() {
        showTimeMark = TimeSource.Monotonic.markNow() + setting.showDelay
    }

    fun render(guiGraphics: IGGuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!this::box.isInitialized || showTimeMark.elapsedNow() < setting.showDelay) return
        val (alpha, offset) = calculateAlphaAndOffset()
        guiGraphics {
            useMatrixStack {
                it.translate(offset.x(), offset.y())
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

    private fun calculateAlphaAndOffset(): Pair<Float, Vector2f> {
        if (setting.fadeInOffset == 0f || setting.fadeInDuration == Duration.ZERO) return 1f to Vector2f(0f, 0f)
        val elapsedTime = showTimeMark.elapsedNow() - setting.showDelay

        // 计算 fadeIn 进度
        val fadeInProgress = (elapsedTime / setting.fadeInDuration).toFloat().coerceIn(0f, 1f)

        // 透明度 (alpha)：从 0 → 1
        val alpha = fadeInProgress

        // 偏移量 (offset)：从 fadeInOffset → 0
        val offset = setting.fadeInOffset * (1f - fadeInProgress)

        // 根据当前方向计算偏移向量
        return alpha to when (currentDirection) {
            Top    -> Vector2f(0f, offset)
            Bottom -> Vector2f(0f, -offset)
            Left   -> Vector2f(offset, 0f)
            Right  -> Vector2f(-offset, 0f)
        }
    }
}