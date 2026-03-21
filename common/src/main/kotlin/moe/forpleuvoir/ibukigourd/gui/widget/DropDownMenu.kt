package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.gui.widget.tip.PopupTip
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.text.translateComment
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.util.lateInitValueOf
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.primitive.either
import moe.forpleuvoir.nebula.event.Event
import kotlin.reflect.KClass

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

val DropDownMenuSeparatorColor = Color.ofRGB(0xCCCCCC)

fun ContainerScope.DropDownMenu(
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    screen: IGScreen = mc.screen as IGScreen,
    scope: DropDownMenuScope.() -> Unit
): IGButtonWidget = Button(
    modifier = Modifier
        .padding(horizontal = 5f, vertical = 4f)
        .render { guiGraphics, _, _, _ ->
            guiGraphics.pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_BACKGROUND)

        } then modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    val expandState = mutableStateOf(false)
    var dropDownContent: BoxScope.() -> Unit
    val dropDownMenuScope = DropDownMenuScope(this.owner(), expandState).apply(scope)
    dropDownContent = dropDownMenuScope.dropDownContent
    val icon = mutableStateOf(WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN)
    expandState.subscribe {
        icon.setValue(it.either(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN))
    }
    click {
        expandState.switch()
        PopupTip(
            optionalDirection = notifiableList(optionsDirection),
            screenModifier = Modifier
                .bgBlurRadius(0f)
                .onClose { expandState.setValue(false) },
            screen = screen
        ) {
            expandState.subscribe {
                if (!it) this.owner().screen()?.close()
            }
            dropDownContent(this)
        }.open()
    }

    Row(
        modifier = Modifier.width(13f).priority(1),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Rect(
            DropDownMenuSeparatorColor,
            Modifier
                .width(1f)
                .matchSibling()
                .margin(horizontal = 2.5f)
        )
        Icon(icon, modifier = Modifier.padding(vertical = 2.5f))
    }

}


val defaultSelectedColor = Colors.AQUA.opacity(.25f)

