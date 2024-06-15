package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Keyboard.KP_1
import moe.forpleuvoir.ibukigourd.input.Keyboard.KP_2
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber

@EventSubscriber
object TestInitialization {
    @Subscriber
    fun init(event: ModInitializerEvent) {
        InputHandler.apply {
            register(KP_1) {
                testScreen(1)
            }
            register(KP_2) {
                testScreen(2)
            }
            register(Keyboard.KP_3) {
                testScreen(3)
            }
        }
    }
}