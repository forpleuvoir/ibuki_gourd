package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.execute
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.recompose
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.ConfirmDialog
import moe.forpleuvoir.ibukigourd.gui.widget.Dialog
import moe.forpleuvoir.ibukigourd.gui.widget.DialogContent
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.changeKeyPreservingOrder
import moe.forpleuvoir.ibukigourd.util.forEachWithLimit
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.common.util.collection.notifiableMap
import moe.forpleuvoir.nebula.common.util.collection.notification
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
        ) {
            //TODO i18n
            HoverTip(
                optionalDirection = Direction.clockwiseFromLeft.notification()
            ) {
                TextLabel(mutableStateBy {
                    val sb = StringBuilder()
                    listValue.forEachWithLimit(10) { t ->
                        sb.appendLine(t)
                    }
                    if (listValue.size > 10) sb.append("...")
                    if (listValue.isEmpty()) sb.append("什么都没有")
                    if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
                    Literal(sb.toString())
                }).apply { listValue.subscribe { this.onChanged() } }
            }
            TextLabel(mutableStateBy { Literal(listValue.size.toString() + "个元素") }).apply { listValue.subscribe { this.onChanged() } }
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
                            //TODO i18n
                            //TODO 很神秘的bug 如果列表为空会导致整个screen都无法正常测量和布局
                            if (listValue.isEmpty()) TextLabel("啥也没有")
                            listValue.forEachIndexed { index, item ->
                                Column(
                                    horizontalArrangement = Arrangement.spacedBy(2f),
                                ) {
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
                                        modifier = Modifier.margin(right = 2f)
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
        ) {
            //TODO i18n
            HoverTip(
                optionalDirection = Direction.clockwiseFromLeft.notification()
            ) {
                TextLabel(mutableStateBy {
                    val sb = StringBuilder()
                    mapValue.forEachWithLimit(10) { k, v ->
                        sb.appendLine("$k => $v")
                    }
                    if (mapValue.size > 10) sb.append("...")
                    if (mapValue.isEmpty()) sb.append("什么都没有")
                    if (sb.endsWith("\n")) sb.deleteAt(sb.length - 1)
                    Literal(sb.toString())
                }).apply { mapValue.subscribe { this.onChanged() } }
            }
            TextLabel(mutableStateBy { Literal(mapValue.size.toString() + "个元素") }).apply { mapValue.subscribe { this.onChanged() } }
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
                            //TODO i18n
                            //TODO 很神秘的bug 如果列表为空会导致整个screen都无法正常测量和布局
                            if (mapValue.isEmpty()) TextLabel("啥也没有")
                            mapValue.forEach { key, value ->
                                Column(
                                    horizontalArrangement = Arrangement.spacedBy(2f),
                                ) {
                                    TextLabel(
                                        key, modifier = Modifier.width(mapValue.keys.maxWidth(mc.textRenderer).coerceAtMost(119) + 1f)
                                    )
                                    FlatButton(
                                        hoveredColor = Colors.PALEGREEN.alpha(.5f)
                                    ) {
                                        Icon(IconTextures.EDIT, modifier = Modifier.size(10f, 10f))
                                        HoverTip {
                                            TextLabel("编辑Key")
                                        }
                                        click {
                                            var newKey = mutableStateOf(key)
                                            ConfirmDialog(
                                                stateOf(Literal("编辑 => $key")),
                                                onConfirm = {
                                                    mapValue.changeKeyPreservingOrder(key, newKey.getValue())
                                                    mc.currentScreen?.close()
                                                    this@RowListWrapped.execute {
                                                        this@RowListWrapped.recompose()
                                                    }
                                                }
                                            ) {
                                                TextEditor(modifier = Modifier.width(240f)) {
                                                    text = key
                                                    textConsumer { newKey.setValue(it) }
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
                                        hoveredColor = Colors.LIGHT_RED,
                                        modifier = Modifier.margin(right = 2f)
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
