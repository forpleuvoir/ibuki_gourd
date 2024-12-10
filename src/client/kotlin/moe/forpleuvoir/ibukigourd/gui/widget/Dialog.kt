package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderAlignmentText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.BiasAlignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.HSVColor

fun SimpleDialog(
    title: State<Text>,
    modifier: Modifier = Modifier,
    contentModifier: RowScope.() -> Modifier = { Modifier },
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    contentOutlineColor: State<ARGBColor> = bgColor,
    contentInnerColor: State<ARGBColor> = stateOf(Colors.WHITE),
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit
): IGScreenImpl = Dialog(
    modifier = modifier,
    screenModifier = screenModifier,
    bgColor = bgColor,
    parentScreen = parentScreen
) {
    //Title
    TextLabel(title)
    //Content
    DialogContent(contentModifier(), contentOutlineColor, contentInnerColor, content)
}

fun ConfirmDialog(
    title: State<Text>,
    modifier: Modifier = Modifier,
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    onConfirm: () -> Unit = {
        mc.currentScreen?.close()
    },
    onCancel: () -> Unit = {
        mc.currentScreen?.close()
    },
    content: RowScope.() -> Unit
): IGScreenImpl = Dialog(
    modifier = modifier,
    screenModifier = screenModifier,
    bgColor = bgColor,
    parentScreen = parentScreen
) {
    //Title
    TextLabel(title)
    //Content
    content()
    //Button
    Column(
        Modifier.matchSibling(),
        horizontalArrangement = Arrangement.spacedBy(4f, Alignment.Right)
    ) {
        //TODO i18n
        Button(
            color = HSVColor(135f, .35f, 1f)
        ) {
            TextLabel("Confirm")
            click {
                onConfirm()
            }
        }
        Button {
            TextLabel("Cancel")
            click { onCancel() }
        }
    }
}


fun Dialog(
    modifier: Modifier = Modifier,
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: RowScope.() -> Unit
): IGScreenImpl = PopupScreen(
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
            }
            .renderOverlay { context, _, _, _ ->
                //TODO i18n
                context.renderAlignmentText(
                    "点击空白处返回",
                    screen()!!.transform,
                    color = HSVColor(0f, 0f, 0.85f),
                    alignment = BiasAlignment.Horizontal(0f) + BiasAlignment.Vertical(0.95f),
                    shadow = true
                )
            }
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(2f),
        horizontalAlignment = Alignment.Left,
        content = content
    )
}

fun WidgetContainerScope.DialogContent(
    modifier: Modifier = Modifier,
    contentOutlineColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    contentInnerColor: State<ARGBColor> = stateOf(Colors.WHITE),
    content: BoxScope.() -> Unit
) = Box(
    modifier = Modifier
        .padding(5f)
        .renderBackground { context, x, y, d ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, WidgetTextures.DIALOG_CONTENT_OUTLINE, contentOutlineColor.getValue())
                pushWidgetTexture(transform, WidgetTextures.DIALOG_CONTENT_INNER, contentInnerColor.getValue())
            }
        }
        .then(modifier),
    content
)