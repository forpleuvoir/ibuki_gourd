package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.screen.execute
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.modifier.bgHoverHighlightBox
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRender
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.SearchBar
import moe.forpleuvoir.ibukigourd.gui.widget.SimpleDialog
import moe.forpleuvoir.ibukigourd.gui.widget.SwitchableProxy
import moe.forpleuvoir.ibukigourd.gui.widget.Widget
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.autoExpandConfigContainer
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.autoExpandConfigContainerLimit
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.configContainerWrapperGuidelinesColor
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.showFirstConfigInContainer
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.manager.ConfigManager

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
    if (configs.count() == 0) TextLabel(IGLang.hasNothing)
    configs.forEach { config ->
        ConfigWrapperMap.wrapper(config, this, Modifier.fill().unlockConstraint())
    }
}

fun WidgetContainerScope.ConfigContainerWrapper(
    config: ConfigContainer,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            modifier = Modifier.width(80f)
        ) {
            TextLabel(IGLang.setting)
            click {
                SimpleDialog(
                    title = stateOf(config.translateText)
                ) {
                    ConfigsWrapper(
                        config.configs(),
                        modifier = Modifier
                            .maxWidth(400f)
                            .maxHeight(260f)
                            .disableRenderBackground()
                            .padding(0),
                        listModifier = {
                            Modifier.weight(1)
                        }
                    )
                }.open()
            }
        }
        ConfigResetButton(config)
    }
}

fun WidgetContainerScope.ExpandableConfigContainerWrapper(
    config: ConfigContainer,
    modifier: Modifier = Modifier
) = Row(modifier) {
    val expanded = mutableStateOf(autoExpandConfigContainer && config.configs().size <= autoExpandConfigContainerLimit)

    var firstConfig = config.configs().find { showFirstConfigInContainer && it !is ConfigContainer }
    if (config.configs().count { it !is ConfigContainer } <= 1) firstConfig = null

    Button(
        modifier = Modifier.disableRender().padding(0)
    ) {
        click { expanded.switch() }
        Column(
            Modifier
                .weight(1)
                .attachLeft {
                    name(config.javaClass.simpleName + "Wrapper")
                        .padding(horizontal = 2f, vertical = if (firstConfig == null) 5 else 0)
                        .bgHoverHighlightBox()
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextLabel(config.translateText, Modifier.hoverText(config.comment))
            if (firstConfig != null) {
                TextLabel("·", modifier = Modifier.margin(horizontal = 1f))
            }
            Column {
                firstConfig?.let { firstConfig ->
                    ConfigWrapperMap.wrapper(firstConfig, this, Modifier.weight(1).disableRenderBackground())
                }
                Icon(
                    mutableStateOf(expanded) { it.pick(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN) },
                    modifier = Modifier.margin(right = 5f, left = 2f)
                )
            }
        }
    }
    SwitchableProxy(
        widgetA = {
            Column {
                Widget(
                    Modifier.width(1.5f)
                        .matchSibling()
                        .padding(0, 0, 2, 2)
                        .margin(4, 0.5, 0, 0)
                        .render { context, _, _, _ ->
                            context.renderBox(
                                transform.asWorldCoordinateBox.trimEdges(padding.top, padding.bottom, padding.left, padding.right),
                                configContainerWrapperGuidelinesColor
                            )
                        }
                )
                Row(
                    modifier = Modifier.padding(2, 8, 2, 2),
                    verticalArrangement = Arrangement.spacedBy(4f)
                ) {
                    if (config.configs().isEmpty()) TextLabel(IGLang.hasNothing)
                    config.configs().forEach { config ->
                        if (config != firstConfig)
                            ConfigWrapperMap.wrapper(config, this, Modifier.fill())
                    }
                }
            }

        },
        widgetB = {
            Widget(Modifier) {}
        },
        expanded
    )
}


fun WidgetContainerScope.ConfigManagerWrapper(
    configManager: ConfigManager,
    modifier: Modifier = Modifier,
) = Column(
    modifier,
    horizontalArrangement = Arrangement.spacedBy(5f)
) {
    val map = buildList {
        (configManager.configs().filterIsInstance<Config<*, *>>() as Collection<ConfigSerializable>).let {
            if (it.isNotEmpty()) add(configManager as ConfigSerializable to it)
        }
        addAll(configManager.configs().filterIsInstance<ConfigContainer>().map { it to it.configs() })
    }
    var (currentGroup, currentConfigs) = if (map.isEmpty()) {
        Literal("empty") to notifiableList<ConfigSerializable>()
    } else {
        map.first().first to notifiableList(map.first().second)
    }

    RowListWrapped(
        modifier = Modifier.fill(),
        listModifier = { Modifier.fill() },
    ) {
        map.forEach { (config, configs) ->
            FlatButton(
                modifier = Modifier.width((map.map { it.first.translateText }.maxWidth + 4f).coerceIn(100f, 160f)),
                hoveredColor = Colors.CYAN.alpha(0.25f),
                pressedColor = Colors.CYAN.alpha(0.5f),
                horizontalArrangement = Arrangement.Left
            ) {
                TextLabel(
                    config.translateText,
                    modifier = Modifier.hoverText(config.comment, Tip.DefaultSetting.copy(optionalDirection = Direction.clockwiseFromRight))
                )
                click {
                    currentGroup = config.translateText
                    currentConfigs.disableNotify {
                        currentConfigs.clear()
                        currentConfigs.addAll(configs)
                    }
                    currentConfigs.onChange(currentConfigs)
                }
            }
        }
    }

    Row(
        verticalArrangement = Arrangement.spacedBy(3f),
    ) {
        SearchBar(
            textConsumer = { str ->
                currentConfigs.disableNotify {
                    currentConfigs.clear()
                    currentConfigs.addAll(
                        map.find { (text, _) -> text.translateText == currentGroup }
                            ?.second?.filter {
                                it.matched(str.toRegex())
                                        || it.translateText.plainText.contains(str)
                                        || it.comment.plainText.contains(str)
                            }
                            ?: emptyList()
                    )
                }
                currentConfigs.onChange(currentConfigs)
            },
            hintText = stateOf(IGLang.search.plainText),
            modifier = Modifier.fill(),
            textEditorModifier = { Modifier.weight(1) }
        )
        ConfigsWrapper(
            currentConfigs,
            modifier = Modifier.fill()
        ).apply {
            currentConfigs.subscribe {
                execute {
                    this.recompose()
                }
            }
        }
    }

}

