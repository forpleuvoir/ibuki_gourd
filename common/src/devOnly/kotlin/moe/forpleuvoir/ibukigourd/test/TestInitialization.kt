package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import com.mojang.serialization.JavaOps
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyEnvironment
import moe.forpleuvoir.ibukigourd.input.KeybindSetting
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.mod.waht.SnakeGame
import moe.forpleuvoir.ibukigourd.platform.services.ModInitialization
import moe.forpleuvoir.ibukigourd.ui.ComposeSceneWarmup
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigUiWrapper
import moe.forpleuvoir.ibukigourd.ui.openComposePopupScreen
import moe.forpleuvoir.ibukigourd.ui.openComposeScreen
import moe.forpleuvoir.ibukigourd.ui.overlay.OverlayService
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IbukiGourdTheme
import moe.forpleuvoir.ibukigourd.ui.preset.BlitTexture
import moe.forpleuvoir.ibukigourd.util.NebulaOps
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class TestInitialization : ModInitialization {
    private val logger = logger(IbukiGourd.MOD_NAME)

    val registryAccess get() = mc.player?.level()?.registryAccess()!!

    override fun init() {
        TestCommand.init()
        logger.info("测试环境")
        ClientLifecycleEvent.Starting.register {
            ComposeSceneWarmup.warmUp()
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
                            BlitTexture(identifier(IbukiGourd.MOD_ID, "icon.png"), modifier = Modifier.size(64.dp))
                            BlitTexture(identifier(IbukiGourd.MOD_ID, "sdada.png"), modifier = Modifier.size(64.dp))
                        }
                    }
                }
            }
            OverlayService.register {
                var current by remember { mutableStateOf(InputHandler.currentPressKeyCode.toSet()) }
                LaunchedEffect(Unit) {
                    while (isActive) {
                        current = InputHandler.currentPressKeyCode.toSet()
                        delay(16.milliseconds)
                    }
                }
                Text("当前按键${current.joinToString(", ", transform = { it.keyName })}", modifier = Modifier.align(Alignment.TopCenter))
            }
            register(
                Keyboard.KP_4, defaultSetting = KeybindSetting(
                    env = KeyEnvironment.Any
                )
            ) {
                openComposeScreen {
                    TestScreen4()
                }
            }
            register(Keyboard.KP_6) {
                openComposeScreen {
                    SnakeGame(modifier = Modifier)
                }
            }

            register(Keyboard.KP_7) {
                openComposeScreen(shouldRenderLevel = { false }) {
                    IbukiGourdTheme {
                        Surface(
                            Modifier.fillMaxSize()
                        ) {
                            Column {
                                Row(modifier = Modifier.padding(8.dp)) {
                                    Button({
                                        openComposePopupScreen {
                                            Card(Modifier.padding(24.dp)) {
                                                Text("Dialog Test")
                                            }
                                        }
                                    }) {
                                        Text("Dialog Test")
                                    }
                                    Text("亮色模式")
                                    ConfigUiWrapper(IGConfig.Gui.Theme.children.find { it.name == "light_mode" }!!)
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