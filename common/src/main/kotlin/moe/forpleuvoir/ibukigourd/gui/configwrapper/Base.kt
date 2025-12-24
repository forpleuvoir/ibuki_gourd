package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.modifier.bgHoverHighlightBox
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.config.ConfigSerializable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

var CONFIG_WRAPPER_TIP: Tip? = null

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
    ConfigName(configSerializable, textWrapperModifier(), textModifier)
    content()
}


fun <T : Resettable> ContainerScope.ConfigResetButton(
    config: T,
    modifier: Modifier = Modifier,
    onRest: (T) -> Unit = {}
): IGButtonWidget {
    val resettable = mutableStateOf(!config.isDefault())
    var timeMark = TimeSource.Monotonic.markNow()
    return Button(
        Modifier
            .active(resettable)
            .renderOverlay { _, _, _, _ ->
                if (timeMark.elapsedNow() > 100.milliseconds) {
                    timeMark = TimeSource.Monotonic.markNow()
                    resettable.setValue(!config.isDefault())
                }
            }
            .then(modifier)
    ) {
        click {
            config.restDefault()
            resettable.setValue(!config.isDefault())
            onRest(config)
        }
        Text(IGLang.reset)
    }
}

fun <T : ConfigSerializable> RowScope.ConfigName(
    config: T,
    modifier: Modifier = Modifier,
    textModifier: RowScope.() -> Modifier = { Modifier }
) = Row(
    modifier = modifier.attachLeft { weight(1) },
    horizontalArrangement = Arrangement.Left
) {
    Text(config.translateText, textModifier().attachLeft { hoverText(config.comment) })
}
