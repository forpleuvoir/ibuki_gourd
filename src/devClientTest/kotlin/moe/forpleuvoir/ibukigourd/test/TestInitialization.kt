package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextInput
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget

@EventSubscriber
object TestInitialization {

    var text: String = ""

    @Subscriber
    fun init(event: ModInitializerEvent) {
        log.info("MOD测试")
        InputHandler.apply {
            register(Keyboard.KP_1) {
                openScreen(TestScreen())
            }
        }
    }
}