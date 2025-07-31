package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.config.userdata.guiWrapper
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.modifier.bgHoverHighlightBox
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRender
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.autoExpandConfigContainer
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.autoExpandConfigContainerLimit
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.configContainerWrapperGuidelinesColor
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.showFirstConfigInContainer
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.lateInitValueOf
import moe.forpleuvoir.ibukigourd.util.state.*
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.manager.ConfigManager

fun ContainerScope.ConfigsWrapper(
    configs: Iterable<ConfigSerializable>,
    modifier: Modifier = Modifier,
    listModifier: RowScope.() -> Modifier = { Modifier.weight(1).fill() },
    scrollerModifier: BoxScope.() -> Modifier = { Modifier },
) = ColumnListWrapped(
    modifier = modifier,
    listModifier = listModifier,
    scrollerModifier = scrollerModifier,
    spacing = 4f
) {
    if (configs.count() == 0) TextLabel(IGLang.hasNothing)
    configs.forEach { config ->
        config.guiWrapper(this, Modifier.fill().unlockConstraint())
    }
}

fun ContainerScope.ConfigContainerWrapper(
    config: ConfigContainer,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    Row(
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

fun ContainerScope.ExpandableConfigContainerWrapper(
    config: ConfigContainer,
    modifier: Modifier = Modifier
) = Column(modifier) {
    val expanded = mutableStateOf(autoExpandConfigContainer && config.configs().size <= autoExpandConfigContainerLimit)

    var firstConfig = config.configs().find { showFirstConfigInContainer && it !is ConfigContainer }
    if (config.configs().count { it !is ConfigContainer } <= 1) firstConfig = null

    Button(
        modifier = Modifier.disableRender().padding(0)
    ) {
        click { expanded.switch() }
        Row(
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
                Rect(configContainerWrapperGuidelinesColor, modifier = Modifier.size(2f, 2f).margin(horizontal = 2f))
            }
            Row {
                firstConfig?.guiWrapper(this, Modifier.weight(1).disableRenderBackground())
                Icon(
                    mutableStateOf(expanded) { it.pick(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN) },
                    modifier = Modifier.margin(right = 5f, left = 2f)
                )
            }
        }
    }
    SwitchableProxy(
        widgetA = {
            Row {
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
                Column(
                    modifier = Modifier.padding(2, 8, 2, 2),
                    verticalArrangement = Arrangement.spacedBy(4f)
                ) {
                    if (config.configs().isEmpty()) TextLabel(IGLang.hasNothing)
                    config.configs().forEach { config ->
                        if (config != firstConfig)
                            config.guiWrapper(this, Modifier.fill())
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

fun ContainerScope.ConfigManagerWrapper(
    configManager: ConfigManager,
    modifier: Modifier = Modifier,
) = Row(
    modifier,
    horizontalArrangement = Arrangement.spacedBy(5f)
) {
    val map = buildList {
        (configManager.configs().filterIsInstance<Config<*, *>>() as Collection<ConfigSerializable>).let {
            if (it.isNotEmpty()) add(configManager as ConfigSerializable to it)
        }
        addAll(configManager.configs().filterIsInstance<ConfigContainer>().map { it to it.configs() })
    }
    var currentGroup = map.firstOrNull()?.first?.key
    var currentConfigs = map.firstOrNull { it.first.key == currentGroup }?.second ?: emptyList()

    var onGroupChange by lateInitValueOf<() -> Unit>()

    ColumnListWrapped(
        modifier = Modifier.fill(),
        listModifier = { Modifier.fill() },
    ) {
        val hoveredColor = Colors.CYAN.alpha(0.25f).asState
        val pressedColor = Colors.CYAN.alpha(0.5f).asState
        val idleColor = { key: String ->
            if (currentGroup == key) Colors.CYAN.alpha(0.25f) else Colors.BLACK.alpha(0f)
        }
        map.forEach { (config, configs) ->
            FlatButton(
                modifier = Modifier.width((map.map { it.first.translateText }.maxWidth + 4f).coerceIn(100f, 160f)),
                hoveredColor = hoveredColor,
                pressedColor = pressedColor,
                idleColor = mutableStateBy { idleColor(config.key) },
                horizontalArrangement = Arrangement.Left
            ) {
                TextLabel(
                    config.translateText,
                    modifier = Modifier.hoverText(config.comment, Tip.DefaultSetting.copy(optionalDirection = Direction.clockwiseFromRight))
                )
                click {
                    if (currentGroup != config.key) {
                        currentGroup = config.key
                        map.firstOrNull { it.first.key == currentGroup }?.second?.let {
                            currentConfigs = it
                            onGroupChange()
                        }
                    }
                }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(3f),
    ) {
        var str = ""
        val predicate = { c: ConfigSerializable ->
            str.isEmpty() || c.matched(str.toRegex()) || c.translateText.plainText.contains(str) || c.comment.plainText.contains(str)
        }
        var onSearch by lateInitValueOf<() -> Unit>()
        SearchBar(
            textConsumer = {
                str = it
                onSearch()
            },
            hintText = stateOf(IGLang.search.plainText),
            modifier = Modifier.fill(),
            textEditorModifier = { Modifier.weight(1) }
        )
        Box {
            ConfigsWrapper(
                currentConfigs.filter(predicate),
                modifier = Modifier.fill()
            )
            onSearch = {
                this.executeRecompose()
            }
        }
        onGroupChange = {
            this.executeRecompose()
        }
    }

}

fun ContainerScope.ConfigManagerWrapperOld(
    configManager: ConfigManager,
    modifier: Modifier = Modifier,
) = Row(
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
        Literal("empty") to notifiableList()
    } else {
        map.first().first to notifiableList(map.first().second)
    }

    ColumnListWrapped(
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

    Column(
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
                executeRecompose()
            }
        }
    }

}

