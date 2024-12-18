package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.IGLang.mapConfigWrapperText
import moe.forpleuvoir.ibukigourd.config.item.ConfigPairList
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.execute
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.recompose
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.ConfirmDialog
import moe.forpleuvoir.ibukigourd.gui.widget.Dialog
import moe.forpleuvoir.ibukigourd.gui.widget.DialogContent
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextArea
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.forEachWithLimit
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.ibukigourd.util.renameKey
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.collection.notifiableMap
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringList
import moe.forpleuvoir.nebula.config.item.impl.ConfigStringMap

fun WidgetContainerScope.StringListConfigWrapper(
    config: ConfigStringList,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {

    val listValue = notifiableList(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            Modifier.width(80f)
                .hoverText(mutableStateBy {
                    val sb = StringBuilder()
                    listValue.forEachWithLimit(10) { t ->
                        sb.appendLine(t)
                    }
                    if (listValue.size > 10) sb.append("...")
                    if (listValue.isEmpty()) sb.append(IGLang.hasNothing.plainText)
                    if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
                    Literal(sb.toString())
                })
        ) {
            TextLabel(mutableStateBy { IGLang.listConfigWrapperText(listValue.size) })
            click {
                Dialog {
                    TextLabel(stateOf(config.translateText))
                    DialogContent(
                        Modifier.padding(5f, 3f, 5f, 5f)
                    ) {
                        RowListWrapped(
                            modifier = Modifier.disableRenderBackground().padding(0).minWidth(240f),
                            listModifier = { Modifier.height(160f) }
                        ) {
                            //TODO 很神秘的bug 如果列表为空会导致整个screen都无法正常测量和布局
                            if (listValue.isEmpty()) TextLabel(IGLang.hasNothing)
                            listValue.forEachIndexed { index, item ->
                                Column(
                                    horizontalArrangement = Arrangement.spacedBy(2f),
                                ) {
                                    MoveButton(this@RowListWrapped, listValue, index)
                                    TextLabel(
                                        index.toString(),
                                        modifier = Modifier.width(mc.textRenderer.getWidth(listValue.lastIndex.toString()) + 1f)
                                    )
                                    TextEditor(modifier = Modifier.width(240f)) {
                                        text = item
                                        textConsumer {
                                            listValue[index] = it
                                        }
                                    }
                                    FlatButton(
                                        hoveredColor = Colors.LIGHT_RED,
                                        modifier = Modifier.margin(right = 2f).hoverText(IGLang.remove)
                                    ) {
                                        Icon(IconTextures.DELETE, Colors.RED, Modifier.size(10f, 10f))
                                        click {
                                            listValue.removeAt(index)
                                            execute {
                                                this@RowListWrapped.recompose()
                                            }
                                        }
                                    }
                                }
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
                            listValue.add("")
                            this@Dialog.recompose()
                        }
                    }
                }.open()
            }
        }
        ConfigResetButton(config) {
            listValue.clear()
            listValue.addAll(config.defaultValue)
        }
    }
}

fun WidgetContainerScope.MoveButton(
    recomposeWidget: RowListScope,
    listValue: MutableList<*>,
    index: Int,
) = Row {
    FlatButton(
        hoveredColor = Colors.BLACK.alpha(.15f),
        round = 0,
        modifier = Modifier.hoverText(IGLang.moveUp).padding(1).active(index > 0)
    ) {
        Icon(IconTextures.UP, modifier = Modifier, color = Colors.GRAY.alpha((index > 0).pick(1f, .25f)))

        click {
            listValue.moveElement(index, (index - 1).coerceAtLeast(0))
            execute { recomposeWidget.recompose() }
        }
    }
    FlatButton(
        hoveredColor = Colors.BLACK.alpha(.15f),
        round = 0,
        modifier = Modifier.hoverText(IGLang.moveDown).padding(1).active(index != listValue.lastIndex)
    ) {
        Icon(
            IconTextures.DOWN,
            modifier = Modifier,
            color = Colors.GRAY.alpha((index != listValue.lastIndex).pick(1f, .25f))
        )

        click {
            listValue.moveElement(index, (index + 1).coerceAtMost(listValue.lastIndex))
            execute { recomposeWidget.recompose() }
        }
    }
}


fun WidgetContainerScope.StringMapConfigWrapper(
    config: ConfigStringMap,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {

    val mapValue = notifiableMap(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            Modifier.width(80f)
                .hoverText(mutableStateBy {
                    val sb = StringBuilder()
                    mapValue.forEachWithLimit(10) { k, v ->
                        sb.appendLine("$k => $v")
                    }
                    if (mapValue.size > 10) sb.append("...")
                    if (mapValue.isEmpty()) sb.append(IGLang.hasNothing.plainText)
                    if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
                    Literal(sb.toString())
                })
        ) {
            TextLabel(mutableStateBy { mapConfigWrapperText(mapValue.size) })
            click {
                Dialog {
                    TextLabel(stateOf(config.translateText))
                    DialogContent(
                        Modifier.padding(5f, 3f, 5f, 5f)
                    ) {
                        RowListWrapped(
                            modifier = Modifier.disableRenderBackground().padding(0).minWidth(240f),
                            listModifier = { Modifier.height(160f) }
                        ) {
                            //TODO 很神秘的bug 如果列表为空会导致整个screen都无法正常测量和布局
                            if (mapValue.isEmpty()) TextLabel(IGLang.hasNothing)
                            mapValue.forEach { key, value ->
                                Column(
                                    horizontalArrangement = Arrangement.spacedBy(2f),
                                ) {
                                    TextLabel(
                                        key, modifier = Modifier.width(mapValue.keys.maxWidth(mc.textRenderer).coerceAtMost(119) + 1f)
                                    )
                                    FlatButton(
                                        hoveredColor = Colors.PALEGREEN.alpha(.5f),
                                        modifier = Modifier.hoverText(IGLang.edit)
                                    ) {
                                        Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
                                        click {
                                            var newKey = key
                                            ConfirmDialog(
                                                stateOf(IGLang.edit.appendLiteral(" => $key")),
                                                onConfirm = {
                                                    if (newKey == key) {
                                                        mc.currentScreen?.close()
                                                        return@ConfirmDialog
                                                    }
                                                    if (mapValue.containsKey(newKey)) {
                                                        Toast.showToast(text = IGLang.keyExists(newKey).withColor(Colors.RED))
                                                        return@ConfirmDialog
                                                    }
                                                    mapValue.renameKey(key, newKey)
                                                    mc.currentScreen?.close()
                                                    this@RowListWrapped.execute {
                                                        this@RowListWrapped.recompose()
                                                    }
                                                }
                                            ) {
                                                TextEditor(modifier = Modifier.width(240f)) {
                                                    text = key
                                                    textConsumer { newKey = it }
                                                }
                                            }.open()
                                        }
                                    }
                                    TextEditor(modifier = Modifier.width(240f)) {
                                        text = value
                                        textConsumer {
                                            mapValue[key] = it
                                            mapValue.onChange(mapValue)
                                        }
                                    }

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
                                                    mapValue[key] = newValue
                                                    mc.currentScreen?.close()
                                                    this@RowListWrapped.execute {
                                                        this@RowListWrapped.recompose()
                                                    }
                                                }
                                            ) {
                                                TextEditor(modifier = Modifier.width(240f)) {
                                                    text = value
                                                    textConsumer { newValue = it }
                                                }
                                            }.open()
                                        }
                                    }

                                    FlatButton(
                                        hoveredColor = Colors.LIGHT_RED,
                                        modifier = Modifier.margin(right = 2f).hoverText(IGLang.remove)
                                    ) {
                                        Icon(IconTextures.DELETE, Colors.RED, Modifier.size(10f, 10f))
                                        click {
                                            mapValue.remove(key)
                                            execute {
                                                this@RowListWrapped.recompose()
                                            }
                                        }
                                    }
                                }
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
                            mapValue["key ${(mapValue.size)}"] = ""
                            this@Dialog.recompose()
                        }
                    }
                }.open()
            }
        }
        ConfigResetButton(config) {
            mapValue.clear()
            mapValue.putAll(config.defaultValue)
        }
    }
}


