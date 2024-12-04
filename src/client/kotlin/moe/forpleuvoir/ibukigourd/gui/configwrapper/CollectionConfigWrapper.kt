package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringList

fun WidgetContainerScope.StringListConfigWrapper(
    config: ConfigStringList,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val boolValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            Modifier.width(40f)
        ) {


        }
        ConfigResetButton(config) {
            boolValue.setValue(config.getValue())
        }
    }
}
