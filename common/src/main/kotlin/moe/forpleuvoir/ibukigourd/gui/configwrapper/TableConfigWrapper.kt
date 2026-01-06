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
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.base.scope.TableLayoutColumnScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.screen.closeScreen
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.ConfirmDialog
import moe.forpleuvoir.ibukigourd.gui.widget.Dialog
import moe.forpleuvoir.ibukigourd.gui.widget.DialogContent
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.DeleteButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextArea
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextSetting
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.appendLiteral
import moe.forpleuvoir.ibukigourd.text.style
import moe.forpleuvoir.ibukigourd.text.width
import moe.forpleuvoir.ibukigourd.text.withColor
import moe.forpleuvoir.ibukigourd.util.renameKey
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.impl.ConfigList
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringList
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringMap
import kotlin.time.Duration.Companion.seconds

fun <T> TableScope<T>.MoveableTableHeader(
    weight: Int = 0,
    header: TableLayoutColumnScope.() -> GuiWidget = {
        Text(IGLang.move)
    }
) = Header(weight, header)

fun <T> TableWidget.Scope.ColumnBuilder<T>.MoveableTableColumCell(
    config: ConfigList<T>,
    recompose: () -> Unit,
    showIndex: Boolean = true,
    cell: TableLayoutColumnScope.(Int, T) -> GuiWidget = { index, _ ->
        Row {
            MoveButton(recompose, config.getValue(), index)
            if (showIndex)
                Text(
                    index.toString(),
                    modifier = Modifier.width(config.getValue().lastIndex.toString().width + 1f)
                )
        }
    },
) = Column(cell)

fun <T> ContainerScope.TableWrappedButton(
    userData: Iterable<T>,
    title: MutableText,
    onAdd: (T) -> Unit,
    newValue: (Iterable<T>) -> T,
    //hover
    hoverSettings: Tip.Setting = Tip.DefaultSetting,
    hoverModifier: Modifier = Modifier,
    hoverContent: BoxScope.(Iterable<T>) -> Unit,
    //button
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        Text(mutableStateBy { IGLang.listConfigWrapperText(userData.count()) })
    },
    dialogModifier: Modifier = Modifier,
    dialogContentModifier: ColumnScope.() -> Modifier = { Modifier },
    tableWrappedModifier: BoxScope.() -> Modifier = { Modifier },
    tableListModifier: RowScope.() -> Modifier = { Modifier },
    tableScope: TableScope<T>.() -> Unit
) = Button(
    Modifier.width(80f)
        .hoverTip(hoverSettings, hoverModifier) { hoverContent(userData) }
        .then(modifier)
) {
    content()
    click {
        Dialog(screenModifier = dialogModifier) {
            Text(title)
            val table = DialogContent(
                Modifier.padding(5f, 3f, 5f, 5f).then(dialogContentModifier())
            ) {
                TableWrapped(
                    userData,
                    modifier = Modifier.disableRenderBackground().padding(0).then(tableWrappedModifier()),
                    tableModifier = { Modifier.height(160f).then(tableListModifier()) },
                    scope = tableScope
                )
            }
            Button(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40f)
                    .hoverText(IGLang.add)
            ) {
                Icon(IconTextures.PLUS, Color.ofARGB(0xFF2EE62E), Modifier.size(8f, 8f))
                click {
                    onAdd(newValue(userData))
                    table.executeRecompose()
                }
            }
        }.open()
    }
}

