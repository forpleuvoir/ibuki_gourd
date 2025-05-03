package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.item.ConfigPairList
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.ConfirmDialog
import moe.forpleuvoir.ibukigourd.gui.widget.Dialog
import moe.forpleuvoir.ibukigourd.gui.widget.DialogContent
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextArea
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.width
import moe.forpleuvoir.ibukigourd.util.forEachWithLimit
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.ibukigourd.util.renameKey
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.impl.ConfigList
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringList
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringMap
import kotlin.time.Duration.Companion.seconds

fun WidgetContainerScope.MoveButton(
    recompose: () -> Unit,
    listValue: MutableList<*>,
    index: Int,
) = Column {
    FlatButton(
        hoveredColor = Colors.BLACK.alpha(.15f),
        round = 0,
        modifier = Modifier.hoverText(IGLang.moveUp).padding(1).active(index > 0)
    ) {
        Icon(IconTextures.UP, modifier = Modifier, color = Colors.GRAY.alpha((index > 0).pick(1f, .25f)))

        click {
            listValue.moveElement(index, (index - 1).coerceAtLeast(0))
            recompose()
        }
    }
    FlatButton(
        hoveredColor = Colors.BLACK.alpha(.15f),
        round = 0,
        modifier = Modifier.hoverText(IGLang.moveDown, Tip.DefaultSetting.copy(optionalDirection = Direction.clockwiseFromBottom)).padding(1)
            .active(index != listValue.lastIndex)
    ) {
        Icon(
            IconTextures.DOWN,
            modifier = Modifier,
            color = Colors.GRAY.alpha((index != listValue.lastIndex).pick(1f, .25f))
        )

        click {
            listValue.moveElement(index, (index + 1).coerceAtMost(listValue.lastIndex))
            recompose()
        }
    }
}

fun <T> WidgetContainerScope.IterableWrappedButton(
    title: Text,
    iterable: Iterable<T>,
    onAdd: (T) -> Unit,
    newValue: (Iterable<T>) -> T,
    //hover
    hoverSettings: Tip.Setting = Tip.DefaultSetting,
    hoverModifier: Modifier = Modifier,
    hoverContent: BoxScope.(Iterable<T>) -> Unit,
    //button
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        TextLabel(mutableStateBy { IGLang.listConfigWrapperText(iterable.count()) })
    },
    //ColumnList
    columnListWrapperModifier: BoxScope.() -> Modifier = { Modifier },
    columnListModifier: RowScope.() -> Modifier = { Modifier },
    entryWrapper: ColumnListScope.(T, index: Int) -> Unit
) = Button(
    Modifier.width(80f)
        .hoverTip(hoverSettings, hoverModifier) { hoverContent(iterable) }
        .then(modifier)
) {
    content()
    click {
        var columnList: WidgetContainerImpl? = null
        Dialog {
            TextLabel(title)
            DialogContent(
                Modifier.padding(5f, 3f, 5f, 5f)
            ) {
                columnList = ColumnListWrapped(
                    modifier = Modifier.disableRenderBackground().padding(0).minWidth(240f).then(columnListWrapperModifier()),
                    listModifier = { Modifier.height(160f).then(columnListModifier()) }
                ) {
                    if (iterable.count() == 0) TextLabel(IGLang.hasNothing)
                    iterable.forEachIndexed { index, entry ->
                        entryWrapper(entry, index)
                    }
                }
            }
            Button(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40f)
                    .hoverText(IGLang.add)
            ) {
                Icon(IconTextures.PLUS, Color(0xFF2EE62E), Modifier.size(8f, 8f))
                click {
                    onAdd(newValue(iterable))
                    columnList?.executeRecompose()
                }
            }
        }.open()
    }
}

