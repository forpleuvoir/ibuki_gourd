package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.widget.wasMouseOver
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.gui.widget.toHSVColor
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.util.state.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick

fun WidgetContainerScope.Button(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    theme: PressableTheme = PressableTheme.Button2,
    color: ARGBColor = Colors.WHITE,
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget(horizontalArrangement, verticalAlignment)) {
    Modifier.padding(horizontal = 5, vertical = 5)
        .name("Button")
        .mouseOverCursor(MouseCursor.POINTING_HAND_CURSOR)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(theme), color)
            }
        }
        .then(modifier).foldInApply()
    ButtonScope { this }.Compose(content)
}

fun WidgetContainerScope.FlatButton(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    round: Int = 2,
    disabledColor: State<out ARGBColor> = stateOf(Color(0)),
    idleColor: State<out ARGBColor> = stateOf(Color(0)),
    hoveredColor: State<out ARGBColor> = stateOf(Color(0)),
    pressedColor: State<out ARGBColor> = stateOf(Color(0)),
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget(horizontalArrangement, verticalAlignment)) {
    Modifier.padding(round)
        .name("FlatButton")
        .mouseOverCursor(MouseCursor.POINTING_HAND_CURSOR)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            wasMouseOver {
                context.batchRenderBox {
                    pushRoundBox(
                        transform.asWorldCoordinateBox,
                        status(disabledColor.getValue(), idleColor.getValue(), hoveredColor.getValue(), pressedColor.getValue()),
                        round
                    )
                }
            }
        }
        .then(modifier).foldInApply()
    ButtonScope { this }.Compose(content)
}

fun WidgetContainerScope.FlatButton(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    round: Int = 2,
    disabledColor: ARGBColor = Color(0),
    idleColor: ARGBColor = Color(0),
    hoveredColor: ARGBColor = Color(0),
    pressedColor: ARGBColor = Color(0),
    content: ButtonScope.() -> Unit = { }
) = FlatButton(
    modifier,
    horizontalArrangement,
    verticalAlignment,
    round,
    stateOf(disabledColor),
    stateOf(idleColor),
    stateOf(hoveredColor),
    stateOf(pressedColor),
    content
)

fun WidgetContainerScope.SwitchButton(
    switchState: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    scope: ButtonScope.() -> Unit = {}
) = Button(
    Modifier
        .name("SwitchButton")
        .mouseOverCursor(MouseCursor.POINTING_HAND_CURSOR)
        .size(36f, 15f)
        .render { context, _, _, _ ->
            val b = transform.asWorldCoordinateBox
            val proportion = 0.55f
            val box = b.copy(switchState.getValue().pick(b.x + b.width * (1 - proportion), b.x), width = b.width * proportion)
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, WidgetTextures.SWITCH_BUTTON_BACKGROUND_BORDER)
                pushWidgetTexture(transform, WidgetTextures.SWITCH_BUTTON_BACKGROUND_CONTENT, switchState.getValue().pick(Color(0XFFA9E2A9), Color(0XFFDC9F9F)))
                pushWidgetTexture(box, WidgetTextures.SWITCH_BUTTON)
            }
        }.then(modifier),
    horizontalArrangement,
    verticalAlignment
) {
    click { switchState.switch() }
    Compose(scope)
}

/**
 * 创建一个锁定按钮组件
 *
 * @param lockState 一个包含锁定状态的 [MutableState] 对象，默认为未锁定状态
 * @param modifier 一个用于修改此组件外观和行为的 [Modifier] 对象
 * @param horizontalArrangement 水平排列方式，默认为 [Arrangement.Center]
 * @param verticalAlignment 垂直对齐方式，默认为 [Alignment.CenterVertically]
 * @param scope 按钮内容的 Lambda 表达式，默认为空
 */
fun WidgetContainerScope.LockButton(
    lockState: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    scope: ButtonScope.() -> Unit = {}
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
    val buttonScope = ButtonScope { this }
    val lock = buttonScope.Icon(list.maxBy { it.width + it.height }) {
        remeasureOnChange = false
    }
    Modifier
        .name("LockButton")
        .mouseOverCursor(MouseCursor.POINTING_HAND_CURSOR)
        .render { context, _, _, _ ->
            this as IGButtonWidget
            lock.iconTexture = theme(lockState.getValue().pick(PressableTheme.LOCK, PressableTheme.UNLOCK))
        }
        .padding(2f)
        .then(modifier).foldInApply()
    this.click { lockState.switch() }
    buttonScope.Compose(scope)
}

fun WidgetContainerScope.ColorButton(
    color: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: ButtonScope.() -> Unit = {}
) = Button(
    modifier = Modifier
        .name("ColorButton")
        .render { context, f, f1, f2 ->
            this as IGButtonWidget
            val hsvColor = color.getValue().toHSVColor()

            val trimEdgesBox = transform.asWorldCoordinateBox.trimEdges(2f)

            context.useScissor(trimEdgesBox) {
                batchRenderTextureColored {
                    pushTileTexture(trimEdgesBox, WidgetTextures.ALPHA)
                }
                renderBox(trimEdgesBox, pressed.pick(hsvColor.reverse(), hsvColor))
            }
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(PressableTheme.ColorButton), hsvColor.clone().alpha(1f).saturation(hsvColor.saturation * 0.2f))
                if (wasMouseOver) pushWidgetTexture(transform, WidgetTextures.COLOR_BUTTON_HOVERED_OUTLINE, if (pressed) hsvColor else hsvColor.reverse())
            }

        }.then(modifier),
    content = scope
)

/**
 * 创建一组单选按钮的函数。
 *
 * @param options 选项集合，用于定义按钮的值和显示内容。
 * @param selected 当前选中的选项，使用可变状态保存和监听选中值的变化，默认为空。
 * @param onChange 回调函数，当选中状态更改时调用，并传递选中的选项值。
 * @param optionWrapper 按钮内容的包装方式，用于自定义每个选项按钮的显示内容。
 * @param modifier 每个按钮的修饰符，用于定义样式和布局的变化。
 */
fun <S : WidgetContainerScope, T> S.RadioButtons(
    options: Iterable<T>,
    selected: MutableState<T?> = mutableStateOf(null),
    onChange: (T) -> Unit = {},
    optionWrapper: ButtonScope.(T) -> Unit,
    modifier: S.(T) -> Modifier = { Modifier },
) {
    val buttons = mutableListOf<ButtonScope>()
    options.forEach { option ->
        Button(modifier = modifier.invoke(this, option)) {
            optionWrapper(option)
            if (selected.getValue() == option) owner().active = false
            buttons.add(this)
            click {
                selected.setValue(option)
                onChange(option)
                buttons.forEach {
                    if (!it.owner().active) it.owner().active = true
                }
                this.owner().active = false
            }
        }
    }
}