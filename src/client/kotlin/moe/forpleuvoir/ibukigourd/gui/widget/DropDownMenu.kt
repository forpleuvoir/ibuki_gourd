package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.PopupTip
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.common.util.valueOf

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
            optionalDirection = notifiableList(Direction.Bottom, Direction.Top, Direction.Right, Direction.Left),
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
        ColoredBox(
            DropDownMenuSeparatorColor,
            Modifier
                .width(1f)
                .matchSibling()
                .margin(horizontal = 2.5f)
        )
        Icon(icon, modifier = Modifier.padding(vertical = 2.5f))
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
    optionWrapper = { TextLabel(it, modifier = Modifier.width(options.maxWidth(textRenderer).toFloat())) },
    modifier,
    scope
)

inline fun <reified E : Enum<E>> ColumnScope.EnumSelector(
    selected: MutableState<E>,
    noinline onChange: (E) -> Unit = { },
    modifier: Modifier = Modifier
) = Spinner(
    options = E::class.java.enumConstants.toList(),
    selected = selected,
    onChange = onChange,
    selectedWrapper = {
        TextLabel(it.name, modifier = Modifier.weight(1))
    },
    optionWrapper = {
        TextLabel(
            it.name,
            modifier = Modifier
                .width(E::class.java.enumConstants.map { it.name }.maxWidth(textRenderer).toFloat())
        )
    },
    modifier = modifier,
)

fun <E : Enum<E>> ColumnScope.NoInlineEnumSelector(
    selected: MutableState<String>,
    enumValue: MutableState<E>,
    modifier: Modifier = Modifier
) = Spinner(
    options = enumValue.getValue()::class.java.enumConstants.map { it.name },
    selected = selected,
    onChange = {
        Enum.valueOf(enumValue.getValue()::class, it)?.let { it1 -> enumValue.setValue(it1) }
    },
    selectedWrapper = {
        TextLabel(it, modifier = Modifier.weight(1))
    },
    optionWrapper = {
        TextLabel(it, modifier = Modifier.width(enumValue.getValue()::class.java.enumConstants.map { it.name }.maxWidth(textRenderer).toFloat()))
    },
    modifier = modifier,
)