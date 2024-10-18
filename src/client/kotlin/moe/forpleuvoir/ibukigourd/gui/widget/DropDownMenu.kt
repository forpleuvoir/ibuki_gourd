package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.TipContainer
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick

class DropDownMenuScope(private val owner: IGButtonWidget, private val state: MutableState<Boolean>) : ButtonScope {

    override fun owner(): IGButtonWidget = owner

    internal var dropDownContent: BoxScope.() -> Unit = {}

    fun DropDownContent(dropDownContent: BoxScope.() -> Unit) {
        this.dropDownContent = dropDownContent
    }

    fun toggle() {
        state.setValue(!state.getValue())
    }

}

val DropDownMenuSeparatorColor = Color(0xFFCCCCCC)

fun WidgetContainerScope.DropDownMenu(
    modifier: Modifier = Modifier,
    screen: IGScreen = mc.currentScreen as IGScreen,
    scope: DropDownMenuScope.() -> Unit
): IGButtonWidget {
    val expandState = mutableStateOf(false)
    //上面的空余空间,下面的空余空间
    var space = 0f to 0f
    //最大空间的位置 false :up true: down
    var maxSpaceDir = false
    var parentBox = Box.Unspecified
    var onPlaced = {}
    var place = {}
    var placedPosition = Vector2f()
    var playSound: () -> Unit
    var dropDownContent: BoxScope.() -> Unit
    return Button(
        modifier = Modifier
            .padding(horizontal = 5f, vertical = 4f)
            .placeCompletion {
                maxSpaceDir = transform.worldCenter.y() - (mc.window.scaledHeight / 2f) < 0f
                space = transform.worldTop to mc.window.scaledHeight.toFloat() - transform.worldBottom
                parentBox = transform.asWorldBox
                onPlaced()
                place()
            }
            .render { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_BACKGROUND)
                }
            } then modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val dropDownMenuScope = DropDownMenuScope(this.owner(), expandState).apply(scope)
        dropDownContent = dropDownMenuScope.dropDownContent
        val icon = mutableStateOf(WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN)
        playSound = { owner().playClickSound(soundManager) }
        expandState.subscribe {
            icon.setValue(it.pick(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN))
        }

        press {
            expandState.switch()
        }

        Column(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ColoredBox(
                DropDownMenuSeparatorColor,
                Modifier
                    .width(1f)
                    .matchSibling()
                    .margin(horizontal = 2.5f)
            )
            Icon(icon, modifier = Modifier.padding(vertical = 2.5f))
        }

        screen.scope.TipContainer {
            Box(
                Modifier
                    .tipParent(this@Button.owner())
                    .active(expandState)
                    .visible(expandState)
                    .padding(3f)
                    .mousePress {
                        onMousePress(it)
                        it.tryUse(!wasMouseOver).onSuccess {
                            expandState.setValue(false)
                            playSound()
                        }
                        it.tryUse()
                    }
                    .placeCompletion {
                        transform.worldX = placedPosition.x
                        transform.worldY = placedPosition.y
                    }
                    .renderBackground { context, _, _, _ ->
                        context.batchRenderTextureColored {
                            pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_EXPEND_BACKGROUND)
                        }
                    }
            ) {
                //当顶层组件被放置时调用,用于测量展开部分的尺寸,并且计算放置位置
                onPlaced = place@{
                    //------------ 计算可放置的Y位置 ------------\\
                    //尝试放在下面
                    val (topSpace, bottomSpace) = space
                    //对比原来的大小是否能放下
                    val greaterThanBottomSpace = owner().transform.height > bottomSpace
                    if (!greaterThanBottomSpace || maxSpaceDir) { //放不下,检查最大空间的位置
                        //在下面时,强制放在下面
                        owner().apply {
                            measure(Constraints.of(maxHeight = bottomSpace))
                            measureCompletion()
                            //放置于父组件下面
                            //先尝试放置于父组件中心
                            var x = parentBox.center.x() - owner().transform.halfWidth
                            //将位置限制在可防止范围内
                            x = x.coerceIn(0f..(mc.window.scaledWidth.toFloat() - owner().transform.width).coerceAtLeast(0f))

                            placedPosition = Vector2f(x, parentBox.bottom)
                        }
                        return@place
                    } else {
                        //在上面时,强制放在上面
                        owner().apply {
                            measure(Constraints.of(maxHeight = topSpace))
                            measureCompletion()
                            //放置于父组件上面
                            //先尝试放置于父组件中心
                            var x = parentBox.center.x() - owner().transform.halfWidth
                            //将位置限制在可防止范围内
                            x = x.coerceIn(0f..(mc.window.scaledWidth.toFloat() - owner().transform.width).coerceAtLeast(0f))

                            placedPosition = Vector2f(x, parentBox.top - owner().transform.height)
                        }
                    }
                }
                place = {
                    owner().transform.worldX = placedPosition.x
                    owner().transform.worldY = placedPosition.y
                    owner().layout()
                }
                dropDownContent(this)
            }
        }
    }
}

fun <T> WidgetContainerScope.Spinner(
    options: Iterable<T>,
    selected: MutableState<T> = mutableStateOf(options.first()),
    onChange: (T) -> Unit = {},
    selectedColor: ARGBColor = Colors.BANANA_YELLOW.opacity(.35f),
    selectedWrapper: DropDownMenuScope.(T) -> IGWidget,
    optionWrapper: ButtonScope.(T) -> IGWidget,
    modifier: Modifier = Modifier,
    scope: DropDownMenuScope.() -> Unit = {}
): IGButtonWidget {
    check(selected.getValue() in options) { "initialOption must be in options" }
    selected.subscribe {
        onChange(it)
    }
    return DropDownMenu(modifier) {
        val proxy: MutableState<DropDownMenuScope.() -> IGWidget> = mutableStateOf {
            selectedWrapper.invoke(this, selected.getValue())
        }
        Proxy(proxy)
        selected.subscribe {
            proxy.setValue {
                selectedWrapper.invoke(this, selected.getValue())
            }
        }
        DropDownContent {
            RowListWrapped(
                modifier = Modifier.padding(0f).disableRenderBackground(),
                horizontalAlignment = Alignment.Left
            ) {
                options.forEachIndexed { index, option ->
                    if (index != 0) {
                        ColoredBox(
                            DropDownMenuSeparatorColor,
                            Modifier.height(1f).matchSibling()
                        )
                    }
                    FlatButton(
                        hoveredColor = selectedColor,
                        horizontalArrangement = Arrangement.Left,
                    ) {
                        optionWrapper(option)
                        press {
                            selected.setValue(option)
                            this@DropDownMenu.toggle()
                        }
                    }
                }
            }
        }
        scope()
    }
}


fun WidgetContainerScope.Spinner(
    options: Iterable<String>,
    selected: MutableState<String> = mutableStateOf(options.first()),
    onChange: (String) -> Unit = {},
    selectedColor: ARGBColor = Colors.BANANA_YELLOW.opacity(.35f),
    modifier: Modifier = Modifier,
    scope: DropDownMenuScope.() -> Unit = {}
) = Spinner(
    options,
    selected,
    onChange,
    selectedColor,
    selectedWrapper = { TextLabel(it) },
    optionWrapper = { TextLabel(it) },
    modifier,
    scope
)