fun <T> WidgetContainerScope.ListConfigWrappedButton(
    config: ConfigList<T>,
    title: Text = config.translateText.style { hover(config.comment) },
    iterable: Iterable<T> = config.getValue(),
    onAdd: (T) -> Unit = { config.getValue().add(it) },
    newValue: (Iterable<T>) -> T,
    //hover
    hoverSettings: Tip.Setting = Tip.DefaultSetting,
    hoverModifier: Modifier = Modifier,
    hoverEntryToString: (T) -> String = { it.toString() },
    hoverContent: BoxScope.(Iterable<T>) -> Unit = {
        TextLabel(mutableStateBy {
            val sb = StringBuilder()
            config.getValue().forEachWithLimit(10) { t ->
                sb.appendLine(hoverEntryToString(t))
            }
            if (config.getValue().size > 10) sb.append("...")
            if (config.getValue().isEmpty()) sb.append(IGLang.hasNothing.plainText)
            if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
            Literal(sb.toString())
        })
    },
    //button
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        TextLabel(mutableStateBy { IGLang.listConfigWrapperText(iterable.count()) })
    },
    //RowList
    columnListWrapperModifier: BoxScope.() -> Modifier = { Modifier },
    columnListModifier: RowScope.() -> Modifier = { Modifier },
    entryWrapper: ColumnListScope.(T, index: Int) -> Unit
) = IterableWrappedButton(
    title = title,
    iterable = iterable,
    onAdd = onAdd,
    newValue = newValue,
    hoverSettings = hoverSettings,
    hoverModifier = hoverModifier,
    hoverContent = hoverContent,
    modifier = modifier,
    content = content,
    columnListWrapperModifier = columnListWrapperModifier,
    columnListModifier = columnListModifier,
    entryWrapper = entryWrapper,
)

fun <T> WidgetContainerScope.ListConfigEntryWrapper(
    config: ConfigList<T>,
    index: Int,
    recompose: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: RowScope.() -> Unit
) = Row(
    modifier,
    horizontalArrangement,
    verticalAlignment,
) {
    TextLabel(
        index.toString(),
        modifier = Modifier.width(config.getValue().lastIndex.toString().width + 1f)
    )
    content()
    FlatButton(
        hoveredColor = Colors.LIGHT_RED,
        modifier = Modifier.margin(right = 2f).hoverText(IGLang.remove)
    ) {
        Icon(IconTextures.DELETE, Colors.RED, Modifier.size(10f, 10f))
        click {
            config.getValue().removeAt(index)
            recompose()
        }
    }
}

fun <T> WidgetContainerScope.MoveableListConfigEntryWrapper(
    config: ConfigList<T>,
    index: Int,
    recompose: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: RowScope.() -> Unit
) = Row(
    modifier,
    horizontalArrangement,
    verticalAlignment,
) {
    MoveButton(recompose, config.getValue(), index)
    TextLabel(
        index.toString(),
        modifier = Modifier.width(config.getValue().lastIndex.toString().width + 1f)
    )
    content()
    FlatButton(
        hoveredColor = Colors.LIGHT_RED,
        modifier = Modifier.margin(right = 2f).hoverText(IGLang.remove)
    ) {
        Icon(IconTextures.DELETE, Colors.RED, Modifier.size(10f, 10f))
        click {
            config.getValue().removeAt(index)
            recompose()
        }
    }
}

fun WidgetContainerScope.StringListConfigWrapper1(
    config: ConfigStringList,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        ListConfigWrappedButton(
            config = config,
            newValue = { "" },
        ) { entry, index ->
            MoveableListConfigEntryWrapper(
                config = config,
                index = index,
                recompose = { this@ListConfigWrappedButton.executeRecompose() }
            ) {
                TextEditor(modifier = Modifier.width(240f)) {
                    text = entry
                    textConsumer {
                        config.getValue()[index] = it
                    }
                }
            }
        }
        ConfigResetButton(config) {
            this@Row.executeRecompose()
        }
    }
}

