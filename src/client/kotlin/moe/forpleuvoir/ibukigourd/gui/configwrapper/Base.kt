package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.active
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.Spinner
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.util.valueOf
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.container.ConfigContainer

fun WidgetContainerScope.ConfigContainerWrapper(
    configContainer: ConfigContainer,
    modifier: Modifier = Modifier,
    listModifier: ColumnScope.() -> Modifier = { Modifier.weight(1) },
    scrollerModifier: ColumnScope.() -> Modifier = { Modifier },
) = RowListWrapped(
    modifier = modifier,
    listModifier = listModifier,
    scrollerModifier = scrollerModifier,
    spacing = 4f
) {
    configContainer.configs().filterIsInstance<Config<*, *>>().forEach { config ->
        ConfigWrapperMap.wrapper(config, this, Modifier.fill())
    }
}


fun <V, T : Config<V, *>> WidgetContainerScope.ConfigResetButton(
    config: T,
    modifier: Modifier = Modifier,
    onRest: (T) -> Unit
): IGButtonWidget {
    val resettable = mutableStateOf(!config.isDefault())
    return Button(
        Modifier
            .active(resettable)
            .renderOverlay { context, f, f1, f2 ->
                resettable.setValue(!config.isDefault())
            }
            .then(modifier)
    ) {
        press {
            config.restDefault()
            resettable.setValue(!config.isDefault())
            onRest(config)
        }
        TextLabel(Translatable("ibukigourd.gui.button.rest"))
    }
}

fun <T : Config<*, *>> WidgetContainerScope.ConfigTextLabel(
    config: T,
    modifier: Modifier = Modifier
) = TextLabel(config.translateText, modifier) {
    HoverTip {
        TextLabel(config.comment)
    }
}

fun <E : Enum<E>> ColumnScope.EnumSelector(
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
        TextLabel(it, modifier = Modifier)
    },
    modifier = modifier,
)