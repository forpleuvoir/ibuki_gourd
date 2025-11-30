package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.screen.closeScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
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
    contentModifier: ColumnScope.() -> Modifier = { Modifier },
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    contentOutlineColor: State<ARGBColor> = bgColor,
    contentInnerColor: State<ARGBColor> = stateOf(Colors.WHITE),
    parentScreen: IGScreen? = mc.screen as IGScreen?,
    content: BoxScope.() -> Unit
): IGScreenImpl = Dialog(
    modifier = modifier,
    screenModifier = screenModifier,
    bgColor = bgColor,
    parentScreen = parentScreen
) {
    //Title
    Text(title)
    //Content
    DialogContent(contentModifier(), contentOutlineColor, contentInnerColor, content)
}

data class ConfirmDialogScope(
    val owner: GuiWidgetContainer,
    var onConfirm: () -> Unit,
    var onCancel: () -> Unit
) : ColumnScope {
    override fun owner(): GuiWidgetContainer = owner

    fun confirm(block: () -> Unit) {
        onConfirm = block
    }

    fun cancel(block: () -> Unit) {
        onCancel = block
    }
}

fun ConfirmDialog(
    title: State<Text>,
    modifier: Modifier = Modifier,
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    parentScreen: IGScreen? = mc.screen as IGScreen?,
    onConfirm: () -> Unit = {
        closeScreen()
    },
    onCancel: () -> Unit = {
        closeScreen()
    },
    content: ConfirmDialogScope.() -> Unit
): IGScreenImpl = Dialog(
    modifier = modifier,
    screenModifier = screenModifier,
    bgColor = bgColor,
    parentScreen = parentScreen
) {
    val scope = ConfirmDialogScope(this.owner(), onConfirm, onCancel)
    //Title
    Text(title)
    //Content
    scope.content()
    //Button
    Row(
        Modifier.matchSibling().minWidth(120f),
        horizontalArrangement = Arrangement.spacedBy(4f, Alignment.Right)
    ) {
        Button {
            Text(IGLang.confirm)
            click {
                scope.onConfirm()
            }
        }
        Button {
            Text(IGLang.cancel)
            click {
                scope.onCancel()
            }
        }
    }
}


fun Dialog(
    modifier: Modifier = Modifier,
    screenModifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    parentScreen: IGScreen? = mc.screen as IGScreen?,
    content: ColumnScope.() -> Unit
): IGScreenImpl = PopupScreen(
    modifier = Modifier.name("Dialog") then screenModifier,
    parentScreen = parentScreen
) {
    Column(
        modifier = Modifier
            .padding(6f)
            .align(Alignment.Center)
            .mousePress {
                onMousePress(it)
                it.tryUse(!wasMouseOver && it.button == Mouse.LEFT).onSuccess {
                    screen()?.close()
                }
            }
            .renderBackground { guiGraphics, _, _, _ ->
                guiGraphics.pushWidgetTexture(transform, WidgetTextures.DIALOG_BG, bgColor.getValue())
            }
            .renderOverlay { guiGraphics, _, _, _ ->
                guiGraphics.pushAlignmentText(
                    IGLang.clickBlankBack,
                    screen()!!.transform,
                    color = HSVColor(0f, 0f, 0.85f),
                    alignment = Alignment.biasedBy(0f, 0.95f),
                    shadow = true
                )
            }
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(2f),
        horizontalAlignment = Alignment.Left,
        content = content
    )
}

fun ContainerScope.DialogContent(
    modifier: Modifier = Modifier,
    contentOutlineColor: State<ARGBColor> = stateOf(Color(0xFFF4D9FF)),
    contentInnerColor: State<ARGBColor> = stateOf(Colors.WHITE),
    content: BoxScope.() -> Unit
) = Box(
    modifier = Modifier
        .padding(5f)
        .renderBackground { guiGraphics, x, y, d ->
            guiGraphics {
                pushWidgetTexture(transform, WidgetTextures.DIALOG_CONTENT_OUTLINE, contentOutlineColor.getValue())
                pushWidgetTexture(transform, WidgetTextures.DIALOG_CONTENT_INNER, contentInnerColor.getValue())
            }
        }
        .then(modifier),
    content
)