package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.wasMouseOver
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextField
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.ibukigourd.util.toggle
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick

fun WidgetContainerScope.Button(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    theme: PressableTheme = PressableTheme.Button2,
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget(horizontalArrangement, verticalAlignment)) {
    Modifier.padding(6)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, status(theme.disabled, theme.idle, theme.hovered, theme.pressed))
            }
        }
        .then(modifier).foldInApply()
    ButtonScope { this }.content()
}

fun WidgetContainerScope.FlatButton(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    disabledColor: State<ARGBColor> = stateOf(Color(0)),
    idleColor: State<ARGBColor> = stateOf(Color(0)),
    hoveredColor: State<ARGBColor> = stateOf(Color(0)),
    pressedColor: State<ARGBColor> = stateOf(Color(0)),
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget(horizontalArrangement, verticalAlignment)) {
    Modifier.padding(1)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            wasMouseOver {
                context.renderBox(
                    transform.asWorldBox,
                    status(disabledColor.getValue(), idleColor.getValue(), hoveredColor.getValue(), pressedColor.getValue())
                )
            }
        }
        .then(modifier).foldInApply()
    ButtonScope { this }.content()
}

fun WidgetContainerScope.FlatButton(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    disabledColor: ARGBColor = Color(0),
    idleColor: ARGBColor = Color(0),
    hoveredColor: ARGBColor = Color(0),
    pressedColor: ARGBColor = Color(0),
    content: ButtonScope.() -> Unit = { }
) = FlatButton(
    modifier,
    horizontalArrangement,
    verticalAlignment,
    stateOf(disabledColor),
    stateOf(idleColor),
    stateOf(hoveredColor),
    stateOf(pressedColor),
    content
)

fun WidgetContainerScope.SwitchButton(
    switchState: State<Boolean> = stateOf(false),
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: ButtonScope.() -> Unit = {
        press { switchState.toggle() }
        val text = stateOf(Literal(switchState.getValue().toString()).style { color(switchState.getValue().pick(Colors.GREEN, Colors.RED)) })
        switchState.subscribe {
            text.setValue(Literal(switchState.getValue().toString()).style { color(switchState.getValue().pick(Colors.GREEN, Colors.RED)) })
        }
        TextField(text)
    }
) = Button(modifier, horizontalArrangement, verticalAlignment, content = content)

/**
 * 创建一个锁定按钮组件
 *
 * @param lockState 一个包含锁定状态的 [State] 对象，默认为未锁定状态
 * @param modifier 一个用于修改此组件外观和行为的 [Modifier] 对象
 * @param horizontalArrangement 水平排列方式，默认为 [Arrangement.Center]
 * @param verticalAlignment 垂直对齐方式，默认为 [Alignment.CenterVertically]
 * @param content 按钮内容的 Lambda 表达式，默认为空
 */
fun WidgetContainerScope.LockButton(
    lockState: State<Boolean> = stateOf(false),
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: ButtonScope.() -> Unit = {}
) = addWidgetChild(IGButtonWidget(horizontalArrangement, verticalAlignment)) {
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
    val scope = ButtonScope { this }
    scope.content()
    val lock = scope.Icon(list.maxBy { it.width + it.height }) {
        changedRemeasure = false
    }
    Modifier
        .render { context, _, _, _ ->
            this as IGButtonWidget
            lock.iconTexture = theme(lockState.getValue().pick(PressableTheme.LOCK, PressableTheme.UNLOCK))
        }
        .padding(2f)
        .then(modifier).foldInApply()
    press { lockState.setValue(!lockState.getValue()) }

}