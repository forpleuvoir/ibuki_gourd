package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.style.SuggestCommand
import moe.forpleuvoir.ibukigourd.util.chatMessage
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber

@EventSubscriber
object TestInitialization {

    @Subscriber
    fun init(event: ModInitializerEvent) {
        log.info("MOD测试")
        InputHandler.apply {
            register(Keyboard.KP_1) {
                openScreen(testScreen())
            }
            register(Keyboard.KP_2) {
                openScreen(TestScreen2())
            }
            register(Keyboard.KP_3) {
                openScreen(testScreen3())
            }
            register(Keyboard.KP_9) {
                kotlin.runCatching {
                    mc.chatMessage(
                        Text {
                            literal("aaaa")
                            literal("bbbb") {
                                style {
                                    click<SuggestCommand>("execute in minecraft:overworld run tp 36 36 36")
                                }
                            }
                        }
                    )
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }

    }
}