fun <T> ContainerScope.TableConfigListWrappedButton(
    config: ConfigList<T>,
    title: MutableText = config.translateText.style { hover(config.comment) },
    onAdd: (T) -> Unit = { config.add(it) },
    newValue: (Iterable<T>) -> T,
    //hover
    hoverSettings: Tip.Setting = Tip.DefaultSetting,
    hoverModifier: Modifier = Modifier,
    hoverTableScope: TableScope<T>.() -> Unit,
    hoverContent: BoxScope.(Iterable<T>) -> Unit = {
        if (config.isEmpty()) {
            Text(IGLang.hasNothing)
        } else {
            Table(config.subList(0, config.size.coerceAtMost(9)), scope = hoverTableScope)
        }
    },
    //button
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        Text(mutableStateBy { IGLang.listConfigWrapperText(config.size) })
    },
    dialogModifier: Modifier = Modifier,
    dialogContentModifier: ColumnScope.() -> Modifier = { Modifier },
    //RowList
    tableWrappedModifier: BoxScope.() -> Modifier = { Modifier },
    tableListModifier: RowScope.() -> Modifier = { Modifier },
    tableScope: TableScope<T>.() -> Unit
) = TableWrappedButton(
    config.getValue(),
    title,
    onAdd,
    newValue,
    hoverSettings,
    hoverModifier,
    hoverContent,
    modifier,
    content,
    dialogModifier,
    dialogContentModifier,
    tableWrappedModifier,
    tableListModifier,
    tableScope
)