fun WidgetContainerScope.StringPairListConfigWrapper1(
    config: ConfigPairList<String, String>,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        ListConfigWrappedButton(
            config = config,
            newValue = { "" to "" },
            hoverEntryToString = { "${it.first} => ${it.second}" }
        ) { (key, value), index ->
            val recompose = { this@ListConfigWrappedButton.executeRecompose() }
            MoveableListConfigEntryWrapper(
                config = config,
                index = index,
                recompose = recompose,
                modifier = Modifier.width(360f)
            ) {
                TextEditor(modifier = Modifier.width(240f).weight(3)) {
                    text = key
                    textConsumer {
                        config.getValue()[index] = it to value
                    }
                }

                TextEditor(modifier = Modifier.width(240f).weight(5)) {
                    text = value
                    textConsumer {
                        config.getValue()[index] = key to it
                    }
                }

                FlatButton(
                    hoveredColor = Colors.PALEGREEN.alpha(.5f),
                    modifier = Modifier.hoverText(IGLang.edit)
                ) {
                    Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
                    click {
                        var newKey = key
                        var newValue = value
                        ConfirmDialog(
                            stateOf(IGLang.edit.appendLiteral(" => $key")),
                            onConfirm = {
                                config.getValue()[index] = newKey to newValue
                                mc.currentScreen?.close()
                                recompose()
                            }
                        ) {
                            TextEditor(modifier = Modifier.width(240f)) {
                                text = key
                                textConsumer { newKey = it }
                            }
                            TextArea(modifier = Modifier.width(240f).height(120f)) {
                                text = value
                                textConsumer { newValue = it }
                            }
                        }.open()
                    }
                }
            }
        }
        ConfigResetButton(config) {
            this@Row.executeRecompose()
        }
    }
}

fun <K, V> mapEntry(key: K, value: V) = object : Map.Entry<K, V> {
    override val key: K = key
    override val value: V = value
}

fun <K, V> WidgetContainerScope.MapConfigWrappedButton(
    config: Config<MutableMap<K, V>, *>,
    title: Text = config.translateText.style { hover(config.comment) },
    iterable: Iterable<Map.Entry<K, V>> = config.getValue().entries,
    onAdd: (Map.Entry<K, V>) -> Unit = { config.getValue().put(it.key, it.value) },
    newValue: (Iterable<Map.Entry<K, V>>) -> Map.Entry<K, V>,
    //hover
    hoverSettings: Tip.Setting = Tip.DefaultSetting,
    hoverModifier: Modifier = Modifier,
    hoverContent: BoxScope.(Iterable<Map.Entry<K, V>>) -> Unit = {
        TextLabel(mutableStateBy {
            val sb = StringBuilder()
            config.getValue().forEachWithLimit(10) { k, v ->
                sb.appendLine("$k => $v")
            }
            if (config.getValue().size > 10) sb.append("...")
            if (config.getValue().isEmpty()) sb.append(IGLang.hasNothing.plainText)
            if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
            Literal(sb.toString())
        })
    },
    //button
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        TextLabel(mutableStateBy { IGLang.listConfigWrapperText(iterable.count()) })
    },
    //RowList
    columnListWrapperModifier: BoxScope.() -> Modifier = { Modifier },
    columnListModifier: RowScope.() -> Modifier = { Modifier },
    entryWrapper: ColumnListScope.(Map.Entry<K, V>, index: Int) -> Unit
) = IterableWrappedButton(
    title = title,
    iterable = iterable,
    onAdd = onAdd,
    newValue = newValue,
    hoverSettings = hoverSettings,
    hoverModifier = hoverModifier,
    hoverContent = hoverContent,
    modifier = modifier,
    content = content,
    columnListWrapperModifier = columnListWrapperModifier,
    columnListModifier = columnListModifier,
    entryWrapper = entryWrapper,
)

