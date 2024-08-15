package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.wasMouseOver
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.ibukigourd.util.delegate
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick

fun GuiScope<out WidgetContainer>.button(
    theme: PressableTheme = PressableTheme.Button2,
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget()) {
    Modifier.padding(6)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, status(theme.disabled, theme.idle, theme.hovered, theme.pressed))
            }
        }
        .then(modifier).foldInApply()
    ButtonScope(this).content()
}

fun GuiScope<out WidgetContainer>.flatButton(
    disabledColor: () -> ARGBColor = { Color(0) },
    idleColor: () -> ARGBColor = { Color(0) },
    hoveredColor: () -> ARGBColor = { Color(0) },
    pressedColor: () -> ARGBColor = { Color(0) },
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget()) {
    Modifier.padding(1)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            wasMouseOver {
                context.renderBox(transform.asWorldBox, status(disabledColor, idleColor, hoveredColor, pressedColor))
            }
        }
        .then(modifier).foldInApply()
    ButtonScope(this).content()
}

fun GuiScope<out WidgetContainer>.booleanButton(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        press { statusDelegate.setValue(!statusDelegate.getValue()) }
        text(text = {
            val status = statusDelegate.getValue()
            Literal(status.toString())
                .style {
                    color(status.pick(Colors.GREEN, Colors.RED))
                }
        })
    }
) = button(modifier = modifier, content = content)

fun GuiScope<out WidgetContainer>.lockButton(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {}
) = addWidgetChild(IGButtonWidget()) {
    val list = listOf(
        PressableTheme.LOCK.pressed,
        PressableTheme.LOCK.idle,
        PressableTheme.LOCK.hovered,
        PressableTheme.LOCK.disabled,
        PressableTheme.UNLOCK.pressed,
        PressableTheme.UNLOCK.idle,
        PressableTheme.UNLOCK.hovered,
        PressableTheme.UNLOCK.disabled,
    )
    val scope = ButtonScope(this)
    scope.content()
    val lock = scope.icon(list.maxBy { it.width + it.height }) {
        changedRemeasure = false
    }
    Modifier
        .render { context, _, _, _ ->
            this as IGButtonWidget
            lock.iconTexture = theme(statusDelegate.getValue().pick(PressableTheme.LOCK, PressableTheme.UNLOCK))
        }
        .padding(2f).renderHoveredOutlineBox(Colors.AQUA)
        .then(modifier).foldInApply()
    press { statusDelegate.setValue(!statusDelegate.getValue()) }

}