package moe.forpleuvoir.ibukigourd.platform

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.platform.services.PlatformHelper
import moe.forpleuvoir.ibukigourd.util.ModLogger
import net.fabricmc.loader.api.FabricLoader
import java.io.File

class FabricPlatformHelper : PlatformHelper {

    companion object {

        private val logger = ModLogger("FabricPlatformHelper", IbukiGourd.MOD_NAME)

        private val loader: FabricLoader by lazy { FabricLoader.getInstance() }

        private val modPacks by lazy {
            ImmutableMap.builder<String, Set<String>>().also { packsMapping ->
                loader.allMods.forEach { mod ->
                    val packs = ImmutableSet.builder<String>()
                    mod.metadata.customValues[IbukiGourd.MOD_ID]?.apply {
                        asObject.get("package")?.asArray?.onEach { value ->
                            packs.add(value.asString)
                            logger.info("Mod: ${mod.metadata.id} register Package: ${value.asString}")
                        }
                    }
                    packsMapping.put(mod.metadata.id, packs.build())
                }
            }.build()
        }
    }

    override fun getPlatformName(): String = "Fabric"

    override fun isModLoaded(modId: String): Boolean = loader.isModLoaded(modId)

    override fun isDevEnvironment(): Boolean = loader.isDevelopmentEnvironment

    override fun getIGModPackage(): Map<String, Set<String>> = modPacks

    override fun getConfigDir(): File = loader.configDir.toFile()

}