fun <K, V> WidgetContainerScope.MapConfigEntryWrapper(
    config: Config<MutableMap<K, V>, *>,
    key: K,
    keyWrapper: RowScope.(K, Map<K, V>) -> Unit,
    keyEditorWrapper: ColumnScope.(K, Map<K, V>, (K) -> Unit) -> (() -> Transform),
    keyToSting: (K) -> String = { it.toString() },
    value: V,
    valueWrapper: RowScope.(V, MutableMap<K, V>) -> Unit,
    valueEditorWrapper: ColumnScope.(V, Map<K, V>, (V) -> Unit) -> Unit,
    recompose: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = Row(
    modifier,
    horizontalArrangement,
    verticalAlignment,
) {
    keyWrapper(key, config.getValue())
    FlatButton(
        hoveredColor = Colors.PALEGREEN.alpha(.5f),
        modifier = Modifier.hoverText(IGLang.edit)
    ) {
        Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
        click {
            var newKey = key
            var editor: (() -> Transform)? = null
            ConfirmDialog(
                stateOf(IGLang.edit.appendLiteral(" => $key")),
                onConfirm = {
                    if (newKey == key) {
                        mc.currentScreen?.close()
                        return@ConfirmDialog
                    }
                    if (config.getValue().containsKey(newKey)) {
                        editor?.let {
                            TipHandler.pushTip(CONFIG_WRAPPER_TIP, 2.seconds, it, Tip {
                                TextLabel(IGLang.keyExists(keyToSting(newKey)).withColor(Colors.RED))
                            })
                        }
                        return@ConfirmDialog
                    }
                    config.getValue().renameKey(key, newKey)
                    mc.currentScreen?.close()
                    recompose()
                },
                screenModifier = Modifier.onClose {
                    TipHandler.popTip(CONFIG_WRAPPER_TIP)
                }
            ) {
                editor = keyEditorWrapper(key, config.getValue()) { newKey = it }
            }.open()
        }
    }
    valueWrapper(value, config.getValue())
    FlatButton(
        hoveredColor = Colors.PALEGREEN.alpha(.5f),
        modifier = Modifier.hoverText(IGLang.edit)
    ) {
        Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
        click {
            var newValue = value
            ConfirmDialog(
                stateOf(IGLang.edit.appendLiteral(" => $key")),
                onConfirm = {
                    config.getValue()[key] = newValue
                    mc.currentScreen?.close()
                    recompose()
                }
            ) {
                valueEditorWrapper(value, config.getValue()) { newValue = it }
            }.open()
        }
    }

    FlatButton(
        hoveredColor = Colors.LIGHT_RED,
        modifier = Modifier.margin(right = 2f).hoverText(IGLang.remove)
    ) {
        Icon(IconTextures.DELETE, Colors.RED, Modifier.size(10f, 10f))
        click {
            config.getValue().remove(key)
            recompose()
        }
    }
}

fun WidgetContainerScope.StringMapConfigWrapper1(
    config: ConfigStringMap,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        MapConfigWrappedButton(
            config = config,
            newValue = { mapEntry("key ${(it.count())}", "") },
        ) { (key, value), index ->
            MapConfigEntryWrapper(
                config = config,
                key = key,
                keyWrapper = { k, map ->
                    TextLabel(
                        key, modifier = Modifier.width(map.keys.maxWidth.coerceAtMost(119f) + 1f)
                    )
                },
                keyEditorWrapper = { k, map, setKey ->
                    val editor = TextEditor(modifier = Modifier.width(240f)) {
                        text = key
                        textConsumer { setKey(it) }
                    }
                    val transform = { editor.transform }
                    transform
                },
                keyToSting = { it },
                value = value,
                valueWrapper = { v, map ->
                    TextEditor(modifier = Modifier.width(240f)) {
                        text = value
                        textConsumer { map[key] = it }
                    }
                },
                valueEditorWrapper = { v, map, setValue ->
                    TextEditor(modifier = Modifier.width(240f)) {
                        text = value
                        textConsumer { setValue(it) }
                    }
                },
                recompose = { this@MapConfigWrappedButton.executeRecompose() }
            )
        }
        ConfigResetButton(config) {
            this@Row.executeRecompose()
        }
    }
}
