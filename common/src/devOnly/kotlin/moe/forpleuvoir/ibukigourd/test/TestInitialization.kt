package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import com.mojang.serialization.JavaOps
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.lang.TranslationRecorder
import moe.forpleuvoir.ibukigourd.mod.IGConfig
import moe.forpleuvoir.ibukigourd.platform.services.ModInitialization
import moe.forpleuvoir.ibukigourd.ui.ComposeSceneWarmup
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigUiWrapper
import moe.forpleuvoir.ibukigourd.ui.openComposeScreen
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IbukiGourdTheme
import moe.forpleuvoir.ibukigourd.ui.preset.ColorPicker
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.debug
import moe.forpleuvoir.ibukigourd.util.NebulaOps
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull
import kotlin.time.measureTime

class TestInitialization : ModInitialization {
    private val logger = logger(IbukiGourd.MOD_NAME)

    val registryAccess get() = mc.player?.level()?.registryAccess()!!

    override fun init() {
        TestCommand.init()
        logger.info("测试环境")
        ClientLifecycleEvent.Starting.register {
            println(measureTime {
                ComposeSceneWarmup.warmUp { ConfigTest() }
            })
            ComposeSceneWarmup.warmUp { ConfigTest() }
            ComposeSceneWarmup.warmUp { ConfigTest() }
            ComposeSceneWarmup.warmUp { ConfigTest() }
            ComposeSceneWarmup.warmUp { ConfigTest() }
            ComposeSceneWarmup.warmUp()
        }

        TranslationRecorder.addFilter {
            it.startsWith("ibukigourd.") && !it.startsWith("ibukigourd.config.test")
        }
        TranslationRecorder.addFilter {
            it.startsWith("enum.")
        }
        TranslationRecorder.categorizer = {
            when {
                it.startsWith("enum.")                      -> "enum"
                it.startsWith("ibukigourd.misc.")           -> "misc"
                it.startsWith("ibukigourd.input.")          -> "input"
                it.startsWith("ibukigourd.config_wrapper.") -> "config_wrapper"
                it.startsWith("ibukigourd.color.")          -> "color"
                else                                        -> "default"
            }
        }

        ClientModConfigHandler.register(TestConfig)

        InputHandler.apply {
            register(Keyboard.P) {
                mc.player?.mainHandItem?.let { item ->
                    ItemStack.CODEC.encodeStart(registryAccess.createSerializationContext(NebulaOps), item).resultOrPartial {
                        logger.info(it)
                    }.getOrNull()?.let {
                        logger.info(JsonDialect.encode(it))
                    }
                }
            }
            register(Keyboard.O) {
                mc.player?.mainHandItem?.let { item ->
                    ItemStack.CODEC.encodeStart(registryAccess.createSerializationContext(JavaOps.INSTANCE), item).resultOrPartial {
                        logger.info(it)
                    }.getOrNull()?.let {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        logger.info(gson.toJson(it))
                    }
                }
            }

            register(Keyboard.KP_1) {
                openComposeScreen {
                    TestScreen1()
                }
            }
            register(Keyboard.KP_2) {
                openComposeScreen {
                    TestScreen2()
                }
            }
            register(Keyboard.KP_3) {
                openComposeScreen {
                    CenteredBox {
                        Column {
                            var color by remember { mutableStateOf(Color.fromARGB(0xFFFFFF00)) }
                            Text(color.hexStr)
                            Card {
                                ColorPicker(color, { color = it })
                            }
                        }
                    }
                }
            }


            register(Keyboard.KP_7) {
                openComposeScreen {
                    IbukiGourdTheme {
                        Surface(Modifier.fillMaxSize().debug()) {
                            Column {
                                Row(modifier = Modifier.padding(8.dp)) {
                                    Text("亮色模式")
                                    ConfigUiWrapper(IGConfig.Gui.children.find { it.name == "light_mode" }!!)
                                }
                                ConfigTest()
                            }
                        }
                    }
                }

            }

        }
    }
}