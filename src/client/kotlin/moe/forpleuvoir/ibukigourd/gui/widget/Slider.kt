package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderAlignmentText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import kotlin.math.abs

fun <T> WidgetContainerScope.NumberSlider(
    value: State<T>,
    minValue: T,
    maxValue: T,
    valueMapper: (progress: Double) -> T,
    textMapper: (T) -> Text = { Literal(it.toString()) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = HSVColor(210f, .3f, .7f),
    colorB: ARGBColor = HSVColor(210f, .1f, 1f),
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): IGWidgetImpl where T : Number, T : Comparable<T> {
    var pressed = false
    value.onSetValue = { it.coerceIn(minValue, maxValue) }
    var notifiable = true
    var progress = value.getValue().toDouble() / abs(maxValue.toDouble() - minValue.toDouble())
    value.subscribe {
        if (notifiable) progress = it.toDouble() / abs(maxValue.toDouble() - minValue.toDouble())
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
            .minWidth(40f)
            .minHeight(16f)
            .mousePress { event ->
                onMousePress(event)
                if (event.button != Mouse.LEFT) return@mousePress
                pressed = wasMouseOver
                event.tryUse(wasMouseOver).onSuccess {
                    soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
                    setFromMouse(transform, event.x, event.y)
                }
            }
            .mouseRelease { pressed = false }
            .mouseDragged { event ->
                event.tryUse(wasDragging && pressed).onSuccess {
                    setFromMouse(transform, event.x, event.y)
                }
            }
            .render { context, _, _, _ ->
                val box1 = orientation.peek({
                    transform.asWorldBox.copy(height = transform.height * progress.toFloat())
                }, {
                    transform.asWorldBox.copy(width = transform.width * progress.toFloat())
                })
                val box2 = orientation.peek({
                    transform.asWorldBox.copy(y = box1.bottom, height = transform.height - box1.height)
                }, {
                    transform.asWorldBox.copy(x = box1.right, width = transform.width - box1.width)
                })
                context.useScissor(box1) {
                    batchRenderTextureColored {
                        pushWidgetTexture(transform, theme(PressableTheme.Button3, hover = wasMouseOver, pressed = pressed), colorA)
                    }
                }
                context.useScissor(box2) {
                    batchRenderTextureColored {
                        pushWidgetTexture(transform, theme(PressableTheme.Button3, hover = wasMouseOver, pressed = pressed), colorB)
                    }
                }
                context.renderAlignmentText(textMapper(value.getValue()), transform.asWorldBox.copy(y = transform.worldY + 1f))
            }.then(modifier)
    ) {
        scope()
    }

}

fun WidgetContainerScope.IntSlider(
    value: State<Int>,
    range: IntRange,
    textMapper: (Int) -> Text = { Literal(it.toString()) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = HSVColor(210f, .3f, .7f),
    colorB: ARGBColor = HSVColor(210f, .1f, 1f),
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.first,
    maxValue = range.last,
    textMapper = textMapper,
    valueMapper = { (range.first + (range.last - range.first) * it).toInt() },
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = modifier,
    scope = scope
)

fun WidgetContainerScope.LongSlider(
    value: State<Long>,
    range: LongRange,
    textMapper: (Long) -> Text = { Literal(it.toString()) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = HSVColor(210f, .3f, .7f),
    colorB: ARGBColor = HSVColor(210f, .1f, 1f),
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.first,
    maxValue = range.last,
    textMapper = textMapper,
    valueMapper = { (range.first + (range.last - range.first) * it).toLong() },
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = modifier,
    scope = scope
)

fun WidgetContainerScope.FloatSlider(
    value: State<Float>,
    range: ClosedFloatingPointRange<Float>,
    textMapper: (Float) -> Text = { Literal("%.2f".format(it)) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = HSVColor(210f, .3f, .7f),
    colorB: ARGBColor = HSVColor(210f, .1f, 1f),
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.start,
    maxValue = range.endInclusive,
    textMapper = textMapper,
    valueMapper = { (range.start + (range.endInclusive - range.start) * it).toFloat() },
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = modifier,
    scope = scope
)

fun WidgetContainerScope.DoubleSlider(
    value: State<Double>,
    range: ClosedFloatingPointRange<Double>,
    textMapper: (Double) -> Text = { Literal("%.2f".format(it)) },
    orientation: Orientation = Orientation.Horizontal,
    colorA: ARGBColor = HSVColor(210f, .3f, .7f),
    colorB: ARGBColor = HSVColor(210f, .1f, 1f),
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = NumberSlider(
    value = value,
    minValue = range.start,
    maxValue = range.endInclusive,
    textMapper = textMapper,
    valueMapper = { (range.start + (range.endInclusive - range.start) * it) },
    colorA = colorA,
    colorB = colorB,
    orientation = orientation,
    modifier = modifier,
    scope = scope
)