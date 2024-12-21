package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.toARGBColorState
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

@JvmName("ColorPicker")
fun WidgetContainerScope.ColorPicker(
    colorState: MutableState<Color>,
    modifier: Modifier = Modifier,
    colorPickerModifier: ColumnScope.() -> Modifier = { Modifier },
    resultModifier: ColumnScope.() -> Modifier = { Modifier },
    scope: TabScope.() -> Unit = {}
) = ColorPicker(colorState.toARGBColorState(), modifier, colorPickerModifier, resultModifier, scope)

@JvmName("HsvColorPicker")
fun WidgetContainerScope.ColorPicker(
    colorState: MutableState<HSVColor>,
    modifier: Modifier = Modifier,
    colorPickerModifier: ColumnScope.() -> Modifier = { Modifier },
    resultModifier: ColumnScope.() -> Modifier = { Modifier },
    scope: TabScope.() -> Unit = {}
) = ColorPicker(colorState.toARGBColorState(), modifier, colorPickerModifier, resultModifier, scope)

@JvmName("ARGBColorPicker")
fun WidgetContainerScope.ColorPicker(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    colorPickerModifier: ColumnScope.() -> Modifier = { Modifier },
    resultModifier: ColumnScope.() -> Modifier = { Modifier },
    scope: TabScope.() -> Unit = {}
) = Tabs(
    direction = Direction.Top,
    modifier = modifier,
) {
    tabColor.setValue(Color(255, 204, 240))
    inactiveColor.setValue(Color(179, 242, 255))
    Tab("HSV") {
        val color = mutableStateOf(colorState.getValue())
        color.subscribe {
            colorState.setValue(it)
        }
        Column(
            horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
        ) {
            HSVColorPicker(color, modifier = colorPickerModifier())
            ColorResult(color, Modifier.size(78f, 78f).then(resultModifier()))
        }
    }
    Tab("RGB") {
        val color = mutableStateOf(colorState.getValue())
        color.subscribe {
            colorState.setValue(it)
        }
        Column(
            horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
        ) {
            this.ARGBColorPicker(color, modifier = colorPickerModifier())
            ColorResult(color, Modifier.size(78f, 78f).then(resultModifier()))
        }
    }
    scope()
}


fun WidgetContainerScope.ARGBColorPicker(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: RowScope.() -> Unit = {}
) = Row(
    modifier = Modifier.size(200f, 82f).then(modifier),
    verticalArrangement = Arrangement.spacedBy(2f, Alignment.CenterVertically),
) {
    Column(
        Modifier.weight(1).name("red").hoverText(IGLang.red, Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Left)))
    ) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            RedColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            mutableStateOf(colorState, { it.red }) { Color(colorState.getValue().argb).red(it) },
            range = 0..255,
            modifier = Modifier.width(38f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(
        Modifier.weight(1).name("green").hoverText(IGLang.green, Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Left)))
    ) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            GreenColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            mutableStateOf(colorState, { it.green }) { Color(colorState.getValue().argb).green(it) },
            range = 0..255,
            modifier = Modifier.width(38f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(
        Modifier.weight(1).name("blue").hoverText(IGLang.blue, Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Left)))
    ) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            BlueColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            mutableStateOf(colorState, { it.blue }) { Color(colorState.getValue().argb).blue(it) },
            range = 0..255,
            modifier = Modifier.width(38f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(
        Modifier.weight(1).name("alpha").hoverText(IGLang.alpha, Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Left)))
    ) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            AlphaColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            mutableStateOf(colorState, { it.alpha }) { Color(colorState.getValue().argb).alpha(it) },
            range = 0..255,
            modifier = Modifier.width(38f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    scope()
}


fun WidgetContainerScope.ColorResult(
    color: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = ColoredBox(
    color,
    Modifier.mousePress {
        it.tryUse(wasMouseOver && it.button == Mouse.LEFT).onSuccess {
            mc.keyboard.clipboard = color.getValue().hexStr
            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }
    }
        .name("ColorResult")
        .hoverText(mutableStateOf(color) { IGLang.clickCopyColor(it.hexStr) })
        .then(modifier),
    scope
)


fun WidgetContainerScope.RedColorSlider(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.redF },
    colorComponentSetter = { c, i -> Color(c.argb).red(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).red(i).alpha(255) },
    modifier, scope
)

fun WidgetContainerScope.GreenColorSlider(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.greenF },
    colorComponentSetter = { c, i -> Color(c.argb).green(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).green(i).alpha(255) },
    modifier, scope
)

fun WidgetContainerScope.BlueColorSlider(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.blueF },
    colorComponentSetter = { c, i -> Color(c.argb).blue(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).blue(i).alpha(255) },
    modifier, scope
)

fun WidgetContainerScope.AlphaColorSlider(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.alphaF },
    colorComponentSetter = { c, i -> Color(c.argb).alpha(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).alpha(i) },
    modifier, scope
)


fun WidgetContainerScope.ColorComponentSlider(
    colorState: MutableState<ARGBColor>,
    colorComponentGetter: (ARGBColor) -> Float,
    colorComponentSetter: (ARGBColor, Float) -> ARGBColor,
    renderColorComponentSetter: (ARGBColor, Float) -> ARGBColor,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): IGWidgetImpl {
    val valueState = mutableStateOf(colorComponentGetter(colorState.getValue()))
    var progress = valueState.getValue().toDouble()
    MutableState.bind(colorState, valueState, {
        colorComponentGetter(it).apply {
            progress = this.toDouble()
        }
    }, {
        colorComponentSetter(colorState.getValue(), it)
    })
    return FloatSlider(
        valueState,
        range = 0f..1f,
        valueMapper = {
            progress = it
            it.toFloat()
        },
        modifier = Modifier
            .name("ColorComponentSlider")
            .render { context, _, _, _ ->
                colorComponentSliderRender(
                    context,
                    RenderLayer.getGui(),
                    progress,
                    colorState.getValue(),
                    renderColorComponentSetter(colorState.getValue(), 0f),
                    renderColorComponentSetter(colorState.getValue(), 1f)
                )
            }.then(modifier),
        scope = scope
    )
}

internal fun IGWidget.colorComponentSliderRender(
    context: IGDrawContext,
    layer: RenderLayer,
    progress: Double,
    color: ARGBColor,
    startColor: ARGBColor,
    endColor: ARGBColor
) {
    val box = transform.asWorldCoordinateBox
    val bg = WidgetTextures.COLOR_SLIDER_BG
    val content = box.copy(box.x + bg.corner.left, box.y + bg.corner.top, box.width - bg.corner.width, box.height - bg.corner.height)
    context.useScissor(content) {
        batchRenderTextureColored {
            pushTileTexture(content, WidgetTextures.ALPHA, tileScale = 0.5f)
        }
    }
    context.renderGradientBox(content, startColor, endColor, layer = layer)
    context.batchRenderTextureColored {
        pushWidgetTexture(box, bg, color = color + Color(0, 0, 0, 255))
        pushWidgetTexture(
            box.copy(
                x = box.x + ((box.width - WidgetTextures.COLOR_SLIDER_ARROW.width.toFloat()) * progress.toFloat()),
                width = WidgetTextures.COLOR_SLIDER_ARROW.width.toFloat()
            ),
            WidgetTextures.COLOR_SLIDER_ARROW
        )
    }
}