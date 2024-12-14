package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber

@EventSubscriber
object TestInitialization {

    @Subscriber
    fun init(event: ModInitializerEvent) {
        if (event.meta != IbukiGourd.metadata) return
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
                openScreen(testScreen4())
            }
            register(Keyboard.KP_5) {
                openScreen(testScreen5())
            }
            register(Keyboard.KP_6) {
                openScreen(testScreen6())
            }
            register(Keyboard.KP_7) {
                testScreen7().open()
            }
            register(Keyboard.KP_9) {
                openScreen(testScreen9())
            }
        }

    }
}