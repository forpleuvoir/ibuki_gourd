package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.descriptionText
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.active
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.tick
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowList
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.container.ConfigContainer

fun WidgetContainerScope.ConfigContainerWrapper(
    configContainer: ConfigContainer
) {
    RowList {
        configContainer.configs().filterIsInstance<Config<*, *>>().forEach { config ->
            ConfigWrapperMap.wrapper(config, this, Modifier.fill())
        }
    }
}

fun <T : ConfigBase<*, *>> WidgetContainerScope.ConfigResetButton(
    config: T,
    modifier: Modifier = Modifier,
    onRest: (T) -> Unit = {}
): IGButtonWidget {
    val resettable = mutableStateOf(!config.isDefault())
    return Button(
        Modifier
            .active(resettable)
            .tick {
                onTick()
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

fun <T : ConfigBase<*, *>> WidgetContainerScope.ConfigTextLabel(
    config: T,
    modifier: Modifier = Modifier
) = TextLabel(config.translateText, modifier) {
    HoverTip {
        TextLabel(config.descriptionText)
    }
}