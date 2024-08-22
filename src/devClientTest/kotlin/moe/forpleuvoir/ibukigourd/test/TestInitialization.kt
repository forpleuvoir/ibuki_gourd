package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.screen.BoxScreen
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.nebula.common.color.Colors
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
                openScreen(BoxScreen(
                    Modifier
                        .padding(5f)
                        .renderOverlay { ctx, _, _, _ ->
                            this as RowScreen
                            ctx.batchRenderBox {
                                pushBoxOutline(contentBox(true), Colors.AQUA)
                                pushBox(0f, transform.center.y(), transform.width, 1f, Colors.RED.opacity(.5f))
                                pushBox(transform.center.x(), 0f, 1f, transform.height, Colors.BLUE.opacity(.5f))
                            }
                        },
                ) {
                    Button(Modifier.renderHoveredOutlineBox(Colors.RICE).align(Alignment.TopLeft)) {
                        Icon(IconTextures.REFRESH, modifier = Modifier.renderHoveredOutlineBox(Colors.RICE))
                        Text("布局测试", modifier = Modifier.renderHoveredOutlineBox(Colors.RICE))
                    }
                })
            }
        }

    }
}