fun WidgetContainerScope.StringPairListConfigWrapper(
    config: ConfigPairList<String, String>,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {

    val listValue = notifiableList(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            Modifier.width(80f)
                .hoverText(mutableStateBy {
                    val sb = StringBuilder()
                    listValue.forEachWithLimit(10) { (k, v) ->
                        sb.appendLine("$k => $v")
                    }
                    if (listValue.size > 10) sb.append("...")
                    if (listValue.isEmpty()) sb.append(IGLang.hasNothing.plainText)
                    if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
                    Literal(sb.toString())
                })
        ) {
            TextLabel(mutableStateBy { mapConfigWrapperText(listValue.size) })
            click {
                Dialog {
                    TextLabel(stateOf(config.translateText))
                    DialogContent(
                        Modifier.padding(5f, 3f, 5f, 5f)
                    ) {
                        RowListWrapped(
                            modifier = Modifier.disableRenderBackground().padding(0).minWidth(240f),
                            listModifier = { Modifier.height(160f) }
                        ) {
                            //TODO 很神秘的bug 如果列表为空会导致整个screen都无法正常测量和布局
                            if (listValue.isEmpty()) TextLabel(IGLang.hasNothing)
                            listValue.forEachIndexed { index, (key, value) ->
                                Column(
                                    horizontalArrangement = Arrangement.spacedBy(2f),
                                    modifier = Modifier.width(360f)
                                ) {
                                    MoveButton(this@RowListWrapped, listValue, index)

                                    TextEditor(modifier = Modifier.width(240f).weight(3)) {
                                        text = key
                                        textConsumer {
                                            listValue[index] = it to value
                                            listValue.onChange(listValue)
                                        }
                                    }

                                    TextEditor(modifier = Modifier.width(240f).weight(5)) {
                                        text = value
                                        textConsumer {
                                            listValue[index] = key to it
                                            listValue.onChange(listValue)
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
                                                    listValue[index] = newKey to newValue
                                                    mc.currentScreen?.close()
                                                    this@RowListWrapped.execute {
                                                        this@RowListWrapped.recompose()
                                                    }
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

                                    FlatButton(
                                        hoveredColor = Colors.LIGHT_RED,
                                        modifier = Modifier.margin(right = 2f).hoverText(IGLang.remove)
                                    ) {
                                        Icon(IconTextures.DELETE, Colors.RED, Modifier.size(10f, 10f))
                                        click {
                                            listValue.removeAt(index)
                                            execute {
                                                this@RowListWrapped.recompose()
                                            }
                                        }
                                    }
                                }
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
                            listValue.add("" to "")
                            this@Dialog.recompose()
                        }
                    }
                }.open()
            }
        }
        ConfigResetButton(config) {
            listValue.clear()
            listValue.addAll(config.defaultValue)
        }
    }
}
