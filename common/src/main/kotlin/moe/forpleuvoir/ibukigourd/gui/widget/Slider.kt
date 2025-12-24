package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useScissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import kotlin.time.Duration

val SliderColorA = HSVColor(210f, .3f, .7f)
val SliderColorB = HSVColor(210f, .1f, 1f)

fun <T : Comparable<T>> ContainerScope.Slider(
    value: MutableState<T>,
    minValue: T,
    maxValue: T,
    progressMapper: (value: T, minVale: T, maxValue: T) -> Double,
    valueMapper: (progress: Double) -> T,
    textMapper: (T) -> MutableText = { Literal(it.toString()) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): GuiWidgetImpl {
    var pressed = false
    value.onSetValue = { it.coerceIn(minValue, maxValue) }
    var notifiable = true
    var progress = progressMapper(value.getValue(), minValue, maxValue)
    if (!progress.isFinite()) progress = 0.0
    value.subscribe {
        if (notifiable) progress = progressMapper(value.getValue(), minValue, maxValue)
    }
    fun setFromMouse(ref: Transform, x: Float, y: Float) {
        progress = orientation.peek({
            1.0 - ((ref.worldBottom.toDouble() - y).coerceIn(0.0, ref.height.toDouble()) / ref.height)
        }, {
            1.0 - ((ref.worldRight.toDouble() - x).coerceIn(0.0, ref.width.toDouble()) / ref.width)
        })
        notifiable = false
        value.setValue(valueMapper(progress))
        notifiable = true
    }
    return Widget(
        modifier = Modifier
            .name("Slider")
            .minWidth(40f)
            .minHeight(16f)
            .mousePress { event ->
                onMousePress(event)
                if (event.button != Mouse.LEFT) return@mousePress
                pressed = wasMouseOver
                event.tryUse(wasMouseOver).onSuccess {
                    soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
                    setFromMouse(transform, event.x, event.y)
                }
            }
            .mouseRelease { pressed = false }
            .mouseDragged { event ->
                event.tryUse(wasDragging && pressed).onSuccess {
                    setFromMouse(transform, event.x, event.y)
                }
            }
            .render { guiGraphics, _, _, _ ->
                val box1 = orientation.peek({
                    transform.asWorldCoordinateBox.copy(height = transform.height * progress.toFloat())
                }, {
                    transform.asWorldCoordinateBox.copy(width = transform.width * progress.toFloat())
                })
                val box2 = orientation.peek({
                    transform.asWorldCoordinateBox.copy(y = box1.bottom, height = transform.height - box1.height)
                }, {
                    transform.asWorldCoordinateBox.copy(x = box1.right, width = transform.width - box1.width)
                })
                val theme = theme(PressableTheme.Button3, hover = wasMouseOver, pressed = pressed)
                guiGraphics {
                    useScissor(box1.expandEdges(0f, 2f)) {
                        pushWidgetTexture(transform, theme, colorA)
                    }
                    useScissor(box2.expandEdges(0f, 2f)) {
                        pushWidgetTexture(transform, theme, colorB)
                    }
                    pushAlignmentText(
                        textMapper(value.getValue()),
//                        transform.asWorldCoordinateBox.copy(y = transform.worldY + ModernUICompat.textEngineEnabled(textRenderOffset.y(), 1f))
                        transform.asWorldCoordinateBox.copy(y = transform.worldY + 1f)
                    )
                }
            }.then(modifier)
    ) {
        scope()
    }

}

fun <T> ContainerScope.NumberSlider(
    value: MutableState<T>,
    minValue: T,
    maxValue: T,
    valueMapper: (progress: Double) -> T,
    textMapper: (T) -> MutableText = { Literal(it.toString()) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) where T : Number, T : Comparable<T> = Slider(
    value = value,
    minValue = minValue,
    maxValue = maxValue,
    progressMapper = { value, min, max -> (value.toDouble() - min.toDouble()) / (max.toDouble() - min.toDouble()) },
    valueMapper = valueMapper,
    textMapper = textMapper,
    orientation = orientation,
    colorA = colorA,
    colorB = colorB,
    modifier = Modifier.name("NumberSlider").then(modifier),
    scope = scope
)

fun ContainerScope.IntSlider(
    value: MutableState<Int>,
    range: IntRange,
    textMapper: (Int) -> MutableText = { Literal(it.toString()) },
    valueMapper: (progress: Double) -> Int = { (range.first + (range.last - range.first) * it).toInt() },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.first,
    maxValue = range.last,
    textMapper = textMapper,
    valueMapper = valueMapper,
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = Modifier.name("IntSlider").then(modifier),
    scope = scope
)

fun ContainerScope.LongSlider(
    value: MutableState<Long>,
    range: LongRange,
    textMapper: (Long) -> MutableText = { Literal(it.toString()) },
    valueMapper: (progress: Double) -> Long = { (range.first + (range.last - range.first) * it).toLong() },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.first,
    maxValue = range.last,
    textMapper = textMapper,
    valueMapper = valueMapper,
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = Modifier.name("LongSlider").then(modifier),
    scope = scope
)

fun ContainerScope.FloatSlider(
    value: MutableState<Float>,
    range: ClosedFloatingPointRange<Float>,
    textMapper: (Float) -> MutableText = { Literal("%.2f".format(it)) },
    valueMapper: (progress: Double) -> Float = { (range.start + (range.endInclusive - range.start) * it).toFloat() },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.start,
    maxValue = range.endInclusive,
    textMapper = textMapper,
    valueMapper = valueMapper,
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = Modifier.name("FloatSlider").then(modifier),
    scope = scope
)

fun ContainerScope.DoubleSlider(
    value: MutableState<Double>,
    range: ClosedFloatingPointRange<Double>,
    textMapper: (Double) -> MutableText = { Literal("%.2f".format(it)) },
    valueMapper: (progress: Double) -> Double = { (range.start + (range.endInclusive - range.start) * it) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.start,
    maxValue = range.endInclusive,
    textMapper = textMapper,
    valueMapper = valueMapper,
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = Modifier.name("DoubleSlider").then(modifier),
    scope = scope
)

fun ContainerScope.PercentageSlider(
    value: MutableState<Double>,
    range: ClosedFloatingPointRange<Double> = 0.0..1.0,
    textMapper: (Double) -> MutableText = { Literal("%.2f".format(it * 100) + "%") },
    valueMapper: (progress: Double) -> Double = { (range.start + (range.endInclusive - range.start) * it) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.start,
    maxValue = range.endInclusive,
    textMapper = textMapper,
    valueMapper = valueMapper,
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = Modifier.name("PercentageSlider").then(modifier),
    scope = scope
)

fun ContainerScope.DurationSlider(
    value: MutableState<Duration>,
    range: ClosedRange<Duration>,
    textMapper: (Duration) -> MutableText = { Literal(it.toString()) },
    valueMapper: (progress: Double) -> Duration = { range.start + (range.endInclusive - range.start) * it },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = SliderColorA,
    colorB: ARGBColor = SliderColorB,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = Slider(
    value = value,
    minValue = range.start,
    maxValue = range.endInclusive,
    progressMapper = { value, min, max -> (value - min) / (max - min) },
    valueMapper = valueMapper,
    textMapper = textMapper,
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = Modifier.name("DurationSlider").then(modifier),
    scope = scope
)
