package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.mousePress
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderBackground
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

fun OpenDialog(
    title: State<Text>,
    modifier: Modifier = Modifier,
    contentModifier: RowScope.() -> Modifier = { Modifier },
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    contentOutlineColor: State<ARGBColor> = bgColor,
    contentInnerColor: State<ARGBColor> = stateOf(Colors.WHITE),
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit
) = openScreen(Dialog(title, modifier, contentModifier, screenModifier, bgColor, contentOutlineColor, contentInnerColor, parentScreen, content))

fun Dialog(
    title: State<Text>,
    modifier: Modifier = Modifier,
    contentModifier: RowScope.() -> Modifier = { Modifier },
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    contentOutlineColor: State<ARGBColor> = bgColor,
    contentInnerColor: State<ARGBColor> = stateOf(Colors.WHITE),
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit
) = PopupScreen(
    modifier = Modifier.name("Dialog") then screenModifier,
    parentScreen = parentScreen
) {
    Row(
        modifier = Modifier
            .padding(6f)
            .align(Alignment.Center)
            .mousePress {
                onMousePress(it)
                it.tryUse(!wasMouseOver && it.button == Mouse.LEFT).onSuccess {
                    screen()?.close()
                }
            }
            .renderBackground { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.DIALOG_BG, bgColor.getValue())
                }
            }.then(modifier),
        verticalArrangement = Arrangement.spacedBy(2f),
        horizontalAlignment = Alignment.Left,
    ) {
        Column(
            modifier = Modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            //Title
            TextLabel(title)
        }
        //Content
        Box(
            modifier = Modifier
                .padding(5f)
                .renderBackground { context, x, y, d ->
                    context.batchRenderTextureColored {
                        pushWidgetTexture(transform, WidgetTextures.DIALOG_CONTENT_OUTLINE, contentOutlineColor.getValue())
                        pushWidgetTexture(transform, WidgetTextures.DIALOG_CONTENT_INNER, contentInnerColor.getValue())
                    }
                }.then(contentModifier())
        ) {
            content()
        }

    }
}