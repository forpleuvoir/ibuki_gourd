package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.screen.execute
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.PopupTip
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.translateComment
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.primitive.pick
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

val DropDownMenuSeparatorColor = Color(0xFFCCCCCC)

fun WidgetContainerScope.DropDownMenu(
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    screen: IGScreen = mc.currentScreen as IGScreen,
    scope: DropDownMenuScope.() -> Unit
): IGButtonWidget = Button(
    modifier = Modifier
        .padding(horizontal = 5f, vertical = 4f)
        .render { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_BACKGROUND)
            }
        } then modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    val expandState = mutableStateOf(false)
    var dropDownContent: BoxScope.() -> Unit
    val dropDownMenuScope = DropDownMenuScope(this.owner(), expandState).apply(scope)
    dropDownContent = dropDownMenuScope.dropDownContent
    val icon = mutableStateOf(WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN)
    expandState.subscribe {
        icon.setValue(it.pick(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN))
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

    Column(
        modifier = Modifier.width(13f),
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

fun <T> WidgetContainerScope.Selector(
    options: Iterable<T>,
    selected: MutableState<T> = mutableStateOf(options.first()),
    onChange: (T) -> Unit = {},
    selectedColor: ARGBColor = defaultSelectedColor,
    selectedWrapper: DropDownMenuScope.(T) -> IGWidget,
    optionWrapper: ButtonScope.(T) -> IGWidget,
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    scope: DropDownMenuScope.() -> Unit = {}
): IGButtonWidget {
    check(selected.getValue() in options) { "initialOption must be in options" }
    selected.subscribe {
        onChange(it)
    }
    return DropDownMenu(modifier, optionsDirection) {
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
                        Rect(
                            DropDownMenuSeparatorColor,
                            Modifier.height(.5f).matchSibling()
                        )
                    }
                    FlatButton(
                        hoveredColor = selectedColor,
                        horizontalArrangement = Arrangement.Left,
                    ) {
                        optionWrapper(option)
                        click {
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

fun <T> WidgetContainerScope.SelectorWithSearcher(
    options: Iterable<T>,
    predicate: (T, String) -> Boolean,
    selected: MutableState<T> = mutableStateOf(options.first()),
    onChange: (T) -> Unit = {},
    selectedColor: ARGBColor = defaultSelectedColor,
    selectedWrapper: DropDownMenuScope.(T) -> IGWidget,
    optionWrapper: ButtonScope.(T) -> IGWidget,
    modifier: Modifier = Modifier,
    searchBarModifier: RowScope.() -> Modifier = { Modifier },
    listModifier: RowScope.() -> Modifier = { Modifier },
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    scope: DropDownMenuScope.() -> Unit = {}
): IGButtonWidget {
    check(selected.getValue() in options) { "initialOption must be in options" }
    selected.subscribe {
        onChange(it)
    }
    return DropDownMenu(modifier, optionsDirection) {
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
            Row(
                horizontalAlignment = Alignment.Left,
            ) {
                val showList = notifiableList(options.toList())
                SearchBar(
                    textConsumer = { str ->
                        showList.disableNotify {
                            showList.clear()
                            showList.addAll(options.toList().filter { predicate(it, str) })
                        }
                        showList.onChange(showList)
                    },
                    hintText = stateOf(IGLang.search.plainText),
                    modifier = searchBarModifier(),
                    textEditorModifier = { Modifier.weight(1) }
                )
                RowListWrapped(
                    modifier = listModifier().attachLeft { padding(0f).disableRenderBackground() },
                    horizontalAlignment = Alignment.Left
                ) {
                    if (showList.isEmpty()) TextLabel(IGLang.hasNothing)
                    showList.forEachIndexed { index, option ->
                        if (index != 0) {
                            Rect(
                                DropDownMenuSeparatorColor,
                                Modifier.height(.5f).matchSibling()
                            )
                        }
                        FlatButton(
                            hoveredColor = selectedColor,
                            horizontalArrangement = Arrangement.Left,
                        ) {
                            optionWrapper(option)
                            click {
                                selected.setValue(option)
                                this@DropDownMenu.toggle()
                            }
                        }
                    }
                }.apply {
                    showList.subscribe {
                        execute { this.recompose() }
                    }
                }
            }
        }
        scope()
    }
}


fun WidgetContainerScope.Selector(
    options: Iterable<String>,
    selected: MutableState<String> = mutableStateOf(options.first()),
    onChange: (String) -> Unit = {},
    selectedColor: ARGBColor = defaultSelectedColor,
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
    scope: DropDownMenuScope.() -> Unit = {}
) = Selector(
    options,
    selected,
    onChange,
    selectedColor,
    selectedWrapper = { TextLabel(it) },
    optionWrapper = { TextLabel(it, modifier = Modifier.width(options.maxWidth(textRenderer).toFloat())) },
    modifier,
    optionsDirection,
    scope
)

fun <E : Enum<E>> WidgetContainerScope.EnumSelector(
    selected: MutableState<E>,
    options: Iterable<E> = selected.getValue()::class.java.enumConstants.toList(),
    onChange: (E) -> Unit = {},
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
) = Selector(
    options = options,
    selected = selected,
    onChange = {
        selected.setValue(it)
        onChange(it)
    },
    selectedWrapper = {
        TextLabel(it.translateText, modifier = Modifier.weight(1).hoverText(it.translateComment, optionalDirection = Direction.clockwiseFromTop))
    },
    optionWrapper = {
        TextLabel(
            it.translateText,
            modifier = Modifier
                .width(options.map { it.translateText }.maxWidth(textRenderer).toFloat().coerceAtLeast(30f))
                .hoverText(it.translateComment, optionalDirection = Direction.leftRightTopBottom)
        )
    },
    modifier = modifier,
    optionsDirection = optionsDirection
)

fun WidgetContainerScope.EventSelector(
    options: Iterable<KClass<out Event>>,
    selected: MutableState<KClass<out Event>> = mutableStateOf(options.first()),
    modifier: Modifier = Modifier,
    optionsDirection: List<Direction> = Direction.bottomTopRightLeft,
) = Selector(
    options = options,
    selected = selected,
    onChange = {
        selected.setValue(it)
    },
    selectedWrapper = {
        TextLabel(it.translateText, modifier = Modifier.weight(1).hoverText(it.translateComment, optionalDirection = Direction.clockwiseFromTop))
    },
    optionWrapper = {
        TextLabel(
            it.translateText,
            modifier = Modifier
                .width(options.map { it.translateText }.maxWidth(textRenderer).toFloat().coerceAtLeast(30f))
                .hoverText(it.translateComment, optionalDirection = Direction.leftRightTopBottom)
        )
    },
    modifier = modifier,
    optionsDirection = optionsDirection
)
