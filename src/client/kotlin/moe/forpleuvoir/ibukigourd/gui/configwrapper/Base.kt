package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.modifier.bgHoverHighlightBox
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.config.ConfigSerializable

const val CONFIG_WRAPPER_TIP = "#config_wrapper_tip"

fun <T : ConfigSerializable> ContainerScope.ConfigRowWrapper(
    configSerializable: T,
    modifier: Modifier = Modifier,
    textWrapperModifier: RowScope.() -> Modifier = { Modifier },
    textModifier: RowScope.() -> Modifier = { Modifier },
    content: RowScope.() -> Unit
) = Row(
    modifier
        .attachLeft {
            name(configSerializable.javaClass.simpleName + "Wrapper")
                .padding(horizontal = 2f)
                .bgHoverHighlightBox()
        },
    horizontalArrangement = Arrangement.SpaceBetween
) {
    ConfigTextLabel(configSerializable, textWrapperModifier(), textModifier)
    content()
}


fun <T : Resettable> ContainerScope.ConfigResetButton(
    config: T,
    modifier: Modifier = Modifier,
    onRest: (T) -> Unit = {}
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
        click {
            config.restDefault()
            resettable.setValue(!config.isDefault())
            onRest(config)
        }
        TextLabel(IGLang.reset)
    }
}

fun <T : ConfigSerializable> RowScope.ConfigTextLabel(
    config: T,
    modifier: Modifier = Modifier,
    textModifier: RowScope.() -> Modifier = { Modifier }
) = Row(
    modifier = modifier.attachLeft { weight(1) },
    horizontalArrangement = Arrangement.Left
) {
    TextLabel(config.translateText, textModifier().attachLeft { hoverText(config.comment) })
}
