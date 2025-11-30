package moe.forpleuvoir.ibukigourd.platform

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.platform.services.PlatformHelper
import moe.forpleuvoir.ibukigourd.util.logger
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.FMLPaths
import java.io.File

class NeoforgePlatformHelper : PlatformHelper {

    companion object {

        private val logger = logger(IbukiGourd.MOD_NAME)

        @Suppress("UNCHECKED_CAST")
        private val modPacks by lazy {
            ImmutableMap.builder<String, Set<String>>().also { packsMapping ->
                ModList.get().mods.forEach { modInfo ->
                    val packs = ImmutableSet.builder<String>()
                    (modInfo.modProperties["package"] as? Iterable<String>)?.forEach { value ->
                        packs.add(value)
                        logger.info("Mod: ${modInfo.modId} register Package: $value")
                    }
                    packsMapping.put(modInfo.modId, packs.build())
                }
            }.build()
        }
    }

    override fun getPlatformName(): String = "Neoforge"

    override fun isModLoaded(modId: String): Boolean = ModList.get().isLoaded(modId)

    override fun isDevEnvironment(): Boolean = !FMLLoader.getCurrent().isProduction

    override fun getIGModPackage(): Map<String, Set<String>> = modPacks

    override fun getConfigDir(): File = FMLPaths.CONFIGDIR.get().toFile()

}