fun <K, V> TableScope<Map.Entry<K, V>>.TableConfigMapKeyColumn(
    config: Config<MutableMap<K, V>, *>,
    keyWrapper: ButtonScope.(K, V, Map<K, V>) -> Unit,
    keyEditorWrapper: ColumnScope.(K, V, Map<K, V>, (K) -> Unit) -> (() -> Transform),
    recompose: () -> Unit,
    weight: Int = 0,
    header: TableLayoutColumnScope.() -> GuiWidget = { Text(IGLang.mapKey, modifier = Modifier.minWidth(80f)) },
    keyToSting: (K) -> String = { it.toString() },
    modifier: TableLayoutColumnScope.() -> Modifier = { Modifier },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = Header(weight, header).Column { (key, value) ->
    FlatButton(
        hoveredColor = Colors.PALEGREEN.alpha(.25f),
        modifier = Modifier.hoverText(IGLang.edit.appendLiteral(" ").append(IGLang.mapKey)).then(modifier()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        keyWrapper(key, value, config.getValue())
        click {
            var newKey = key
            var editor: (() -> Transform)? = null
            ConfirmDialog(
                stateOf(IGLang.edit.appendLiteral(" → $key")),
                onConfirm = {
                    if (newKey == key) {
                        closeScreen()
                        return@ConfirmDialog
                    }
                    if (config.getValue().containsKey(newKey)) {
                        editor?.let {
                            TipHandler.popTip(CONFIG_WRAPPER_TIP)
                            CONFIG_WRAPPER_TIP = TipHandler.pushTip(2.seconds, it, Tip {
                                Text(IGLang.keyExists(keyToSting(newKey)).withColor(Colors.RED))
                            })
                        }
                        return@ConfirmDialog
                    }
                    config.getValue().renameKey(key, newKey)
                    closeScreen()
                    recompose()
                },
                screenModifier = Modifier.onClose {
                    TipHandler.popTip(CONFIG_WRAPPER_TIP)
                }
            ) {
                editor = keyEditorWrapper(key, value, config.getValue()) { newKey = it }
            }.open()
        }
    }
}


fun <V> TableScope<Map.Entry<String, V>>.TableConfigMapStringKeyColumn(
    config: Config<MutableMap<String, V>, *>,
    keyWrapper: ButtonScope.(String, V, Map<String, V>) -> Unit = { k, _, _ ->
        Text(k, modifier = Modifier.width(120f))
    },
    keyEditorWrapper: ColumnScope.(String, V, Map<String, V>, (String) -> Unit) -> (() -> Transform) = { k, _, _, setKey ->
        val editor = TextEditor(modifier = Modifier.width(240f)) {
            text = k
            textConsumer { setKey(it) }
        }
        val transform = { editor.transform }
        transform
    },
    recompose: () -> Unit = { this.executeRecompose() },
    weight: Int = 0,
    header: TableLayoutColumnScope.() -> GuiWidget = { Text(IGLang.mapKey, modifier = Modifier.minWidth(80f)) },
    keyToSting: (String) -> String = { it },
    modifier: TableLayoutColumnScope.() -> Modifier = { Modifier.minWidth(120f) },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = TableConfigMapKeyColumn(
    config,
    keyWrapper,
    keyEditorWrapper,
    recompose,
    weight,
    header,
    keyToSting,
    modifier,
    horizontalArrangement,
    verticalAlignment
)


fun <K, V> TableScope<Map.Entry<K, V>>.TableConfigMapValueColumn(
    config: Config<MutableMap<K, V>, *>,
    valueWrapper: RowScope.(K, V, MutableMap<K, V>) -> Unit,
    valueEditorWrapper: ColumnScope.(K, V, Map<K, V>, (V) -> Unit) -> Unit,
    recompose: () -> Unit,
    weight: Int = 0,
    header: TableLayoutColumnScope.() -> GuiWidget = { Text(IGLang.mapValue, modifier = Modifier.minWidth(80f)) },
    modifier: TableLayoutColumnScope.() -> Modifier = { Modifier },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = Header(weight, header).Column { _, (key, value) ->
    Row(
        modifier = Modifier.then(modifier()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        valueWrapper(key, value, config.getValue())
        FlatButton(
            hoveredColor = Colors.PALEGREEN.alpha(.5f),
            modifier = Modifier.hoverText(IGLang.edit.appendLiteral(" ").append(IGLang.mapValue))
        ) {
            Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
            click {
                var newValue = config.getValue()[key]
                ConfirmDialog(
                    stateOf(IGLang.edit.appendLiteral(" → $key")),
                    onConfirm = {
                        config.getValue()[key] = newValue!!
                        closeScreen()
                        recompose()
                    }
                ) {
                    valueEditorWrapper(key, config.getValue()[key]!!, config.getValue()) { newValue = it }
                }.open()
            }
        }
    }
}

fun <K> TableScope<Map.Entry<K, String>>.TableConfigMapStringValueColumn(
    config: Config<MutableMap<K, String>, *>,
    valueWrapper: RowScope.(K, String, MutableMap<K, String>) -> Unit = { k, v, map ->
        TextEditor(modifier = Modifier.width(160f)) {
            text = v
            textConsumer { map[k] = it }
        }
    },
    valueEditorWrapper: ColumnScope.(K, String, Map<K, String>, (String) -> Unit) -> Unit = { _, v, _, setValue ->
        TextArea(modifier = Modifier.width(240f).height(160f)) {
            text = v
            textConsumer { setValue(it) }
        }
    },
    recompose: () -> Unit = { this.executeRecompose() },
    weight: Int = 0,
    header: TableLayoutColumnScope.() -> GuiWidget = { Text(IGLang.mapValue, modifier = Modifier.minWidth(80f)) },
    modifier: TableLayoutColumnScope.() -> Modifier = { Modifier.minWidth(120f) },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2f),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = TableConfigMapValueColumn(
    config,
    valueWrapper,
    valueEditorWrapper,
    recompose,
    weight,
    header,
    modifier,
    horizontalArrangement,
    verticalAlignment
)

fun <K, V> ContainerScope.TableConfigMapWrappedButton(
    config: Config<MutableMap<K, V>, *>,
    title: MutableText = config.translateText.style { hover(config.comment) },
    onAdd: (Map.Entry<K, V>) -> Unit = { config.getValue()[it.key] = it.value },
    newValue: (Iterable<Map.Entry<K, V>>) -> Map.Entry<K, V>,
    //hover
    hoverSettings: Tip.Setting = Tip.DefaultSetting,
    hoverModifier: Modifier = Modifier,
    hoverTableScope: TableScope<Map.Entry<K, V>>.() -> Unit,
    hoverContent: BoxScope.(Iterable<Map.Entry<K, V>>) -> Unit = {
        if (config.getValue().isEmpty()) {
            Text(IGLang.hasNothing)
        } else {
            Table(config.getValue().entries.toList().subList(0, config.getValue().size.coerceAtMost(9)), scope = hoverTableScope)
        }
    },
    //button
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = {
        Text(mutableStateBy { IGLang.listConfigWrapperText(config.getValue().size) })
    },
    dialogModifier: Modifier = Modifier,
    dialogContentModifier: ColumnScope.() -> Modifier = { Modifier },
    //RowList
    tableWrappedModifier: BoxScope.() -> Modifier = { Modifier },
    tableListModifier: RowScope.() -> Modifier = { Modifier },
    tableScope: TableScope<Map.Entry<K, V>>.() -> Unit
) = TableWrappedButton(
    config.getValue().entries,
    title,
    onAdd,
    newValue,
    hoverSettings,
    hoverModifier,
    hoverContent,
    modifier,
    content,
    dialogModifier,
    dialogContentModifier,
    tableWrappedModifier,
    tableListModifier,
    tableScope
)

fun ContainerScope.StringPairListConfigWrapper(
    config: ConfigPairList<String, String>,
    modifier: Modifier = Modifier,
    firstTableName: MutableText = IGLang.pairFirst,
    secondTableName: MutableText = IGLang.pairSecond,
    showIndex: Boolean = false
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        TableConfigListWrappedButton(
            config = config,
            newValue = { "" to "" },
            hoverTableScope = {
                Header {
                    Text(firstTableName, modifier = Modifier.align(Alignment.CenterLeft))
                }.Column {
                    Text(it.first.take(60), modifier = Modifier.align(Alignment.CenterLeft).minWidth(80f).maxWidth(160f))
                }

                Header {
                    Text(secondTableName, modifier = Modifier.align(Alignment.CenterLeft))
                }.Column {
                    Text(it.second.take(60), modifier = Modifier.align(Alignment.CenterLeft).minWidth(80f).maxWidth(160f))
                }
            }
        ) {
            val recompose = { this@TableConfigListWrappedButton.executeRecompose() }

            MoveableTableHeader {
                Text(IGLang.move, modifier = Modifier.padding(bottom = 3f))
            }.MoveableTableColumCell(config, recompose, showIndex)

            Header {
                Text(
                    firstTableName,
                    setting = TextSetting().copy(horizontalAlignment = Alignment.CenterHorizontally),
                    modifier = Modifier.padding(bottom = 3f).minWidth(80f)
                )
            }.Column { index, (first, _) ->
                TextEditor(modifier = Modifier.width(120f)) {
                    text = first
                    textConsumer {
                        config.getValue()[index] = it to config.getValue()[index].second
                    }
                }
            }

            Header {
                Text(
                    secondTableName,
                    setting = TextSetting().copy(horizontalAlignment = Alignment.CenterHorizontally),
                    modifier = Modifier.padding(bottom = 3f).minWidth(120f)
                )
            }.Column { index, (_, second) ->
                TextEditor(modifier = Modifier.width(160f)) {
                    text = second
                    textConsumer {
                        config.getValue()[index] = config.getValue()[index].first to it
                    }
                }
            }

            Header {
                Text(IGLang.edit, modifier = Modifier.padding(bottom = 3f))
            }.Column { index, (_, _) ->
                Row {
                    FlatButton(
                        hoveredColor = Colors.PALEGREEN.alpha(.5f),
                        modifier = Modifier.hoverText(IGLang.edit.appendLiteral(" ").append(IGLang.pairFirst).appendLiteral(" ").append(IGLang.pairSecond))
                    ) {
                        Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
                        click {
                            var newKey = config.getValue()[index].first
                            var newValue = config.getValue()[index].second
                            ConfirmDialog(
                                stateOf(IGLang.edit.appendLiteral(" → ${config.getValue()[index].first}")),
                                onConfirm = {
                                    config.getValue()[index] = newKey to newValue
                                    closeScreen()
                                    recompose()
                                }
                            ) {
                                TextEditor(modifier = Modifier.width(240f)) {
                                    text = config.getValue()[index].first
                                    textConsumer { newKey = it }
                                }
                                TextArea(modifier = Modifier.width(240f).height(120f)) {
                                    text = config.getValue()[index].second
                                    textConsumer { newValue = it }
                                }
                            }.open()
                        }
                    }
                    DeleteButton({ IGLang.removeConfirm("${config.getValue()[index].first} → ${config.getValue()[index].second}") }, recompose) {
                        config.removeAt(index)
                    }
                }
            }
        }
        ConfigResetButton(config) {
            this@Row.executeRecompose()
        }
    }
}

fun ContainerScope.StringListConfigWrapper(
    config: ConfigStringList,
    modifier: Modifier = Modifier,
    contentTableName: MutableText = IGLang.content,
    showIndex: Boolean = false
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        TableConfigListWrappedButton(
            config = config,
            newValue = { "" },
            hoverTableScope = {
                ColumnBuilder {
                    Text(it.take(120), modifier = Modifier.align(Alignment.CenterLeft).minWidth(80f).maxWidth(160f))
                }
            }
        ) {
            val recompose = { this@TableConfigListWrappedButton.executeRecompose() }
            MoveableTableHeader {
                Text(IGLang.move, modifier = Modifier.padding(bottom = 3f))
            }.MoveableTableColumCell(config, recompose, showIndex)

            Header {
                Text(
                    contentTableName,
                    setting = TextSetting().copy(horizontalAlignment = Alignment.CenterHorizontally),
                    modifier = Modifier.padding(bottom = 3f).minWidth(160f)
                )
            }.Column { index, entry ->
                TextEditor(modifier = Modifier.width(240f)) {
                    text = entry
                    textConsumer {
                        config[index] = it
                    }
                }
            }

            Header {
                Text(IGLang.edit, modifier = Modifier.padding(bottom = 3f))
            }.Column { index, _ ->
                DeleteButton({ IGLang.removeConfirm("[$index]${config[index]}") }, recompose) {
                    config.removeAt(index)
                }
            }
        }
        ConfigResetButton(config) {
            this@Row.executeRecompose()
        }
    }
}

fun ContainerScope.StringMapConfigWrapper(
    config: ConfigStringMap,
    modifier: Modifier = Modifier,
    keyTableName: MutableText = IGLang.mapKey,
    valueTableName: MutableText = IGLang.mapValue,
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        TableConfigMapWrappedButton(
            config = config,
            newValue = { mapEntry("key ${(it.count())}", "") },
            hoverTableScope = {
                Header {
                    Text(keyTableName, modifier = Modifier.align(Alignment.CenterLeft))
                }.Column {
                    Text(it.key.take(60), modifier = Modifier.align(Alignment.CenterLeft).minWidth(20f).maxWidth(100f))
                }

                Header {
                    Text(valueTableName, modifier = Modifier.align(Alignment.CenterLeft))
                }.Column {
                    Text(it.value.take(60), modifier = Modifier.align(Alignment.CenterLeft).minWidth(20f).maxWidth(100f))
                }
            }
        ) {

            TableConfigMapStringKeyColumn(
                config,
                header = {
                    Text(
                        keyTableName,
                        setting = TextSetting().copy(horizontalAlignment = Alignment.CenterHorizontally),
                        modifier = Modifier.padding(bottom = 3f).minWidth(80f)
                    )
                })

            TableConfigMapStringValueColumn(
                config,
                header = {
                    Text(
                        valueTableName,
                        setting = TextSetting().copy(horizontalAlignment = Alignment.CenterHorizontally),
                        modifier = Modifier.padding(bottom = 3f).minWidth(80f)
                    )
                })

            Header {
                Text(
                    IGLang.remove,
                    setting = TextSetting().copy(horizontalAlignment = Alignment.CenterHorizontally),
                    modifier = Modifier.padding(bottom = 3f)
                )
            }.Column { _, (key, _) ->
                DeleteButton(
                    { IGLang.removeConfirm("$key → ${config[key]}") },
                    { this@TableConfigMapWrappedButton.executeRecompose() }
                ) {
                    config.remove(key)
                }
            }
        }
        ConfigResetButton(config) {
            this@Row.executeRecompose()
        }
    }
}