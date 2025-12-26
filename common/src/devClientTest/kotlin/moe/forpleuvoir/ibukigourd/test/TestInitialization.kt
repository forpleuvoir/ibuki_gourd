package moe.forpleuvoir.ibukigourd.test

import com.mojang.serialization.JavaOps
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.event.IbukiGourdEventManager
import moe.forpleuvoir.ibukigourd.event.events.IbukigourdInitializerEvent
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.gui.screen.BoxScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.LockButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.NebulaOps
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.nebula.common.api.ExperimentalApi
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import moe.forpleuvoir.nebula.serialization.extensions.toSerializeElement
import moe.forpleuvoir.nebula.serialization.json.JsonSerializer.Companion.dumpAsJson
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

@EventSubscriber
object TestInitialization {
    private val logger = logger(IbukiGourd.MOD_NAME)

    val registryAccess get() = mc.player?.level()?.registryAccess()!!

    @OptIn(ExperimentalApi::class)
    @Subscriber
    fun init(event: IbukigourdInitializerEvent) {
        logger.info("测试环境")
        InputHandler.apply {
            register(Keyboard.P) {
                mc.player?.mainHandItem?.let { item ->
                    ItemStack.CODEC.encodeStart(registryAccess.createSerializationContext(NebulaOps), item).resultOrPartial {
                        logger.info(it)
                    }.getOrNull()?.let {
                        logger.info(it.dumpAsJson(true))
                    }
                }
            }
            register(Keyboard.O) {
                mc.player?.mainHandItem?.let { item ->
                    ItemStack.CODEC.encodeStart(registryAccess.createSerializationContext(JavaOps.INSTANCE), item).resultOrPartial {
                        logger.info(it)
                    }.getOrNull()?.let {
                        logger.info(it.toSerializeElement().dumpAsJson(true))
                    }
                }
            }
            register(Keyboard.KP_0) {
                IbukiGourdEventManager.eventSet().forEach {
                    println(it.qualifiedName)
                }
            }
            register(Keyboard.KP_1) {
                openScreen(TestScreen())
            }
            register(Keyboard.KP_2) {
                TestScreen2()
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
            register(Keyboard.KP_8) {
                example()
            }
            register(Keyboard.KP_9) {
                openScreen(testScreen9())
            }
        }

    }
}


fun example() = BoxScreen {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button {
            click {
                Toast.showToast(text = "hello minecraft")
            }
            Text("hello minecraft")
            LockButton()
            Icon(IconTextures.LOCK)
        }

    }
}.open()//打开屏幕