package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

fun WidgetContainerScope.ColorPicker(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    resultModifier: ColumnScope.() -> Modifier = { Modifier }
) {
    Row(
        Modifier.size(320f, 120f).then(modifier),
    ) {
        val picker: State<ColumnScope.() -> IGWidget> = stateOf {
            val color = stateOf(colorState.getValue())
            color.subscribe {
                colorState.setValue(it)
            }
            HSVColorPicker(color, Modifier.weight(1))
        }
        val isHSV = stateOf(true).apply {
            subscribe { hsv ->
                if (hsv) picker.setValue {
                    val color = stateOf(colorState.getValue())
                    color.subscribe {
                        colorState.setValue(it)
                    }
                    HSVColorPicker(color, Modifier.weight(1))
                } else picker.setValue {
                    val color = stateOf(colorState.getValue())
                    color.subscribe {
                        colorState.setValue(it)
                    }
                    ARGBColorPicker(color, Modifier.weight(1))
                }
            }
        }
        Column {
            Button(
                Modifier.active(stateOf(isHSV) { !it })
            ) {
                TextLabel("HSV")
                press { isHSV.switch() }
            }
            Button(
                Modifier.active(isHSV)
            ) {
                TextLabel("RGB")
                press { isHSV.switch() }
            }
            Column(
                Modifier.weight(1),
                horizontalArrangement = Arrangement.Right
            ) {
                TextLabel(stateOf(colorState) { Literal(it.hexStr).style { color(it.rgb) } })
            }
        }
        Column(
            horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally),
            modifier = Modifier.weight(1).fill()
        ) {
            Proxy(picker)
            ColorResult(colorState, modifier = Modifier.size(84f, 84f).then(resultModifier()))
        }
    }

}

fun WidgetContainerScope.ARGBColorPicker(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier
) = Row(
    modifier = Modifier.size(260f, 84f).then(modifier),
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically),
) {
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 2f).weight(1)
        ) {
            RedColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            stateOf(colorState, { it.red }) { Color(colorState.getValue().argb).red(it) },
            range = 0..255,
            modifier = Modifier.width(40f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 2f).weight(1)
        ) {
            GreenColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            stateOf(colorState, { it.green }) { Color(colorState.getValue().argb).green(it) },
            range = 0..255,
            modifier = Modifier.width(40f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 2f).weight(1)
        ) {
            BlueColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            stateOf(colorState, { it.blue }) { Color(colorState.getValue().argb).blue(it) },
            range = 0..255,
            modifier = Modifier.width(40f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 2f).weight(1)
        ) {
            AlphaColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        IntEditor(
            stateOf(colorState, { it.alpha }) { Color(colorState.getValue().argb).alpha(it) },
            range = 0..255,
            modifier = Modifier.width(40f),
            editorModifier = { Modifier.weight(1) }
        )
    }
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
    }.then(modifier)
) {
    HoverTip {
        TextLabel(stateOf(color) { "点击复制颜色:${it.hexStr}" })
    }
    scope()
}


fun WidgetContainerScope.RedColorSlider(
    colorState: State<ARGBColor>,
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
    colorState: State<ARGBColor>,
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
    colorState: State<ARGBColor>,
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
    colorState: State<ARGBColor>,
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
    colorState: State<ARGBColor>,
    colorComponentGetter: (ARGBColor) -> Float,
    colorComponentSetter: (ARGBColor, Float) -> ARGBColor,
    renderColorComponentSetter: (ARGBColor, Float) -> ARGBColor,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): IGWidgetImpl {
    val valueState = stateOf(colorComponentGetter(colorState.getValue()))
    var progress = valueState.getValue().toDouble()
    State.bind(colorState, valueState, {
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
    val box = transform.asWorldBox
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