fun <T> ContainerScope.Selector(
    options: Iterable<T>,
    selected: MutableState<T> = mutableStateOf(options.first()),
    checker: (T, T) -> Boolean = { a, b -> a == b },
    onSelected: (T) -> Unit = {},
    selectedColor: ARGBColor = defaultSelectedColor,
    selectedWrapper: DropDownMenuScope.(T) -> GuiWidget,
    optionWrapper: ButtonScope.(T) -> GuiWidget,
    modifier: Modifier = Modifier,
    listWrapperModifier: BoxScope.() -> Modifier = { Modifier },
    listModifier: RowScope.() -> Modifier = { Modifier },
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    amountStep: Float? = null,
    scope: DropDownMenuScope.() -> Unit = {}
): IGButtonWidget {
    check(options.any { checker(it, selected.getValue()) }) { "initialOption must be in options" }
    return DropDownMenu(modifier, optionsDirection) {
        val proxy: MutableState<DropDownMenuScope.() -> GuiWidget> = mutableStateOf {
            selectedWrapper.invoke(this, selected.getValue())
        }
        Proxy(proxy)
        selected.subscribe {
            proxy.setValue {
                selectedWrapper.invoke(this, selected.getValue())
            }
            onSelected(it)
        }
        DropDownContent {
            ColumnListWrapped(
                modifier = Modifier.padding(0f).disableRenderBackground().then(listWrapperModifier()),
                horizontalAlignment = Alignment.Left,
                listModifier = listModifier
            ) {
                amountStep?.let { amountStep(it) }
                options.forEachIndexed { index, option ->
                    if (index != 0) {
                        Rect(
                            DropDownMenuSeparatorColor,
                            Modifier.height(1f).matchSibling()
                        )
                    }
                    FlatButton(
                        hoveredColor = selectedColor,
                        horizontalArrangement = Arrangement.Left,
                    ) {
                        optionWrapper(option)
                        click {
                            if (checker(option, selected.getValue())) onSelected(option)
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

/**
 * 用于创建一个带有搜索功能的选择器组件。
 *
 * @param options 选项的集合。
 * @param predicate 一个函数，用于根据输入字符串筛选选项。返回值为布尔类型，表示是否匹配。
 * @param selected 当前选中的选项，作为一个可变状态对象。
 * @param checker 一个函数，用于比较两个选项是否相等，默认值是直接比较它们的相等性。
 * @param onSelected 当选中的选项更改时调用的回调函数。
 * @param searchBarHideLimit 搜索栏显示的最小选项数量阈值，默认值为5。
 * @param selectedColor 用于表示选中项时的颜色。
 * @param selectedWrapper 一个函数，用于包装选中项的界面。
 * @param optionWrapper 一个函数，用于包装每个选项的界面。
 * @param modifier 修饰器，用于定制组件的样式。
 * @param searchBarModifier 修饰搜索栏的外观样式函数，带默认实现。
 * @param listWrapperModifier 修饰选项列表包装的外观样式函数，带默认实现。
 * @param listModifier 修饰选项列表的外观样式函数，带默认实现。
 * @param optionsDirection 指定选项显示方向的列表。
 * @param scope 在下拉菜单组件中的作用域配置。
 * @return 返回一个用于显示下拉选择菜单的按钮组件。
 */
fun <T> ContainerScope.SelectorWithSearcher(
    options: Iterable<T>,
    predicate: (T, String) -> Boolean,
    selected: MutableState<T> = mutableStateOf(options.first()),
    checker: (T, T) -> Boolean = { a, b -> a == b },
    onSelected: (T) -> Unit = {},
    searchBarHideLimit: Int = 5,
    selectedColor: ARGBColor = defaultSelectedColor,
    selectedWrapper: DropDownMenuScope.(T) -> GuiWidget,
    optionWrapper: ButtonScope.(T) -> GuiWidget,
    modifier: Modifier = Modifier,
    searchBarModifier: ColumnScope.() -> Modifier = { Modifier },
    listWrapperModifier: ColumnScope.() -> Modifier = { Modifier },
    listModifier: RowScope.() -> Modifier = { Modifier },
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    amountStep: Float? = null,
    scope: DropDownMenuScope.() -> Unit = {}
): IGButtonWidget {
    check(options.any { checker(it, selected.getValue()) }) { "initialOption must be in options" }
    return DropDownMenu(modifier, optionsDirection) {
        val proxy: MutableState<DropDownMenuScope.() -> GuiWidget> = mutableStateOf {
            selectedWrapper.invoke(this, selected.getValue())
        }
        Proxy(proxy)
        selected.subscribe {
            proxy.setValue {
                selectedWrapper.invoke(this, selected.getValue())
            }
            onSelected(it)
        }

        DropDownContent {
            Column(
                horizontalAlignment = Alignment.Left,
            ) {
                val showList = options.toMutableList()
                var listRecompose by lateInitValueOf<() -> Unit>()
                if (options.count() > searchBarHideLimit)
                    SearchBar(
                        textConsumer = { str ->
                            showList.clear()
                            showList.addAll(options.filter { predicate(it, str) })
                            listRecompose()
                        },
                        hintText = stateOf(IGLang.search.plainText),
                        modifier = searchBarModifier(),
                        textEditorModifier = { Modifier.weight(1) }
                    )
                ColumnListWrapped(
                    modifier = listWrapperModifier().attachLeft { padding(0f).disableRenderBackground() },
                    listModifier = listModifier,
                    horizontalAlignment = Alignment.Left,
                    onCreate = {
                        listRecompose = { this.executeRecompose() }
                    }
                ) {
                    amountStep?.let { amountStep(it) }
                    if (showList.isEmpty()) Text(IGLang.hasNothing)
                    showList.forEachIndexed { index, option ->
                        if (index != 0) {
                            Rect(
                                DropDownMenuSeparatorColor,
                                Modifier.height(1f).matchSibling()
                            )
                        }
                        FlatButton(
                            hoveredColor = selectedColor,
                            horizontalArrangement = Arrangement.Left,
                        ) {
                            optionWrapper(option)
                            click {
                                if (checker(option, selected.getValue())) onSelected(option)
                                selected.setValue(option)
                                this@DropDownMenu.toggle()
                            }
                        }
                    }
                }
            }
        }
        scope()
    }
}


fun ContainerScope.Selector(
    options: Iterable<String>,
    selected: MutableState<String> = mutableStateOf(options.first()),
    onSelected: (String) -> Unit = {},
    selectedColor: ARGBColor = defaultSelectedColor,
    modifier: Modifier = Modifier,
    listWrapperModifier: BoxScope.() -> Modifier = { Modifier },
    listModifier: RowScope.() -> Modifier = { Modifier },
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    amountStep: Float? = null,
    scope: DropDownMenuScope.() -> Unit = {}
) = Selector(
    options,
    selected,
    onSelected = onSelected,
    selectedColor = selectedColor,
    selectedWrapper = { Text(it) },
    optionWrapper = { Text(it, modifier = Modifier.width(options.maxWidth)) },
    modifier = modifier,
    listWrapperModifier = listWrapperModifier,
    listModifier = listModifier,
    optionsDirection = optionsDirection,
    amountStep = amountStep,
    scope = scope
)

fun <E : Enum<E>> ContainerScope.EnumSelector(
    selected: MutableState<E>,
    options: Iterable<E> = selected.getValue()::class.java.enumConstants.toList(),
    onSelected: (E) -> Unit = {},
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    amountStep: Float? = null
) = Selector(
    options = options,
    selected = selected,
    onSelected = onSelected,
    selectedWrapper = {
        Text(it.translateText, modifier = Modifier.weight(1).hoverText(it.translateComment, optionalDirection = Direction.clockwiseFromTop))
    },
    optionWrapper = {
        Text(
            it.translateText,
            modifier = Modifier
                .width(options.map { it.translateText }.maxWidth.coerceAtLeast(30f))
                .hoverText(it.translateComment, optionalDirection = Direction.leftRightTopBottom)
        )
    },
    modifier = modifier,
    optionsDirection = optionsDirection,
    amountStep = amountStep
)

fun ContainerScope.EventSelector(
    options: Iterable<KClass<out Event>>,
    selected: MutableState<KClass<out Event>> = mutableStateOf(options.first()),
    onSelected: (KClass<out Event>) -> Unit = {},
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    amountStep: Float? = null
) = Selector(
    options = options,
    selected = selected,
    onSelected = onSelected,
    selectedWrapper = {
        Text(it.translateText, modifier = Modifier.weight(1).hoverText(it.translateComment, optionalDirection = Direction.clockwiseFromTop))
    },
    optionWrapper = {
        Text(
            it.translateText,
            modifier = Modifier
                .width(options.map { it.translateText }.maxWidth.coerceAtLeast(30f))
                .hoverText(it.translateComment, optionalDirection = Direction.leftRightTopBottom)
        )
    },
    modifier = modifier,
    optionsDirection = optionsDirection,
    amountStep = amountStep
)
