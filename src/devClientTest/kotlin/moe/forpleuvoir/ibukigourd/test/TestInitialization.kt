package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.keyPress
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.keyRelease
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.Proxy
import moe.forpleuvoir.ibukigourd.gui.widget.Spinner
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.ibukigourd.util.switch
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber

@EventSubscriber
object TestInitialization {

    @Subscriber
    fun init(event: ModInitializerEvent) {
        log.info("MOD测试")
        InputHandler.apply {
            register(Keyboard.KP_1) {
                openScreen(TestScreen())
            }
            register(Keyboard.KP_2) {
                openScreen(TestScreen2())
            }
            register(Keyboard.KP_3) {
                openScreen(testScreen3())
            }
            register(Keyboard.KP_4) {
                openScreen(ColumnScreen {
                    IntEditor(stateOf(5), modifier = Modifier.width(50f), editorModifier = { Modifier.weight(1) }, scope = {
                        HoverTip(
                            modifier = Modifier.margin(3f),
                            optionalDirection = notifiableList(Direction.Top)
                        ) {
                            Button {
                                TextLabel("悬浮测试")
                            }
                        }
                    })
                    val keepState = stateOf(false)
                    Button(Modifier
                        .keyPress {
                            if (it.keyCode == Keyboard.LEFT_CONTROL) {
                                keepState.setValue(true)
                            }
                        }.keyRelease {
                            if (it.keyCode == Keyboard.LEFT_CONTROL) {
                                keepState.setValue(false)
                            }
                        }
                    ) {
                        TextLabel("高度测试1")
                        HoverTip(
                            modifier = Modifier.margin(3f),
                            keepShow = keepState,
                            optionalDirection = notifiableList(Direction.Bottom)
                        ) {
                            Button {
                                TextLabel("悬浮测试")
                            }
                        }
                    }
                    SwitchButton(stateOf(true)) {
                        HoverTip(
                            modifier = Modifier.margin(3f),
                            optionalDirection = notifiableList(Direction.Left)
                        ) {
                            Button {
                                TextLabel("悬浮测试")
                            }
                        }
                    }
                    Spinner(listOf("下拉菜单", "选项1", "选项2", "选项3")) {
                        HoverTip(
                            modifier = Modifier.margin(3f),
                            optionalDirection = notifiableList(Direction.Right)
                        ) {
                            Button {
                                TextLabel("悬浮测试")
                            }
                        }
                    }
                })
            }
            register(Keyboard.KP_9) {
                openScreen(ColumnScreen {
                    val proxy: State<ColumnScope.() -> IGWidget> = stateOf {
                        Button(
                            horizontalArrangement = Arrangement.spacedBy(2f, Alignment.CenterHorizontally)
                        ) {
                            Icon(IconTextures.CLOSE)
                            TextLabel("关闭")
                        }
                    }
                    Proxy(proxy)
                    val state = stateOf(false)
                    state.subscribe { s ->
                        proxy.setValue {
                            if (s) {
                                Button(
                                    horizontalArrangement = Arrangement.spacedBy(2f, Alignment.CenterHorizontally)
                                ) {
                                    Icon(IconTextures.LOCK)
                                    TextLabel("锁定")
                                }
                            } else {
                                Button(
                                    horizontalArrangement = Arrangement.spacedBy(2f, Alignment.CenterHorizontally)
                                ) {
                                    Icon(IconTextures.CLOSE)
                                    TextLabel("关闭")
                                }
                            }
                        }
                    }
                    Button {
                        TextLabel("切换")
                        press {
                            state.switch()
                        }
                    }

                })
            }
        }

    }
}