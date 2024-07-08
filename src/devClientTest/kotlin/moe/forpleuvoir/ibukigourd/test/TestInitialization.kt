package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget

@EventSubscriber
object TestInitialization {
    @Subscriber
    fun init(event: ModInitializerEvent) {
        InputHandler.apply {
            register(Keyboard.KP_1) {
                openScreen(object : Screen(Literal("test")) {

                    override fun shouldPause(): Boolean {
                        return false
                    }

                    override fun init() {
                        ButtonWidget.builder(Literal("测试按钮")) {
                            println("测试按钮")
                        }.dimensions(40, 40, 120, 20).build().let {
                            addDrawableChild(it)
                        }

                        ButtonWidget.builder(Literal("测试按钮2")) {
                            println("测试按钮2")
                        }.dimensions(180, 40, 120, 20).build().let {
                            addDrawableChild(it)
                        }

                    }
                })
            }
        }
    }
}