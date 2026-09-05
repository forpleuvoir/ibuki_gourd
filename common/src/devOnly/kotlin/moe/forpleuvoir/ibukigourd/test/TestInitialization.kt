package moe.forpleuvoir.ibukigourd.test

import com.google.gson.GsonBuilder
import com.mojang.serialization.JavaOps
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.platform.services.ModInitialization
import moe.forpleuvoir.ibukigourd.util.NebulaOps
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

class TestInitialization : ModInitialization {
    private val logger = logger(IbukiGourd.MOD_NAME)

    val registryAccess get() = mc.player?.level()?.registryAccess()!!

    override fun init() {
        TestCommand.init()
        SokitsuAtlasValidation.run()
        logger.info("测试环境")

        ClientModConfigHandler.register(TestConfig)

        InputHandler.apply {
            register(Keyboard.KP_1) {
                SokitsuTestScreen()
            }

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
        }
    }
}