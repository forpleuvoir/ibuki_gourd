package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreenScope
import moe.forpleuvoir.ibukigourd.gui.widget.Proxy
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextField
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.ibukigourd.util.toggle
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
            register(Keyboard.KP_9) {
                openScreen(ColumnScreen {
                    val proxy: State<ColumnScreenScope.() -> IGWidget> = stateOf {
                        Button {
                            Icon(IconTextures.CLOSE)
                            TextField("关闭")
                        }
                    }
                    Proxy(proxy)
                    val state = stateOf(false)
                    state.subscribe { s ->
                        proxy.setValue {
                            if (s) {
                                Button {
                                    Icon(IconTextures.LOCK)
                                    TextField("锁定")
                                }
                            } else {
                                Button {
                                    Icon(IconTextures.CLOSE)
                                    TextField("关闭")
                                }
                            }
                        }
                    }
                    Button {
                        TextField("切换")
                        press {
                            state.toggle()
                        }
                    }

                })
            }
        }

    }
}