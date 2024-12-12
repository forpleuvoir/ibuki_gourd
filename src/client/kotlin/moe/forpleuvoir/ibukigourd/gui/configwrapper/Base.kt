package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.SearchBar
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.ConfigSerializable

fun WidgetContainerScope.ConfigsWrapper(
    configs: Iterable<ConfigSerializable>,
    modifier: Modifier = Modifier,
    listModifier: ColumnScope.() -> Modifier = { Modifier.weight(1).fill() },
    scrollerModifier: ColumnScope.() -> Modifier = { Modifier },
) = RowListWrapped(
    modifier = modifier,
    listModifier = listModifier,
    scrollerModifier = scrollerModifier,
    spacing = 4f
) {
    //TODO i18n
    //TODO 很神秘的bug 如果列表为空会导致整个screen都无法正常测量和布局
    if (configs.count() == 0) TextLabel("啥也没有")
    configs.forEach { config ->
        ConfigWrapperMap.wrapper(config, this, Modifier.fill())
    }
}

inline fun <reified T : ConfigSerializable> WidgetContainerScope.ConfigColumnWrapper(
    configSerializable: T,
    modifier: Modifier = Modifier,
    crossinline content: ColumnScope.() -> Unit
) = Column(
    modifier
        .attachLeft {
            //TODO 配置化
            var alpha = 0f
            val maxAlpha = 0.25f
            // alpha per tick
            val aupt = maxAlpha * 0.15f
            val adpt = maxAlpha * 0.25f
            val color = Colors.CYAN.alpha(alpha)
            fun updateAlpha(wasMouseOver: Boolean, delta: Float) {
                alpha = if (wasMouseOver)
                    (alpha + aupt * delta).coerceIn(0f, maxAlpha)
                else (alpha - adpt * delta).coerceIn(0f, maxAlpha)
            }
            name(T::class.simpleName!! + "Wrapper")
                .padding(horizontal = 2f)
                .renderBackground { context, x, y, delta ->
                    updateAlpha(wasMouseOver, delta)
                    context.batchRenderBox {
                        pushRoundBox(transform, color.alpha(alpha), 2)
                    }
                }
        },
    horizontalArrangement = Arrangement.SpaceBetween
) {
    ConfigTextLabel(configSerializable)
    content()
}


fun <T : Resettable> WidgetContainerScope.ConfigResetButton(
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
        TextLabel(Translatable("ibukigourd.gui.button.rest"))
    }
}

fun <T : ConfigSerializable> ColumnScope.ConfigTextLabel(
    config: T,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier
) = Column(
    modifier = modifier.attachLeft { weight(1) },
    horizontalArrangement = Arrangement.Left
) {
    TextLabel(config.translateText, textModifier) {
        HoverTip {
            TextLabel(config.comment)
        }
    }
}

fun WidgetContainerScope.ConfigFilter(
    collection: Collection<ConfigSerializable>
) = Column(

) {
    SearchBar(textConsumer = {

    })

}